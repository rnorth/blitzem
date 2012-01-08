package org.blitzem.model;


/**
 * Models a server sizing definition.
 * 
 * @author Richard North <rich.north@gmail.com>
 *
 */
public class Size {

	public static final Size DEFAULT = new Size();
	private int minRam = Integer.valueOf(Defaults.DEFAULTS.get("minRam") + "");
	private double minCores = Double.valueOf(Defaults.DEFAULTS.get("minCores") + "");

	private Size() { }
	
	public Size(int minRam) {
		this.minRam = minRam;
	}

	public Size(int minRam, double minCores) {
		this.minRam = minRam;
		this.minCores = minCores;
	}

	public static Size ram(int minRam) {
		return new Size(minRam);
	}
	
	public static Size ramAndCores(int minRam, double minCores) {
		return new Size(minRam, minCores);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Size [minRam=").append(minRam).append(", minCores=").append(minCores).append("]");
		return builder.toString();
	}

	/**
	 * @return the minRam
	 */
	public int getMinRam() {
		return minRam;
	}

	/**
	 * @param minRam the minRam to set
	 */
	public void setMinRam(int minRam) {
		this.minRam = minRam;
	}

	/**
	 * @return the minCores
	 */
	public double getMinCores() {
		return minCores;
	}

	/**
	 * @param minCores the minCores to set
	 */
	public void setMinCores(double minCores) {
		this.minCores = minCores;
	}

}
