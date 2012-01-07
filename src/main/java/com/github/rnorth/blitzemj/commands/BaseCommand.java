package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.loadbalancer.LoadBalancerService;
import org.jclouds.loadbalancer.domain.LoadBalancerMetadata;

import com.github.rnorth.blitzemj.model.LoadBalancer;
import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * Base implementation for Command object classes, providing support for common
 * arguments.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public abstract class BaseCommand implements Command {

	private String noun;
	private Boolean verbose = false;
	private Boolean superVerbose = false;
	private String source = "./environment.groovy";

	/** 
	 * {@inheritDoc}
	 */
	public String getNoun() {
		return noun;
	}

	/** 
	 * {@inheritDoc}
	 */
	public void setNoun(String noun) {
		this.noun = noun;
	}

	/**
	 * @return the verbose
	 */
	public Boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(Boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the superVerbose
	 */
	public Boolean isSuperVerbose() {
		return superVerbose;
	}

	/**
	 * @param superVerbose the superVerbose to set
	 */
	public void setSuperVerbose(Boolean superVerbose) {
		this.superVerbose = superVerbose;
	}
}
