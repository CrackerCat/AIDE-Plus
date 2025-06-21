package com.aide.ui.services;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.aide.common.AppLog;
import com.aide.common.MessageBox;
import com.aide.ui.ServiceContainer;
import com.google.common.io.Files;
import com.probelytics.Probelytics;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Objects;

import io.github.zeroaicy.util.ContextUtil;
import kotlin.text.Typography;

public class RunService$d implements Runnable {

    final String WB;

    final String mb;

    public RunService$d(RunService runService, String str, String str2) {
        this.WB = str;
        this.mb = str2;
    }

    private static void j6(Object obj, Intent intent) {
        ((Context) obj).startActivity(intent);
        Probelytics.BT(obj, intent);
    }

    private void DW(String str) {
        ServiceContainer.aj(() -> MessageBox.BT(ServiceContainer.getMainActivity(), "Run Error", "Could not run the App directly as root. Consider disabling direct running in the settings.\n静默安装失败。\n\n" + str));
    }

    @Override
    public void run() {


        try {
            File to = new File(Objects.requireNonNull(ContextUtil.getApplication()).getCacheDir(), "temp.apk");
            Files.copy(new File(this.WB), to);
            Process exec = Runtime.getRuntime().exec("su");
            String shell = "pm install -r \"" + to.getAbsolutePath() + Typography.quote;
            PackageInfo packageInfo = null;
            PrintStream printStream = new PrintStream(new BufferedOutputStream(exec.getOutputStream(), 8192));
            printStream.println(shell);
            printStream.flush();
            printStream.close();
            int waitFor = exec.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()), 20);
            StringBuilder stringBuffer = new StringBuilder();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuffer.append(readLine);
                stringBuffer.append("\n");
                AppLog.d("pm: " + readLine);
            }
            if (stringBuffer.toString().contains("signatures do not match previously")) {
                stringBuffer.append("安装失败，与已安装 APK 签名不一致，请卸载当前版本后再安装");
            } else if (stringBuffer.toString().contains("INSTALL_FAILED_VERSION_DOWNGRADE")) {
                stringBuffer.append("安装失败，不能降级安装，请卸载当前版本后再安装");
            } else {
                stringBuffer.append("exited with ").append(waitFor);
            }
            stringBuffer.append("exited with ").append(waitFor);
            AppLog.d("pm: exited with " + waitFor);
            String trim = stringBuffer.toString().trim();
            if (waitFor == 0 && !trim.contains("Failure")) {
                PackageManager packageManager = ServiceContainer.getContext().getPackageManager();
                try {
                    packageInfo = packageManager.getPackageInfo(this.mb, 64);
                } catch (PackageManager.NameNotFoundException unused) {
                    AppLog.d("packageManager：" + unused.getMessage());
                }
                Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(this.mb);
                if (launchIntentForPackage != null) {
                    j6(ServiceContainer.getContext(), launchIntentForPackage);
                    return;
                } else if (packageInfo == null) {
                    ServiceContainer.aj(() -> MessageBox.BT(ServiceContainer.getMainActivity(), "Run Problem", "The app was not installed.\n该应用未安装"));
                    return;
                } else {
                    ServiceContainer.aj(() -> MessageBox.BT(ServiceContainer.getMainActivity(), "Run Problem", "The App has been installed successfully but no launch intent was found in the manifest.\n该应用程序已成功安装，但在清单中未发现任何启动意图。"));
                    return;
                }
            }
            DW(trim);
        } catch (Exception e) {
            AppLog.e(e);
            DW(e.getMessage());
        }
    }
}

