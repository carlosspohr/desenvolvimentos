package com.google.translate.exceptions;

/**
 * Exception padrão para idiomas iguais.
 * 
 * @author Carlos A. Junior (CIH - Centro Internacional de Hidroinformática)
 */
public class SameLanguageException extends Exception
{
	private static final long serialVersionUID = -5576380650482356101L;

	public SameLanguageException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SameLanguageException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public SameLanguageException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public SameLanguageException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
