/**
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.ui.services;
import android.app.Activity;
import android.os.Build;
import android.os.StatFs;
import com.aide.common.MessageBox;
import com.aide.ui.ServiceContainer;
import com.aide.ui.build.android.NdkConfiguration;
import com.aide.ui.services.DownloadService;
import com.aide.ui.util.FileSystem;
import java.io.File;

public class ZeroAicyDownloadService extends DownloadService {

	static final int[] ndkInstallTypeOrdinals;

	static final int[] ndkAppInstallTypeOrdinals;

	static {
		ndkInstallTypeOrdinals = new int[NdkConfiguration.NdkInstallType.values().length];
		try {
			ndkInstallTypeOrdinals[NdkConfiguration.NdkInstallType.NOT_INSTALLED.ordinal()] = 1;
		} catch (NoSuchFieldError unused) {
		}
		try {
			ndkInstallTypeOrdinals[NdkConfiguration.NdkInstallType.OUTDATED.ordinal()] = 2;
		} catch (NoSuchFieldError unused2) {
		}
		try {
			ndkInstallTypeOrdinals[NdkConfiguration.NdkInstallType.AIDE_OUTDATED.ordinal()] = 3;
		} catch (NoSuchFieldError unused3) {
		}
		try {
			ndkInstallTypeOrdinals[NdkConfiguration.NdkInstallType.EXPANSION_FILE_MISSING.ordinal()] = 4;
		} catch (NoSuchFieldError unused4) {
		}
		try {
			ndkInstallTypeOrdinals[NdkConfiguration.NdkInstallType.INSTALLED_OK.ordinal()] = 5;
		} catch (NoSuchFieldError unused5) {
		}

		ndkAppInstallTypeOrdinals = new int[NdkConfiguration.NdkAppInstallType.values().length];
		try {
			ndkAppInstallTypeOrdinals[NdkConfiguration.NdkAppInstallType.NOT_INSTALLED.ordinal()] = 1;
		} catch (NoSuchFieldError unused6) {
		}
		try {
			ndkAppInstallTypeOrdinals[NdkConfiguration.NdkAppInstallType.OUTDATED.ordinal()] = 2;
		} catch (NoSuchFieldError unused7) {
		}

	}

	@Override
	public void ca(Activity activity, String describe) {

		// AIDE_OUTDATED, AIDE过时
		// EXPANSION_FILE_MISSING 扩展文件丢失
		// INSTALLED_OK, 已安装
		// NOT_INSTALLED 没有安装
		// OUTDATED 过时
		NdkConfiguration.NdkInstallType ndkInstallType = NdkConfiguration.Ws(activity);

		if (ndkInstallType == NdkConfiguration.NdkInstallType.INSTALLED_OK) {
			return;
		}

		if ("This project contains native code.".equals(describe)) {
			describe = "此项目包含本机代码";
		}

		if (ndkInstallType == NdkConfiguration.NdkInstallType.NOT_INSTALLED) {
			describe += "\n\n请使用下列网址安装\nhttps://github.com/ZeroAicy/AIDE-Ndk-Install\n\nhttps://gitee.com/ZeroAicy/AIDE-Ndk-Install";
			MessageBox.BT(activity, "Native development (C/C++)", describe);
			return;
		}

		if (Build.VERSION.SDK_INT >= 29) {
			if (ServiceContainer.isX86()) {
				MessageBox.BT(activity, "Native development (C/C++)", describe + "\n\n运行Android 10及以上版本的X86设备不支持本机开发");
				return;
			}

			// OUTDATED 过时的
			// NOT_INSTALLED 没有安装
			// INSTALLED_OK 已安装
			NdkConfiguration.NdkAppInstallType ndkAppInstallType = NdkConfiguration.getNdkAppInstallType(activity);

			int ndkAppInstallTypeOrdinal = ZeroAicyDownloadService.ndkAppInstallTypeOrdinals[ndkAppInstallType
					.ordinal()];
			if (ndkAppInstallTypeOrdinal == 1) {
				MessageBox.rN(activity, "Install binaries for native code (C/C++)", describe
						+ "After installing the AIDE NDK Binaries (for Android 10+) package and the AIDE NDK Support package you can build apps using native-code languages such as C and C++. \n\nDownload the AIDE NDK Binaries (for Android 10+) package now?",
						new DownloadService.l(), null);
				return;
			}
			if (ndkAppInstallTypeOrdinal == 2) {
				MessageBox.rN(activity, "AIDE NDK Binaries package is outdated", describe
						+ "\n\nThe  AIDE NDK Binaries (for Android 10+) package is outdated. Open Play Store to update?",
						new DownloadService.m(), null);
				return;
			}
		}

		int ndkInstallTypeOrdinal = ZeroAicyDownloadService.ndkInstallTypeOrdinals[ndkInstallType.ordinal()];

		if (ndkInstallTypeOrdinal == 2) {
			MessageBox.rN(activity, "NDK Support package is outdated",
					describe + "\n\nThe AIDE NDK Support package is outdated. Open Play Store to update?",
					new DownloadService.o(), null);
			return;
		}
		if (ndkInstallTypeOrdinal == 3) {
			MessageBox.rN(activity, "AIDE is outdated", describe
					+ "\n\nYour version of AIDE is outdated and does not support the updated AIDE NDK Support package. Open Play Store to update?",
					new DownloadService.a(), null);
			return;
		}
		if (ndkInstallTypeOrdinal == 4) {
			MessageBox.rN(activity, "Install support for native code (C/C++)", describe
					+ "\n\nThe AIDE NDK Support Package has not been completely installed. Open AIDE NDK Support Package to download expansion file?",
					new DownloadService.b(activity), null);
			return;
		}
		if (ndkInstallTypeOrdinal == 5) {
			Mz(activity);
			return;
		}
		MessageBox.rN(activity, "Install support for native code (C/C++)", describe
				+ "\n\nAfter installing support for native code you can build apps using native-code languages such as C and C++. The native code support takes about 750MB of space on internal storage space once installed.\n\nDownload native code support package now?",
				new DownloadService.n(), null);

	}

	private void Mz(Activity activity) {
		NdkConfiguration.deleteOldNdk();

		File file = new File(FileSystem.getSafeCacheDirPath());
		if (!er(FileSystem.getNoBackupFilesDirPath(), 750000000L)) {
			MessageBox.rN(activity, "Download support for native code (C/C++)",
					"There does not seem to be enough space on internal storage. At least 200MB are required. Continue anyway?",
					new DownloadService.c(activity, file), null);
			return;
		}
		I(activity, file);

	}

	private boolean er(String str, long j) {
		StatFs statFs = new StatFs(str);
		return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() > j;
	}

	private void I(Activity activity, File file) {
		//		if (this.task != null) {
		//			this.task.cancel(true);
		//			this.task = null;
		//		}
		//		Runnable task = new Task(activity, new DownloadService.r(activity, file));
		//		this.task = task;
		//		this.taskTitle = "Installing support for native code (C/C++)";
		//		this.executorService.execute(task);
		//		MessageBox.showDialog(activity, new jd());
	}
}

