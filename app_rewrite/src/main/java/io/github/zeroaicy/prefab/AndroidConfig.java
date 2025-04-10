
package io.github.zeroaicy.prefab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AndroidConfig implements PlatformConfig {

	public AndroidConfig(String abi, String osVersion, String stl, int ndkVersion) {
		this();
		this.abi = abi;
		this.osVersion = osVersion;
		this.stl = stl;
		this.ndkVersion = ndkVersion;
	}

	private AndroidConfig() {

	}

	private String abi;
	private String osVersion;

	// new String[]{"c++_shared", "c++_static", "gnustl_shared", "gnustl_static", "none",
	// "stlport_shared", "stlport_static", "system"},

	private String stl;
	private int ndkVersion;

	@Nullable
	public final String getAbi() {
		return this.abi;
	}

	@NotNull
	public final String getOsVersion() {
		return this.osVersion;
	}

	@NotNull
	public final String getStl() {
		return this.stl;
	}

	public final int getNdkVersion() {
		return this.ndkVersion;
	}
}

