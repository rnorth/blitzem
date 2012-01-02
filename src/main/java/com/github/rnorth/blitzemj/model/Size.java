package com.github.rnorth.blitzemj.model;


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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Size [minRam=").append(minRam).append(", minCores=").append(minCores).append("]");
		return builder.toString();
	}

	public int getMinRam() {
		return minRam;
	}

	public double getMinCores() {
		return minCores;
	}
}
