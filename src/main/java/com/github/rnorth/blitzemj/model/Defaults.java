package com.github.rnorth.blitzemj.model;

import groovy.lang.GroovyShell;

import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

public class Defaults {

	private Defaults() {}
	
	public static final Map<String, Object> DEFAULTS = Maps.newConcurrentMap();

	public static void load() {
		final GroovyShell groovyShell = new GroovyShell();
		groovyShell.setVariable("defaults", Defaults.DEFAULTS);
		try {
			groovyShell.evaluate(Resources.toString(Resources.getResource("standard-defaults.groovy"), Charsets.UTF_8));
		} catch (CompilationFailedException e) {
			throw new IllegalArgumentException("Could not compile standard-defaults.groovy!", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load standard-defaults.groovy!", e);
		}
	}

}
