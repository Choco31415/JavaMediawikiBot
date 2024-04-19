package Mediawiki;

import Errors.UnsupportedError;

public class VersionNumber implements Comparable<VersionNumber> {

	private String version;
	
	private int releaseVersion;
	private int majorVersion;
	private int minorVersion;
	
	public VersionNumber(String version_) {
		version = version_;
		
		if (version.contains("-")) {
			//This is likely a developmental/weekly release. We don't need to go into that level of detail.
			version = version.substring(version.indexOf("-")).trim();
		}
		
		int indexMajor = version.indexOf(".");
		if (indexMajor == -1) {
			releaseVersion = Integer.parseInt(version);
			majorVersion = 0;
			minorVersion = 0;
		} else {
			int indexMinor = version.indexOf(".", indexMajor+1);
			
			releaseVersion = Integer.parseInt(version.substring(0, indexMajor));
			if (indexMinor == -1) {
				majorVersion = Integer.parseInt(version.substring(indexMajor+1));
				minorVersion = 0;
			} else {
				majorVersion = Integer.parseInt(version.substring(indexMajor+1, indexMinor));
				minorVersion = Integer.parseInt(version.substring(indexMinor+1));
				
				if (version.indexOf(".", indexMinor+1) != -1) {
					throw new UnsupportedError("Please use a version in the format: XX.xx.xx");
				}
			}
		}
	}
	
	public int getReleaseVersion() { return releaseVersion; }
	public int getMajorVersion() { return majorVersion; }
	public int getMinorVersion() { return minorVersion; }
	

	/**
	 * This method compares two VersionNumber's.
	 * 
	 * Return -1 if this is less then o, or not comparable.
     * Return 1 if this is greater then o.
	 * Return 0 if this is o.
	 */
	@Override
	public int compareTo(VersionNumber o) {
		if (o == null) {
			return -1;
		}

		if (o.getReleaseVersion() != releaseVersion) {
			return (o.getReleaseVersion() < releaseVersion) ? 1 : -1;
		} else {
			if (o.getMajorVersion() != majorVersion) {
				return (o.getMajorVersion() < majorVersion) ? 1 : -1;
			} else {
				if (o.getMinorVersion() != minorVersion) {
					return (o.getMinorVersion() < minorVersion) ? 1 : -1;
				} else {
					return 0;
				}
			}
		}
	}
	
	/**
	 * This method compares a VersionNumber and a String.
	 * 
	 * Return -1 if this is less then o, or not comparable.
     * Return 1 if this is greater then o.
	 * Return 0 if this is o.
	 */
	public int compareTo(String st) {
		return compareTo(new VersionNumber(st));
	}
	
	@Override
	public String toString() {
		return version;
	}
}
