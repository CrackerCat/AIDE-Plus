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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RunNativeExecutableActivity extends OutputConsoleActivity {

	static ShellEnvironment termuxShellEnvironment = ShellEnvironmentUtils.getShellEnvironment();

	private vf processBuilder;

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
		return runNativeExecutableActivity.processBuilder;
	}

	public static void lg(Activity activity, boolean z, String str, int i) {
		Intent intent = new Intent(activity, (Class<?>) RunNativeExecutableActivity.class);
		intent.putExtra("EXTRA_EXECUTABLE", str);
		intent.putExtra("EXTRA_THEME", z);
		OutputConsoleActivity.QX(activity, z, i, intent);
	}

	protected void XL() {

		String extarExecutable = getIntent().getExtras().getString("EXTRA_EXECUTABLE");

		List<String> arguments = termuxShellEnvironment.setupShellCommandArguments(Arrays.asList(extarExecutable));
		String[] extarExecutables = new String[arguments.size()];
		arguments.toArray(extarExecutables);

		Map<String, String> environment = termuxShellEnvironment.getEnvironment(false);
		String directory = "/";
		boolean redirectErrorStream = false;
		vf processBuilder = new vf(extarExecutables, environment, directory, redirectErrorStream);
		this.processBuilder = processBuilder;

		OutputStream outputStream = this.WB.getOutputStream();
		RunNativeExecutableActivity.ConsoleOutputStream consoleOutputStream = new ConsoleOutputStream(outputStream);
		processBuilder.QX(consoleOutputStream);
		
		OutputStream processOutputStream = this.processBuilder.XL();
		this.WB.setProcessOutputStream(processOutputStream);
		
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

	public static class ConsoleOutputStream extends OutputStream {
		// 值太大而又没有 ASCII就会出现 不输出
		private static final int maxCount = 4;
		// 修复乱码
		private final boolean repairGarbled;

		protected OutputStream out;

		private byte[] data;
		private int count;

		public ConsoleOutputStream(OutputStream out) {
			this.out = out;
			this.repairGarbled = this.out instanceof OutputConsole.h;
			if (this.repairGarbled) {
				this.data = new byte[maxCount + 4];
			}

		}

		// 实际 将b 当成 char然后写入的，
		// 所以必须缓存计算 然后写入
		// 或者 使用 write(byte[] p)
		// 多4个 预留一个 utf-8位置
		@Override
		public void write(int b) throws IOException {
			if (!repairGarbled) {
				this.out.write(b);
				return;
			}
			writeUTF8(b);
		}

		private void writeUTF8(int b) throws IOException {

			// 是ASCII 就说明之前的都可以 flush()
			if ((b & 0x80) == 0) {
				if( count == 0 ){
					// 没有缓存字节, 直接当 char 输出
					out.write(b);
				}else{
					data[count++] = (byte) b;
					flush();
				}
				return;
			}

			if (count < maxCount) {
				// 塞入后返回
				data[count++] = (byte) b;
				return;
			}
			//  data[count - 1 ] = ??? 2
			//  data[count - 1 ] = ??? 
			//  data[count - 1 ] = ??? 
			//  data[count ] = null

			// 倒查 末尾是否是 完整的utf-8
			for (int i = count - 3; i < count; i++) {
				byte read = data[i];
				if ((read & 0x80) == 0) {
					// ASCII 跳过
					continue;
				}

				// 计算 以read开头的 utf-8 结束偏移量
				while ((read & 0x40) != 0) {
					read <<= 1;
					++i;
				}
				// 完整的 utf-8
				if (i == count) {
					data[count++] = (byte) b;
					flush();
					return;
				}
				// 不完整的 utf-8
				if (i > count) {
					data[count++] = (byte) b;
					return;
				}
			}

			if ((b & 0x80) == 0) {
				// 也是ASCII
				data[count++] = (byte) b;
				flush();
				return;
			}
			// 末尾全是 ASCII，直接写入
			flush();
			// 保存 此次 b
			data[count++] = (byte) b;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			for (; off < len; off++) {
				write(b[off]);
			}
		}

		@Override
		public void flush() throws IOException {
			if (!repairGarbled) {
				return;
			}
			if (count > 0) {
				this.out.write(data, 0, count);
			}
			count = 0;
		}

	}
}

