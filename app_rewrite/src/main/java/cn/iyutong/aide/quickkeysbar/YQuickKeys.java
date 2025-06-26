package cn.iyutong.aide.quickkeysbar;

import android.text.TextUtils;

import com.aide.ui.AppPreferences;

import cn.iyutong.aide.YAIDEEditor;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class YQuickKeys {
    public static String getText() {
        String pathLowerCase = YAIDEEditor.getAideEditor().getFilePath().toLowerCase();
        String txt = getsjcl(pathLowerCase).replaceAll("\n", " ");
        StringBuilder result = new StringBuilder();
        for (String a : txt.split(" ")) {
            if (ZeroAicySetting.isEnabledblxys()) {
                if (!TextUtils.isEmpty(a)) {
                    if (a.equals("#TAB")) {
                        int indentationSize = getIndentationSize();
                        if (indentationSize % YAIDEEditor.getAideEditor().getTabSize() == 0) {
                            int tabCount = indentationSize / YAIDEEditor.getAideEditor().getTabSize();
                            for (int count = 0; count < tabCount; count++) {
                                result.append("\t");
                            }
                        } else {
                            for (int count = 0; count < indentationSize; count++) {
                                result.append("s");
                            }
                        }
                        result.append(" ");
                    } else {
                        result.append(a).append(" ");
                    }
                }
            } else {
                if (a.length() == 1) {
                    result.append(a).append(" ");
                } else if (a.equals("#TAB")) {
                    int indentationSize = getIndentationSize();
                    if (indentationSize % YAIDEEditor.getAideEditor().getTabSize() == 0) {
                        int tabCount = indentationSize / YAIDEEditor.getAideEditor().getTabSize();
                        for (int count = 0; count < tabCount; count++) {
                            result.append("\t");
                        }
                    } else {
                        for (int count = 0; count < indentationSize; count++) {
                            result.append("s");
                        }
                    }
                    result.append(" ");
                }
            }
        }
        return result.toString();
    }

    private static int getIndentationSize() {
        String lowerCase = YAIDEEditor.getAideEditor().getFilePath().toLowerCase();
        if (lowerCase.endsWith(".java")) {
            return AppPreferences.getJavaIndentationSize();
        }
        if (lowerCase.endsWith(".js")) {
            return AppPreferences.getJsIndentationSize();
        }

        if (lowerCase.endsWith(".c") || lowerCase.endsWith(".cpp") || lowerCase.endsWith(".h")
                || lowerCase.endsWith(".cc") || lowerCase.endsWith(".hh") || lowerCase.endsWith(".hpp")) {
            return AppPreferences.getCppIndentationSize();
        }

        if (lowerCase.endsWith(".xml")) {
            return AppPreferences.getXmlIndentationSize();
        }

        if (lowerCase.endsWith(".html") || lowerCase.endsWith(".htm")) {
            return AppPreferences.getHtmlIndentationSize();
        }
        if (lowerCase.endsWith(".css")) {
            return AppPreferences.getCssIndentationSize();
        }
        return YAIDEEditor.getAideEditor().getTabSize();
    }

    private static String getsjcl(String pathLowerCase) {
        if (pathLowerCase.endsWith(".css")) {
            return ZeroAicySetting.getProjectPunctuationcss();
        }
        if (pathLowerCase.endsWith(".xml") || pathLowerCase.endsWith(".html") || pathLowerCase.endsWith(".htm")) {
            return ZeroAicySetting.getProjectPunctuationxml();
        }
        if (pathLowerCase.endsWith(".java") || pathLowerCase.endsWith(".js")) {
            return ZeroAicySetting.getProjectPunctuationjava();
        }
        return ZeroAicySetting.getProjectPunctuationqt();
    }
}
