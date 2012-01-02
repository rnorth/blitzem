package com.github.rnorth.blitzemj.model;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.io.payloads.StringPayload;

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
	public TemplateOptions asTemplateOption() {
		return runScript(new StringPayload(scriptAsString));
	}

}
