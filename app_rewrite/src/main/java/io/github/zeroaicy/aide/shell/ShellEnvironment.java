/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package io.github.zeroaicy.aide.shell;
import android.content.Context;
import java.util.Map;
import java.util.List;

public interface ShellEnvironment{

	/**
	 * 初始化 并 set Context
	 */
	public void init(Context context);
	
	
	public Map<String, String> getEnvironment(boolean isFailSafe);
	
	// env 作为初始值 会被实现覆盖
	public Map<String, String> getEnvironment(boolean isFailSafe, Map<String, String> env);
	
	// 不修改返回选原对象
	public List<String> setupShellCommandArguments(List<String> arguments);
}
