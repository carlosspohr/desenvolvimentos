package com.google.translate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.google.translate.exceptions.SameLanguageException;

/**
 * Classe que contém a implementação da tradução automatizada
 * através do uso da API do Google Translate.
 * 
 * @author Carlos A. Junior (CIH - Centro Internacional de Hidroinformática)
 */
public class TranslatePropertyResourceBundle
{
	/**
	 * URL de referência para o site que está chamando as traduções.
	 */
	private String httpReferer;
	
	/**
	 * Construtor padrão com a dependência direta para a URL do site que
	 * está requisitando as traduções.
	 * 
	 * @param httpReferer - URL do site que está requisitando a tradução.
	 */
	public TranslatePropertyResourceBundle(String httpReferer)
	{
		super();
		this.httpReferer = httpReferer;
	}
	
	/**
	 * Traduz um arquivo de mensagens para um array de outros idiomas.
	 * 
	 * @param from - Idioma do arquivo atual.
	 * 
	 * @param to - array de idiomas de destino.
	 * @throws SameLanguageException 
	 */
	public void traduzArquivoi18n(File origem, Language from, Language to[]) throws SameLanguageException
	{
		for (Language language : to)
		{
			this.traduzArquivoi18n(origem, from, language);
		}
	}
	
	/**
	 * Carrega e realiza a leitura do arquivo para um arquivo do tipo PropertyResourceBundle.
	 * 
	 * @param origem - arquivo de origem, exemplo: messages_pt_BR.properties.
	 * 
	 * @return
	 * 
	 * @throws IOException
	 */
	private PropertyResourceBundle carregaPropertyResourceBundle (File origem) throws IOException
	{
		InputStream stream 				= new FileInputStream(origem);
		
		return new PropertyResourceBundle(stream);
	}
	/**
	 * Traduz um arquivo de mensagens para um idioma.
	 * 
	 * @param from - Idioma do arquivo atual.
	 * 
	 * @param to - Idioma de destino.
	 * 
	 * @throws SameLanguageException 
	 */
	public void traduzArquivoi18n(File origem, Language from, Language to) throws SameLanguageException
	{
		if(from.equals(to))
		{
			throw new SameLanguageException("Você está tentando traduzir o arquivo para o mesmo idioma dele.");
		}
		
		Translate.setHttpReferrer(this.httpReferer);

		try
		{
			System.out.println("Traduzindo o arquivo " + origem.getName() + " do idioma " + from + " para " + to);
			
			PropertyResourceBundle bundle = this.carregaPropertyResourceBundle(origem);
			
			Enumeration<String> keys = bundle.getKeys();
			
			String conteudo = null;
			
			while(keys.hasMoreElements())
			{
				String key = keys.nextElement();
				String traduzido = Translate.execute(bundle.getString(key), from, to);
				
				conteudo  += key + "=" + traduzido + "\n";
				System.out.println("Traduzindo a chave: " + key + " - > " + traduzido);
			}
			File saida = new File("./dist/out/messages_" + to + ".properties");
			
			if(!saida.exists())
			{
				saida.createNewFile();
			}
			
			FileWriter writer = new FileWriter(saida);
			
			BufferedWriter buffer = new BufferedWriter(writer);
			
			buffer.write(conteudo);
			buffer.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Tradução feita com sucesso, verifique os arquivos gerados no diretório ./dist/out/");
	}
}
