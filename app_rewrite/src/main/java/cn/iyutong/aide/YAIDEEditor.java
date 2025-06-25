package cn.iyutong.aide;

import com.aide.ui.AIDEEditor;
import com.aide.ui.AIDEEditorExtend;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.views.CodeEditText;

import java.io.StringReader;

import io.github.zeroaicy.util.ContextUtil;

public class YAIDEEditor{

    //获取光标所选择的文本
    public static String getText(){
        return getAideEditor().getSelectionContent();
    }

    //获取编辑器
    public static AIDEEditor getAideEditor(){
        return AIDEEditorExtend.getCurrentEditor(ServiceContainer.getMainActivity().getAIDEEditorPager());
    }

    //在光标处插入文本
    public static void setText(String commitText){
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
        oEditor.getEditorModel().ys(oEditor.getCaretColumn(), oEditor.getCaretLine(),
                oEditor.getInsertTabsAsSpaces(), oEditor.getTabSize(), new StringReader(commitText), ContextUtil.getContext());
        oEditor.eN(caretLine, endLineNumber);
    }
}
