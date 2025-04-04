package io.github.zeroaicy.aide.ui.project;

import com.aide.engine.EngineSolution;
import com.aide.engine.EngineSolutionProject;
import com.aide.ui.project.AndroidProjectSupport;
import java.util.List;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;
import android.text.TextUtils;
import java.io.File;
import java.util.Arrays;

public class ZeroAicyAndroidProjectSupport extends AndroidProjectSupport {
	public ZeroAicyAndroidProjectSupport() {

	}

	@Override
	public EngineSolution makeEngineSolution() {
		EngineSolution makeEngineSolution = super.makeEngineSolution();

		addCmakeSourceDir(makeEngineSolution);

		return makeEngineSolution;
	}

	// 添加 cmake的源码目录
	private void addCmakeSourceDir(EngineSolution makeEngineSolution) {
		List<EngineSolutionProject> engineSolutionProjects = (List<EngineSolutionProject>) makeEngineSolution.engineSolutionProjects;
		for (EngineSolutionProject engineSolutionProject : engineSolutionProjects) {
			String projectPath = engineSolutionProject.getProjectPath();
			if (!GradleTools.isGradleProject(projectPath)) {
				continue;
			}
			String buildGradlePath = GradleTools.getBuildGradlePath(projectPath);
			if (!FileSystem.exists(buildGradlePath)) {
				// 存在build.gradle
				// 错误的 gradle 项目
				continue;
			}

			ZeroAicyBuildGradle configuration = ZeroAicyBuildGradle.getSingleton().getConfiguration(buildGradlePath);

			String cmakeListsTxtPath = configuration.getCmakeListsTxtPath();

			if (TextUtils.isEmpty(cmakeListsTxtPath)) {
				cmakeListsTxtPath = "src/main/cpp";
			}
			if (cmakeListsTxtPath.endsWith("CMakeLists.txt")) {
				cmakeListsTxtPath = FileSystem.getParent(cmakeListsTxtPath);
			}

			engineSolutionProject.fY
					.add(new EngineSolution.File(projectPath + "/" + cmakeListsTxtPath, "C++:+", null, false, false));
		}
	}

	// has AndroidMk
	@Override
	public boolean vy(String projectPath) {
		// 安卓 Gradle 非 Cmake项目 
		// isAndroidGradleProject只要是安卓项目(就是不管是否是 gradle)就返回true
		if (GradleTools.isGradleProject(projectPath) && GradleTools.isAndroidGradleProject(projectPath)
				&& !isCmakeGradleProject(projectPath)) {
			return FileSystem.isFileAndNotZip(projectPath + "/src/main/jni/Android.mk");
		}
		return super.vy(projectPath);
	}

	public static boolean isCmakeGradleProject(String modulePath) {

		String buildGradlePath = GradleTools.getBuildGradlePath(modulePath);
		if (!FileSystem.exists(buildGradlePath)) {
			// 存在build.gradle
			// 错误的 gradle 项目
			return false;
		}

		ZeroAicyBuildGradle configuration = ZeroAicyBuildGradle.getSingleton().getConfiguration(buildGradlePath);

		String projectPath = modulePath;

		// 默认 "src/main/cpp/CMakeLists.txt"
		String cmakeListsTxtPath = configuration.getCmakeListsTxtPath("src/main/cpp/CMakeLists.txt");

		if (new File(projectPath, cmakeListsTxtPath).exists()) {
			// 文件不存在，不是cmake项目
			return true;
		}

		return false;
	}

	private static List<String> advises = Arrays.asList(new String[]{"com.google.android.material:material::+",
			"androidx.appcompat:appcompat:+", "androidx.arch.core:core-common:+", "androidx.arch.core:core:+",
			"androidx.arch.core:core-runtime:+", "androidx.lifecycle:lifecycle-common:+",
			"androidx.lifecycle:lifecycle-common-java8:+", "androidx.lifecycle:lifecycle-compiler:+",
			"androidx.lifecycle:lifecycle-extensions:+", "androidx.lifecycle:lifecycle-livedata:+",
			"androidx.lifecycle:lifecycle-livedata-core:+", "androidx.lifecycle:lifecycle-reactivestreams:+",
			"androidx.lifecycle:lifecycle-runtime:+", "androidx.lifecycle:lifecycle-viewmodel:+",
			"androidx.paging:paging-common:+", "androidx.paging:paging-runtime:+", "androidx.paging:paging-rxjava2:+",
			"androidx.room:room-common:+", "androidx.room:room-compiler:+", "androidx.room:room-guava:+",
			"androidx.room:room-migration:+", "androidx.room:room-runtime:+", "androidx.room:room-rxjava2:+",
			"androidx.sqlite:sqlite:+", "androidx.sqlite:sqlite-framework:+",
			"androidx.constraintlayout:constraintlayout:+", "androidx.constraintlayout:constraintlayout-solver:+",
			"androidx.vectordrawable:vectordrawable-animated:+", "androidx.asynclayoutinflater:asynclayoutinflater:+",
			"androidx.car:car:+", "androidx.cardview:cardview:+", "androidx.collection:collection:+",
			"androidx.coordinatorlayout:coordinatorlayout:+", "androidx.cursoradapter:cursoradapter:+",
			"androidx.browser:browser:+", "androidx.customview:customview:+", "com.google.android.material:material:+",
			"androidx.documentfile:documentfile:+", "androidx.drawerlayout:drawerlayout:+",
			"androidx.exifinterface:exifinterface:+", "androidx.gridlayout:gridlayout:+",
			"androidx.heifwriter:heifwriter:+", "androidx.interpolator:interpolator:+", "androidx.leanback:leanback:+",
			"androidx.loader:loader:+", "androidx.localbroadcastmanager:localbroadcastmanager:+",
			"androidx.media2:media2:+", "androidx.media2:media2-exoplayer:+", "androidx.mediarouter:mediarouter:+",
			"androidx.multidex:multidex:+", "androidx.multidex:multidex-instrumentation:+",
			"androidx.palette:palette:+", "androidx.percentlayout:percentlayout:+",
			"androidx.leanback:leanback-preference:+", "androidx.legacy:legacy-preference-v14:+",
			"androidx.preference:preference:+", "androidx.print:print:+", "androidx.recommendation:recommendation:+",
			"androidx.recyclerview:recyclerview-selection:+", "androidx.recyclerview:recyclerview:+",
			"androidx.slice:slice-builders:+", "androidx.slice:slice-core:+", "androidx.slice:slice-view:+",
			"androidx.slidingpanelayout:slidingpanelayout:+", "androidx.annotation:annotation:+",
			"androidx.core:core:+", "androidx.contentpager:contentpager:+", "androidx.legacy:legacy-support-core-ui:+",
			"androidx.legacy:legacy-support-core-utils:+", "androidx.dynamicanimation:dynamicanimation:+",
			"androidx.emoji:emoji:+", "androidx.emoji:emoji-appcompat:+", "androidx.emoji:emoji-bundled:+",
			"androidx.fragment:fragment:+", "androidx.media:media:+", "androidx.tvprovider:tvprovider:+",
			"androidx.legacy:legacy-support-v13:+", "androidx.legacy:legacy-support-v4:+",
			"androidx.vectordrawable:vectordrawable:+", "androidx.swiperefreshlayout:swiperefreshlayout:+",
			"androidx.textclassifier:textclassifier:+", "androidx.transition:transition:+",
			"androidx.versionedparcelable:versionedparcelable:+", "androidx.viewpager:viewpager:+",
			"androidx.wear:wear:+", "androidx.webkit:webkit:+"});
	@Override
	public List<String> getAddToProjectAdvise(String string) {
		List<String> addToProjectAdvise = super.getAddToProjectAdvise(string);
		addToProjectAdvise.addAll(0, advises);
		return addToProjectAdvise;
	}

}

