package com.github.rnorth.blitzemj.model;

/**
 * Models an Operating system definition.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class Os {

	public static final Os DEFAULT = new Os();
	private String family = Defaults.DEFAULTS.get("osFamily") + "";
	private String version = Defaults.DEFAULTS.get("osVersion") + "";
	private Boolean os64Bit = Boolean.valueOf(Defaults.DEFAULTS.get("os64Bit") + "");

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Os [family=").append(family).append(", version=").append(version).append(", os64Bit=").append(os64Bit).append("]");
		return builder.toString();
	}

	/**
	 * @return the family
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * @param family the family to set
	 */
	public void setFamily(String family) {
		this.family = family;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the os64Bit
	 */
	public Boolean getOs64Bit() {
		return os64Bit;
	}

	/**
	 * @param os64Bit the os64Bit to set
	 */
	public void setOs64Bit(Boolean os64Bit) {
		this.os64Bit = os64Bit;
	}

	/**
	 * @return the default
	 */
	public static Os getDefault() {
		return DEFAULT;
	}
}
