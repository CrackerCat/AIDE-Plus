/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */
package com.aide.ui.build.java;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import com.aide.ui.build.OutputConsole;
import com.aide.ui.build.OutputConsoleActivity;
import com.aide.ui.rewrite.R;
import dalvik.system.DexClassLoader;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import io.github.zeroaicy.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class RunJavaActivity extends OutputConsoleActivity {

	static Handler getUiHandler(RunJavaActivity runJavaActivity) {
		return runJavaActivity.mb;
	}

	protected static void U2(Activity activity, Class<?> cls, boolean z, String str, String str2, boolean z2, int i) {
		Intent intent = new Intent(activity, cls);
		intent.putExtra("EXTRA_DEX", str);
		intent.putExtra("EXTRA_CLASS", str2);
		intent.putExtra("EXTRA_DEBUG", z2);
		OutputConsoleActivity.QX(activity, z, i, intent);
	}

	public static void a8(Activity activity, boolean z, String str, String str2, boolean z2) {
		U2(activity, RunJavaActivity.class, z, str, str2, z2, 0);
	}

	static void j3(RunJavaActivity runJavaActivity) {
		runJavaActivity.EQ();
	}

	private Class<?> adrtClass;

	private static ThreadPoolService defaultThreadPoolService = ThreadPoolService.getDefaultThreadPoolService();

	@Override
	protected void XL() {

		String extarDex = getIntent().getExtras().getString("EXTRA_DEX");

		String extarClass = getIntent().getExtras().getString("EXTRA_CLASS");

		boolean extarDebug = getIntent().getBooleanExtra("EXTRA_DEBUG", false);
		File optimizedDirectory = getDir("outdex", 0);

		File extarDexFile = new File(extarDex);
		File extarDexOutFile = new File(optimizedDirectory, extarDexFile.getName());

		// 复制dex.jar到 私有目录
		CopyDexRunable copyDexRunable = new CopyDexRunable(this, extarDexFile, extarDexOutFile, optimizedDirectory,
				extarClass, extarDebug);

		defaultThreadPoolService.submit(copyDexRunable);

	}

	// 主线程中运行
	private void runMainMethod(String extarDex, String optimizedDirectory, String extarClass, boolean extarDebug) {
		
		boolean isEnableJavaConsoleHostMode = ZeroAicySetting.isEnableJavaConsoleHostMode();
		ClassLoader parent = isEnableJavaConsoleHostMode ? this.getClassLoader() : ClassLoader.getSystemClassLoader();
		DexClassLoader dexClassLoader = new DexClassLoader(extarDex, optimizedDirectory, null,
														   parent);
		// 调试模式
		if (extarDebug) {
			try {
				this.adrtClass = dexClassLoader.loadClass("adrt/ADRT");
				this.adrtClass
						.getDeclaredMethod("connectDebugger", Context.class, String.class, Boolean.TYPE, Boolean.TYPE)
						.invoke(null, getApplicationContext(), getPackageName(), Boolean.FALSE, Boolean.FALSE);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		try {

			OutputConsole outputConsole = this.WB;

			// 设置 控制台输入输出流
			System.setOut(outputConsole.getPrintStream());
			System.setErr(outputConsole.getPrintStream());
			System.setIn(outputConsole.getInputStream());
			
			
			Method mainMethod = dexClassLoader.loadClass(extarClass).getDeclaredMethod("main", String[].class);

			// 异步 运行 main方法
			defaultThreadPoolService.submit(new InvokeMainMethodRunnable(this, mainMethod, new String[0]));

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	@Override
	protected void onDestroy() {
		if (this.adrtClass != null) {
			try {
				this.adrtClass.getDeclaredMethod("disconnectDebugger", new Class[0]).invoke(null, new Object[0]);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
		super.onDestroy();

		// 退出
		Process.killProcess(Process.myPid());
	}

	@Override
	protected int u7() {
		// 0x7f07007b
		return R.drawable.ic_launcher_java;
	}

	public static class CopyDexRunable implements Runnable {

		RunJavaActivity runJavaActivity;
		File extarDexFile;
		File optimizedDirectory;

		File extarDexOutFile;

		String extarClass;
		boolean extarDebug;

		public CopyDexRunable(RunJavaActivity runJavaActivity, File extarDexFile, File extarDexOutFile,
				File optimizedDirectory, String extarClass, boolean extarDebug) {
			this.runJavaActivity = runJavaActivity;
			this.extarDexFile = extarDexFile;
			this.extarDexOutFile = extarDexOutFile;
			this.optimizedDirectory = optimizedDirectory;

			this.extarClass = extarClass;
			this.extarDebug = extarDebug;

		}

		@Override
		public void run() {

			// 创建 存放dex.zip 缓存目录
			if (!this.optimizedDirectory.exists()) {
				this.optimizedDirectory.mkdirs();
			}

			if (this.extarDexOutFile.isDirectory()) {
				FileUtil.deleteFolder(this.extarDexOutFile);
			}

			if (this.extarDexOutFile.lastModified() <= this.extarDexFile.lastModified()) {

				// 删除
				this.extarDexOutFile.delete();

				// 复制文件到
				FileInputStream input = null;
				FileOutputStream output = null;
				try {
					input = new FileInputStream(this.extarDexFile);
					output = new FileOutputStream(this.extarDexOutFile);
					IOUtils.streamTransfer(input, output);
				} catch (Throwable e) {
				} finally {
					IOUtils.close(input);
					IOUtils.close(output);
				}
			}

			// 适配 Android 14 -> DexClassLoader 不再支持从可写文件加载 dex/jar 文件
			this.extarDexOutFile.setWritable(false, false);

			// extarDex -> extarDexOutFile
			RunMainMethodRunnable runMainMethodRunnable = new RunMainMethodRunnable(this.runJavaActivity,
					extarDexOutFile.getAbsolutePath(), optimizedDirectory.getAbsolutePath(), this.extarClass,
					this.extarDebug);
			// 调用 入口函数
			Handler uiHandler = RunJavaActivity.getUiHandler(this.runJavaActivity);
			uiHandler.post(runMainMethodRunnable);
		}
	}

	public static class RunMainMethodRunnable implements Runnable {
		RunJavaActivity runJavaActivity;
		String extarDex;
		String optimizedDirectory;
		String extarClass;
		boolean extarDebug;

		public RunMainMethodRunnable(RunJavaActivity runJavaActivity, String extarDex, String optimizedDirectory,
				String extarClass, boolean extarDebug) {
			this.runJavaActivity = runJavaActivity;
			this.extarDex = extarDex;
			this.optimizedDirectory = optimizedDirectory;

			this.extarClass = extarClass;
			this.extarDebug = extarDebug;
		}

		@Override
		public void run() {
			this.runJavaActivity.runMainMethod(extarDex, optimizedDirectory, extarClass, extarDebug);
		}

	}

	public static class InvokeMainMethodRunnable implements Runnable {
		final RunJavaActivity runJavaActivity;
		final Method curMethod;
		final Object args;

		public InvokeMainMethodRunnable(RunJavaActivity runJavaActivity, Method curMethod, Object args) {
			this.runJavaActivity = runJavaActivity;
			this.curMethod = curMethod;
			this.args = args;
		}

		@Override
		public void run() {
			try {
				this.curMethod.invoke(null, this.args);
			} catch (Throwable th) {
				if (th.getCause() != null) {
					th.getCause().printStackTrace();
				} else {
					th.printStackTrace();
				}
			}
			Handler uiHandler = RunJavaActivity.getUiHandler(this.runJavaActivity);
			uiHandler.post(new SyncRunnable(this.runJavaActivity));
		}
	}

	public static class SyncRunnable implements Runnable {
		final RunJavaActivity runJavaActivity;

		public SyncRunnable(RunJavaActivity runJavaActivity) {
			this.runJavaActivity = runJavaActivity;
		}

		@Override
		public void run() {
			RunJavaActivity.j3(this.runJavaActivity);
		}

	}
}

