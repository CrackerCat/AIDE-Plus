/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package com.aide.ui;
import com.aide.ui.views.CodeEditText;
import java.util.List;

public class AIDEEditorExtend {

	public static AIDEEditor getCurrentEditor(AIDEEditorPager aideEditorPager) {
		return AIDEEditorPager.lp(aideEditorPager);
	}

	public static AIDEEditor.t getEditorModel(AIDEEditor aideEditor) {
		return AIDEEditor.jJ(aideEditor);
	}

	public static CodeEditText.EditorView getEditorView(AIDEEditor aideEditor) {
		return AIDEEditor.Ev(aideEditor);
	}

	public static void DW(AIDEEditorCompletion aideEditorCompletion, int p, int p1, List list) {
		AIDEEditorCompletion.DW(aideEditorCompletion, p, p1, list);
	}

	public static AIDEEditor getAIDEEditor(AIDEEditorCompletion aideEditorCompletion) {
		return AIDEEditorCompletion.FH(aideEditorCompletion);
	}

	public static int Hw(AIDEEditorCompletion aideEditorCompletion) {
		return AIDEEditorCompletion.Hw(aideEditorCompletion);
	}

	public static int v5(AIDEEditorCompletion aideEditorCompletion) {
		return AIDEEditorCompletion.v5(aideEditorCompletion);
	}

	public static int Zo(AIDEEditorCompletion aideEditorCompletion) {
		return AIDEEditorCompletion.Zo(aideEditorCompletion);
	}

	public static void VH(AIDEEditorCompletion aideEditorCompletion, String s) {
		AIDEEditorCompletion.VH(aideEditorCompletion, s);
	}

}

