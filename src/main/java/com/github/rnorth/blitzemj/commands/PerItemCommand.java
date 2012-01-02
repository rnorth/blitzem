package com.github.rnorth.blitzemj.commands;

import org.jclouds.compute.ComputeService;

import com.github.rnorth.blitzemj.model.Node;

public interface PerItemCommand extends Command{

	void execute(Node node, ComputeService computeService) throws CommandException;

}
