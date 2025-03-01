/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.shell;
import android.content.Context;
import android.os.Build;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeroAicyShellEnvironment implements ShellEnvironment {
	
	private static ZeroAicyShellEnvironment zeroAicyShellEnvironment;
	public static ZeroAicyShellEnvironment getInstance(Context context){
		if( zeroAicyShellEnvironment == null ){
			zeroAicyShellEnvironment = new ZeroAicyShellEnvironment();
			zeroAicyShellEnvironment.init(context);
		}
		return zeroAicyShellEnvironment;
	}
	
	Context applicationContext;
	
	@Override
	public void init(Context context) {
		if (applicationContext != null) {
			return;
		}
		this.applicationContext = context.getApplicationContext();
		initProotEnv(this.applicationContext);
		
	}

	@Override
	public Map<String, String> getEnvironment(boolean isFailSafe) {
		HashMap<String, String> environment = new HashMap<>();
		putCustomizeEnv(environment);
		return environment;
	}

	@Override
	public Map<String, String> getEnvironment(boolean isFailSafe, Map<String, String> env) {
		
		HashMap<String, String> environment = new HashMap<>();
		if( env != null ){
			environment.putAll(env);
		}
		putCustomizeEnv(environment);
		return environment;
	}
	
	@Override
	public List<String> setupShellCommandArguments(List<String> arguments) {
		if (!ZeroAicyShellEnvironment.ProotMod) {
			return arguments;
		}
		List<String> result = new ArrayList<>();

		String PACKAGE_NAME_PATH = ZeroAicyShellEnvironment.PACKAGE_NAME_PATH;
		//以proot方式启动
		result.add(ZeroAicyShellEnvironment.PROOT_PATH);

		result.add("--rootfs=/");
		result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/data/com.termux");
		result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/user/0/com.termux");

		result.add("--bind=" + PACKAGE_NAME_PATH + "/cache" + ":/linkerconfig");

		result.addAll(arguments);

		return result;
	}
	
	// proot模式
	public static boolean ProotMod;

	//proot路径
	public static String PROOT_PATH;
	///data/data/包名 路径
	public static String PACKAGE_NAME_PATH;
	// /linkerconfig/ld.config.txt路径
	public static String PROOT_TMP_DIR;

	private static void initProotEnv(Context currentPackageContext) {

		if (ZeroAicyShellEnvironment.PROOT_PATH != null) {
			return;
		}

		try {
			ZeroAicyShellEnvironment.ProotMod = currentPackageContext
				.getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.P;
		} catch (Throwable e) {
			ZeroAicyShellEnvironment.ProotMod = true;
		}

		if (ZeroAicyShellEnvironment.PROOT_PATH == null) {
			ZeroAicyShellEnvironment.PROOT_PATH = currentPackageContext.getApplicationInfo().nativeLibraryDir
				+ "/libproot.so";
		}

		if (ZeroAicyShellEnvironment.PACKAGE_NAME_PATH == null) {
			ZeroAicyShellEnvironment.PACKAGE_NAME_PATH = currentPackageContext.getDataDir().getAbsolutePath();
		}

		ZeroAicyShellEnvironment.PROOT_TMP_DIR = new File(ZeroAicyShellEnvironment.PROOT_PATH).getParent();

		File cacheDirFile = new File(PACKAGE_NAME_PATH, "cache");
		if (!cacheDirFile.exists()) {
			cacheDirFile.mkdir();
		}
		File ld_config_txt_file = new File(cacheDirFile, "ld.config.txt");
		if (!ld_config_txt_file.exists() || ld_config_txt_file.length() == 0) {
			try {
				Files.copy(Paths.get("/linkerconfig/ld.config.txt"), ld_config_txt_file.toPath(),
						   StandardCopyOption.REPLACE_EXISTING);
				ld_config_txt_file.setReadable(true, false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private void putCustomizeEnv(HashMap<String, String> environment) {
		//为proot添加缓存路径 PROOT_TMP_DIR
		environment.put("PROOT_TMP_DIR", PROOT_TMP_DIR);
		//自定义参数
		// environment.put("JAVA_TOOL_OPTIONS", "-Duser.language=zh -Duser.region=CN");
	}
}

