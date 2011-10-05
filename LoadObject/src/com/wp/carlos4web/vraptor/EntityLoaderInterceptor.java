package com.wp.carlos4web.vraptor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Arrays.asList;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import net.vidageek.mirror.dsl.Mirror;

import org.apache.log4j.Logger;

import br.com.caelum.vraptor.Converter;
import br.com.caelum.vraptor.InterceptionException;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Lazy;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.core.Converters;
import br.com.caelum.vraptor.core.InterceptorStack;
import br.com.caelum.vraptor.core.Localization;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.interceptor.Interceptor;
import br.com.caelum.vraptor.interceptor.ParametersInstantiatorInterceptor;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.view.FlashScope;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 * @author carlos
 *
 */
@Intercepts(before=ParametersInstantiatorInterceptor.class)
@Lazy
public class EntityLoaderInterceptor implements Interceptor
{
	private static final Logger logger = Logger.getLogger(EntityLoaderInterceptor.class);

	private final EntityManager em;
	private final HttpServletRequest request;
	private final ParameterNameProvider provider;
	private final Result result;
	private final Converters converters;
	private final Localization localization;
	private final FlashScope flash;

	public EntityLoaderInterceptor(EntityManager em,
			HttpServletRequest request, ParameterNameProvider provider,
			Result result, Converters converters, Localization localization,
			FlashScope flash) {
		this.em = em;
		this.request = request;
		this.provider = provider;
		this.result = result;
		this.converters = converters;
		this.localization = localization;
		this.flash = flash;
	}

	public boolean accepts(ResourceMethod method) {
		return any(asList(method.getMethod().getParameterAnnotations()),
				hasLoadAnnotation());
	}

	public void intercept(InterceptorStack stack, ResourceMethod method, Object resourceInstance) throws InterceptionException
	{
		Annotation[][] annotations = method.getMethod().getParameterAnnotations();

		String[] names = provider.parameterNamesFor(method.getMethod());

		Class<?>[] types = method.getMethod().getParameterTypes();

		Object[] args = flash.consumeParameters(method);

		for (int i = 0; i < names.length; i++)
		{
			Iterable<LoadObject> loads = Iterables.filter(asList(annotations[i]), LoadObject.class);
			
			if (!isEmpty(loads))
			{
				Object loaded = this.load(names[i], types[i]);

				if (loaded == null)
				{
					LoadObject next = loads.iterator().next();
					
					if(next != null && next.required())
					{
						if(next.redirectToWhenObjectNotFound() != null && !next.redirectToWhenObjectNotFound().isEmpty())
						{
							logger.info("Entity not found, the page will be redirected to " + next.redirectToWhenObjectNotFound());
							result.redirectTo(next.redirectToWhenObjectNotFound());
						}
						else
						{
							logger.info("Entity not found, the redirect URL was not specified, then i will be return nothing.");
							result.nothing();
							return;
						}
					}
				}

				if (args != null)
					args[i] = loaded;
				else
					request.setAttribute(names[i], loaded);
			}
		}
		flash.includeParameters(method, args);

		stack.next(method, resourceInstance);
	}

	private Object load(String name, Class type)
	{
		String parameter = request.getParameter(name + ".id");
		if (parameter == null) {
			return null;
		}
		Field field = new Mirror().on(type).reflect().field("id");
		checkArgument(field != null, "Entity " + type.getSimpleName() + " must have an id property for @LoadObject.");

		Class<?> idType = field.getType();
		Converter<?> converter = converters.to(idType);
		checkArgument(converter != null, "Entity " + type.getSimpleName()
				+ " id type " + idType + " must have a converter");

		Serializable id = (Serializable) converter.convert(parameter, type,
				localization.getBundle());
		return em.find(type, id);
	}

	private Predicate<Annotation[]> hasLoadAnnotation() {
		return new Predicate<Annotation[]>() {
			public boolean apply(Annotation[] param) {
				return any(asList(param), instanceOf(LoadObject.class));
			}
		};
	}
}
