package org.blitzem.model;

import static org.jclouds.compute.options.TemplateOptions.Builder.runScript;

import java.io.File;
import java.io.IOException;

import org.blitzem.console.BlitzemConsole;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.scriptbuilder.domain.CreateFile;
import org.jclouds.scriptbuilder.domain.Statement;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * A provisioning step to copy a file from the local filesystem onto the newly
 * created server node.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class FileCopy extends Provisioning {

	private String from;
	private String to;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Statement asStatement() {
		File localFile = new File(new File(BlitzemConsole.command.getSource()).getParentFile(), from);
		Iterable<String> lines;
		try {
			lines = Files.readLines(localFile, Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Could not read source file for FileCopy: " + localFile, e);
		}
		return new CreateFile(to, lines);
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

}
