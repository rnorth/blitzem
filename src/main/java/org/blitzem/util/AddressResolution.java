package org.blitzem.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.Address;

/**
 * Utility class to support IP Address resolution.
 * 
 * @author Richard North <rich.north@gmail.com>
 * 
 */
public class AddressResolution {

	private static final long TIMEOUT = 120 * 1000L;
	private static final long INTERVAL = 1000L;

	/**
	 * Utility class.
	 */
	private AddressResolution() {}
	
	/**
	 * Resolve the IP Address for a given hostname, blocking until successful
	 * DNS resolution or a timeout occurs.
	 * 
	 * @param hostname
	 * @return
	 * @throws UnknownHostException
	 */
	public static InetAddress blockingResolveHostname(String hostname) throws UnknownHostException {

		int failCount = 0;
		while (failCount < TIMEOUT / INTERVAL) {

			try {
				return Address.getByName(hostname);
			} catch (UnknownHostException e) {
				failCount++;
			}

			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
			}
		}

		throw new UnknownHostException(hostname);
	}
}
