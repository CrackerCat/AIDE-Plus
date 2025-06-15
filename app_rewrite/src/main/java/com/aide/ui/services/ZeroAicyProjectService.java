package com.aide.ui.services;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import com.aide.common.AppLog;
import com.aide.engine.EngineSolution;
import com.aide.engine.service.CodeModelFactory;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.firebase.FireBaseLogEvent;
import com.aide.ui.project.AndroidProjectSupport;
import com.aide.ui.project.JavaGradleProjectSupport;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.util.ClassPath;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.activity.ZeroAicyMainActivity;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import io.github.zeroaicy.aide.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZeroAicyProjectService extends ProjectService {
	/**
	 * 尽量将所有代码都从主线程挪到 ProjectServiceThreadPoolService
	 * 但是，其中混合UI报错，以及切换至线程的操作[runOnUiThread]
	 * 导致并不完全迁移到ProjectServiceThreadPoolService
	 * 除非重写
	 */
	private static final String TAG = "ZeroAicyProjectService";

	// 使用ProjectService的实现类的类名作为线程池标记
	public static final String executorsName = ZeroAicyProjectService.class.getName();
	private static final ThreadPoolService executorsService = ThreadPoolService
			.getSingleThreadPoolService(ZeroAicyProjectService.executorsName);

	/**
	 * 使用此线程池的有: AaptService 
	 */
	public static ThreadPoolService getProjectServiceThreadPoolService() {
		return ZeroAicyProjectService.executorsService;
	}

	public static ExecutorService getProjectServiceExecutorService() {
		return getProjectServiceThreadPoolService();
	}

	// private static ProjectService singleton;
	public static ProjectService getSingleton() {
		/*if (singleton == null) {
		 singleton = new ZeroAicyProjectService();
		 AppLog.d(TAG,  "替换ZeroAicyProjectService");
		 }*/
		return new ZeroAicyProjectService();
	}

	/**
	 * 必须在主线程调用
	 */
	@SuppressWarnings("deprecation")
	public static void showProgressDialog(Activity activity, String string, final Runnable asynTask,
			final Runnable onUiTask) {
		final ProgressDialog show = ProgressDialog.show(activity, null, string, true, false);
		Window window = show.getWindow();
		// 允许窗口在输入法（IME）需要焦点时获取焦点
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		// 窗口将不再监听外部触摸事件
		window.clearFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		final Runnable syncTask = new Runnable() {
			@Override
			public void run() {
				try {
					show.dismiss();
				} finally {
					if (onUiTask != null) {
						ThreadPoolService.postOfUi(onUiTask);
					}
				}
			}
		};

		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					if (asynTask != null) {
						asynTask.run();
					}
				} finally {
					ThreadPoolService.postOfUi(syncTask);
				}
			}
		});
	}
	/**
	 * AndroidProjectSupport
	 * 以及不预先解析，边解析边添加依赖路径会有问题
	 */
	public static void preResolving() {
		ProjectService projectService = ServiceContainer.getProjectService();
		String currentAppHome = projectService.getCurrentAppHome();
		if (currentAppHome != null && getProjectSupport(projectService) instanceof AndroidProjectSupport) {
			long nowTime = Utils.nowTime();
			AndroidProjectSupport.getProjectClassPathEntrys(currentAppHome, projectService.getFlavor());
			AppLog.d(TAG, "AndroidProjectSupport::preResolving(): %sms", Utils.nowTime() - nowTime);
		}

	}

	static ProjectSupport getProjectSupport(ProjectService projectService) {
		return ProjectService.Hw(projectService);
	}

	public ZeroAicyProjectService() {
		super();
		// 防止并发
		synchronized (this) {
			// Collections.synchronizedMap(new HashMap<String, List<String>>());
			// 项目路径 -> 所有maven依赖
			this.libraryMapping = new HashMap<String, List<String>>() {
				public static final long serialVersionUID = 0x1;
				@Override
				public List<String> put(String key, List<String> value) {
					if (!ZeroAicyProjectService.executorsService.isCurrentThread()) {
						AppLog.e(Thread.currentThread().getName(), new Throwable());
					}
					return super.put(key, value);
				}

				@Override
				public List<String> remove(Object key) {
					if (!ZeroAicyProjectService.executorsService.isCurrentThread()) {
						AppLog.e(Thread.currentThread().getName(), new Throwable());
					}
					return super.remove(key);
				}

				@Override
				public void clear() {
					if (!ZeroAicyProjectService.executorsService.isCurrentThread()) {
						AppLog.e(Thread.currentThread().getName(), new Throwable());
					}
					super.clear();
				}
			};
			// new ConcurrentHashMap<String, List<String>>();
			// mainAppWearApps 主项目和wear app项目
			this.mainAppWearApps = new Vector<String>();

			// Debugger必须在主线程中创建
			// 因为创建了 Handler
			ServiceContainer.getDebugger();
		}
	}

	@Override
	public boolean J0() {
		return super.J0();
	}

	/**
	 * classPathEntrys字段的 noNull 封装
	 */
	public List<ClassPath.Entry> getClassPathEntrys() {
		List<ClassPath.Entry> classPathEntrys = this.classPathEntrys;

		if (classPathEntrys == null) {
			return Collections.emptyList();
		}
		return classPathEntrys;
	}

	// is_add_lib filebrowserMenuAddLibrary
	// canAddLib
	@Override
	public boolean containJarLib(String filePath) {

		if (this.currentAppHome == null || this.pojectSupport == null) {
			// 没有打开项目
			return false;
		}
		// AndroidProjectSupport没有考虑并发
		// 对其进行特殊处理
		if (this.pojectSupport instanceof AndroidProjectSupport) {

			if (GradleTools.isAndroidGradleProject(filePath) && !currentAppHome.equals(filePath)
					&& !this.getLibraryMapping().get(this.currentAppHome).contains(filePath)) {
				return true;
			}

			List<ClassPath.Entry> classPathEntrys = getClassPathEntrys();
			String name = FileSystem.getName(filePath);
			if (name.toLowerCase().endsWith(".jar") && classPathEntrys != null
					&& !containsLib(this.currentAppHome, filePath, classPathEntrys)) {
				return true;
			}
		}
		return super.containJarLib(filePath);
	}

	// 是否包含库
	// 用于移除依赖库，但Gradle项目无法移除
	// canRemoveLib
	@Override
	public boolean we(String filePath) {
		// 没有打开项目
		if (this.currentAppHome == null || this.pojectSupport == null) {
			return false;
		}
		// 对AndroidProjectSupport进行特殊处理
		if (this.pojectSupport instanceof AndroidProjectSupport) {

			List<ClassPath.Entry> classPathEntrys = this.classPathEntrys;

			List<String> librarys = this.getLibraryMapping().get(this.currentAppHome);
			// 异步bug修复
			if (librarys == null || librarys.contains(filePath)) {
				return true;
			}

			if (classPathEntrys != null && containsLib(this.currentAppHome, filePath, classPathEntrys)) {
				return true;
			}
			return false;
		}

		return super.we(filePath);
	}

	private List<ClassPath.Entry> classPathEntrys;
	private static boolean containsLib(String currentAppHome, String filePath, List<ClassPath.Entry> classPathEntrys) {
		for (ClassPath.Entry entry : classPathEntrys) {
			if (entry.isLibKind() && entry.resolveFilePath(currentAppHome).equals(filePath)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean tp(String string) {
		// 未初始化完毕
		if (!this.isInited()) {
			return false;
		}
		return super.tp(string);
	}
	/**
	 * 返回所有model[子项目 包括 aar]路径
	 */
	@Override
	public List<String> P8() {
		return new ArrayList<String>(this.getLibraryMapping().keySet());

	}

	// libraryMapping只读副本
	private Map<String, List<String>> libraryMappingCopy;

	/*
	 * 非ProjectService线程仅返回只读副本
	 * @return libraryMapping key: 项目路径 -> value: 所有maven依赖
	 */
	@Override
	public synchronized Map<String, List<String>> getLibraryMapping() {
		if (!isInited() || !executorsService.isCurrentThread()) {
			Map<String, List<String>> libraryMappingCopy = this.libraryMappingCopy;
			if (libraryMappingCopy == null) {
				return Collections.emptyMap();
			}
			// 动态创建只读副本
			return libraryMappingCopy;
		}

		return this.libraryMapping;
	}

	// 判断文件夹及父文件夹是否是项目
	@Override
	public String isProjectDirectory(String str) {

		if (FileSystem.isPreProcessor(str)) {
			return null;
		}
		while (!FileSystem.isRoot(str)) {
			if (getProjectSupport(str) != null) {
				return str;
			}
			str = FileSystem.getParent(str);
		}
		return null;
	}

	// 返回当前的文件是否支持[Design]
	@Override
	public boolean J8() {
		if (this.pojectSupport == null) {
			return false;
		}
		String curOpenFile = ServiceContainer.getOpenFileService().getVisibleFile();
		if (curOpenFile == null) {
			return false;
		}
		/**
		 * if (SI(curOpenFile) == null) {
		 *	return false;
		 * }
		 */
		if (!ServiceContainer.isTrainerMode() || ServiceContainer.getTrainerService().CU(curOpenFile)) {
			// 非常耗时的操作
			// getProjectSupport(SI).u7(u7)
			// 我认为就应该只是当前ProjectSupport进行判断
			return this.pojectSupport.u7(curOpenFile);
		}
		return false;
	}
	/*****************************************************************/
	/**
	 * 需要异步原因是 com.aide.ui.build.android.AndroidProjectBuildService.dx
	 * 会使用 yS().get(yS().size() -1);
	 * 在一个线程应该就不会了
	 * 不对 yS().size()如果为0必然get(-1)
	 * 所以问题不在并发
	 * 初步猜测是因为buildProject被调用时，ProjectService再次被初始化
	 * 即刚刚this.mainAppWearApps.clear();
	 */

	@Override
	public void buildProject(boolean p) {
		setUnBuildProjected();
		super.buildProject(p);
		setBuildProjected();
	}

	/**
	 * 返回主项目路径及子项目路径 [签名服务会回调]
	 */
	// yS() -> getMainAppWearApps
	List<String> mainAppWearAppsCopy;

	/**
	 * 构建时不能返回空对象，之前要有一个
	 */
	@Override
	public synchronized List<String> getMainAppWearApps() {
		if (!executorsService.isCurrentThread()) {

			synchronized (this) {
				List<String> mainAppWearAppsCopy = this.mainAppWearAppsCopy;
				if (mainAppWearAppsCopy == null || this.mainAppWearAppsCopy.isEmpty()) {
					// 一般不会有WearApp项目
					return Collections.singletonList(this.currentAppHome);
				}
				return mainAppWearAppsCopy;
			}
		}
		// project中所有model文件夹路径
		return this.mainAppWearApps;
	}

	/*****************************************************************/
	/**
	 * 防止 Hw 与 libraryMapping值被覆盖
	 */
	private void closeProjectAsync() {
		// 置空当前项目路径
		this.saveCurrentAppHome(null);
		// 重置项目属性
		this.mainAppWearApps.clear();
		this.libraryMapping.clear();
		// libraryMapping只读副本
		this.libraryMappingCopy = null;
		this.mainAppWearAppsCopy = null;

		// 关闭项目
		this.resetProjectAttributeCache();

		// 同步代码分析进程
		// 置空代码分析进程信息
		this.jJ(); // 关闭项目

		// 同步主线程 已关闭项目
		ThreadPoolService.postOfUi(new Runnable() {
			@Override
			public void run() {
				// 关闭项目 关闭所有已打开文件
				// closeFile 有Ui操作
				ServiceContainer.getOpenFileService().Zo();
				ServiceContainer.getDebugger().v5();
				ServiceContainer.getMainActivity().q7();
			}
		});
	}

	// Ws() -> closeProject
	/*
	 * 有可能在主线程运行
	 */
	@Override
	public void closeProject() {
		this.setUnInited();

		if (this.currentAppHome == null) {
			return;
		}
		// onUiRun
		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				closeProjectAsync();
			}
		});

	}
	/*****************************************************************/

	public void sGAsync() {
		try {
			if (ServiceContainer.isShutdowned()) {
				return;
			}
			if (ServiceContainer.getMavenService() == null) {
				return;
			}
			super.verifyResourcesDownload();
		} catch (Throwable e) {
		}
	}
	/**
	 * verifyResourcesDownload
	 */
	@Override
	public boolean verifyResourcesDownload() {
		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				sGAsync();
			}
		});
		return false;
	}

	/*****************************************************************/
	protected void etAsync(List<String> list, boolean p) {
		if (this.pojectSupport != null) {
			this.pojectSupport.cn(list, p);
		}
	}

	/**
	 * etAsync将执行
	 * 预构建，比如[aapt，aidl] -> androidProjectBuildService::yO
	 * reloadingProject
	 
	 *  savedFilePaths 改变的文件(保存)
	 * 是否重载(重新加载)项目
	 * 文件改变回调
	 */
	@Override
	public void et(final List<String> savedFilePaths, final boolean reloadingProject) {
		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				long nowTime = Utils.nowTime();
				etAsync(savedFilePaths, reloadingProject);
				AppLog.d(TAG, "pre processing sync: %sms", Utils.nowTime() - nowTime);
			}
		});
	}

	private void delayPreBuild() {
		// AndroidProjectSupport 中则为 运行 aapt/aapt2 & aidl
		// 也只有 AndroidProjectSupport 实现了 cn()
		try {
			ThreadPoolService.postDelayedOfUi(new Runnable() {
				@Override
				public void run() {
					if (isOpenProject()) {
						ZeroAicyProjectService.this.etAsync(null, false);
					}
				}
			}, 1500);
		} catch (Throwable e) {
			AppLog.d(TAG, e);
		}

	}

	/*****************************************************************/

	@Override
	public void yO(String projectDir) {
		// XX(str, true);
		super.yO(projectDir);
	}

	@Override
	public void XX(String projectDir) {
		// kQ(str, true);
		super.XX(projectDir);
	}

	/**
	 * 打开或者切换 项目
	 * 
	 */
	@Override
	public void kQ(final String projectDir, final boolean p) {
		// 打开新项目
		this.setUnInited();
		//super.kQ(projectDir, p);

		kQAsync(projectDir, p);

	}
	/**
	 * 与 super.kQ(projectDir, p); 不同的是 showProgressDialog
	 * 其实还是在主线程运行
	 * 切换项目 或者 先关闭项目在打开一个新项目
	 */
	private void kQAsync(final String projectDir, final boolean z) {
		// 上一个项目路径
		String lastProjectDir = getCurrentAppHome();

		if (projectDir == null) {
			saveCurrentAppHome(null);
			closeProject();
			return;
		}

		if (!ef(projectDir) || projectDir.equals(lastProjectDir)) {
			// 已打开项目
			return;
		}

		// 必须赋值
		this.currentAppHome = projectDir;
		this.saveCurrentAppHome(projectDir);

		this.pojectSupport = getProjectSupport(projectDir);
		this.classPathEntrys = null;
		this.projectProperties = null;

		// ye();
		// 清空错误列表
		ServiceContainer.getErrorService().aM();
		ServiceContainer.getNavigateService().Hw();
		// 关闭所有文件
		ServiceContainer.getOpenFileService().Zo();
		// 停止构建
		ServiceContainer.getBuildService().QX();

		// 与原版不同
		Runnable asynTask = new Runnable() {
			@Override
			public void run() {
				// 赋值 pojectSupport
				ZeroAicyProjectService.this.init();
				ZeroAicyProjectService.this.jJ(); // 切换项目

				// AndroidProjectSupport 中则为 运行 aapt/aapt2 & aidl
				delayPreBuild();

			}
		};

		Runnable onUiTask = new Runnable() {
			@Override
			public void run() {
				ServiceContainer.getDebugger()
						.P8(ProjectService.Hw(ZeroAicyProjectService.this).getProjectPackageName(), false);
				ServiceContainer.getMainActivity().q7();
				ServiceContainer.getFileBrowserService().v5();
				ZeroAicyProjectService.this.verifyResourcesDownload();

			}
		};
		String title = lastProjectDir == null ? "打开项目中[请等待]..." : "切换项目中[请等待]...";
		showProgressDialog(ServiceContainer.getMainActivity(), title, asynTask, onUiTask);
	}

	private String projectProperties = null;

	@Override
	public String getProjectAttribute() {

		// AppLog.d(TAG, "getProjectAttribute() projectProperties %s", projectProperties);
		if (this.currentAppHome == null) {
			// 没有打开项目,，或初始化未完成
			return "";
		}
		// 返回缓存
		if (this.projectProperties != null) {
			return this.projectProperties;
		}
		// 未做优化处理
		return this.projectProperties = super.getProjectAttribute();
	}
	private String getProjectAttributeAsync() {
		return super.getProjectAttribute();
	}
	/*****************************************************************/

	/**
	 * 由ServiceContainer调用
	 * 主线程
	 * openProject更像 init😓😓😓
	 * 就连sy("init")也是😓😓😓
	 */
	@Override
	public void openProject(final String projectPath) {
		try {

			if (projectPath != null) {
				// 弹窗是否打开上次项目
			}

			// 新打开项目，需要初始化
			this.setUnInited();

			// 将openProject 异步执行
			final Runnable asynTask = new Runnable() {
				@Override
				public void run() {
					long nowTime = Utils.nowTime();
					// 打开项目
					openProjectAsync(projectPath);
					AppLog.d(TAG, "openProjectAsync(): %sms", Utils.nowTime() - nowTime);

				}
			};

			final Runnable onUiTask = new Runnable() {
				@Override
				public void run() {
					// 同步界面
					ServiceContainer.getFileBrowserService().v5();
					ServiceContainer.getMainActivity().kf();

					// 反正在主线程调用也是异步
					ZeroAicyProjectService.this.verifyResourcesDownload();

				}
			};
			// 显示弹窗
			showProgressDialog(ServiceContainer.getMainActivity(), "打开项目中[请等待]...", asynTask, onUiTask);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 异步，仅有openProject调用
	 * 运行线程 ProjectService单线程--线程池
	 */
	private void openProjectAsync(String projectPath) {

		SharedPreferences sharedPreferences = ServiceContainer.getContext().getSharedPreferences("ProjectService",
				Context.MODE_PRIVATE);

		if (!ServiceContainer.isTrainerMode()
				&& ServiceContainer.getMainActivity().isSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

			if (projectPath != null) {
				// isProjectDirectory是耗时操作[遍历ProjectSupport]
				this.saveCurrentAppHome(isProjectDirectory(projectPath));
			} else {
				this.currentAppHome = sharedPreferences.getString("CurrentAppHome", null);
				if (this.currentAppHome != null
						// 找不到 ProjectSupport
						&& getProjectSupport(this.currentAppHome) == null) {
					// 没有支持此目录的项目支持器，置空
					this.currentAppHome = null;
				}
			}
		}

		if (isOpenProject()) {
			// 冷启动打开的项目 显示分析进度条
			MainActivity mainActivity = ServiceContainer.getMainActivity();
			if (mainActivity instanceof ZeroAicyMainActivity) {
				ZeroAicyMainActivity zeroAicyMainActivity = (ZeroAicyMainActivity) mainActivity;
				zeroAicyMainActivity.showCodeAnalysisProgress();
			}
		}
		this.pojectSupport = getProjectSupport(this.currentAppHome);

		// this.initAsync();
		this.init();
		// AndroidProjectSupport 中则为 运行 aapt/aapt2 & aidl
		delayPreBuild();

		if (this.pojectSupport != null) {
			ServiceContainer.getDebugger().P8(this.pojectSupport.getProjectPackageName(), true);
		}
		// 为了优化冷启动 不在等待 代码分析进程启动成功
		// 等待 EngineServiceConnection 回调 dx() -> dx2();

		// 当dx() 现于此运行会置空锁
		//		Object lock = this.engineServiceConnectionLock;
		//		if (lock != null) {
		//			synchronized (lock) {
		//				try {
		//					// 等待5s防止死锁
		//					AppLog.d("Waiting EngineServiceConnection");
		//					lock.wait(5000);
		//
		//				} catch (Throwable e) {
		//					AppLog.d(TAG, e);
		//				}
		//			}
		//		}
		//		 // 完成EngineServiceConnection，执行 jJ
		//		 // 同步EngineService
		//		 this.jJ();
	}

	/*******************************************************************/
	// 不要以为使用了同步集合就万事大吉，
	// 同步集合只能保证本身的操作是同步的，
	// 但是它所属的代码块不是同步的话，
	// 多线程情况下也会出问题

	// 如果集合正在遍历，这时又有写入操作就会触发并发错误
	// 如上所述，并发集合仅是集合自己的操作是有锁
	// 但是集合的遍历器不是
	// 防止并发错误

	/**
	 * 接收EngineService回调，否则无法同步
	 */
	@Deprecated
	private Object engineServiceConnectionLock = new Object();
	@Deprecated
	public void dx2() {

		// EngineService$EngineServiceConnection::onServiceConnected() -> EngineService::Mr()
		// 代码分析进程启动成功 向代码分析进程同步 -> jJ() 
		// 通知执行 jJAsync()
		Object engineServiceConnectionLock = this.engineServiceConnectionLock;
		if (engineServiceConnectionLock == null) {
			// 锁已置空
			jJ();
			return;
		}

		synchronized (engineServiceConnectionLock) {
			// 先获取锁
			Object lock = this.engineServiceConnectionLock;
			// 置空锁
			this.engineServiceConnectionLock = null;
			AppLog.d(TAG, "EngineService Connection");
			// 通知
			lock.notifyAll();
		}
	}

	@Override
	public void dx() {
		super.dx();
	}

	/**
	 * 必将在executorsService运行且只有一个线程
	 */
	protected void jJAsync() {
		final EngineService engineService;
		//super.jJ();
		if (ServiceContainer.isShutdowned() || (engineService = ServiceContainer.getEngineService()) == null) {
			AppLog.d(TAG, "not found engine service");
			return;
		}

		EngineSolution engineSolution;

		synchronized (engineService) {
			if (this.currentAppHome != null && this.pojectSupport != null) {
				engineSolution = this.pojectSupport.makeEngineSolution();
			} else {
				// 置空

				List<String> hw = ServiceContainer.Hw();
				TreeMap<String, List<String>> findCodeModels = CodeModelFactory.findCodeModels(hw);
				List emptyList = Collections.emptyList();
				engineSolution = new EngineSolution(emptyList, (String) null, findCodeModels, hw);
			}
			try {
				// 设置 engineSolution
				engineService.setEngineSolution(engineSolution);
				engineService.ef();
				engineService.ei();
			} catch (Throwable e) {
				AppLog.d(TAG, e);
			}
		}
	}

	// [ProjectService$f, ProjectService$d] 通过 FH()调用
	// private long jJMethodCallTime;
	@Override
	protected void jJ() {
		// jJMethodCallTime = Utils.nowTime();
		// updateEngineSolution
		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				long nowTime = Utils.nowTime();
				jJAsync();
				AppLog.d(TAG, "engine service sync: %sms", Utils.nowTime() - nowTime);
			}
		});
	}
	/*****************************************************************/
	private final AtomicBoolean inited = new AtomicBoolean(false);

	public void setInited() {
		this.inited.set(true);
	}

	/**
	 * 仅在 xxxAsync() 调用所在函数
	 * 与 改变项目状态[ 打开 关闭 初始化等]调用
	 *
	 */
	public void setUnInited() {
		this.inited.set(false);
	}
	public boolean isInited() {
		return this.inited.get();
	}

	/****************************buildProject*************************************/
	private final AtomicBoolean buildProjected = new AtomicBoolean(false);

	public void setBuildProjected() {
		this.buildProjected.set(true);
	}
	public void setUnBuildProjected() {
		this.buildProjected.set(false);
	}
	public boolean isBuildProjected() {
		return this.buildProjected.get();
	}

	/*****************************************************************/
	/**
	 * 异步重载
	 */
	public void reloadingProjectAsync() {

		// 项目目录不存在或没有项目支持器支持
		if (this.currentAppHome == null || getProjectSupport(this.currentAppHome) == null) {
			// 没有项目支持器支持
			closeProject();
			return;
		}

		// reloadingProjectAsync需要置空
		this.resetProjectAttributeCache();

		ServiceContainer.getDebugger().ef();

		//在主线程执行showProgressDialog
		ThreadPoolService.postOfUi(new Runnable() {
			@Override
			public void run() {
				MainActivity mainActivity = ServiceContainer.getMainActivity();

				final Runnable asynTask = new Runnable() {
					@Override
					public void run() {
						// 修复重载项目后 依赖信息未重置
						ZeroAicyProjectService.this.init();
						// 向代码分析进程
						ZeroAicyProjectService.this.jJ(); // 重新载入项目
					}
				};

				final Runnable onUiTask = new Runnable() {
					@Override
					public void run() {
						// 反正在主线程调用也是异步
						ZeroAicyProjectService.this.verifyResourcesDownload();
						// 同步界面
						ServiceContainer.getFileBrowserService().v5();
						ServiceContainer.getMainActivity().kf();

						// 猜测 aapt2 aidl
						// ZeroAicyProjectService.this.et(null, false);

					}
				};

				String title = "Reloading project...";
				showProgressDialog(mainActivity, title, asynTask, onUiTask);
			}
		});
	}
	@Override
	public void reloadingProject() {
		// 刷新, 需要初始化
		this.setUnInited();

		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				reloadingProjectAsync();
			}
		});
	}
	/*****************************************************************/

	/**
	 * 更像openProject
	 * 但与提前更改当前项目路径|
	 */
	@Override
	protected void init() {
		//异步
		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				initAsync();
			}
		});
	}

	private void initAsync() {
		if (this.isInited()) {
			// 老是重复初始化
			// AppLog.e(new Throwable());
			// AppLog.d("initAsync() Reloading");
			// return;
		}

		// 置空项目属性
		// 项目中的主项目
		this.mainAppWearApps.clear();
		this.mainAppWearAppsCopy = null;

		// 项目目录(包括主项目) -> 子项目
		this.libraryMapping.clear();
		this.libraryMappingCopy = null;

		// 初始化
		this.resetProjectAttributeCache();

		if (this.currentAppHome == null) {
			// 初始化完成
			this.setInited();
			return;
		}

		// 重置仓库依赖
		ServiceContainer.getMavenService().resetDepMap();
		// 填充this.libraryMapping[修改this.libraryMapping中]
		// libraryMapping是所有子项目目录[aar也算且包含当前项目目录]
		this.pojectSupport.init(this.currentAppHome, this.libraryMapping, this.mainAppWearApps);

		this.libraryMappingCopy = new HashMap<String, List<String>>(this.libraryMapping);
		this.mainAppWearAppsCopy = new ArrayList<String>(this.mainAppWearApps);

		if (this.pojectSupport instanceof AndroidProjectSupport) {
			// 可以做一些额外处理
			ZeroAicyProjectService.this.classPathEntrys = AndroidProjectSupport
					.getProjectClassPathEntrys(ZeroAicyProjectService.this.getCurrentAppHome(), null);
		}
		this.projectProperties = this.getProjectAttributeAsync();

		// AppLog.d(TAG, "projectProperties %s", projectProperties);

		// 初始化完成
		this.setInited();
	}

	private void resetProjectAttributeCache() {
		this.classPathEntrys = null;
		this.projectProperties = null;
	}

	/*****************************************************************/

	private synchronized void saveCurrentAppHome(String projectPath) {
		this.currentAppHome = projectPath;

		SharedPreferences.Editor edit = ServiceContainer.getContext().getSharedPreferences("ProjectService", 0).edit();
		edit.putString("CurrentAppHome", projectPath);
		edit.commit();
	}

	// is_open_project
	@Override
	public boolean Mz(String projectPath) {
		// JavaGradle项目需要单独处理

		ProjectSupport projectSupport = getProjectSupport(projectPath);
		if (projectSupport instanceof JavaGradleProjectSupport) {
			// 当前项目不能包含他
			return !isInCurrentProjectDirectory(projectPath);
		}
		return projectSupport != null;
	}

	@Override
	public int getOpenProjectNameStringId(String str) {
		ProjectSupport projectSupport = getProjectSupport(str);
		if (projectSupport == null) {
			return 0;
		}
		return projectSupport.getOpenProjectNameStringId(str);
	}

	@Override
	public ProjectSupport getProjectSupport(String projectPath) {
		if (projectPath == null) {
			return null;
		}
		try {

			// 重定向至 ZeroAicyExtensionInterface.getProjectSupports();
			ProjectSupport[] projectSupports = ServiceContainer.getProjectSupports();

			for (ProjectSupport projectSupport : projectSupports) {
				if (projectSupport.isSupport(projectPath)) {
					return projectSupport;
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/*****************************************************************/
	@Override
	public void wc() {
		if (!ThreadPoolService.isUiThread()) {
			wcAsync();
			return;
		}

		executorsService.submit(new Runnable() {
			@Override
			public void run() {
				wcAsync();
			}
		});
	}

	// 频繁调用，且感觉无意义(明明已经inited了)，但还是总调用init() 与 jJ()
	// 
	private void wcAsync() {
		if (this.currentAppHome == null && this.pojectSupport == null) {
			return;
		}

		if (this.currentAppHome == null || getProjectSupport(this.currentAppHome) == null) {
			// 关闭不支持的且已打开的项目
			closeProject();
		}

		this.init();

		// 刷新远程端[代码分析器]
		this.jJ();
	}

	/*****************************************************************/

	// 好像是 判断当前目录是否在项目目录中
	@Override
	public boolean isInCurrentProjectDirectory(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return false;
		}
		if (this.pojectSupport instanceof AndroidProjectSupport) {
			return isAndroidProjectInwhat(filePath);
		}
		return super.isInCurrentProjectDirectory(filePath);
	}

	public boolean isAndroidProjectInwhat(String filePath) {
		String currentAppHome = getCurrentAppHome();
		if (currentAppHome == null) {
			return false;
		}
		if (filePath.startsWith(currentAppHome)) {
			return true;
		}

		Map<String, List<String>> libraryMapping = ServiceContainer.getProjectService().getLibraryMapping();
		for (String key : libraryMapping.keySet()) {
			if (filePath.startsWith(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String er() {
		return super.er();
	}
	public String getBuildType() {
		return this.er();
	}

	@Override
	public void sy(String string) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("isPremium", Boolean.toString(isPremium()));
		hashMap.put("libraryCount", Integer.toString(this.libraryMapping.size()));
		hashMap.put("referrer", string);
		String currentAppHome = getCurrentAppHome();
		if (!JavaGradleProjectSupport.isJavaGradleProject(currentAppHome)
				&& AndroidProjectSupport.isAndroidGradleProject(currentAppHome)) {
			hashMap.put("package", AndroidProjectSupport.getProjectPackageName(getCurrentAppHome(), (String) null));
		}
		FireBaseLogEvent.EQ("Project opened", hashMap);

	}
	/*****************************************************************/

}

