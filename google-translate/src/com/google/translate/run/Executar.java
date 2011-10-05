package com.google.translate.run;

import java.io.File;

import com.google.api.translate.Language;
import com.google.translate.TranslatePropertyResourceBundle;
import com.google.translate.exceptions.SameLanguageException;

public class Executar 
{
	public static void main(String[] args)
	{
		TranslatePropertyResourceBundle traducao 
			= new TranslatePropertyResourceBundle("http://teste.hidroinformatica.org/sigbiogas/");
		
		Language[] lang = new Language[]{Language.ENGLISH, Language.SPANISH};
		
		try
		{
			File origem = new File("./dist/messages_pt_BR.properties");
			
			traducao.traduzArquivoi18n(origem, Language.PORTUGUESE, lang);
		} catch (SameLanguageException e) {
			e.printStackTrace();
		}
	}
}
