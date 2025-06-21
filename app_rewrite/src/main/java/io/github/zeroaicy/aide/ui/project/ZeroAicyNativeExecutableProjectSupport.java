/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.ui.project;
import android.text.TextUtils;
import com.aide.engine.EngineSolution;
import com.aide.engine.EngineSolutionProject;
import com.aide.ui.project.NativeExecutableProjectSupport;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.utils.ZeroAicyBuildGradle;
import java.io.File;
import java.util.List;

public class ZeroAicyNativeExecutableProjectSupport extends NativeExecutableProjectSupport{
	
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
			if( !isSupport(projectPath )){
				continue;
			}
			String cmakeListsTxtPath = "cpp";
			engineSolutionProject.fY
				.add(new EngineSolution.File(projectPath + "/" + cmakeListsTxtPath, "C++", null, false, false));
		}
	}
	
	@Override
	public boolean isSupport(String projectPath) {
		//NDK C/C++本机可执行项目
		return isNativeExecutableCmakeProject(projectPath) || super.isSupport(projectPath);
	}

	// hasAndroidMk
	@Override
	public boolean vy(String projectPath) {
		
		if( FileSystem.isFileAndNotZip(projectPath + "/cpp/CMakeLists.txt")){
			return false;
		}
		return FileSystem.isFileAndNotZip(projectPath + "/jni/Android.mk");
	}
	
	
	public static boolean isNativeExecutableCmakeProject(String projectPath) {
		if (isAndroidProject(projectPath)) {
			return false;
		}
		if (GradleTools.isGradleProject(projectPath)) {
			return false;
		}
		return isCmakeProject(projectPath, false);
	}

	private static boolean isAndroidProject(String projectPath) {
		return new File(projectPath, "AndroidManifest.xml").exists();
	}


	public static boolean isCmakeProject(String projectPath) {
		return isCmakeProject(projectPath, GradleTools.isGradleProject(projectPath));
	}
	
    //判断是否是 cmake项目，指定CMakeLists.txt(计划支持)
    public static boolean isCmakeProject(String projectPath, boolean isGradle) {

        //检查 cmake及ndk安装情况
		File cMakeListsTxtFile = 
            new File(projectPath, 
                     isGradle ? 
                     "src/main/cpp/CMakeLists.txt" : 
                     "cpp/CMakeLists.txt");

        return cMakeListsTxtFile.isFile();
	}
}
