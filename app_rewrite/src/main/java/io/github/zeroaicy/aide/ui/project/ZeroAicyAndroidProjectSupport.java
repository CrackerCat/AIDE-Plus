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
					.add(new EngineSolution.File(projectPath + "/" + cmakeListsTxtPath, "C++", null, false, false));
		}
	}

	// has AndroidMk
	@Override
	public boolean vy(String string) {
		if (GradleTools.isAndroidGradleProject(string) && !isCmakeGradleProject(string )) {
			return FileSystem.isFileAndNotZip(string + "src/main/jni/Android.mk");
		}
		return super.vy(string);
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

		return true;
	}

}

