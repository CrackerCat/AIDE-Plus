/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.cmake;

import io.github.zeroaicy.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import io.github.zeroaicy.util.IOUtils;

public class ProcessUtil {

	public static ProcessExitInfo exec(List<String> commandList, String workDir, Map<String, String> env,
			boolean redirectErrorStream, OutputStream outputStream, byte[] bArr) {
		try {

			ProcessUtil processUtil = new ProcessUtil(commandList, workDir, env, redirectErrorStream);
			return processUtil.start();
		} catch (final Throwable e) {
			return new ProcessExitInfo() {
				@Override
				public int exit() {
					return -1;
				}

				@Override
				public byte[] getMessagen() {
					return Log.getStackTraceString(e).getBytes();
				}
			};
		}
	}

	List<String> commandList;
	String workDir;
	Map<String, String> env;
	boolean redirectErrorStream;

	public ProcessUtil(List<String> commandList, String workDir, Map<String, String> env, boolean redirectErrorStream) {
		this.commandList = commandList;
		this.workDir = workDir;
		this.env = env;

		if (this.commandList == null) {
			this.commandList = Collections.emptyList();
		}
		this.redirectErrorStream = redirectErrorStream;

	}
	public ProcessExitInfo start() throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder();

		processBuilder.command(commandList);

		if (this.workDir != null) {
			processBuilder.directory(new File(this.workDir));
		}

		if (this.env != null) {
			processBuilder.environment().putAll(env);
		}

		processBuilder.redirectErrorStream(this.redirectErrorStream);

		// 运行命令
		final Process process = processBuilder.start();

		//new Thread(new ReadRunnable(process.getInputStream(), new ByteArrayOutputStream())).start();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ProcessUtil.ReadRunnable readRunnable = new ReadRunnable(process.getInputStream(), byteArrayOutputStream);
		// 读取输出流
		readRunnable.run();

		process.waitFor();

		final int exitValue = process.exitValue();
		final byte[] messagen = byteArrayOutputStream.toByteArray();

		return new ProcessExitInfo() {
			@Override
			public int exit() {
				return exitValue;
			}

			@Override
			public byte[] getMessagen() {
				return messagen;
			}
		};
	}

	public static class ReadRunnable implements Runnable {
		private InputStream inputStream;
		private OutputStream outputStream;

		public ReadRunnable(InputStream inputStream, OutputStream outputStream) {
			this.inputStream = inputStream;
			this.outputStream = outputStream;
		}

		@Override
		public void run() {
			try {
				IOUtils.streamTransfer(this.inputStream, this.outputStream);
			} catch (IOException e) {

			}
		}
	}

}

