/**
 * 
 */
package com.github.rnorth.blitzemj.commands;


/**
 * @author richardnorth
 *
 */
public class CommandException extends Exception {

	public CommandException(String string, Exception e) {
		super(string, e);
	}

}
