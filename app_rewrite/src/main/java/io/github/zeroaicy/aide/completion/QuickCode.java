/**
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.completion;
import java.util.List;
import java.util.ArrayList;

public class QuickCode{
	private static final List<QuickCode> all = new ArrayList<>();
	private String name;
	private String nameLowerCase;
	private String codeText;

	public static List<QuickCode> getAll() {
		if( all.isEmpty()){
			QuickCode quickCode = new QuickCode();
			quickCode.name = "for-正序循环体";
			quickCode.codeText= "for( int i = 0; i < length; i++) {\n\t\t\n}";
			all.add(quickCode);
		}
		return all;
	}

	public String getName(){
		return name;
	}
	public String getNameLowerCase(){
		if( nameLowerCase == null){
			nameLowerCase = name.toLowerCase();
		}
		return nameLowerCase;
	}
	
	public String getCodeText(){
		return codeText;
	}
	public boolean supportLanguage(String fileSuffixName){
		return true;
	}
}
