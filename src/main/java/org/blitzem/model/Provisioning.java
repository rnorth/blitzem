package org.blitzem.model;

import org.jclouds.compute.options.TemplateOptions;

/**
 * Base class for provisioning steps to be carried out after server
 * creation.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class Provisioning {

	/**
	 * @return a JClouds-compatible {@link TemplateOptions} object describing
	 *         what needs to be done to carry out this provisioning step.
	 */
	public TemplateOptions asTemplateOption() {
		throw new UnsupportedOperationException();
	}

}
