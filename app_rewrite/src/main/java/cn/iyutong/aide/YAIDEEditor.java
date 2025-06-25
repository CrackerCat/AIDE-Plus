package cn.iyutong.aide;

import android.content.Context;
import com.aide.ui.AIDEEditor;
import com.aide.ui.AIDEEditorExtend;
import com.aide.ui.AIDEEditorPager;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.views.CodeEditText;
import java.io.StringReader;

public class YAIDEEditor {

	//获取光标所选择的文本
	public static String getText() {
		return getAideEditor().getSelectionContent();
	}

	//获取编辑器
	public static AIDEEditor getAideEditor() {
		MainActivity mainActivity = ServiceContainer.getMainActivity();
		AIDEEditorPager aideEditorPager = mainActivity.getAIDEEditorPager();
		return AIDEEditorExtend.getCurrentEditor(aideEditorPager);
	}

	//在光标处插入文本
	public static void setText(String commitText) {
		CodeEditText.EditorView oEditor = AIDEEditorExtend.getEditorView(getAideEditor());
		if (oEditor.getSelectionVisibility()) {
			oEditor.getEditorModel().b1();
			oEditor.k4();
			oEditor.setSelectionVisibility(false);
		}
		int newLineNumber = 0;
		for (int offset = 0; offset < commitText.length(); offset++) {
			if (commitText.charAt(offset) == '\n') {
				newLineNumber++;
			}
		}
		int caretLine = oEditor.getCaretLine();
		int endLineNumber = newLineNumber + caretLine;
		int caretColumn = oEditor.getCaretColumn();
		boolean insertTabsAsSpaces = oEditor.getInsertTabsAsSpaces();
		int tabSize = oEditor.getTabSize();
		StringReader stringReader = new StringReader(commitText);
		Context context = ServiceContainer.getContext();
		oEditor.getEditorModel().ys(caretColumn, caretLine, insertTabsAsSpaces, tabSize, stringReader, context);
		oEditor.eN(caretLine, endLineNumber);
	}
}

