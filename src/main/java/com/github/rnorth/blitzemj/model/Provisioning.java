package com.github.rnorth.blitzemj.model;

import java.util.List;

import org.jclouds.compute.options.TemplateOptions;

/**
 * Abstract base class for provisioning steps to be carried out after server
 * creation.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public abstract class Provisioning {

	public static final List<Provisioning> DEFAULT = (List<Provisioning>) Defaults.DEFAULTS.get("provisioning");

	/**
	 * @return a JClouds-compatible {@link TemplateOptions} object describing
	 *         what needs to be done to carry out this provisioning step.
	 */
	public abstract TemplateOptions asTemplateOption();

}
