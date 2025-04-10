/**
 * @Date 
 * @AIDE AIDE+ 
 */
 
package io.github.zeroaicy.prefab;

import com.google.prefab.api.BuildSystemProvider;
import com.google.prefab.cmake.CMakePluginProvider;
import com.google.prefab.ndkbuild.NdkBuildPluginProvider;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BuildSystemRegistry {

	@NotNull
	public static final BuildSystemRegistry INSTANCE = new BuildSystemRegistry();

	@NotNull
	private static final List<BuildSystemProvider> buildSystems;

	private BuildSystemRegistry() {
	}

	static {
		List<BuildSystemProvider> buildSystems_1 = new ArrayList<BuildSystemProvider>();
		buildSystems_1.add(new NdkBuildPluginProvider());
		buildSystems_1.add(new CMakePluginProvider());
		buildSystems = buildSystems_1;
	}

	public final boolean supports(@NotNull String identifier) {
		Intrinsics.checkNotNullParameter(identifier, "identifier");
		return find(identifier) != null;
	}

	@Nullable
	public final BuildSystemProvider find(@NotNull String identifier) {
		Intrinsics.checkNotNullParameter(identifier, "identifier");
		for (BuildSystemProvider buildSystemProvider : buildSystems) {
			if (Intrinsics.areEqual(buildSystemProvider.getIdentifier(), identifier)) {
				return buildSystemProvider;
			}
		}
		return null;
	}
}

