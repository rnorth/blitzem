package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;

import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;

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
	private String source = "./environment.groovy";

	/**
	 * Find an existing live node ({@link NodeMetadata}) which matches 1:1 with
	 * a modelled {@link Node} in the local environment spec.
	 * 
	 * @param node
	 *            the modelled Node
	 * @param computeService
	 *            for looking up live nodes
	 * @return a set of matching {@link NodeMetadata}s - usually expect just
	 *         one, but more could be found and should be handled.
	 */
	protected Set<? extends NodeMetadata> findExistingNodesMatching(final Node node, ComputeService computeService) {
		Set<? extends NodeMetadata> existingNodes = computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {

			public boolean apply(ComputeMetadata arg0) {
				final String name = arg0.getName();

				if (name.contains("-")) {
					String trimmedName = name.substring(0, name.lastIndexOf('-'));
					return trimmedName.equals(node.getName());
				} else {
					return name.equals(node.getName());
				}

			}

		});
		return existingNodes;
	}

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
}
