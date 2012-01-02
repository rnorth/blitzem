/**
 * 
 */
package com.github.rnorth.blitzemj.commands;

import org.jclouds.compute.ComputeService;

/**
 * @author richardnorth
 *
 */
public interface WholeEnvironmentCommand extends Command {

	void execute(ComputeService computeService);

}
