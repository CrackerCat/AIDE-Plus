/**
 * @Date 
 * @AIDE AIDE+ 
 */

package io.github.zeroaicy.prefab;

import com.google.prefab.api.Android;
import com.google.prefab.api.BuildSystemInterface;
import com.google.prefab.api.BuildSystemProvider;
import com.google.prefab.api.Package;
import com.google.prefab.api.PlatformDataInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import java.util.Arrays;

public class Cli {

	// "--build-system=ndk-build"
	private final String buildSystem;

	// "--output=
	private final File outputFile;

	// prefab 包
	private final List<Package> packages;

	// "--platform=android"

	private final Collection<? extends PlatformDataInterface> platformRequirements;

	public Cli(String abi, String osVersion, String stl, int ndkVersion, String buildSystem, String output,
			String... prefabPaths) {
		this.buildSystem = buildSystem;
		this.outputFile = new File(output);

		AndroidConfig androidConfig = new AndroidConfig(abi, osVersion, stl, ndkVersion);
		this.platformRequirements = makeAndroidRequirements(androidConfig);

		this.packages = new ArrayList<>(prefabPaths.length);
		Set<File> packagePaths = new HashSet<>(prefabPaths.length);
		for (String prefabPath : prefabPaths) {
			File prefabFile = new File(prefabPath);
			if (!packagePaths.contains(prefabFile) && prefabFile.isDirectory()) {
				packagePaths.add(prefabFile);
				packages.add(new Package(prefabFile.toPath()));
			}
		}

	}

	public Cli(String buildSystem, File outputFile, List<Package> packages, AndroidConfig androidConfig) {
		this.buildSystem = buildSystem;
		this.outputFile = outputFile;
		this.packages = packages;

		this.platformRequirements = makeAndroidRequirements(androidConfig);

	}

	private String getBuildSystem() {
		return this.buildSystem;
	}

	private File getOutput() {
		return this.outputFile;
	}

	private List<Package> getPackages() {
		return this.packages;
	}

	public Collection<Android> makeAndroidRequirements(AndroidConfig config) {
		String abi = config.getAbi();
		String osVersion = config.getOsVersion();
		Android.Stl stl = Android.Stl.Companion.fromString(config.getStl());
		int ndkVersion = config.getNdkVersion();
		if (abi != null) {
			return CollectionsKt.listOf(
					new Android(Android.Abi.Companion.fromString(abi), Integer.parseInt(osVersion), stl, ndkVersion));
		}
		Android.Abi[] values = Android.Abi.values();
		Collection<Android> destination$iv$iv = new ArrayList<>(values.length);
		for (Android.Abi abi2 : values) {
			destination$iv$iv.add(new Android(abi2, Integer.parseInt(osVersion), stl, ndkVersion));
		}
		return destination$iv$iv;
	}

	private Collection<? extends PlatformDataInterface> getPlatformRequirements() {
		return this.platformRequirements;
	}

	protected void validate() {
		if (!BuildSystemRegistry.INSTANCE.supports(getBuildSystem())) {
			throw new Error("unsupported build system requested");
		}
		Map<String, Package> seenPackageNames = new LinkedHashMap<>();
		for (Package pkg : getPackages()) {
			Package seenPackage = (Package) seenPackageNames.get(pkg.getName());
			if (seenPackage != null) {
				throw new DuplicatePackageNamesException(pkg, seenPackage);
			}
			seenPackageNames.put(pkg.getName(), pkg);
		}
	}

	public void run() {
		validate();

		List<Package> packages = getPackages();

		Collection<String> packageNames = new ArrayList<String>(CollectionsKt.collectionSizeOrDefault(packages, 10));
		for (Package prefabPackage : packages) {
			packageNames.add(prefabPackage.getName());
		}

		Set<String> knownPackages = CollectionsKt.toSet(packageNames);
		for (Package pkg : packages) {

			for (String dep : pkg.getDependencies()) {
				if (!knownPackages.contains(dep)) {
					throw new Error("Error: " + pkg.getName() + " depends on unknown dependency " + dep);
				}
			}
		}

		String buildSystem = getBuildSystem();
		BuildSystemProvider find = BuildSystemRegistry.INSTANCE.find(buildSystem);
		Intrinsics.checkNotNull(find);

		File output = getOutput();

		BuildSystemInterface buildSystemIntegration = find.create(output, packages);
		buildSystemIntegration.generate(getPlatformRequirements());

	}

	public static class Builder {

		String abi;
		String osVersion;
		String stl = "c++_shared";
		int ndkVersion = 29;

		// "--build-system=ndk-build"
		private String buildSystem;
		// "--output=
		private File outputFile;

		Collection<String> prefabPaths;

		public Builder() {

		}

		/**
		 * AndroidConfig
		 */
		public Builder setAbi(String abi) {
			this.abi = abi;
			return this;
		}
		public Builder setOsVersion(String osVersion) {
			this.osVersion = osVersion;
			return this;
		}

		public Builder setStl(String stl) {
			this.stl = stl;
			return this;
		}

		public Builder setNdkVersion(int ndkVersion) {
			this.ndkVersion = ndkVersion;
			return this;
		}

		/**
		 * cli
		 */
		public Builder setBuildSystem(String buildSystem) {
			this.buildSystem = buildSystem;
			return this;
		}

		public Builder setOutputFile(String outputPath) {
			if (outputPath == null)
				return this;

			return setOutputFile(new File(outputPath));
		}

		public Builder setOutputFile(File outputFile) {
			this.outputFile = outputFile;
			return this;
		}

		public Builder setPrefabPaths(String... prefabPaths) {
			return setPrefabPaths(Arrays.asList(prefabPaths));
		}

		public Builder setPrefabPaths(Collection<String> prefabPaths) {
			this.prefabPaths = prefabPaths;
			return this;
		}

		public Cli build() {

			// prefab 包
			List<Package> packages = makePackages();

			AndroidConfig androidConfig = new AndroidConfig(this.abi, this.osVersion, stl, ndkVersion);
			return new Cli(this.buildSystem, this.outputFile, packages, androidConfig);
		}

		private List<Package> makePackages() {
			List<Package> packages = new ArrayList<>(prefabPaths.size());
			
			// 虑重
			Set<String> packagePaths = prefabPaths instanceof Set
					? (Set<String>) prefabPaths
					: new HashSet<String>(prefabPaths);

			for (String prefabPath : packagePaths) {
				File prefabFile = new File(prefabPath);
				if (prefabFile.isDirectory()) {
					packages.add(new Package(prefabFile.toPath()));
				}
			}
			return packages;
		}
	}
}

