
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

public class CliForZeroAicy {

	// "--build-system=ndk-build"
	private String buildSystem;

	// "--output=
	private File outputFile;

	// "--platform=android"
	private PlatformConfig platformConfig;

	// prefab 包
	private List<Package> packages;

	private Collection<? extends PlatformDataInterface> platformRequirements;

	public CliForZeroAicy(String abi, String osVersion, String stl, int ndkVersion, String buildSystem, String output, String... prefabPaths) {
		this.buildSystem = buildSystem;
		this.outputFile = new File(output);

		this.platformConfig = new AndroidConfig(abi, osVersion, stl, ndkVersion);

		this.packages = new ArrayList<>(prefabPaths.length);
		Set<File> packagePaths = new HashSet<>(prefabPaths.length);
		for (String prefabPath : prefabPaths) {
			File prefabFile = new File(prefabPath);
			if (!packagePaths.contains(prefabFile) && prefabFile.isDirectory()) {
				packagePaths.add(prefabFile);
				packages.add(new Package(prefabFile.toPath()));
			}
		}

		if (platformConfig instanceof AndroidConfig) {
			AndroidConfig androidConfig = (AndroidConfig) platformConfig;
			platformRequirements = makeAndroidRequirements(androidConfig);
		}
	}

	private String getBuildSystem() {
		return this.buildSystem;
	}

	private File getOutput() {
		return this.outputFile;
	}

	public PlatformConfig getPlatform() {
		return this.platformConfig;
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
}

