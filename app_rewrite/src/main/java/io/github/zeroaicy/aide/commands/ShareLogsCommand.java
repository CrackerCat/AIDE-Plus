/**
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.commands;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.aide.ui.ServiceContainer;
import com.aide.ui.command.MenuItemCommand;
import com.aide.ui.rewrite.R;
import com.aide.ui.util.FileSystem;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ShareLogsCommand implements MenuItemCommand {

	@Override
	public int getMenuItemId() {
		return R.id.mainShareLogs;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean run() {
		// 压缩这个文件夹
		// CrashApphandler instance = CrashApphandler.getInstance();
		// String crashLogFolderPath = instance.getCAHCE_CRASH_LOG();

		String crashLogFolderPath = FileUtil.CrashLogPath;

		File crashLogFolder = new File(crashLogFolderPath);

		String crashLogParentFolder = crashLogFolder.getParent();
		File shareLogsZipFile = new File(crashLogParentFolder, "ShareLogs.zip");
		if (shareLogsZipFile.exists()) {
			shareLogsZipFile.delete();
		}

		List<File> logFiles = FileUtil.findFile(crashLogFolder, null);

		// 一般不会为空，除非未启用 ZeroAicy Log
		if (logFiles.isEmpty()) {
			Toast.makeText(ServiceContainer.getContext(), "没有日志", Toast.LENGTH_SHORT).show();
			return false;
		}

		// 压缩完毕 切换成修线程
		final Runnable asynTask = new Runnable() {
			@Override
			public void run() {
				// 压缩日志
				compressLogs(shareLogsZipFile, logFiles, crashLogParentFolder);
			}
		};

		final Runnable onUiTask = new Runnable() {
			@Override
			public void run() {
				// 压缩完毕 切换成修线程
				shareLogsFile(shareLogsZipFile);
			}
		};

		ThreadPoolService defaultThreadPoolService = ThreadPoolService.getDefaultThreadPoolService();
		showProgressDialog(defaultThreadPoolService, ServiceContainer.getMainActivity(), "压缩日志中", asynTask, onUiTask);
		return true;
	}

	private void shareLogsFile(File shareLogsZipFile) {
		// 获取上下文和资源
		final Context context = ServiceContainer.getMainActivity();

		final String shareTitle = context.getString(R.string.command_share_logs);

		// 1. 构建基础Intent
		final Intent shareIntent = new Intent(Intent.ACTION_SEND);

		// 2. 安全处理MIME类型
		String shareLogsZipFileName = shareLogsZipFile.getName();
		String fileExtension = FileSystem.getSuffixName(shareLogsZipFileName).toLowerCase(Locale.ROOT);
		MimeTypeMap singleton = MimeTypeMap.getSingleton();
		String mimeType = singleton.getMimeTypeFromExtension(fileExtension);

		// 设置默认MIME类型防止空值
		if (mimeType == null) {
			// 表示文件是原始的二进制数据，系统无法识别其具体格式。
			// 相当于告诉接收方："这是一个文件，但我不确定具体是什么类型"。
			mimeType = "application/octet-stream";
		}
		shareIntent.setType(mimeType);

		// 3. 配置FileProvider URI（确保FILE_PROVIDER_AUTHORITY已正确定义）
		// 推荐使用标准格式
		String authority = context.getPackageName() + ".fileprovider";
		Uri fileUri = FileProvider.getUriForFile(context, authority, shareLogsZipFile);

		// 4. 添加URI和权限
		shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		// 5. 创建选择器并处理异常
		try {
			Intent chooser = Intent.createChooser(shareIntent, shareTitle);

			// 可选：检查是否有应用可处理（需要API 16+）
			if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
				context.startActivity(chooser);
			} else {
				showNoAppAvailableToast(context);
			}
		} catch (ActivityNotFoundException e) {
			showNoAppAvailableToast(context);
		}
	}

	// 辅助方法：显示无可用应用提示
	private void showNoAppAvailableToast(Context context) {
		Toast.makeText(context, "无可用分享应用", Toast.LENGTH_LONG).show();
	}

	// 压缩日志文件
	private void compressLogs(File shareLogsZipFile, List<File> logFiles, String crashLogParentFolder) {
		ZipOutputStream shareLogsZipOutput = null;
		try {
			shareLogsZipOutput = new ZipOutputStream(new FileOutputStream(shareLogsZipFile));
			for (File logFile : logFiles) {
				if (logFile.isDirectory()) {
					continue;
				}
				ZipEntry zipEntry = new ZipEntry(getZipEntryName(logFile, crashLogParentFolder));
				shareLogsZipOutput.putNextEntry(zipEntry);
				FileInputStream logFileInput = null;

				try {
					logFileInput = new FileInputStream(logFile);
					IOUtils.streamTransfer(logFileInput, shareLogsZipOutput);
				} catch (Throwable e) {
				} finally {
					IOUtils.close(logFileInput);
				}
				// ZipEntry 写入完毕
				shareLogsZipOutput.closeEntry();
			}
		} catch (Throwable e) {

		} finally {
			IOUtils.close(shareLogsZipOutput);
		}
	}

	private static String getZipEntryName(File logFile, String crashLogParentFolder) {
		String logFilePath = logFile.getAbsolutePath();

		int index = crashLogParentFolder.length();
		int length = logFilePath.length();

		while (index < length && logFilePath.charAt(index) == '/') {
			index++;
		}
		String zipEntryName = logFilePath.substring(index);
		return zipEntryName;
	}

	@SuppressWarnings("deprecation")
	public static void showProgressDialog(ThreadPoolService defaultThreadPoolService, Activity activity, String message,
			final Runnable asynTask, final Runnable onUiTask) {
		final ProgressDialog show = ProgressDialog.show(activity, null, message, true, false);
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

		defaultThreadPoolService.submit(new Runnable() {
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
}

