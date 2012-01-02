package com.github.rnorth.blitzemj.model;

import java.util.List;

import org.jclouds.compute.options.TemplateOptions;

public abstract class Provisioning {

	public static final List<Provisioning> DEFAULT = (List<Provisioning>) Defaults.DEFAULTS.get("provisioning");

	public abstract TemplateOptions asTemplateOption();

}
