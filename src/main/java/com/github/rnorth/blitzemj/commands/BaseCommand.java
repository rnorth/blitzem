package com.github.rnorth.blitzemj.commands;

import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;

import com.github.rnorth.blitzemj.model.Node;
import com.google.common.base.Predicate;



public abstract class BaseCommand implements Command {

	private String noun;
	private Boolean verbose = false;
	private String source = "./environment.groovy";

	public String getNoun() {
		return noun;
	}

	public void setNoun(String noun) {
		this.noun = noun;
	}

	public Boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(Boolean verbose) {
		this.verbose = verbose;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

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
}
