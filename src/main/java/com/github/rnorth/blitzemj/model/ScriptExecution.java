package com.github.rnorth.blitzemj.model;

import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.io.payloads.StringPayload;

import static org.jclouds.compute.options.TemplateOptions.Builder.*;


public class ScriptExecution extends Provisioning {

	private final String scriptAsString;

	public ScriptExecution(String scriptAsString) {
		this.scriptAsString = scriptAsString;
	}

	@Override
	public TemplateOptions asTemplateOption() {
		return runScript(new StringPayload(scriptAsString));
	}

}
