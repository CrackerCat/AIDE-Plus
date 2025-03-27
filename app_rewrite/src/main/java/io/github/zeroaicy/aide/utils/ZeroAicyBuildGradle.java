/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */
package io.github.zeroaicy.aide.utils;

import android.text.TextUtils;
import com.aide.common.AppLog;
import com.aide.engine.SyntaxError;
import com.aide.ui.ServiceContainer;
import com.aide.ui.services.AssetInstallationService;
import com.aide.ui.util.ArtifactNode;
import com.aide.ui.util.BuildGradle;
import com.aide.ui.util.FileSystem;
import groovyjarjarantlr.TokenStreamRecognitionException;
import groovyjarjarantlr.collections.AST;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.maven.model.Exclusion;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;

public class ZeroAicyBuildGradle extends BuildGradle {

	private static String TAG = "ZeroAicyBuildGradleTest";

	private static ZeroAicyBuildGradle singleton;
	/**
	 * 单例
	 */
	public static synchronized ZeroAicyBuildGradle getSingleton() {
		if (singleton == null) {
			singleton = new ZeroAicyBuildGradle(true);
			AppLog.d("ZeroAicyBuildGradleTest", "替换gradle解析器");
		}
		return singleton;
	}
	// xxx project(xxxxx)依赖
	private List<ProjectDependency> projectDependencys = new ArrayList<>();
	public List<ProjectDependency> getProjectDependencys() {
		return this.projectDependencys;
	}
	private List<DependencyExt> dependencyExts = new ArrayList<>();
	public List<DependencyExt> getDependencyExts() {
		return this.dependencyExts;
	}

	/**
	 * 混淆
	 */
	private boolean minifyEnabled;
	private boolean shrinkResources;
	private List<String> proguardFiles;

	public boolean isMinifyEnabled() {
		return this.minifyEnabled;
	}

	public boolean isShrinkResources() {
		return this.shrinkResources;
	}
	public List<String> getProguardFiles() {
		if (this.proguardFiles == null) {
			return Collections.emptyList();
		}
		return this.proguardFiles;
	}
	private final boolean isSingleton;
	public boolean isSingleton() {
		return this.isSingleton;
	}
	@Override
	public ZeroAicyBuildGradle getConfiguration(String path) {
		return (ZeroAicyBuildGradle) super.getConfiguration(path);
	}
	public ZeroAicyBuildGradle makeConfiguration(String path) {
		return new ZeroAicyBuildGradle(path);
	}

	private ZeroAicyBuildGradle(boolean isSingleton) {
		super();
		this.isSingleton = isSingleton;
		init();
	}

	PropertiesConfiguration gradlePropertiesConfiguration;

	public ZeroAicyBuildGradle(String path) {
		super();
		// getConfiguration在调用 makeConfiguration后会赋值
		// 导致解析时无法使用此变量, 赋值
		this.configurationPath = path;
		this.isSingleton = false;

		init();

		FileReader fileReader = null;
		UnicodeEscapingReader unicodeEscapingReader = null;
		try {

			File buildGradle = new File(this.configurationPath);
			if (!buildGradle.isFile()) {
				return;
			}

			File buildGradleParentFile = buildGradle.getParentFile();

			File gradlePropertiesParentFile = buildGradleParentFile.getParentFile();

			if (new File(gradlePropertiesParentFile, "settings.gradle").isFile()) {
				File gradlePropertiesFile = new File(gradlePropertiesParentFile, "gradle.properties");
				String gradlePropertiesFilePath = gradlePropertiesFile.getAbsolutePath();

				if (gradlePropertiesFile.isFile()) {
					this.gradlePropertiesConfiguration = PropertiesConfiguration.getSingleton()
							.getConfiguration(gradlePropertiesFilePath);
				}
			}

			fileReader = new FileReader(buildGradle);
			SourceBuffer sourceBuffer = new SourceBuffer();
			unicodeEscapingReader = new UnicodeEscapingReader(fileReader, sourceBuffer);

			GroovyLexer groovyLexer = new GroovyLexer(unicodeEscapingReader);
			unicodeEscapingReader.setLexer(groovyLexer);

			GroovyRecognizer groovyRecognizer = GroovyRecognizer.make(groovyLexer);
			groovyRecognizer.setSourceBuffer(sourceBuffer);

			groovyRecognizer.compilationUnit();

			fileReader.close();

			AST rootNode = groovyRecognizer.getAST();
			// 遍历
			for (AST ast = rootNode; ast != null; ast = getNextSibling(ast)) {
				// 深度优先遍历 Ast
				nw(ast, "");

				// ASTPrinter.printASTTree(ast);

			}
			String androidUseAndroidX = gradlePropertiesConfiguration.getProperty("android.useAndroidX");
			if ("true".equals(androidUseAndroidX)) {
				// 兼容 gradle.properties 中的 android.useAndroidX
				this.useAndroidx = true;
			}
			
			// 添加 viewBinding运行时库
			if (this.viewBindingEnabled) {
				String groupId;
				if (this.useAndroidx) {
					groupId = "androidx.databinding";
				} else {
					groupId = "com.android.databinding";
				}
				this.dependencies.add(new ArtifactNode(groupId, "viewbinding", "+"));
			}
			// 打印
			//Log.d(TAG, "signingConfigMap", signingConfigMap);
		} catch (FileNotFoundException e) {

		}

		catch (Exception e) {
			if (e instanceof TokenStreamRecognitionException) {
				TokenStreamRecognitionException tokenStreamRecognitionException = (TokenStreamRecognitionException) e;
				int line = tokenStreamRecognitionException.WB.jw;

				Map<String, List<SyntaxError>> hashMap = new HashMap<>();

				SyntaxError syntaxError = new SyntaxError();
				syntaxError.jw = line;
				syntaxError.fY = 1;
				syntaxError.qp = line;
				syntaxError.k2 = 1000;
				syntaxError.zh = tokenStreamRecognitionException.toString();

				hashMap.put(path, Collections.singletonList(syntaxError));
				ServiceContainer.getErrorService().EQ("Gradle", hashMap);
			}
			AppLog.e(TAG, e.getMessage(), e);
		} catch (Throwable e) {
			AppLog.d(TAG, e.getMessage(), e);
		} finally {
			IOUtils.close(unicodeEscapingReader);
			IOUtils.close(fileReader);
		}
	}

	private ZeroAicyProductFlavor defaultZeroAicyProductFlavor;
	private SortedMap<String, ZeroAicyProductFlavor> zeroAicyProductFlavorMap;

	private void init() {

		this.zeroAicyProductFlavorMap = new TreeMap<>();
		this.productFlavorMap = (SortedMap) this.zeroAicyProductFlavorMap;

		// ZeroAicyProductFlavor泛型问题
		this.defaultZeroAicyProductFlavor = makeProductFlavor();
		this.defaultConfigProductFlavor = this.defaultZeroAicyProductFlavor;

		this.curAndroidNodeLine = -1;
		this.curFlavorsNodeLine = -1;

		this.curProjectsRepositorys = new ArrayList<>();
		this.allProjectsRepositorys = new ArrayList<>();
		this.subProjectsRepositorys = new ArrayList<>();
		this.curDependenciesNodeLine = -1;
		this.dependencies = new ArrayList<>();
		this.subProjectsDependencies = new ArrayList<>();
		this.allProjectsDependencies = new ArrayList<>();
		this.signingConfigMap = new HashMap<>();
	}

	// 是否是 parentNodeName的子节点
	private boolean isChildNode(String nodeName, String parentNodeName) {
		return nodeName.startsWith(parentNodeName + ".");
	}

	private void FH(String str, String str2, int i) {
		try {
			ArrayList<String> arrayList = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.configurationPath));
			int i2 = 1;
			while (true) {
				String readLine = bufferedReader.readLine();
				if (readLine == null) {
					break;
				}
				arrayList.add(readLine);
				if (i2 == i) {
					arrayList.add(str);
				}
				i2++;
			}
			if (i < 0) {
				arrayList.add(str2 + " {");
				arrayList.add(str);
				arrayList.add("}");
			}
			bufferedReader.close();
			FileWriter fileWriter = new FileWriter(ZeroAicyBuildGradle.this.configurationPath);
			for (String str3 : arrayList) {
				fileWriter.write(str3);
				fileWriter.write("\n");
			}
			fileWriter.close();
		} catch (IOException e) {
			AppLog.e(e);
		}
	}

	private void Hw(String str) {
		FH("\t" + str, "dependencies", this.curDependenciesNodeLine);
	}

	private void parserRepositories(AST ast, String repositorieName, List<Repository> repositorys) {
		// google
		repositorys.add(new RemoteRepository(ast.getLine(), "https://dl.google.com/dl/android/maven2"));
		switch (repositorieName) {
			case "jcenter" :
				repositorys.add(new RemoteRepository(ast.getLine(), "https://jcenter.bintray.com"));
				break;
			case "mavenCentral" :
				repositorys.add(new RemoteRepository(ast.getLine(), "http://repo.maven.apache.org/maven2"));
				break;
			case "maven.url" :
				String url = getExprNodeValue(ast);
				if (TextUtils.isEmpty(url)) {
					break;
				}

				if (url.endsWith("/")) {
					int length = url.length() - 1;
					if (length < 1) {
						break;
					}
					// 规范化repositorieURL
					url = url.substring(0, length);
				}
				repositorys.add(new RemoteRepository(ast.getLine(), url));
				break;
			case "flatDir.dirs" :
				FlatLocalRepository flatLocalRepository = new FlatLocalRepository(ast.getLine());
				flatLocalRepository.flatDir = getExprNodeValue(ast);
				repositorys.add(flatLocalRepository);
				break;
			default :
				return;
		}
	}

	private String Mr(String str, int i) {

		String[] split = str.split("\\.");

		int length = split.length;
		if (length > i) {
			String str2 = "";
			for (int i2 = i; i2 < length; i2++) {
				if (str2.length() > 0) {
					str2 = str2 + ".";
				}
				str2 = str2 + split[i2];
			}

			return str2;
		}
		return null;

	}

	private String cmakeListsTxtPath;
	private String cmakeVersion;
	private String ndkVersion;
	private String cmakeCppFlags;
	private LinkedHashSet<String> cmakeAbiFilters;
	private LinkedHashSet<String> cmakeArguments;

	public String getNdkVersion() {
		return this.ndkVersion;
	}

	public String getNdkVersion(String defaultValue) {
		if (TextUtils.isEmpty(this.ndkVersion)) {
			return defaultValue;
		}
		return this.ndkVersion;
	}

	public String getCmakeListsTxtPath() {
		return cmakeListsTxtPath;
	}
	public String getCmakeListsTxtPath(String defaultValue) {
		if (TextUtils.isEmpty(this.cmakeListsTxtPath)) {
			return defaultValue;
		}
		return cmakeListsTxtPath;
	}

	public String getCmakeVersion() {
		return cmakeVersion;
	}

	public String getCmakeCppFlags() {
		return cmakeCppFlags;
	}

	private static LinkedHashSet<String> defaultCmakeAbiFilters;
	public LinkedHashSet<String> getCmakeAbiFilters() {

		if (this.cmakeAbiFilters == null || this.cmakeAbiFilters.isEmpty()) {
			// abiFilters不能为空

			if (ZeroAicyBuildGradle.defaultCmakeAbiFilters == null) {
				// 懒加载 初始化默认值
				LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
				linkedHashSet.add("arm64-v8a");

				ZeroAicyBuildGradle.defaultCmakeAbiFilters = linkedHashSet;
			}

			return ZeroAicyBuildGradle.defaultCmakeAbiFilters;
		}

		return this.cmakeAbiFilters;
	}

	public Set<String> getCmakeArguments() {
		return this.cmakeArguments;
	}

	private void parsereEternalNativeBuildCmake(AST ast, String attributeName) {
		switch (attributeName) {
			// CMakeLists.txt路径
			case "path" :
				this.cmakeListsTxtPath = getText(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));

				// AppLog.println_d("path -> %s", this.cmakeListsTxtPath);
				break;
			// cmake版本

			case "version" :

				this.cmakeVersion = getText(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));
				// AppLog.println_d("version -> %s", this.cmakeVersion);
				break;

			case "ndkVersion" :

				this.ndkVersion = getText(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));
				// AppLog.println_d("ndkVersion -> %s", this.ndkVersion);
				break;

			// 编译器参数
			case "cppFlags" :

				this.cmakeCppFlags = getText(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));

				// AppLog.println_d("cppFlags -> %s", this.cmakeCppFlags);
				break;

			case "abiFilters" : {

				AST elistNode = getNextSibling(getFirstChild(getFirstChild(ast)));
				AST firstValueNode = getFirstChild(elistNode);
				if (firstValueNode == null) {
					break;
				}
				if (this.cmakeAbiFilters == null) {
					this.cmakeAbiFilters = new LinkedHashSet<>();
				}
				this.cmakeAbiFilters.add(getText(firstValueNode));

				while ((firstValueNode = firstValueNode.getNextSibling()) != null) {
					this.cmakeAbiFilters.add(getText(firstValueNode));
				}
				// AppLog.println_d("abiFilters -> %s", this.cmakeAbiFilters);
			}
				break;

			case "arguments" :

				AST elistNode = getNextSibling(getFirstChild(getFirstChild(ast)));
				AST firstValueNode = getFirstChild(elistNode);
				if (firstValueNode == null) {
					break;
				}
				if (this.cmakeArguments == null) {
					this.cmakeArguments = new LinkedHashSet<>();
				}
				this.cmakeArguments.add(getText(firstValueNode));

				while ((firstValueNode = firstValueNode.getNextSibling()) != null) {
					this.cmakeArguments.add(getText(firstValueNode));
				}
				break;
		}
	}

	public void parserSourceSets(AST ast, String nodeName, int i) {
		String productFlavorName = getNodeSimpleNameAt(nodeName, i);

		ZeroAicyProductFlavor productFlavor;

		if (!"main".equals(productFlavorName)) {
			if (!this.productFlavorMap.containsKey(productFlavorName)) {
				this.productFlavorMap.put(productFlavorName, makeProductFlavor());
			}
			productFlavor = this.zeroAicyProductFlavorMap.get(productFlavorName);
		} else {
			productFlavor = this.defaultZeroAicyProductFlavor;
		}

		String attributeName = Mr(nodeName, i + 1);

		if (attributeName == null) {
			return;
		}

		parserProductFlavor(ast, attributeName, productFlavor);
	}

	private void parserProductFlavor(AST ast, String attributeName, ZeroAicyProductFlavor productFlavor) {
		switch (attributeName) {
			case "assets.srcDirs" :
				AST firstChild = getFirstChild(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));

				if (firstChild == null) {
					break;
				}

				if (productFlavor.assetsSrcDirs == null) {
					productFlavor.assetsSrcDirs = new LinkedHashSet<>();
				}

				LinkedHashSet<String> assetsSrcDirs = productFlavor.assetsSrcDirs;

				assetsSrcDirs.add(getText(getFirstChild(firstChild)));

				while ((firstChild = firstChild.getNextSibling()) != null) {
					assetsSrcDirs.add(getText(getFirstChild(firstChild)));
				}

				// AppLog.println_d("assetsSrcDirs -> %s", assetsSrcDirs);

				break;

			case "minSdk" :
			case "minSdkVersion" :
			case "minSdkVersion.apiLevel" :
				productFlavor.minSdkVersion = getExprNodeValue(ast);
				break;
			case "targetSdk" :
			case "targetSdkVersion" :
			case "targetSdkVersion.apiLevel" :
				productFlavor.targetSdkVersion = getExprNodeValue(ast);
				break;
			case "versionCode" :
				productFlavor.versionCode = getExprNodeValue(ast);
				break;
			case "versionName" :
				productFlavor.versionName = getExprNodeValue(ast);
				break;
			case "packageName" :
			case "namespace" :
			case "applicationId" :
				productFlavor.applicationId = getExprNodeValue(ast);
				break;
			case "multiDexEnabled" :
				productFlavor.multiDexEnabled = getExprNodeValue(ast);
				break;
			case "dependencies" :
				/**
				 * 为渠道包提供 dependencies块支持
				 */
				List<BuildGradle.Dependency> productFlavorDependencies = ((ZeroAicyProductFlavor) productFlavor).productFlavorDependencies;
				for (AST dependencieChildAst : getExprNodes(ast)) {
					String dependencieChildAstName = getExprNodeName(dependencieChildAst);
					ei(dependencieChildAst, dependencieChildAstName, productFlavorDependencies);
				}

				break;
		}

	}

	private void SI(AST ast, String str, int i) {
		String productFlavorName = getNodeSimpleNameAt(str, i);

		if (!this.zeroAicyProductFlavorMap.containsKey(productFlavorName)) {
			this.zeroAicyProductFlavorMap.put(productFlavorName, makeProductFlavor());
		}

		String attributeName = Mr(str, i + 1);
		if (attributeName != null) {
			parserProductFlavor(ast, attributeName, this.zeroAicyProductFlavorMap.get(productFlavorName));
		}
	}

	private static AST getFirstChild(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getFirstChild();

	}

	private static AST getNextSibling(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getNextSibling();

	}

	private void cn(AST ast, String str, SigningConfig signingConfig) {
		switch (str) {
			case "storePassword" :
				signingConfig.storePassword = getExprNodeValue(ast);
				break;
			case "keyPassword" :
				signingConfig.keyPassword = getExprNodeValue(ast);
				break;
			case "keyAlias" :
				signingConfig.keyAlias = getExprNodeValue(ast);
				break;
			case "storeFile" :
				signingConfig.storeFilePath = getExprNodeValue(ast, "file");
				break;
		}
	}

	public static int getDependencyExtType(String type) {
		switch (type) {
			case "compileOnly" :
				return DependencyExt.CompileOnly;
			// 仅打包 看看能不能
			// 不在编译列表，仅在打包列表中
			case "runtimeOnly" :
				return DependencyExt.RuntimeOnly;

			case "natives" :
			case "libgdxNatives" :
				return DependencyExt.LibgdxNatives;
			default :
				return -1;
		}
	}
	private void ei(AST ast, String str, List<Dependency> dependencieList) {
		switch (str) {
			// 忽略 coreLibraryDesugaring 以后支持
			case "coreLibraryDesugaring" :
				return;

			case "testCompile" :
			case "androidTestCompile" :
				dependencieList.add(new k(ast.getLine()));
				break;
			case "wearApp" :
				Map<String, String> we;
				String projectValue = getExprNodeValue(ast, "project");
				if (projectValue == null && (we = we(ast, "project")) != null && we.containsKey("path")) {
					projectValue = we.get("path");
				}
				if (projectValue != null) {
					ProjectDependency projectDependency = new ProjectDependency(ast.getLine());
					projectDependency.projectName = projectValue;
					this.wearAppProject = projectDependency;
					return;
				}
				dependencieList.add(new l(ast.getLine()));

				break;
			// 仅用于标记依赖
			// 仅加入编译列表
			// 不加入打包列表
			case "compileOnly" :
				// 仅打包 看看能不能
				// 不在编译列表，仅在打包列表中
			case "runtimeOnly" :

				// libgdx natives
			case "libgdxNatives" :
				// 别名
			case "natives" :

			case "implementation" :
			case "api" :
			case "compile" :
				int dependencyExtType = getDependencyExtType(str); {
				{
					// xxx project(:"xx");
					String projectPath = getExprNodeValue(ast, "project");

					if (projectPath != null) {
						ProjectDependency projectDependency = new ProjectDependency(ast.getLine());
						projectDependency.projectName = projectPath;

						dependencieList.add(projectDependency);
						// 添加项目依赖
						this.projectDependencys.add(projectDependency);
						return;
					}
				}

				{
					// xxx files("xx");
					String getFilesValue = getExprNodeValue(ast, "files");

					if (getFilesValue != null) {
						FilesDependency filesDependency = new FilesDependency(ast.getLine());
						filesDependency.filesPath = getFilesValue;

						if (!DependencyExt.isRuntimeOnly(dependencyExtType)) {
							// runtimeOnly files 依赖会在打包服务进程自动添加
							// 在此处拦截可以使得编译器不知道这个依赖
							dependencieList.add(filesDependency);
						}

						if (DependencyExt.isExt(dependencyExtType)) {
							this.dependencyExts.add(new DependencyExt(dependencyExtType, filesDependency));
						}

						return;
					}
				}

				{
					// xxx fileTree(dir: "xx");
					Map<String, String> getFileTree = we(ast, "fileTree");
					if (getFileTree != null) {
						FileTreeDependency fileTreeDependency = new FileTreeDependency(ast.getLine());
						//getFileTree.get("include");
						fileTreeDependency.dirPath = getFileTree.get("dir");
						dependencieList.add(fileTreeDependency);

						if (DependencyExt.isExt(dependencyExtType)) {
							this.dependencyExts.add(new DependencyExt(dependencyExtType, fileTreeDependency));
						}

						return;
					}
				}

				{
					// xxx groupId:artifactId:version:classifier@extension
					ArtifactNode artifactNode = parserMavenDependency(ast);
					// AppLog.e("ZeroAicyBuildGradleTest", String.valueOf(artifactNode));

					if (artifactNode != null) {
						if (artifactNode.getVersion() == null) {
							// AppLog.e("ZeroAicyBuildGradleTest" , "没有版本 " + artifactNode);
							artifactNode.setVersion("+");
						}

						dependencieList.add(artifactNode);
						if (DependencyExt.isExt(dependencyExtType)) {
							this.dependencyExts.add(new DependencyExt(dependencyExtType, artifactNode));
						}
						return;
					}
				}

				dependencieList.add(new l(ast.getLine()));
			}

				break;

			default :
				AppLog.println_d(TAG, "Unknown dependency ", str);
				dependencieList.add(new l(ast.getLine()));
				break;
		}

	}
	public ArtifactNode parserMavenDependency(AST ast) {

		String coords = getExprNodeValue(ast);
		if (coords == null) {
			return null;
		}

		// ELIST -> STRING_CONSTRUCTOR
		// 处理STRING_CONSTRUCTOR
		if ("STRING_CONSTRUCTOR".equals(coords)) {
			// groupId:artifactId:version@extension
			// {group}:{name}:{version}[{:classifier}@{extension}]

			// groupId:artifactId
			// STRING_CONSTRUCTOR
			// :classifier
			AST stringConstructorAst = getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast))));
			if (getType(stringConstructorAst) == EXPR) {
				stringConstructorAst = getFirstChild(stringConstructorAst);
			}
			if (stringConstructorAst == null) {
				return null;
			}

			// 'groupId:artifactId:'
			AST gaAst = getFirstChild(stringConstructorAst);

			String gaAstText = getText(gaAst);

			String[] coordsArray = gaAstText.split(":");

			if (coordsArray.length < 2) {
				return null;
			}

			String groupId = coordsArray[0];
			String artifactId = coordsArray[1];
			// 默认空值
			String version = "+";

			AST versionAst = getNextSibling(gaAst);
			String versionAstText = getText(versionAst);

			if (this.gradlePropertiesConfiguration != null) {
				version = this.gradlePropertiesConfiguration.getProperty(versionAstText, "+");
			}

			String extendAstText = getText(getNextSibling(versionAst));

			ArtifactNode artifactNode = new ArtifactNode(ast.getLine(), groupId, artifactId, version);

			List<Exclusion> exclusions = parserExclusions(ast);
			// 设置排除选项
			artifactNode.setExclusions(exclusions);

			coords = groupId + ":" + artifactId + ":" + version;

			if (TextUtils.isEmpty(extendAstText)) {
				// 没啥用
				artifactNode.coords = coords;
				return artifactNode;
			}

			if (extendAstText.startsWith("@")) {
				coords += extendAstText;
				artifactNode.packaging = extendAstText.substring(1);
				return artifactNode;
			}

			int classifierEnd = extendAstText.lastIndexOf('@');
			classifierEnd = classifierEnd > 0 ? classifierEnd : extendAstText.length();

			if (classifierEnd != extendAstText.length()) {
				artifactNode.packaging = extendAstText.substring(classifierEnd + 1);
			}

			artifactNode.classifier = extendAstText.substring(1, classifierEnd);

			return artifactNode;
		}

		// ELIST -> EXPR 的 值
		String[] coordsArray = coords.split(":");

		if (coordsArray.length < 2) {
			// 这样可能会导致
			// 显示依赖aar时必须要有groupId
			return null;
		}

		String groupId = coordsArray[0];
		String artifactId = coordsArray[1];
		// 默认空值
		String version = "+";

		ArtifactNode artifactNode = new ArtifactNode(ast.getLine(), groupId, artifactId, version);

		List<Exclusion> exclusions = parserExclusions(ast);
		// 设置排除选项
		artifactNode.setExclusions(exclusions);

		// 没啥用
		artifactNode.coords = coords;

		// 没有 version
		if (coordsArray.length < 3) {
			return artifactNode;
		}
		// groupId:artifactId:version@extension
		// {group}:{name}:{version}[{:classifier}@{extension}]

		version = resolvingVarValue(coordsArray[2]);

		if (version == null)
			version = "";

		// has extension ？
		int extensionEnd = version.indexOf("@");
		if (extensionEnd >= 0) {
			artifactNode.setVersion(version.substring(0, extensionEnd));
			artifactNode.packaging = version.substring(extensionEnd + 1);
			return artifactNode;
		}

		// version没有包含 extension
		artifactNode.setVersion(version);

		// 没有 classifier
		if (coordsArray.length < 4) {
			return artifactNode;
		}
		// has classifier
		// classifier是否包含 extension

		String classifier = coordsArray[3];

		int classifierEnd = classifier.indexOf("@");

		if (extensionEnd >= 0) {
			artifactNode.classifier = classifier.substring(0, classifierEnd);
			artifactNode.packaging = classifier.substring(classifierEnd + 1);
			return artifactNode;
		}
		// classifier没有有包含 extension
		artifactNode.classifier = classifier;

		return artifactNode;
	}

	private List<Exclusion> parserExclusions(AST ast) {
		// 处理 exclude
		AST method_call_node = getFirstChild(ast);
		// 依赖类型
		AST api_node = getFirstChild(method_call_node);
		AST elist_node = getNextSibling(api_node);
		AST exclude_boy = getNextSibling(elist_node);

		List<Exclusion> exclusions = new ArrayList<>();

		for (AST exclude_expr_node = getNextSibling(
				getFirstChild(exclude_boy)); exclude_expr_node != null; exclude_expr_node = getNextSibling(
						exclude_expr_node)) {
			String exprNodeName = getExprNodeName(exclude_expr_node);

			if (!"exclude".equals(exprNodeName)) {
				continue;
			}
			AST exclude_elist_node = getNextSibling(getFirstChild(getFirstChild(exclude_expr_node)));

			Exclusion exclusion = new Exclusion();

			boolean hasSetting = false;
			// 遍历 labeled_arg_node
			for (AST labeled_arg_node = getFirstChild(
					exclude_elist_node); labeled_arg_node != null; labeled_arg_node = getNextSibling(
							labeled_arg_node)) {
				// group group_value
				// group | module
				AST type_node = getFirstChild(labeled_arg_node);

				AST type_value_node = getNextSibling(type_node);

				String type_node_text = getText(type_node);
				String type_value_node_text = getText(type_value_node);

				if ("group".equals(type_node_text)) {
					exclusion.setGroupId(type_value_node_text);
					hasSetting = true;
				} else if ("module".equals(type_node_text)) {
					exclusion.setArtifactId(type_value_node_text);
					hasSetting = true;
				}
			}

			if (hasSetting) {
				exclusions.add(exclusion);
			}
		}
		return exclusions;
	}

	// private Map<String, String> varValueMap = new HashMap<>();

	// 版本信息 也可能是 变量引用 $varName || ${varName}
	// 如果是变量引用则解析变量
	private String resolvingVarValue(String version) {

		if (version == null) {
			return "+";
		}
		return version;
	}

	private String getNodeSimpleNameAt(String str, int index) {
		String[] split = str.split("\\.");
		if (split.length > index) {
			return split[index];
		}
		return null;

	}

	private static String getText(AST ast) {
		if (ast == null) {
			return null;
		}
		return ast.getText();

	}

	private void nw(AST ast, String parentNodeName) {
		String nodeName = getExprNodeName(ast);
		if (nodeName == null) {
			return;
		}

		if (parentNodeName.length() != 0) {
			nodeName = parentNodeName + "." + nodeName;
		}

		//System.out.printf("解析%s\n", nodeName);

		switch (nodeName) {
			case "android" :
			case "model.android" :
				this.curAndroidNodeLine = getLine(ast);
				break;

			case "android.productFlavors" :
			case "model.android.productFlavors" :
				this.curFlavorsNodeLine = getLine(ast);
				break;
			case "dependencies" :
				this.curDependenciesNodeLine = getLine(ast);
				break;
			case "android.compileSdkVersion" :
			case "model.android.compileSdkVersion" :
				// getExprNodeValue(ast);
				break;
			case "android.ndkVersion" :
				// ndkVersion
				parsereEternalNativeBuildCmake(ast, "ndkVersion");
				break;
			default :

				if (isChildNode(nodeName, "android.defaultConfig.ndk")) {
					parsereEternalNativeBuildCmake(ast, Mr(nodeName, 3));
					break;
				}

				if (isChildNode(nodeName, "android.externalNativeBuild.cmake")) {
					parsereEternalNativeBuildCmake(ast, Mr(nodeName, 3));
					break;
				}
				if (isChildNode(nodeName, "android.defaultConfig.externalNativeBuild.cmake")) {
					parsereEternalNativeBuildCmake(ast, Mr(nodeName, 4));
					break;
				}

				if (isChildNode(nodeName, "android.sourceSets")) {
					parserSourceSets(ast, nodeName, 2);
					break;
				}

				if (isChildNode(nodeName, "android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 2), this.defaultZeroAicyProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig")) {
					parserProductFlavor(ast, Mr(nodeName, 3), this.defaultZeroAicyProductFlavor);
					break;
				} else if (isChildNode(nodeName, "model.android.defaultConfig.with")) {
					parserProductFlavor(ast, Mr(nodeName, 4), this.defaultZeroAicyProductFlavor);
					break;
				} else if (isChildNode(nodeName, "android.productFlavors")) {
					SI(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "model.android.productFlavors")) {
					SI(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "android.signingConfigs")) {
					ro(ast, nodeName, 2);
					break;
				} else if (isChildNode(nodeName, "android.buildFeatures")) {
					parserBuildFeatures(nodeName, ast);
					break;
				} else if (isChildNode(nodeName, "android.buildTypes.release")) {
					parserBuildTypesRelease(nodeName, ast);
					break;
				} else if (isChildNode(nodeName, "android.compileOptions")) {
					parserCompileOptions(nodeName, ast);
					break;
				} else if (isChildNode(nodeName, "model.android.signingConfigs")) {
					ro(ast, nodeName, 3);
					break;
				} else if (isChildNode(nodeName, "dependencies")) {
					ei(ast, Mr(nodeName, 1), this.dependencies);
					break;
				} else if (isChildNode(nodeName, "subprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.subProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "allprojects.dependencies")) {
					ei(ast, Mr(nodeName, 2), this.allProjectsDependencies);
					break;
				} else if (isChildNode(nodeName, "repositories")) {
					parserRepositories(ast, Mr(nodeName, 1), this.curProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "subprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.subProjectsRepositorys);
					break;
				} else if (isChildNode(nodeName, "allprojects.repositories")) {
					parserRepositories(ast, Mr(nodeName, 2), this.allProjectsRepositorys);
					break;
				}
				break;
		}

		for (AST ast2 : getExprNodes(ast)) {
			nw(ast2, nodeName);
		}
	}

	boolean viewBindingEnabled = false;
	boolean useAndroidx = true;

	public boolean isViewBindingEnabled() {
		return this.viewBindingEnabled;
	}
	public boolean isUseAndroidx() {
		return this.useAndroidx;
	}

	String sourceCompatibility;
	public String getSourceCompatibility() {
		return this.sourceCompatibility;
	}

	String targetCompatibility;
	public String getTargetCompatibility() {
		return this.targetCompatibility;
	}

	// 递归方法，用于打印当前节点及其所有子节点  
	public static void printTree(AST node, String indent) {
		if (node == null) {
			return;
		}

		AppLog.println_d("%stext: %s type: %s", indent, getText(node), getType(node));
		// System.out.printf("%stext: %s type: %s\n", indent, getText(node), getType(node));

		// 打印当前节点的文本  

		// 获取当前节点的第一个子节点，并递归打印  
		AST child = node.getFirstChild();
		while (child != null) {
			printTree(child, indent + "\t");
			// 移动到下一个兄弟节点  
			child = child.getNextSibling();
		}
	}

	// EXPR 28
	private void parserBuildFeatures(String nodeName, AST ast) {
		// printTree(ast, "");
		// android.buildFeatures 所以自己是2 从0开始
		String nodeSimpleName = getNodeSimpleNameAt(nodeName, 2);

		/**
		 * getFirstChild(getNextSibling(getFirstChild(getFirstChild())))
		 *EXPR{
		 *----<command>{
		 *--------viewBinding
		 *--------ELIST{
		 *------------true
		 *--------}
		 *----}
		 *}
		 text: EXPR type: 28
		 text: <command> type: 27
		 text: viewBinding type: 87
		 text: ELIST type: 33
		 text: false type: 157
		 */
		/*
		 *EXPR{
		 *	  ={
		 *	 	viewBinding
		 *		true
		 *	}
		 *}
		 */
		String astValue =
				// getText(getNextSibling(getFirstChild(getFirstChild(ast))));
				getText(getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast)))));

		if ("viewBinding".equals(nodeSimpleName)) {
			this.viewBindingEnabled = "true".equals(astValue);
		}

		if ("useAndroidx".equals(nodeSimpleName)) {
			// 这样才是默认true
			this.useAndroidx = !"false".equals(astValue);
		}
	}

	private void parserBuildTypesRelease(String nodeName, AST ast) {

		String nodeSimpleName = getNodeSimpleNameAt(nodeName, 3);
		if ("minifyEnabled".equals(nodeSimpleName)) {
			this.minifyEnabled = "true".equals(getExprNodeValue(ast));
			return;
		}

		if ("shrinkResources".equals(nodeSimpleName)) {
			this.shrinkResources = "true".equals(getExprNodeValue(ast));
			return;
		}
		if (this.proguardFiles == null && "proguardFiles".equals(nodeSimpleName)) {

			AST nextSibling = getNextSibling(getFirstChild(getFirstChild(ast)));
			if (getType(nextSibling) != 33) {
				return;
			}

			List<String> proguardFiles = new ArrayList<>();

			for (AST firstChild1 = getFirstChild(nextSibling); firstChild1 != null; firstChild1 = getNextSibling(
					firstChild1)) {
				if (getType(firstChild1) == 88) {
					String proguardFilePath = FileSystem.resolveFilePath(FileSystem.getParent(this.configurationPath),
							getText(firstChild1));
					proguardFiles.add(proguardFilePath);
					continue;
				}
				if (getType(firstChild1) == 27) {
					AST firstChild2 = getFirstChild(firstChild1);

					if (getType(firstChild2) == 87 && "getDefaultProguardFile".equals(getText(firstChild2))) {

						AST firstChild4 = getFirstChild(getFirstChild(getNextSibling(firstChild2)));
						String defaultProguardFile = getDefaultProguardFile(getText(firstChild4));
						proguardFiles.add(defaultProguardFile);
					}
				}
			}
			if (!proguardFiles.isEmpty()) {
				this.proguardFiles = proguardFiles;
			}
		}
	}

	private void parserCompileOptions(String nodeName, AST ast) {

		// android.compileOptions 所以自己是2 从0开始
		String nodeSimpleName = getNodeSimpleNameAt(nodeName, 2);
		if (!"sourceCompatibility".equals(nodeSimpleName) && !"targetCompatibility".equals(nodeSimpleName)) {
			return;
		}
		/**
		 * compileOptions {
		 *    sourceCompatibility JavaVersion.VERSION_11
		 *    targetCompatibility JavaVersion.VERSION_11
		 * }
		 */
		AST compatibilityValueAst = getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast))));
		String versionValue = getText(compatibilityValueAst);

		if (".".equals(versionValue)) {

			// JavaVersion
			AST javaVersionAst = getFirstChild(compatibilityValueAst);
			// VERSION_
			AST versionAst = getNextSibling(javaVersionAst);
			versionValue = getText(versionAst);

			if (versionValue == null) {
				printTree(compatibilityValueAst, "");
				printTree(javaVersionAst, "");
			}
		}

		if ("sourceCompatibility".equals(nodeSimpleName)) {
			if (versionValue != null && versionValue.startsWith("VERSION_")) {
				versionValue = versionValue.substring("VERSION_".length());
			}
			this.sourceCompatibility = versionValue;
		}

		if ("targetCompatibility".equals(nodeSimpleName)) {
			if (versionValue.startsWith("VERSION_")) {
				versionValue = versionValue.substring("VERSION_".length());
			}
			// 这样才是默认true
			this.targetCompatibility = versionValue;
		}
	}

	private static Set<String> proguards = new HashSet<>();
	static {
		proguards.add("proguard-android.txt");
		proguards.add("proguard-android-optimize.txt");
		proguards.add("proguard-defaults.txt");
	}

	private String getDefaultProguardFile(String proguardFileName) {
		if (ServiceContainer.getContext() == null) {
			ServiceContainer.setContext(ContextUtil.getContext());
		}
		if (proguards.contains(proguardFileName)) {
			return AssetInstallationService.DW(proguardFileName, true);
		}
		return null;
	}

	private static int getType(AST ast) {
		if (ast == null) {
			return 0;
		}
		return ast.getType();

	}

	private void ro(AST ast, String str, int i) {
		String signingConfigName = getNodeSimpleNameAt(str, i);
		if (!this.signingConfigMap.containsKey(signingConfigName)) {
			this.signingConfigMap.put(signingConfigName, new SigningConfig());
		}
		String Mr2 = Mr(str, i + 1);
		if (Mr2 != null) {
			cn(ast, Mr2, this.signingConfigMap.get(signingConfigName));
		}

	}

	private int getLine(AST ast) {
		/*
		 *87 = XL
		 ****(ast) 大概率是expr
		 **** =[124] || <command>[127]
		 *		****(87)
		 *		****(XL)
		 */
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		return XL == null ? ast.getLine() : XL.getLine();

	}

	private List<AST> getExprNodes(AST ast) {
		ArrayList<AST> arrayList = new ArrayList<>();
		/*
		  |
		  |
		 | |
		   | ( firstChild )
		*/

		AST firstChild = getFirstChild(getNextSibling(getFirstChild(getFirstChild(ast))));

		for (AST node = firstChild; node != null; node = getNextSibling(node)) {
			// EXPR 28
			if (getType(node) == EXPR) {
				arrayList.add(node);
			}
		}
		return arrayList;

	}

	private Map<String, String> we(AST ast, String str) {
		AST XL = getNextSibling(getFirstChild(getFirstChild(ast)));
		if (getType(XL) == 33 && getType(getFirstChild(XL)) == 27
				&& str.equals(getText(getFirstChild(getFirstChild(XL))))) {
			HashMap<String, String> hashMap = new HashMap<>();
			for (AST Ws = getFirstChild(
					getNextSibling(getFirstChild(getFirstChild(XL)))); Ws != null; Ws = getNextSibling(Ws)) {
				if (getType(Ws) == 54) {
					String lg = getText(getFirstChild(Ws));
					AST Ws2 = getFirstChild(getNextSibling(getFirstChild(Ws)));
					if (getType(Ws2) == 57) {
						hashMap.put(lg, getText(getFirstChild(getFirstChild(getFirstChild(Ws2)))));
					} else {
						hashMap.put(lg, getText(Ws2));
					}
				}
			}
			return hashMap;
		}
		return null;

	}

	public void addProductFlavor(String str) {
		String str2 = "\t\t" + str + " {\n\t\t}";
		if (this.curFlavorsNodeLine != -1) {
			FH(str2, "", this.curFlavorsNodeLine);
			return;
		}
		FH("\tproductFlavors {\n" + str2 + "\n\t}\n", "android", this.curAndroidNodeLine);

	}

	public void addMavenDependency(String str) {
		Hw("api '" + str + "'");
	}

	public void addProjectDependency(String subProjectDirPath) {

		String currentProjectDirPath = FileSystem.getParent(this.configurationPath);
		// 计算相对路径
		String currentProjectParentDir = FileSystem.getParent(currentProjectDirPath);

		Path currentProjectParentPath = Paths.get(currentProjectParentDir);
		Path subProjectPath = Paths.get(subProjectDirPath);

		// String relativePath = FileSystem.getRelativePath(parentDir, subProjectPath);
		String relativePath = currentProjectParentPath.relativize(subProjectPath).toString();

		Hw("api project('" + (":" + relativePath) + "')");

	}

	@Override
	public String getFlavorApplicationId(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).applicationId != null) {
			return this.productFlavorMap.get(productFlavorName).applicationId;
		}
		return this.defaultConfigProductFlavor.applicationId;
	}

	public LinkedHashSet<String> getDefaultAssetsSrcDirs() {
		return getFlavorAssetsSrcDirs("main");
	}
	public LinkedHashSet<String> getFlavorAssetsSrcDirs(String productFlavorName) {
		ZeroAicyProductFlavor productFlavor;

		if (productFlavorName == null) {
			return null;
		}
		if (!"main".equals(productFlavorName)) {
			productFlavor = this.zeroAicyProductFlavorMap.get(productFlavorName);
		} else {
			productFlavor = this.defaultZeroAicyProductFlavor;
		}

		if (productFlavor != null) {
			return productFlavor.assetsSrcDirs;
		}

		return null;
	}

	public List<BuildGradle.Dependency> getFlavorDependencies(String productFlavorName) {
		ZeroAicyProductFlavor productFlavor;

		if (productFlavorName == null) {
			return null;
		}
		productFlavor = this.zeroAicyProductFlavorMap.get(productFlavorName);

		if (productFlavor != null) {
			return productFlavor.productFlavorDependencies;
		}
		return Collections.emptyList();

	}

	@Override
	public String getMinSdkVersion(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).minSdkVersion != null) {
			return this.productFlavorMap.get(productFlavorName).minSdkVersion;
		}
		return this.defaultConfigProductFlavor.minSdkVersion;
	}

	@Override
	public String getTargetSdkVersion(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).targetSdkVersion != null) {
			return this.productFlavorMap.get(productFlavorName).targetSdkVersion;
		}
		return this.defaultConfigProductFlavor.targetSdkVersion;

	}

	@Override
	public String getVersionCode(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).versionCode != null) {
			return this.productFlavorMap.get(productFlavorName).versionCode;
		}
		return this.defaultConfigProductFlavor.versionCode;

	}

	@Override
	public String getVersionName(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).versionName != null) {
			return this.productFlavorMap.get(productFlavorName).versionName;
		}
		return this.defaultConfigProductFlavor.versionName;
	}

	@Override
	public SigningConfig getSigningConfig(String signingConfigName) {
		if (signingConfigName == null) {
			return null;
		}
		return this.signingConfigMap.get(signingConfigName);
	}

	@Override
	public boolean isMultiDexEnabled(String productFlavorName) {
		if (productFlavorName != null && this.productFlavorMap.containsKey(productFlavorName)
				&& this.productFlavorMap.get(productFlavorName).multiDexEnabled != null) {
			return "true".equals(this.productFlavorMap.get(productFlavorName).multiDexEnabled);
		}
		return "true".equals(this.defaultConfigProductFlavor.multiDexEnabled);
	}

	// groovy ast api及其常量
	private static int VARIABLE_DEF = 9;
	private static int TYPE = 12;

	/**
	* EXPR
	* 	<command>
	* ELIST
	* 	(
	*/
	// <command> 或者 (
	public static final int METHOD_CALL = 27;

	public static final int EXPR = 28;
	public static final int ELIST = 33;

	// 通配符
	public static final int WILDCARD = 50;
	// 
	public static final int LITERAL_options = 51;

	public static final int LABELED_ARG = 54;
	public static final int DIGIT = 57;

	public static final int TYPE_PARAMETERS = 72;

	public static final int CLOSURE_LIST = 77;

	public static final int IDENT = 87;
	public static final int STRING_LITERAL = 88;
	public static final int DOT = 90;
	public static final int ASSIGN = 124;

	public static final int LITERAL_false = 157;
	public static final int NUM_INT = 199;

	/**
	* 
	 * api project(':xxxx')
	 * wearApp project(':xxxx')
	 * storeFile file("app-debug.jks")
	 * callMethodName 调用方法的名称
	 * 返回调用方法的参数 xxx
	 */
	private String getExprNodeValue(AST ast, String callMethodName) {
		AST elistNode = getNextSibling(getFirstChild(getFirstChild(ast)));

		/**				EXPR(28)
		 *				↓
		 * 			METHOD_CALL(27)
		 *				↙				↘
		 *		IDENT (87) [api]			ELIST (var node)
		 * 									↓
		 * 								METHOD_CALL
		 * 								↙			↘
		 *  			IDENT ( 对比是否是 nodeName)	ELIST
		 *												↓
		 * 												EXPR
		 * 
		 */

		if (getType(elistNode) != ELIST || getType(getFirstChild(elistNode)) != METHOD_CALL) {
			return null;
		}
		AST identNode = getFirstChild(getFirstChild(elistNode));
		if (!callMethodName.equals(getText(identNode))) {
			return null;
		}

		AST exprNode = getFirstChild(getNextSibling(identNode));

		if (getType(exprNode) != EXPR) {
			return null;
		}

		String exprNodeValue = getText(getFirstChild(exprNode));
		return exprNodeValue;
	}

	/**
	 * 如果表达式是一个字符串字面量或整数字面量，直接返回值。
	 * 如果表达式是一个列表（ELIST），从中提取第一个值。
	 * 如果无法解析值（例如类型不匹配），返回 null。
	* 
	*/
	private String getExprNodeValue(AST exprNode) {

		AST exprValueNode = getNextSibling(getFirstChild(getFirstChild(exprNode)));

		int type = getType(exprValueNode);

		if (type == STRING_LITERAL || type == NUM_INT) {
			return getText(exprValueNode);
		}

		if (type != ELIST) {
			return null;
		}

		AST elistFirstChildNode = getFirstChild(exprValueNode);
		if (getType(elistFirstChildNode) == EXPR) {
			return getText(getFirstChild(elistFirstChildNode));
		}

		return getText(elistFirstChildNode);
	}

	/**
	 * 计算 METHOD_CALL 或 ASSIGN 表达式 的名称
	 */
	private String getExprNodeName(AST ast) {

		if (getType(ast) != EXPR) {
			return null;
		}

		int type = getType(getFirstChild(ast));
		if (type == METHOD_CALL || type == ASSIGN) {
			return aM(getFirstChild(getFirstChild(ast)));
		}
		return null;

	}

	private String aM(AST ast) {

		if (getType(ast) != DOT) {
			// 不是点
			return getText(ast);
		}

		AST firstChildNode = getFirstChild(ast);
		AST secondChildNode = getNextSibling(firstChildNode);

		// 把第一 第二兄弟几点 拼接起来
		return aM(firstChildNode) + "." + getText(secondChildNode);

	}

	/**
	 * new ProductFlavor统一创建点，方便统一替换实例
	 */
	private ZeroAicyProductFlavor makeProductFlavor() {
		return new ZeroAicyProductFlavor();
	}

	public class ZeroAicyProductFlavor extends ProductFlavor {

		public final List<BuildGradle.Dependency> productFlavorDependencies = new ArrayList<>();

		public LinkedHashSet<String> assetsSrcDirs;
	}

	/**
	 * 依赖类型: 
	 *     仅编译: 作为依赖，但不dexing与打包]
	 *     仅打包: 不参与dexing]
	 *     libgdx natives(别名: natives) 打包libgdx的 native库依赖，
	 * 
	 */
	public static class DependencyExt extends com.aide.ui.util.BuildGradle.Dependency {
		public static final int CompileOnly = 0x1;
		public static final int RuntimeOnly = 0x2;
		public static final int LibgdxNatives = 0x3;

		public final int type;
		public final BuildGradle.Dependency dependency;
		public DependencyExt(int type, BuildGradle.Dependency dependency) {
			super(dependency.line);
			this.type = type;
			this.dependency = dependency;
		}

		public boolean isCompileOnly() {
			return this.type == CompileOnly;
		}
		public boolean isRuntimeOnly() {
			return this.type == RuntimeOnly;
		}
		public boolean isLibgdxNatives() {
			return this.type == LibgdxNatives;
		}
		public static boolean isCompileOnly(int type) {
			return type == CompileOnly;
		}
		public static boolean isRuntimeOnly(int type) {
			return type == RuntimeOnly;
		}
		public static boolean isLibgdxNatives(int type) {
			return type == LibgdxNatives;
		}
		public static boolean isExt(int type) {
			return type == CompileOnly || type == RuntimeOnly || type == LibgdxNatives;
		}

	}
}

