/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */

//
// Decompiled by Jadx - 1257ms
//
package com.aide.ui.build;

import abcd.hy;
import abcd.wf;
import abcd.xf;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import androidx.annotation.Keep;
import com.aide.common.AppLog;
import com.aide.common.StreamUtilities;
import com.aide.engine.SyntaxError;
import com.aide.ui.AppPreferences;
import com.aide.ui.ServiceContainer;
import com.aide.ui.build.android.NdkConfiguration;
import com.aide.ui.build.android.g;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.services.ProjectService;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.cmake.CmakeBuild;
import io.github.zeroaicy.aide.cmake.ProcessExitInfo;
import io.github.zeroaicy.aide.cmake.ProcessUtil;
import io.github.zeroaicy.aide.extend.ZeroAicyExtensionInterface;
import io.github.zeroaicy.aide.shell.ShellEnvironment;
import io.github.zeroaicy.aide.shell.ShellEnvironmentUtils;
import io.github.zeroaicy.aide.utils.PropertiesConfiguration;
import io.github.zeroaicy.aide.utils.Utils;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.FileUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.Set;
import io.github.zeroaicy.aide.ui.project.ZeroAicyAndroidProjectSupport;
import java.util.concurrent.atomic.AtomicBoolean;

public class NdkBuildService {
	public static final String TAG = "NdkBuildService";

	static ShellEnvironment shellEnvironment = ShellEnvironmentUtils.getShellEnvironment();

	private RunNdkBuildFutureTask runNdkBuildFutureTask;

	private g FH;

	private final ExecutorService executorService;

	public NdkBuildService() {
		this.executorService = ZeroAicyExtensionInterface.getProjectExecutorService();
	}
	public static PendingIntent j6(Context context, int requestCode, Intent intent, int flags) {
		flags |= PendingIntent.FLAG_MUTABLE;
		return PendingIntent.getActivity(context, requestCode, intent, flags);
	}
	static void DW(NdkBuildService ndkBuildService, Map<String, List<SyntaxError>> map) {
		ndkBuildService.we(map);
	}

	private void EQ() {
		if (this.FH != null) {
			this.FH.J0();
		}
	}

	static void FH(NdkBuildService ndkBuildService) {
		ndkBuildService.EQ();
	}

	static void Hw(NdkBuildService ndkBuildService, Throwable th) {
		ndkBuildService.tp(th);
	}

	private SyntaxError VH(String str, int i, int i2, String str2) {
		SyntaxError syntaxError = new SyntaxError();
		syntaxError.jw = i;
		syntaxError.fY = i2;
		syntaxError.qp = i;
		syntaxError.k2 = 1000;
		syntaxError.zh = str + ": " + str2;
		return syntaxError;
	}

	static Map<String, List<SyntaxError>> Zo(NdkBuildService ndkBuildService, String str, String str2) {
		return ndkBuildService.gn(str, str2);
	}

	private Map<String, List<SyntaxError>> gn(String str, String str2) {
		HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();

		for (String errorLine : str2.split("\n")) {
			errorLine = errorLine.trim();

			if (errorLine.length() <= 0) {
				continue;
			}
			try {
				int indexOf = errorLine.indexOf(':');

				if (indexOf > 0) {
					String path = new File(str, errorLine.substring(0, indexOf)).getPath();

					if (FileSystem.KD(path)) {
						int i2 = indexOf + 1;
						int indexOf2 = errorLine.indexOf(58, i2);
						if (indexOf2 < 0) {
							indexOf2 = errorLine.indexOf(32, i2);
						}
						if (indexOf2 > 0) {
							int i;

							try {
								i = Integer.parseInt(errorLine.substring(i2, indexOf2));
							} catch (NumberFormatException unused) {
								i = 1;
							}
							int i3 = indexOf2 + 1;
							int indexOf3 = errorLine.indexOf(58, i3);
							if (indexOf3 > 0) {
								try {
									Integer.parseInt(errorLine.substring(i3, indexOf3));
								} catch (NumberFormatException unused2) {
								}
							}
							String trim2 = errorLine.substring(indexOf3 + 1, errorLine.length()).trim();
							if (trim2.startsWith("error:")) {
								SyntaxError VH = VH("NDK", i, 1, trim2.substring(6, trim2.length()).trim());
								if (!hashMap.containsKey(path)) {
									hashMap.put(path, new ArrayList<SyntaxError>());
								}
								hashMap.get(path).add(VH);
							}
						}
					}
				}
			} catch (Exception e) {
				AppLog.e(e);
			}

			if (!hashMap.containsKey(str)) {
				hashMap.put(str, new ArrayList<>());
			}
			hashMap.get(str).add(VH("NDK", 1, 1, errorLine));

		}
		return hashMap;

	}

	static void j6(NdkBuildService ndkBuildService, boolean z) {
		ndkBuildService.u7(z);
	}

	private void tp(Throwable th) {
		AppLog.e(th);
		if (this.FH != null) {
			this.FH.g3();
		}
	}

	private void u7(boolean z) {
		if (this.FH != null) {
			this.FH.vJ(z);
		}

	}

	static SyntaxError makeSyntaxError(NdkBuildService ndkBuildService, String str, int i, int i2, String str2) {
		return ndkBuildService.VH(str, i, i2, str2);
	}

	private void we(Map<String, List<SyntaxError>> map) {
		if (this.FH != null) {
			this.FH.Mz(map);
		}
	}

	@Keep
	public void J0(boolean z) {
		if (this.runNdkBuildFutureTask != null) {
			this.runNdkBuildFutureTask.cancel(true);
			this.runNdkBuildFutureTask = null;
		}
		RunNdkBuildFutureTask runNdkBuildFutureTask = new RunNdkBuildFutureTask(this, new RunNdkBuildCallable(this, z,
				AppPreferences.isNativeBuildParallel(), ServiceContainer.getProjectService().P8()));

		this.runNdkBuildFutureTask = runNdkBuildFutureTask;
		this.executorService.execute(runNdkBuildFutureTask);

	}

	@Keep
	public void J8(g gVar) {
		this.FH = gVar;
	}

	static class RunNdkBuildFutureTask extends FutureTask<Map<String, List<SyntaxError>>> {

		private RunNdkBuildCallable runNdkBuildCallable;

		@hy
		final NdkBuildService ndkBuildService;

		public RunNdkBuildFutureTask(NdkBuildService ndkBuildService, RunNdkBuildCallable runNdkBuildCallable) {
			super(runNdkBuildCallable);
			this.ndkBuildService = ndkBuildService;
			this.runNdkBuildCallable = runNdkBuildCallable;
		}

		@Override
		protected void done() {
			if (isCancelled()) {
				return;
			}
			boolean isEnablePackaging = false;
			ProjectService projectService = ServiceContainer.getProjectService();
			for (String module : runNdkBuildCallable.modules) {
				// is Android Mk Module
				if (projectService.g3(module)) {
					isEnablePackaging = true;
					break;
				}

				if (ZeroAicyAndroidProjectSupport.isCmakeGradleProject(module)) {
					isEnablePackaging = true;
					break;
				}
			}
			try {
				Map<String, List<SyntaxError>> errors = get();
				if (errors == null) {
					NdkBuildService.j6(this.ndkBuildService, isEnablePackaging);
				} else {
					NdkBuildService.DW(this.ndkBuildService, errors);
				}
			} catch (InterruptedException unused) {
				NdkBuildService.FH(this.ndkBuildService);
			} catch (ExecutionException e) {
				NdkBuildService.Hw(this.ndkBuildService, e.getCause());
			}
		}
	}

	static class RunNdkBuildCallable implements Callable<Map<String, List<SyntaxError>>> {

		private final boolean isNativeBuildParallel;

		public final List<String> modules;

		@hy
		final NdkBuildService ndkBuildService;

		private final boolean isBuildRefresh;

		public RunNdkBuildCallable(NdkBuildService ndkBuildService, boolean isClean, boolean isNativeBuildParallel,
				List<String> modules) {
			this.ndkBuildService = ndkBuildService;
			this.isBuildRefresh = isClean;

			this.isNativeBuildParallel = isNativeBuildParallel;
			this.modules = modules;
		}

		private String DW(byte[] data, int i) {
			String str = "";
			try {
				str = StreamUtilities.readTextReader(new InputStreamReader(new ByteArrayInputStream(data)));
			} catch (Exception unused) {
			}

			String trim = str.trim();
			if (trim.length() != 0) {
				return trim;
			}
			return "ndk-build exited with code " + i;

		}

		// has Android Mk Module
		public boolean hasAndroidMkModule() {

			ProjectService projectService = ServiceContainer.getProjectService();
			for (String module : this.modules) {
				// is Android Mk Module
				if (projectService.g3(module)) {
					return true;
				}
			}
			return false;
		}

		private static void Hw(List<String> list, String str) {

			StringBuilder sb = new StringBuilder();

			sb.append("Running ndk-build [" + str + "] ");

			for (int i = 0; i < list.size(); i++) {
				sb.append('\"');
				sb.append(list.get(i));
				sb.append('\"');
				if (i != list.size() - 1) {
					sb.append(" ");
				}
			}
			AppLog.d(TAG, sb.toString());
		}

		// 是否有 ndk-build项目构建
		private boolean hasAndroidMkModule;

		private Map<String, List<SyntaxError>> runNdkBuild(String arg, boolean isNativeBuildParallel) {

			ProjectService projectService = ServiceContainer.getProjectService();

			for (String module : this.modules) {
				if (!projectService.g3(module)) {
					continue;
				}

				// isAndroidMkModule
				hasAndroidMkModule = true;

				// 检查ndk是否安装
				Map<String, List<SyntaxError>> checkInstalledNdk = checkInstalledNdk();
				if (checkInstalledNdk != null) {
					return checkInstalledNdk;
				}

				// 并行构建
				int threadCount = isNativeBuildParallel ? 8 : 1;

				List<String> ndkConfiguration = NdkConfiguration.VH(arg, threadCount);
				// 修改 HOST_ARCH=arm
				if (!ServiceContainer.isX86()) {
					// 为了防止影响Ndk安装包中的脚本
					// 可以已经在 build/core/init.mk 中写死了
					ndkConfiguration.set(ndkConfiguration.size() - 5, "HOST_ARCH=aarch64");
				}

				// 移除 TARGET_AR=$(TOOLCHAIN_PREFIX)ar  倒数第二个
				ndkConfiguration.remove(ndkConfiguration.size() - 2);

				List<String> ndkBuildArgs = shellEnvironment.setupShellCommandArguments(ndkConfiguration);
				// 安卓gradle android mk 工程
				if (GradleTools.isGradleProject(module) && GradleTools.isAndroidGradleProject(module)) {
					//安卓gradle工程
					ndkBuildArgs.add("NDK_PROJECT_PATH=.");
					ndkBuildArgs.add("APP_BUILD_SCRIPT=src/main/jni/Android.mk");
					ndkBuildArgs.add("NDK_APP_OUT=build/bin/intermediates/obj");
					ndkBuildArgs.add("NDK_LIBS_OUT=src/main/jniLibs");

					File ApplicationFile = new File(module, "src/main/jni/Application.mk");
					if (ApplicationFile.exists()) {
						ndkBuildArgs.add("NDK_APPLICATION_MK=" + "src/main/jni/Application.mk");
					}
				}

				//  只有PATH
				Map<String, String> env = NdkConfiguration.gn();

				// 安卓gradle android mk 工程
				if (GradleTools.isGradleProject(module) && GradleTools.isAndroidGradleProject(module)) {
					//安卓gradle工程
					env.put("NDK_PROJECT_PATH", ".");
					env.put("APP_BUILD_SCRIPT", "src/main/jni/Android.mk");
					env.put("NDK_APP_OUT", "build/bin/intermediates/obj");
					env.put("NDK_LIBS_OUT", "src/main/jniLibs");

					File ApplicationFile = new File(module, "src/main/jni/Application.mk");
					if (ApplicationFile.exists()) {
						env.put("NDK_APPLICATION_MK", "src/main/jni/Application.mk");
					}
				}

				Map<String, String> termuxEnvironment = shellEnvironment.getEnvironment(false, env);

				env = termuxEnvironment.isEmpty() ? env : termuxEnvironment;

				Hw(ndkBuildArgs, module);

				// 运行ndk-build
				wf j6 = xf.j6(ndkBuildArgs, module, env, true, (OutputStream) null, (byte[]) null);

				if (j6.DW() != 0) {
					return NdkBuildService.Zo(this.ndkBuildService, module, DW(j6.j6(), j6.DW()));
				}
			}

			return null;
		}

		@Override
		public Map<String, List<SyntaxError>> call() {

			{
				Map<String, List<SyntaxError>> compileSyntaxErrors = runCmakeBuild();
				if (compileSyntaxErrors != null) {
					return compileSyntaxErrors;
				}

			}

			// 所有module没有 Android.mk项目
			//			if (!hasAndroidMkModule()) {
			//				return null;
			//			}

			hasAndroidMkModule = false;
			// busybox适配 从com.aide.ndk29创建软连接到 PATH
			// NdkConfiguration.U2();
			// 构建刷新
			if (this.isBuildRefresh) {
				// ndk-build clean
				Map<String, List<SyntaxError>> syntaxErrors = runNdkBuild("clean", false);
				if (syntaxErrors != null) {
					return syntaxErrors;
				}
			}

			long currentTimeMillis = System.currentTimeMillis();

			// 构建 Android mk module
			Map<String, List<SyntaxError>> compileSyntaxErrors = runNdkBuild(null, this.isNativeBuildParallel);

			if (hasAndroidMkModule)
				AppLog.d("NDK build elapsed " + (System.currentTimeMillis() - currentTimeMillis) + "ms");

			return compileSyntaxErrors;

		}

		private Map<String, List<SyntaxError>> checkInstalledNdk() {
			if (NdkConfiguration.isInstalledNdk()) {
				return null;
			}

			HashMap<String, List<SyntaxError>> hashMap = new HashMap<>();
			String module = this.modules.get(0);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ServiceContainer.isX86()) {

				hashMap.put(module, new ArrayList<SyntaxError>());

				SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
						"Native development is not supported on X86 devices running Android 10 and above.");
				SyntaxError syntaxError = makeSyntaxError;

				hashMap.get(module).add(syntaxError);
			} else {

				hashMap.put(module, new ArrayList<SyntaxError>());

				SyntaxError makeSyntaxError = NdkBuildService.makeSyntaxError(this.ndkBuildService, "NDK", 1, 1,
						"NDK support not installed.");
				hashMap.get(module).add(makeSyntaxError);
			}
			return hashMap;

		}

		/*
		 * 遍历module 运行cmake
		 */

		private static final ZeroAicyBuildGradle singleton = ZeroAicyBuildGradle.getSingleton();

		private Map<String, List<SyntaxError>> runCmakeBuild() {

			Context context = ContextUtil.getContext();
			String androidSdkBaseDir = "framework/android-sdk";

			boolean isPro = context.getPackageName().equals("aidepro.top");
			if (isPro) {
				androidSdkBaseDir = "framework/android-sdk";
			} else {
				androidSdkBaseDir = "home/android-sdk";
			}

			String androidSdkPath = new File(context.getFilesDir(), androidSdkBaseDir).getAbsolutePath();

			ProjectService projectService = ServiceContainer.getProjectService();

			for (String modulePath : this.modules) {

				// 是否是 cmake项目

				// gradle项目
				if (GradleTools.isGradleProject(modulePath)) {
					// 
					String buildGradlePath = GradleTools.getBuildGradlePath(modulePath);
					if (!FileSystem.exists(buildGradlePath)) {
						// 存在build.gradle
						// 错误的 gradle 项目
						continue;
					}

					ZeroAicyBuildGradle configuration = singleton.getConfiguration(buildGradlePath);

					String projectPath = modulePath;

					// 默认 "src/main/cpp/CMakeLists.txt"
					String cmakeListsTxtPath = configuration.getCmakeListsTxtPath("src/main/cpp/CMakeLists.txt");

					if (!new File(projectPath, cmakeListsTxtPath).exists()) {
						// 文件不存在，不是cmake项目
						continue;
					}

					// 计算 CMakeLists.txt 父目录
					if (cmakeListsTxtPath.endsWith("CMakeLists.txt")) {
						cmakeListsTxtPath = FileSystem.getParent(cmakeListsTxtPath);
					}

					// cmake缓存路径相对于 projectPath
					String cmakeBuildCachePath = "build/bin/intermediates/cmake";

					// android 版本
					String minSdkVersion = configuration.getMinSdkVersion(null);
					// AppLog.println_d("minSdkVersion %s", minSdkVersion);

					// cmake版本
					String cmakeVersion = configuration.getCmakeVersion();
					// ndk版本
					String ndkVersion = configuration.getNdkVersion();

					// cppFlags
					String cppFlags = configuration.getCmakeCppFlags();

					// 待编译 abi
					LinkedHashSet<String> cmakeAbiFilters = configuration.getCmakeAbiFilters();
					Set<String> cmakeArguments = configuration.getCmakeArguments();

					CmakeBuild.Builder builder = new CmakeBuild.Builder()

							// 指定ndk所在父目录 android-sdk
							// 空值 build() 后自动指定 CMAKE_VERSION 与 NDK_VERSION
							.setAndroidSdkPath(androidSdkPath)
							// ndk版本
							.setNdkVersion(ndkVersion)
							// 指定cmake版本
							.setCmakeVersion(cmakeVersion)
							// 指定 cppFlags
							.setCmakeCppFlags(cppFlags)
							// 设置附加参数
							.setCmakeArguments(cmakeArguments)
							// 项目路径
							.setProjectPath(projectPath)
							// 指定安卓版本
							.setSystemVersion(minSdkVersion)
							// 输出目录
							.setCmakeOutputDirectoryPath("src/main/jniLibs")
							// 必须在setCmakeOutputDirectoryPath之后调用
							// 否则被覆盖
							.setCmakeBuildCachePath(cmakeBuildCachePath)
							// 源码路径
							.setCmakeListsTxtPath(cmakeListsTxtPath);

					if (this.isBuildRefresh) {
						// 清除
						FileUtil.deleteFolder(new File(projectPath, builder.getCmakeBuildCachePath()));
					}

					boolean checkCmakeVersioned = false;
					for (String abi : cmakeAbiFilters) {

						// 指定构建ABI
						builder.setAndroidABI(abi)
								// 指定安卓版本，build() 后会被修改
								.setSystemVersion(minSdkVersion);

						CmakeBuild cmakeBuild = builder.build();

						// 检查
						{
							// CmakeBuild cmakeBuild = builder.build();
							if (!cmakeBuild.error() && !checkCmakeVersioned) {
								// 标记已检查
								checkCmakeVersioned = false;

								String cmakeVersionString = builder.getCmakeVersion();

								String ndkVersionString = builder.getNdkVersion();

								int ndkVersionInt;
								if (TextUtils.isEmpty(ndkVersion) || ndkVersionString.length() < 2) {
									ndkVersionInt = 0;
								} else {
									ndkVersionInt = Utils.parseInt(ndkVersionString.substring(0, 2), 0);
								}

								if (cmakeVersionString.startsWith("3.31") && ndkVersionInt <= 24) {
									cmakeBuild.addErrorInfo("cmake 3.31 时，Ndk必须高于 ndk-24");
								}
							}
						}

						ProcessExitInfo runCmakeBuildInfo = runCmakeBuild(cmakeBuild, projectPath);
						if (runCmakeBuildInfo == null) {
							continue;
						}
						if (runCmakeBuildInfo.exit() == 0) {
							continue;
						}
						// make
						return NdkBuildService.Zo(
								// 
								this.ndkBuildService, projectPath,
								DW(runCmakeBuildInfo.getMessagen(), runCmakeBuildInfo.exit()));
					}
				} else if (FileSystem.isFileAndNotZip(modulePath + "/cpp/CMakeLists.txt")) {

					// 非gradle项目
					String projectPath = modulePath;

					String cmakeListsTxtPath = "cpp";
					String abi = "arm64-v8a";
					String cmakeOutputDirectoryPath = "libs";

					final String minSdkVersion;
					final String ndkVersion;
					// cmake版本
					final String cmakeVersion;
					// cppFlags
					final String cppFlags;

					// 自定义配置 cmake.properties
					File cmakePropertiesFile = new File(projectPath, "cpp/cmake.properties");

					if (cmakePropertiesFile.isFile()) {
						PropertiesConfiguration singleton = PropertiesConfiguration.getSingleton();

						PropertiesConfiguration cmakePropertiesConfiguration = singleton
								.getConfiguration(cmakePropertiesFile.getAbsolutePath());

						minSdkVersion = cmakePropertiesConfiguration.getProperty("android.minSdkVersion", "21");
						ndkVersion = cmakePropertiesConfiguration.getProperty("android.ndkVersion");

						// 参数
						cppFlags = cmakePropertiesConfiguration.getProperty("cmake.cppFlags");
						cmakeVersion = cmakePropertiesConfiguration.getProperty("cmake.version");

					} else {
						minSdkVersion = "20";
						ndkVersion = null;

						cppFlags = null;
						cmakeVersion = null;
					}

					CmakeBuild.Builder builder = new CmakeBuild.Builder()

							// 指定ndk所在父目录 android-sdk
							// 空值 build() 后自动指定 CMAKE_VERSION 与 NDK_VERSION
							.setAndroidSdkPath(androidSdkPath)
							// ndk版本
							.setNdkVersion(ndkVersion)
							// 指定cmake版本
							.setCmakeVersion(cmakeVersion)
							// 指定 cppFlags
							.setCmakeCppFlags(cppFlags)
							// 项目路径
							.setProjectPath(projectPath)
							// 指定安卓版本
							.setSystemVersion(minSdkVersion)
							// abi
							.setAndroidABI(abi)
							// 输出目录
							.setCmakeOutputDirectoryPath(cmakeOutputDirectoryPath)
							// 源码路径
							.setCmakeListsTxtPath(cmakeListsTxtPath);

					if (this.isBuildRefresh) {
						// 清除
						FileUtil.deleteFolder(new File(projectPath, builder.getCmakeBuildCachePath()));
					}

					CmakeBuild cmakeBuild = builder.build();

					if (!cmakeBuild.error()) {

						// String cmakeVersion = builder.getCmakeVersion();
						// String ndkVersionString = builder.getNdkVersion();

						String cmakeVersionString = builder.getCmakeVersion();

						String ndkVersionString = builder.getNdkVersion();

						int ndkVersionInt;
						if (TextUtils.isEmpty(ndkVersion) || ndkVersionString.length() < 2) {
							ndkVersionInt = 0;
						} else {
							ndkVersionInt = Utils.parseInt(ndkVersionString.substring(0, 2), 0);
						}

						if (cmakeVersionString.startsWith("3.31") && ndkVersionInt <= 24) {
							cmakeBuild.addErrorInfo("cmake 3.31 时，Ndk必须高于 ndk-24");
							cmakeBuild.addErrorInfo("请在CMakeLists.txt同目录下，创建cmake.properties文件 ");
							cmakeBuild.addErrorInfo(
									"支持 cmake.version cmake.cppFlags android.ndkVersion android.minSdkVersion");

							List<String> cmakeCommandList = cmakeBuild.getCmakeCommandList();
							cmakeBuild.addErrorInfo(String.valueOf(cmakeCommandList));

						}
					}

					ProcessExitInfo runCmakeBuildInfo = runCmakeBuild(cmakeBuild, projectPath);

					if (runCmakeBuildInfo == null) {
						continue;
					}
					if (runCmakeBuildInfo.exit() == 0) {
						continue;
					}
					// make
					return NdkBuildService.Zo(
							// 
							this.ndkBuildService, projectPath,
							DW(runCmakeBuildInfo.getMessagen(), runCmakeBuildInfo.exit()));

				}
			}

			return null;
		}

		private static ProcessExitInfo runCmakeBuild(final CmakeBuild cmakeBuild, String projectPath) {

			if (cmakeBuild.error()) {
				return new ProcessExitInfo() {
					@Override
					public int exit() {
						return -1;
					}
					@Override
					public byte[] getMessagen() {
						return cmakeBuild.getBuildInfo().getBytes();
					}
				};
			}

			Map<String, String> termuxEnvironment = shellEnvironment.getEnvironment(false);

			//进程信息
			Map<String, String> env = termuxEnvironment.isEmpty() ? System.getenv() : termuxEnvironment;

			List<String> cmakeCommandList = shellEnvironment
					.setupShellCommandArguments(cmakeBuild.getCmakeCommandList());

			// AppLog.d(TAG, cmakeCommandList);

			ProcessExitInfo processInfo = ProcessUtil.j6(cmakeCommandList, projectPath, env, true, null, null);

			if (processInfo.exit() != 0) {
				return processInfo;
			}

			//ninja build.ninja
			List<String> ninjaCommandList = shellEnvironment
					.setupShellCommandArguments(cmakeBuild.getNinjaCommandList());

			// AppLog.d(TAG, ninjaCommandList);

			processInfo = ProcessUtil.j6(ninjaCommandList, projectPath, env, true, null, null);

			if (processInfo.exit() != 0) {
				return processInfo;
			}

			return null;
		}

	}
}

