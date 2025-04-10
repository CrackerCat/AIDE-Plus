
package io.github.zeroaicy.prefab;

import com.google.prefab.api.Package;
import kotlin.jvm.internal.Intrinsics;

public final class DuplicatePackageNamesException extends Error {

	public static final long serialVersionUID = 0x10;
	public DuplicatePackageNamesException(Package a, Package b) {
		super("Multiple packages named " + a.getName() + " found: " + a.getPath() + " and " + b.getPath() + '.');
		Intrinsics.checkNotNullParameter(a, "a");
		Intrinsics.checkNotNullParameter(b, "b");
	}
}

