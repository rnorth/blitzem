package org.blitzem.model;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.io.payloads.StringPayload;
import org.jclouds.scriptbuilder.domain.Statement;

import static org.jclouds.compute.options.TemplateOptions.Builder.*;

/**
 * A provisioning step requiring a simple (short) script snippet to be run as
 * root on the new node.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class ScriptExecution extends Provisioning {

	private final String scriptAsString;

	public ScriptExecution(String scriptAsString) {
		this.scriptAsString = scriptAsString;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Statement asStatement() {
		return runScript(new StringPayload(scriptAsString)).getRunScript();
	}

}
