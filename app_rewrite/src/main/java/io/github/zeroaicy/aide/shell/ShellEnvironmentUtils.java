/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.shell;
import android.content.Context;

public class ShellEnvironmentUtils{
	
	
	private static Context applicationContext;
	
	/**
	 * 调用_getShellEnvironment()前，必须初始化
	 */
	public static void init(Context context){
		ShellEnvironmentUtils.applicationContext = context;
	}
	
	public static ShellEnvironment getShellEnvironment(){
		return ZeroAicyShellEnvironment.getInstance(applicationContext);
	}
}
