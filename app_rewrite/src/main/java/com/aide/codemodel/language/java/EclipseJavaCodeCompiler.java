package com.aide.codemodel.language.java;

import com.aide.codemodel.api.ErrorTable;
import com.aide.codemodel.api.FileEntry;
import com.aide.codemodel.api.FileSpace;
import com.aide.codemodel.api.Model;
import com.aide.codemodel.api.SyntaxTree;
import com.aide.codemodel.api.abstraction.CodeCompiler;
import com.aide.codemodel.api.abstraction.CodeModel;
import com.aide.codemodel.api.abstraction.Language;
import com.aide.codemodel.api.collections.FunctionOfIntInt;
import com.aide.codemodel.api.collections.OrderedMapOfIntInt;
import com.aide.codemodel.api.collections.SetOfFileEntry;
import io.github.zeroaicy.util.reflect.ReflectPie;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EclipseJavaCodeCompiler implements CodeCompiler {

	public final Language language;
	// private final Model model;

	private FileSpace fileSpace;
	private ReflectPie fileSpaceReflect;

	public final ErrorTable errorTable;

	public FileEntry fileEntry;

	public EclipseJavaCodeCompiler(Model model, JavaLanguage language) {
		// this.model = model;
		this.language = language;
		// AppLog.println_d("<init> %s", this.getClass());

		if (model != null) {
			this.fileSpace = model.fileSpace;
			this.fileSpaceReflect = ReflectPie.on(this.fileSpace);
			errorTable = model.errorTable;

		} else {
			errorTable = null;
		}

	}

	private JavaCodeModelPro javaCodeModelPro;

	Model model;
	public EclipseJavaCodeCompiler(Model model, JavaCodeModelPro javaCodeModelPro) {
		// this.model = model;
		this.javaCodeModelPro = javaCodeModelPro;
		this.language = javaCodeModelPro.javaLanguage;
		this.model = model;
		if (model != null) {
			this.fileSpace = model.fileSpace;
			this.fileSpaceReflect = ReflectPie.on(this.fileSpace);
			errorTable = model.errorTable;

		} else {
			errorTable = null;
		}
	}

	public void createClassWriter() {

	}

	/**
	 * 初始化项目信息
	 * 结构
	 * 1. 查找android.jar
	 * 2. 建立项目依赖结构
	 * 3. 遍历所有文件并进行分租
	 *   将源码文件划分到源码依赖中
	 * 4. 待编译源码也要分组到项目
	 *   且统一编译
	 */

	public Language getLanguage() {
		return language;
	}

	@Override
	public void init(CodeModel codeModel) {
		if (codeModel instanceof JavaCodeModelPro) {
			javaCodeModelPro = (JavaCodeModelPro) codeModel;
		}
	}

	private Set<String> completedFiles = new HashSet<>();

	@Override
	public void compile(List<SyntaxTree> syntaxTrees, boolean p) {
		// AppLog.println_d(" compile() -> %s", javaCodeModelPro);

		if (this.javaCodeModelPro == null) {
			return;
		}

		if (completed) {
			completed = false;
			// AppLog.println_d("\n\n\n\n\n开始编译");
		}

		for (SyntaxTree syntaxTree : syntaxTrees) {

			Language syntaxTreeLanguage = syntaxTree.getLanguage();
			if (syntaxTreeLanguage == this.language) {
				compile(syntaxTree);

				break;
			} else {
				// AppLog.println_d("Language %s  %s\n", this.language, syntaxTreeLanguage);
			}

		}

	}

	private void compile(SyntaxTree syntaxTree) {
		try {
			// AppLog.println_d("compile2()");

			FileEntry fileEntry = syntaxTree.getFile();
			String pathString = fileEntry.getPathString();
			
			// 记录已编译的文件
			this.completedFiles.add(pathString);
			
			// AppLog.println_d("编译 %s", pathString);
			
			int assemblyId = fileSpace.getAssembly(fileEntry);
			ProjectEnvironment projectEnvironment = this.javaCodeModelPro.projectEnvironments.get(assemblyId);
			projectEnvironment.compile(syntaxTree);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	boolean completed;
	@Override
	public void completed() {

		// 首次编译
		SetOfFileEntry chachedFiles = this.model.syntaxTreeSpace.getChachedFiles();
		
		SetOfFileEntry.Iterator default_Iterator = chachedFiles.default_Iterator;
		default_Iterator.init();

		Set<String> paths = new HashSet<>();
		SyntaxTree syntaxTree = new SyntaxTree(this.model);
		while (default_Iterator.hasMoreElements()) {
			FileEntry fileEntry = default_Iterator.nextKey();
			String pathString = fileEntry.getPathString();
			paths.add(pathString);
			
			if( !this.completedFiles.contains(pathString)){
				// 补充编译
				// AppLog.println_d("补充编译 %s", pathString);
				syntaxTree.lg(fileEntry, language);
				compile(syntaxTree);
			}
		}
		// AppLog.println_d("[\n%s\n]", String.join("\n", paths));
		// 清除已编译的文件
		this.completedFiles.clear();
		
		// 编译结束
		this.completed = true;
		// AppLog.println_d("编译结束\n\n\n\n\n");
	}

	public static Set<FileEntry> convert(SetOfFileEntry setOfFileEntry) {
		HashSet<FileEntry> set = new HashSet<FileEntry>();
		SetOfFileEntry.Iterator default_Iterator = setOfFileEntry.default_Iterator;
		default_Iterator.init();

		while (default_Iterator.hasMoreElements()) {
			FileEntry fileEntry = default_Iterator.nextKey();
			set.add(fileEntry);
		}
		return set;
	}
	/*
	 SparseArray<Project> projects = new SparseArray<>();
	
	 @Override
	 public void init(CodeModel codeModel) {
	 if (!(codeModel instanceof JavaCodeModelPro)
	 || this.model == null) {
	 return;
	 }
	 // 置空
	 projects.clear();
	
	
	 // 构建项目依赖信息
	 // 构建库依赖信息
	
	 // android.jar AssemblyId[路径为android.jar]
	 String bootclasspath = null;
	 int androidJarAssemblyId = 0;
	 // 主项目AssemblyId[好像不需要🤔]
	
	 // int mainProjectAssemblyId;
	
	 HashMap<Integer, FileSpace.Assembly> assemblyMap = getAssemblyMap();
	 // 遍历创建项目
	 for (Map.Entry<Integer, FileSpace.Assembly> entry : assemblyMap.entrySet()) {
	 Integer assemblyId = entry.getKey();
	 FileSpace.Assembly assembly = entry.getValue();
	
	 String assemblyName = Assembly.VH(assembly);
	 if ("rt.jar".equals(assemblyName)
	 || "android.jar".equals(assemblyName)) {
	 androidJarAssemblyId = assemblyId;
	 bootclasspath = FileSpace.Assembly.Zo(assembly);
	 continue;
	 }
	 // 创建项目
	 // System.out.printf("assemblyName %s id: %s\n", assemblyName, assemblyId);
	 Project project = new Project(assemblyId, assembly);
	 projects.put(assemblyId, project);
	 }
	
	
	 OrderedMapOfIntInt assemblyReferences = getAssemblyReferences();
	 OrderedMapOfIntInt.Iterator referencesIterator = assemblyReferences.default_Iterator;
	 referencesIterator.init();
	 // 遍历所有 SolutionProject的 AssemblyId
	 while (referencesIterator.hasMoreElements()) {
	 int projectAssemblyId = referencesIterator.nextKey();
	 int referencedProjectAssembly = referencesIterator.nextValue();
	
	 // 自己会依赖自己，排除
	 if (projectAssemblyId == referencedProjectAssembly
	 // 过滤referencedProjectAssembly
	 // 这个单独指定
	 || referencedProjectAssembly == androidJarAssemblyId) {
	 continue;
	 }
	
	 Project project = this.projects.get(projectAssemblyId);
	 Project referencedProject = this.projects.get(referencedProjectAssembly);
	
	 if (referencedProject == null) {
	 FileSpace.Assembly assembly = assemblyMap.get(referencedProjectAssembly);
	 String assemblyName = Assembly.VH(assembly);
	 System.out.printf("没有创建 assemblyName %s id: %s\n ", assemblyName, referencedProjectAssembly);
	 continue;
	 }
	 project.addProjectReferences(referencedProject);
	 }
	
	 // 填充项目信息
	 for (int i = 0, size = this.projects.size(); i < size; i++) {
	 Project project = this.projects.valueAt(i);
	 if (project.isJarProject()) {
	 continue;
	 }
	 project.setBootClasspath(bootclasspath);
	 project.initialize();
	 AppLog.println_d("init: assemblyName %s\n", project.assemblyName);
	
	 }
	
	 AppLog.println_d("init: project size %s\n", projects.size());
	
	 }
	
	
	
	 private List<FileEntry> compilerFiles = new ArrayList<>();
	
	
	
	 FileEntry fileEntry;
	 @Override
	 public void compile(List<SyntaxTree> syntaxTrees, boolean p) {
	
	 for (SyntaxTree syntaxTree : syntaxTrees) {
	 if (language != this.language) {
	 continue;
	 }
	 FileEntry file = fileEntry;
	 if (fileEntry == null) {
	 fileEntry = file;
	 }
	 // this.model.errorTable.clearNonParserErrors(file, this.language);
	 this.compilerFiles.add(file);
	
	 return;
	 }
	 }
	 static class Main {
	
	 public FileEntry file;
	 public Language language;
	 public int startLine;
	 public int startColumn;
	 public int endLine;
	 public int endColumn;
	 public String msg;
	
	 public Main(FileEntry file, Language language, int startLine, int startColumn, int endLine, int endColumn, String msg) {
	 this.file = file;
	 this.language = language;
	 this.startLine = startLine;
	 this.startColumn = startColumn;
	 this.endLine = endLine;
	 this.endColumn = endColumn;
	 this.msg = msg;
	 }
	 }
	 @Override
	 public void completed() {
	 AppLog.println_d("init: completed\n");
	
	 if (compilerFiles.isEmpty()) {
	 return;
	 }
	
	 // 对compilerFiles分组 -> 可能是多个项目的 文件
	 for (FileEntry file : compilerFiles) {
	 // clearError(file);
	 // 所在项目
	 int assembly = file.getAssembly();
	 Project project = projects.get(assembly);
	 String pathString = file.getPathString();
	 System.out.println( pathString );
	
	 AppLog.println_d("init: addCompileFile %s\n", pathString);
	
	 project.addCompileFile(pathString);
	
	 }
	
	 // 编译完成清空编译列表
	 compilerFiles.clear();
	
	 // 应该是根据依赖来编译
	 Set<Project> handleProjects  = new HashSet<>();
	
	 for (int i = 0; i < projects.size(); i++) {
	 Project project = projects.valueAt(i);
	 if (handleProjects.contains(project)) {
	 continue; 
	 }
	 // 优先编译其子依赖
	 for (Project projectReference : project.getProjectReferences()) {
	 if (handleProjects.contains(projectReference)) {
	 continue; 
	 }
	 // 标记已编译
	 handleProjects.add(projectReference);
	 if (projectReference.needCompile()) {
	 compileProject(projectReference);
	 }
	 }
	 if (project.needCompile()) {
	 compileProject(project);
	 // 标记已编译
	 handleProjects.add(project);
	 }
	 }
	
	 }
	
	 private void clearError(FileEntry fileEntry) {
	 int index = 0;
	 // error count
	 Language language = this.language;
	 int count = errorTable.SI(fileEntry, language);
	 List<Main> mains = new ArrayList<>();
	 while (index < count) {
	 int startLine = errorTable.getErrorStartLine(fileEntry, language, index);
	 int startColumn = errorTable.getErrorStartColumn(fileEntry, language, index);
	 int endLine = errorTable.getErrorEndLine(fileEntry, language, index);
	 int endColumn = errorTable.getErrorEndColumn(fileEntry, language, index);
	 String msg = errorTable.getErrorText(fileEntry, language, index);
	 int kind = errorTable.getErrorKind(fileEntry, language, index);
	
	 if (kind == 300) {
	 // AppLog.d("JavaCodeAnalyzer:: 找到 静态方法 " + msg + " 在文件 " + fileEntry.getPathString());
	 // AppLog.d("JavaCodeAnalyzer:: 位置(" + startLine + "," + startColumn + "," + endLine + "," + endColumn + ")");
	 mains.add(new Main(fileEntry, language, startLine, startColumn, endLine, endColumn, msg));
	 } else {
	 // AppLog.d("JavaCodeAnalyzer:: 错误文件(" + fileEntry.getPathString() + ")");
	 // AppLog.d("JavaCodeAnalyzer:: 位置(" + startLine + "," + startColumn + "," + endLine + "," + endColumn + ")");
	 // AppLog.d("JavaCodeAnalyzer:: 类型 " + kind);
	 // AppLog.d("JavaCodeAnalyzer:: 信息 " + msg);
	 }
	 index++;
	 }
	
	 //errorTable.DW(fileEntry, language);
	 errorTable.clearNonParserErrors(fileEntry, language);
	 for (Main main : mains) {
	 errorTable.Hw(main.file, main.language, main.startLine, main.startColumn, main.endLine, main.endColumn, main.msg, 300);
	 }
	 }
	
	 private void compileProject(Project project) {
	
	 AppLog.println_d("compileProject: project assemblyName %s\n", project.assemblyName);
	
	 if (!project.needCompile()) {
	 return;
	 }
	
	 List<String> projectArgs = project.getArgs();
	 Set<String> compilerSourceFiles = project.getCompilerSourceFiles();
	
	 int initialCapacity = projectArgs.size() + compilerSourceFiles.size();
	 String[] args = new String[initialCapacity];
	
	 int count = 0;
	 for (String arg : projectArgs) {
	 args[count] = arg;
	 count++;
	 }
	 for (String arg : compilerSourceFiles) {
	 args[count] = arg;
	 count++;
	 }
	 AppLog.println_d("编译 assemblyName%s\n\t%s\n", project.assemblyName, Arrays.toString(args));
	
	 PrintWriter outWriter = new PrintWriter(System.out);
	 EcjCompilerImpl  compile = new EcjCompilerImpl(outWriter, outWriter, false);
	 compile.setDiagnosticListener(new ErrorTableDiagnosticListener(this));
	 // compile.configure(projectArgs.toArray(new String[projectArgs.size()]));
	
	 compile.compile(args);
	 AppLog.println_d("编译完成");
	
	 project.completed();
	 }
	
	 private ICompilerRequestor method() {
	 return new ICompilerRequestor(){
	 @Override
	 public void acceptResult(CompilationResult compilationResult) {
	 CategorizedProblem[] problems = compilationResult.getAllProblems();
	 if (problems == null) {
	 return;
	 }
	 for (CategorizedProblem rawProblem : problems) {
	
	 DefaultProblem problem = (DefaultProblem) rawProblem;
	 FileEntry fileEntry = EclipseJavaCodeCompiler.this.fileEntry.getEntry(new String(problem.getOriginatingFileName()));
	
	 int line = problem.getSourceLineNumber();
	 int column = problem.column;
	 int endColumn = (problem.column + problem.getSourceEnd() - problem.getSourceStart()) + 1;
	
	 String msg = problem.getMessage();
	
	 if (problem.isError()) {
	 // AppLog.d("JavaCodeAnalyzer:: ECJ 错误文件(" + fileEntry.getPathString() + ")");
	 EclipseJavaCodeCompiler.this.errorTable.Hw(fileEntry, language, line, column, line, endColumn, msg, 20);
	 } else {
	 // AppLog.d("JavaCodeAnalyzer:: ECJ 警告文件(" + fileEntry.getPathString() + ")");
	 EclipseJavaCodeCompiler.this.errorTable.Hw(fileEntry, language, line, column, line, endColumn, msg, 49);
	 }
	
	 // AppLog.d("JavaCodeAnalyzer:: ECJ 位置(" + line + "," + column + "," + line + "," + endColumn + ")");
	 // AppLog.d("JavaCodeAnalyzer:: ECJ 信息 " + msg);
	 }
	 }
	 };
	 }
	
	 private void clearError() {
	 // TODO: Implement this method
	 }
	 */

	/**
	 * AssemblyId -> Assembly[assemblyName，assembly路径，]
	 */
	public HashMap<Integer, FileSpace.Assembly> getAssemblyMap() {
		return this.fileSpaceReflect.get("assemblyMap");
	}

	/**
	 * assembly之间的依赖关系
	 * key -> value[被依赖]
	 */
	public OrderedMapOfIntInt getAssemblyReferences() {
		return fileSpaceReflect.get("assemblyReferences");
	}

	/**
	 * 文件与所在项目
	 */
	public FunctionOfIntInt getFileAssembles() {
		return this.fileSpaceReflect.get("fileAssembles");
	}

	/*
	 * 注册文件容器
	 */
	public SetOfFileEntry getRegisteredSolutionFiles() {
		return this.fileSpaceReflect.get("registeredSolutionFiles");
	}

	/*************************************************************************************************************************************************************************************/

}

