package com.github.rnorth.blitzemj.model;


public class Os {

	public static final Os DEFAULT = new Os();
	private String family = Defaults.DEFAULTS.get("osFamily") + "";
	private String version = Defaults.DEFAULTS.get("osVersion") + "";
	private Boolean os64Bit = Boolean.valueOf(Defaults.DEFAULTS.get("os64Bit") + "");
	
	public String getFamily() {
		return family;
	}
	public String getVersion() {
		return version;
	}
	public Boolean getOs64Bit() {
		return os64Bit;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Os [family=").append(family).append(", version=").append(version).append(", os64Bit=").append(os64Bit).append("]");
		return builder.toString();
	}
}
