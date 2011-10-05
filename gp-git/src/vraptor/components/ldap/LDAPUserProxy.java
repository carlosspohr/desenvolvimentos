package vraptor.components.ldap;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.hidroinformatica.gp.beans.Usuario;

/**
 * Classe que encapsula a classe Usuário para adicionar os elementos de LDAP
 * para o carregamento dos dados remotos.
 * 
 * @author Carlos A. Junior (CIH - Centro Internacional de Hidroinformática)
 */
public class LDAPUserProxy extends Usuario
{
	private static final long serialVersionUID = -2652759101054493625L;

	private SearchResult result;

	/**
	 * Construtor padrão com a dependência direta de um SearchResult.
	 * 
	 * @param result
	 * 
	 * @throws NamingException
	 */
	public LDAPUserProxy(SearchResult result) throws NamingException
	{
		super();
		this.result = result;
		// inicia os dados diretamente.
		this.setUserDetails();
	}
	
	/**
	 * Carrega os dados do SearchResult para o próprio objeto da
	 * herança direta (Usuário).
	 * 
	 * @throws NamingException
	 */
	private void setUserDetails() throws NamingException
	{
		if(this.result != null)
		{
			this.setNome	((String) this.get("cn"));
			this.setLogin	((String) this.get("uid"));
			this.setEmail	((String) this.get("mail"));
		}
		else
		{
			throw new NamingException("Nenhum usuário no SearchResult atual.");
		}
	}
	
	/**
	 * Retorna a informação de um atributo do SearchResult.
	 * 
	 * @param attrId
	 * 
	 * @return
	 * 
	 * @throws NamingException
	 */
	private Object get (String attrId) throws NamingException
	{
		return this.getResult().getAttributes().get(attrId).get();
	}

	public SearchResult getResult() {
		return result;
	}

	public void setResult(SearchResult result) {
		this.result = result;
	}
}
