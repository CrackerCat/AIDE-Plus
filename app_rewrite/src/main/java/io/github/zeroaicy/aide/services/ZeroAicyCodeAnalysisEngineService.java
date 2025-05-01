package io.github.zeroaicy.aide.services;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.aide.common.AppLog;
import com.aide.engine.service.CodeAnalysisEngineService;
import com.aide.ui.AppPreferences;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.util.FileUtil;
import java.io.File;
import java.util.Locale;

public class ZeroAicyCodeAnalysisEngineService extends CodeAnalysisEngineService {

	private static int id = 0x26f5;

	private static final String TAG = ZeroAicyCodeAnalysisEngineService.class.getSimpleName();

	private static Locale defaultLocale = Locale.getDefault();

	private NotificationManager notificationManager;

	private Notification notification;

	//*
	@Override
	public void onCreate() {
		AppLog.d(TAG, "onCreate");
		super.onCreate();
		// 初始化 App
		Context applicationContext = getApplicationContext();
		ServiceContainer.setContext(applicationContext);
		// 设置初始化
		ZeroAicySetting.init(applicationContext);
		AppPreferences.init(applicationContext);

		setAppLocale();
		checkCompilerImplementation();

		// setNotificationAndForeground();

		try {
			
			if (ZeroAicySetting.isEnableEnsureCapacity(false)) {
				try {
					AppLog.d(TAG, "启用扩容库");
					System.loadLibrary("EnsureCapacity");
				} catch (Throwable e) {
					AppLog.d("CompilationUnitDeclarationResolver2", "load EnsureCapacity", e);
				}
			}else{
				AppLog.d(TAG, "未启用扩容库");				
			}
		} catch (Throwable e) {
			AppLog.d("CompilationUnitDeclarationResolver2", "isEnableEnsureCapacity", e);

		}
	}

	private void setAppLocale() {
		String appLanguage = AppPreferences.getAppLanguage();
		Locale locale;
		if (appLanguage == null || "default".equals(appLanguage)) {
			// 使用默认
			locale = defaultLocale;
		} else {
			locale = new Locale(appLanguage);
		}
		// 设置App语言
		if (locale != null) {
			Locale.setDefault(locale);
		}
	}

	private void checkCompilerImplementation() {
		// ecj false
		boolean isLastCompilerImplementForDefault = ZeroAicySetting.isLastCompilerImplementForDefault();
		boolean isEnableEclipseCompilerForJava = ZeroAicySetting.isEnableEclipseCompilerForJava();

		// 是否是默认编译器
		boolean isDefaultCompilerForJava = !isEnableEclipseCompilerForJava;
		// 上一次编译器 与  当前编译器实现一致
		if (isLastCompilerImplementForDefault == isDefaultCompilerForJava) {
			return;
		}
		// 同步当前编译器实现
		ZeroAicySetting.switchLastCompilerImplement(isEnableEclipseCompilerForJava);
		// 删除编译器序列化
		File enginecacheFile = new File(getCacheDir(), "enginecache");
		FileUtil.deleteFolder(enginecacheFile);
		enginecacheFile.mkdirs();
	}

	@Override
	public IBinder onBind(Intent intent) {
		AppLog.d(TAG, "onBind");
		return super.onBind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		AppLog.d(TAG, "onUnbind");

		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (this.notificationManager == null) {
			this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		if (this.notification != null) {
			this.notification = null;
			this.notificationManager.cancel(id);
		}
	}

	private void setNotificationAndForeground() {
		// String CHANNEL_ID = "engine";
		String CHANNEL_ID = "other";

		try {
			/*
			if (this.notificationChannel == null) {
				NotificationChannelCompat.Builder builder = new NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
			
				this.notificationChannel = builder.build();
				NotificationManagerCompat from = NotificationManagerCompat.from(this);
				from.createNotificationChannel(this.notificationChannel);	
			}//*/

			if (this.notification == null) {
				PendingIntent pendingIntent = MainActivity.sy(this);
				NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
				builder.setWhen(System.currentTimeMillis());
				builder.setSmallIcon(android.R.drawable.stat_notify_more);
				builder.setContentTitle("Code Analysis");
				builder.setContentText("Code analysis engine is active");
				builder.setContentIntent(pendingIntent);
				builder.setPriority(-2);

				this.notification = builder.build();

				//startForeground服务前台化，要在5秒内调用成功，否则前台化失败

				startForeground(id, notification);

			}
		} catch (Throwable e) {
			AppLog.e(TAG, e);
		}
	}

}

