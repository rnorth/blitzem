package com.github.rnorth.blitzemj.commands;


/**
 * An exception raised during execution of a {@link Command}.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class CommandException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3293956689241887134L;

	public CommandException(String string, Exception e) {
		super(string, e);
	}

}
