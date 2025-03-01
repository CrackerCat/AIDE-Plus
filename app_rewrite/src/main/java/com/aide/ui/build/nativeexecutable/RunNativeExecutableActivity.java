/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package com.aide.ui.build.nativeexecutable;

import abcd.vf;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.aide.ui.build.OutputConsole;
import com.aide.ui.build.OutputConsoleActivity;
import com.aide.ui.rewrite.R;
import io.github.zeroaicy.aide.shell.ShellEnvironment;
import io.github.zeroaicy.aide.shell.ShellEnvironmentUtils;
import java.util.Arrays;
import java.util.List;

public class RunNativeExecutableActivity extends OutputConsoleActivity {

	static ShellEnvironment termuxShellEnvironment = ShellEnvironmentUtils.getShellEnvironment();
	
	private vf w9;
	
	static OutputConsole Mr(RunNativeExecutableActivity runNativeExecutableActivity) {
		return runNativeExecutableActivity.WB;
	}

	static void U2(RunNativeExecutableActivity runNativeExecutableActivity) {
		runNativeExecutableActivity.EQ();
	}

	static Handler a8(RunNativeExecutableActivity runNativeExecutableActivity) {
		return runNativeExecutableActivity.mb;
	}

	static vf j3(RunNativeExecutableActivity runNativeExecutableActivity) {
		return runNativeExecutableActivity.w9;
	}

	public static void lg(Activity activity, boolean z, String str, int i) {
		Intent intent = new Intent(activity, (Class<?>) RunNativeExecutableActivity.class);
		intent.putExtra("EXTRA_EXECUTABLE", str);
		intent.putExtra("EXTRA_THEME", z);
		OutputConsoleActivity.QX(activity, z, i, intent);
	}

	protected void XL() {
		
		String extarExecutable  = getIntent().getExtras().getString("EXTRA_EXECUTABLE");
		
		List<String> arguments = termuxShellEnvironment.setupShellCommandArguments(Arrays.asList(extarExecutable));
		String[] extarExecutables = new String[arguments.size()];
		arguments.toArray(extarExecutables);
		
		vf vfVar = new vf(extarExecutables, termuxShellEnvironment.getEnvironment(false), "", false);
		
		this.w9 = vfVar;
		
		
		vfVar.QX(this.WB.getPrintStream());
		this.WB.setProcessOutputStream(this.w9.XL());
		new Thread(new RunNativeExecutableRunnable(this)).start();

	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	}

	protected int u7() {
		return R.drawable.ic_launcher;
	}

	public static class RunNativeExecutableRunnable implements Runnable {
		RunNativeExecutableActivity activity;

		public RunNativeExecutableRunnable(RunNativeExecutableActivity activity) {
			this.activity = activity;
		}

		@Override
		public void run() {

			RunNativeExecutableActivity.j3(this.activity).aM();
			int exitCode = RunNativeExecutableActivity.j3(this.activity).J0();
			if (exitCode != 0) {
				RunNativeExecutableActivity.Mr(this.activity).getPrintStream()
						.println("Process exited with code " + exitCode);
			}
			RunNativeExecutableActivity.a8(this.activity).post(new SyncRunnable(this.activity));
		}
	}

	public static class SyncRunnable implements Runnable {

		RunNativeExecutableActivity activity;

		public SyncRunnable(RunNativeExecutableActivity activity) {
			this.activity = activity;
		}
		@Override
		public void run() {
			RunNativeExecutableActivity.U2(this.activity);
		}

	}
}

