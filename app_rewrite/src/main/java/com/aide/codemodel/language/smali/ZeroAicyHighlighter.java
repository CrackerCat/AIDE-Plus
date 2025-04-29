/**
 * @Date 
 * @AIDE AIDE+ 
 */
package com.aide.codemodel.language.smali;

import com.aide.codemodel.JFlexLexer;
import com.aide.codemodel.api.FileEntry;
import com.aide.codemodel.api.SyntaxTreeStyles;
import com.aide.common.AppLog;
import java.io.IOException;
import java.io.Reader;
import com.aide.ui.util.FileSystem;
import com.aide.codemodel.language.json.JsonLexer;
import com.aide.codemodel.language.cmake.CmakeLexer;
import com.aide.codemodel.language.properties.PropertiesLexer;
import com.aide.codemodel.language.toml.TomlLexer;
import com.aide.codemodel.language.yaml.YamlLexer;

public class ZeroAicyHighlighter {
	private static final String LOG_TAG = "Highlighter";

	private static final SmaliLexer smaliLexer = new SmaliLexer();
	private static final JsonLexer jsonLexer = new JsonLexer();
	private static final CmakeLexer cmakeLexer = new CmakeLexer();
	private static final PropertiesLexer propertiesLexer = new PropertiesLexer();
	private static final TomlLexer tomlLexer = new TomlLexer();
	private static final YamlLexer yamlLexer = new YamlLexer();

	public boolean highlight(FileEntry fileEntry, Reader reader, SyntaxTreeStyles syntaxTreeStyles) {
		JFlexLexer lexer = getLexer(fileEntry);
		if (lexer == null) {
			return false;
		}

		AppLog.i(LOG_TAG, "highlight: " + lexer.getClass().getName());
		syntaxTreeStyles.clear(); // j6() -> DW()
		try {
			lexer.yyreset(reader);
			lexer.yybegin(lexer.getDefaultState());
			int style = lexer.yylex();

			int startLine = lexer.getLine() + 1;
			int startColumn = lexer.getColumn() + 1;

			while (true) {
				int nextStyle = lexer.yylex();
				int endLine = lexer.getLine() + 1;
				int endColumn = lexer.getColumn() + 1;

				// 填充风格
				syntaxTreeStyles.addSyntaxTag(style, 0, startLine, startColumn, endLine, endColumn);

				style = nextStyle;
				startLine = endLine;
				startColumn = endColumn;
				if (nextStyle == -1)
					break;
				syntaxTreeStyles.addSyntaxTag(0, 0, startLine, startColumn, endLine, endColumn);
			}
		} catch (IOException e) {
			AppLog.e(LOG_TAG, "highlight: " + lexer.getClass().getName(), e);
		} finally {
			try {
				lexer.yyclose();
			} catch (Throwable ignored) {
			}
		}
		return true;
	}

	private JFlexLexer getLexer(FileEntry fileEntry) {
		JFlexLexer lexer = null;
		String filePath = fileEntry.getPathString();
		String fileName = FileSystem.getName(filePath).toLowerCase();
		String suffixName = FileSystem.getSuffixName(fileName);
		switch (suffixName) {
			case "smali" :
				lexer = smaliLexer;
				break;
			case "json" :
				lexer = jsonLexer;
				break;
			case "cmake" :
				lexer = cmakeLexer;
				break;
			case "properties" :
				lexer = propertiesLexer;
				break;
			case "toml" :
				lexer = tomlLexer;
				break;
			case "yaml" :
				lexer = yamlLexer;
				break;

			default :
				switch (fileName) {
					case "cmakelists.txt" :
						lexer = cmakeLexer;
						break;
				}
				break;
		}
		return lexer;
	}
}

