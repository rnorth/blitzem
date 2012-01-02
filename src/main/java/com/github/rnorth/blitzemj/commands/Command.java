package com.github.rnorth.blitzemj.commands;


/**
 * Base interface for Command object classes.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public interface Command {

	/**
	 * @return the noun this command should act on, if specified.
	 */
	String getNoun();
	
	/**
	 * @param noun the noun to set.
	 */
	void setNoun(String noun);
}
