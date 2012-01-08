package org.blitzem.console;


/**
 * Any kind of reflection exception that could not be handled.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class UnhandledReflectionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8925478305100428188L;

	public UnhandledReflectionException(Exception e) {
		super(e);
	}

}
