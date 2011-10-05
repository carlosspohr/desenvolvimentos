package vraptor.components.ldap;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hidroinformatica.gp.beans.Usuario;

import vraptor.components.message.MessageComponent;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.ioc.Component;

/**
 * Classe de gerenciamento das requisições e obtenção e autenticação de
 * usuários LDAP.
 * 
 * 
 * 
 * @author Carlos A. Junior (CIH - Centro Internacional de Hidroinformática)
 */
@Component
public class LDAPComponent implements Serializable
{
	private static final long serialVersionUID = -2115416453624593622L;
	
	private static final Logger logger = Logger.getLogger(LDAPComponent.class);
	
	private Result 				result;
	
	private MessageComponent 	message;
	
	private ServletContext 		context;
	
	/**
	 * Construtor padrão do componente com as devidas dependências.
	 * 
	 * @param result
	 * 
	 * @param message
	 * 
	 * @param context
	 * 
	 * @throws NamingException
	 */
	public LDAPComponent(Result result, MessageComponent message, ServletContext context) throws NamingException
	{
		super();
		this.result 	= result;
		this.message 	= message;
		this.context	= context;
		
		logger.info("Configurando a instância do LDAPAuthComponent");
	}
	

	/**
	 * Retorna a configuração básica do acesso ao LDAP.
	 * 
	 * @param context
	 * 
	 * @return
	 */
	public Hashtable<String, String> getBasicAccessLdapConfiguration (ServletContext context)
	{
		Hashtable<String, String> propriedades = new Hashtable<String, String>(3);

		propriedades.put(Context.INITIAL_CONTEXT_FACTORY, 	context.getInitParameter("ldap.factory"));
		propriedades.put(Context.PROVIDER_URL, 				context.getInitParameter("ldap.address"));
		propriedades.put(Context.SECURITY_AUTHENTICATION, 	"none");
		
		return propriedades;
	}
	
	private String getLDAPDomainName ()
	{
		return this.context.getInitParameter("ldap.dn");
	}
	
	/**
	 * Método que realiza a consulta de um usuário LDAP e retorna os seus 
	 * detalhes remotos.
	 * 
	 * @param usuario - objeto usuário com o atributo login preenchido.
	 * 
	 * @return - um objeto Usuario populado caso o mesmo exista.
	 * 
	 * @throws NamingException 
	 */
	public LDAPUserProxy getUserDetails (Usuario usuario) throws NamingException
	{
		if(usuario != null && usuario.getLogin() != "")
		{
			LDAPUserProxy proxy = null;
			try
			{
				Hashtable<String, String> propriedades = this.getBasicAccessLdapConfiguration(context);
				
				DirContext dir = new InitialDirContext(propriedades);
				
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				NamingEnumeration<SearchResult> result = dir.search(this.getLDAPDomainName(), "uid=" + usuario.getLogin(), controls);
				
				while (result.hasMoreElements())
				{
					SearchResult sr = (SearchResult) result.nextElement();
					proxy = new LDAPUserProxy(sr);
					
					// Somente um.
					break;
				}
				
				dir.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
			return proxy;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Retorna o DN do usuário na árvore do LDAP.
	 * 
	 * @param usuario
	 * 
	 * @return
	 */
	public String getLDAPUserDomainName (Usuario usuario)
	{
		if(usuario == null)
		{
			return null;
		}
		else
		{
			logger.info("Obtendo o User Domain User.");
			
			String userDomainName = null;
			
			try
			{
				Hashtable<String, String> propriedades = this.getBasicAccessLdapConfiguration(context);
				
				DirContext dir = new InitialDirContext(propriedades);
				
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				NamingEnumeration<SearchResult> result = dir.search(this.getLDAPDomainName(), "uid=" + usuario.getLogin(), controls);
				
				while (result.hasMoreElements())
				{
					SearchResult sr = (SearchResult) result.nextElement();
					userDomainName = sr.getNameInNamespace();
					
					// Somente um.
					break;
				}
				
				dir.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
			
			return userDomainName;
		}
	}
	
	/**
	 * Método que realiza a autenticação de um usuário através do LDAP.
	 * 
	 * @param usuario - objeto usuário {login, senha} informado no login.
	 * 
	 * @return - um objeto Usuario populado caso o mesmo exista.
	 * 
	 * @throws NamingException 
	 */
	public LDAPUserProxy authenticate (Usuario usuario)
	{
		if(usuario == null || usuario.getLogin() == null || usuario.getSenha() == null)
		{
			logger.info("O login ou senha do usuário podem estar nulos, retornando NULL.");
			return null;
		}
		else
		{
			logger.info("Iniciando a autenticação do usuário LDAP: " + usuario.getLogin());
			
			String userDomainName = this.getLDAPUserDomainName(usuario);
			
			if(userDomainName == null)
			{
				logger.info("Nenhum DN encontrado para o login [" + usuario.getLogin() + "]");
				return null;
			}
			else
			{
				logger.info("DN encontrado [" + userDomainName + "] iniciando a validação da senha no LDAP.");
				
				// Configuro a busca autenticada ao banco do LDAP.
				LDAPUserProxy proxy = null;
				try
				{
					logger.info("Autenticando o usuário no DN: " + userDomainName);
					
					Hashtable<String, String> propriedades = this.getBasicAccessLdapConfiguration(context);
					
					// Adiciona a autenticação do usuário no LDAP. Caso a senha não seja
					// a correta, uma exception será disparada.
					propriedades.put(Context.SECURITY_AUTHENTICATION, 	"simple");
					propriedades.put(Context.SECURITY_PRINCIPAL, 		userDomainName);
					propriedades.put(Context.SECURITY_CREDENTIALS, 		usuario.getSenha());
					
					DirContext dir = new InitialDirContext(propriedades);
					
					SearchControls controls = new SearchControls();
					controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
					NamingEnumeration<SearchResult> result = dir.search(userDomainName, "uid=" + usuario.getLogin(), controls);
					
					while (result.hasMoreElements())
					{
						SearchResult sr = (SearchResult) result.nextElement();
						logger.info("Carregando o usuário LDAP: " + sr.getAttributes());
						proxy = new LDAPUserProxy(sr);
					}
					logger.info("Usuário Retorno: " + proxy.getNome());
					// dir.close();
					
				} catch (Exception e) {
					logger.info("Exception na autenticação: " + e.getLocalizedMessage());
					e.printStackTrace();
				}
				
				return proxy;
			}
		}
	}

	public Result getResult() {
		return result;
	}

	public MessageComponent getMessage() {
		return message;
	}

	public ServletContext getContext() {
		return context;
	}
}
