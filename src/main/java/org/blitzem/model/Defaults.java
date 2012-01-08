package org.blitzem.model;

import groovy.lang.GroovyShell;

import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

/**
 * State-holder class for system defaults. While these defaults may be
 * overridden in the environment spec file, the original defaults will be
 * sourced from a 'standard-defaults.groovy' file.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public abstract class Defaults {

	private Defaults() {
	}

	/**
	 * System-wide defaults.
	 */
	public static final Map<String, Object> DEFAULTS = Maps.newConcurrentMap();

	/**
	 * Load initial defaults from classpath standard-defaults.groovy resource.
	 */
	static {
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
