/**
 * @Author ZeroAicy
 * @AIDE AIDE+
 */

package abcd;

import com.aide.common.AppLog;
import com.aide.common.KeyStroke;
import com.aide.ui.AIDEEditor;
import com.aide.ui.AIDEEditorExtend;
import com.aide.ui.AIDEEditorPager;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.command.KeyStrokeCommand;
import com.aide.ui.command.MenuItemCommand;
import com.aide.ui.rewrite.R;
import com.aide.ui.services.EngineService;
import com.aide.ui.services.OpenFileService;
import com.aide.ui.util.FileSpan;
import com.aide.ui.util.FileSystem;
import com.aide.ui.views.editor.EditorModel;
import com.aide.ui.views.editor.SelectedRegion;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * 代码格式化
 */
public class ma implements MenuItemCommand, KeyStrokeCommand {

	public ma() {
	}

	public KeyStroke getKeyStroke() {
		return new KeyStroke(34, true, true, false);
	}

	public int getMenuItemId() {
		return R.id.editorMenuFormatCode;
	}

	public String getName() {
		return "Format Code";
	}

	public boolean isEnabled() {
		if (ServiceContainer.getOpenFileService().Ws()) {
			if (ServiceContainer.getOpenFileService().XL()) {
				return true;
			}
		}
		return false;
	}

	private static String getEditorText(EditorModel editorModel) {
		EditorModel.h textBuffer = editorModel.pN(new char[0x8000]);
		return String.valueOf(textBuffer.j6, 0, textBuffer.DW);
	}

	private void formatFormAIDE(AIDEEditorPager aideEditorPager) {
		FileSpan currentFileSpan = aideEditorPager.getCurrentFileSpan();
		MainActivity mainActivity = ServiceContainer.getMainActivity();
		mainActivity.delayedShowAnalyzingProgressDialog();

		// selectionStartLine
		int startLine = currentFileSpan.DW;
		// selectionEndLine
		int endLine = currentFileSpan.Hw;

		if (startLine == endLine) {
			endLine = aideEditorPager.getCurrentFileLineCount();
			startLine = 1;
		}
		OpenFileService openFileService = ServiceContainer.getOpenFileService();
		EngineService engineService = ServiceContainer.getEngineService();
		String visibleFile = openFileService.getVisibleFile();
		int tabSize = aideEditorPager.getTabSize();
		engineService.Hw(visibleFile, startLine, endLine, tabSize);
	}
	@Override
	public boolean run() {
		AIDEEditorPager aideEditorPager = ServiceContainer.getMainActivity().getAIDEEditorPager();
		String currentFilePath = aideEditorPager.getVisibleFile();
		if (currentFilePath == null) {
			return true;
		}

		String suffixName = FileSystem.getSuffixName(currentFilePath).toLowerCase();
		if ("c".equals(suffixName) || "h".equals(suffixName) || "cpp".equals(suffixName)) {
			
			return true;
		}
		if (ZeroAicySetting.isEnableEclipseJavaFormat() && "java".equals(suffixName)) {
			AIDEEditor currentEditor = AIDEEditorExtend.getCurrentEditor(aideEditorPager);
			ThreadPoolService defaultThreadPoolService = ThreadPoolService.getDefaultThreadPoolService();
			defaultThreadPoolService.submit(new JavaFormatterRunnable(currentEditor));
			return true;
		}

		formatFormAIDE(aideEditorPager);
		return true;
	}

	public static class JavaFormatterRunnable implements Runnable {

		AIDEEditor currentEditor;

		public JavaFormatterRunnable(AIDEEditor currentEditor) {
			this.currentEditor = currentEditor;
		}

		@Override
		public void run() {
			try {
				synchronized (currentEditor) {
					final AIDEEditor.t editorModel = AIDEEditorExtend.getEditorModel(currentEditor);
					if (editorModel == null) {
						return;
					}
					synchronized (editorModel) {
						format(editorModel);
					}
				}
			} catch (Throwable e) {
				AppLog.e("Format Code", e);
			}

		}

		// 防止污染 defaultSettingsMap
		private static final Map<String, String> defaultSettingsMap = DefaultCodeFormatterOptions.getDefaultSettings()
				.getMap();

		private void format(AIDEEditor.t editorModel) throws MalformedTreeException, BadLocationException {
			String inputText = getEditorText(editorModel);

			IDocument doc = new Document(inputText);

			int kind = CodeFormatter.K_COMPILATION_UNIT;

			// 防止污染 defaultSettingsMap
			HashMap<String, String> settings = new HashMap<String, String>(defaultSettingsMap);
			// DefaultCodeFormatterOptions options = new DefaultCodeFormatterOptions(settings);
			DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(settings);

			int indentationLevel = 0;
			String lineSeparator = "\n";
			// 修改增量 基于 charArray offset
			TextEdit edit = codeFormatter.format(kind, inputText, 0, inputText.length(), indentationLevel,
					lineSeparator);
			if (edit == null) {
				return;
			}
			// 提交修改
			edit.apply(doc);

			String outputText = doc.get();
			// 在主线程修改 EditorModel
			currentEditor.post(new ApplyTextRunnable(currentEditor, outputText));
		}

	}

	// 填充格式化文本
	public static class ApplyTextRunnable implements Runnable {
		AIDEEditor currentEditor;
		String formatterOutputText;
		public ApplyTextRunnable(AIDEEditor currentEditor, String formatterOutputText) {
			this.currentEditor = currentEditor;
			this.formatterOutputText = formatterOutputText;
		}

		@Override
		public void run() {
			synchronized (currentEditor) {
				final AIDEEditor.t editorModel = AIDEEditorExtend.getEditorModel(currentEditor);
				if (editorModel == null) {
					return;
				}
				synchronized (editorModel) {
					String outputText = this.formatterOutputText;

					int startLine = 1;
					int startColumn = 1;

					int endLine = editorModel.getLineCount();

					int endColumn = editorModel.getColumnCount(endLine);

					// 替换 行列皆以 1开始
					// editorModel.cb(startLine, startColumn, endLine + 1, endColumn + 1, outputText, false, true);

					AIDEEditor.EditorView editorView = AIDEEditorExtend.getEditorView(this.currentEditor);

					this.currentEditor.getKeyStrokeDetector().U2();

					int caretLine = editorView.getCaretLine();
					int caretColumn = editorView.getCaretColumn();
					// 清空数据
					editorModel.Bx(new SelectedRegion(0, 0, endLine, endColumn), editorModel);
					// 写入格式化 文本
					editorModel.a5(0, 0, new StringReader(outputText), editorModel);

					editorView.TI(caretColumn, caretLine);
				}
			}
		}

	}

	//									if( !true ){
	//										// 废弃
	//										ServiceContainer.getRefactoringService().DW( test(currentFilePath, inputText, editorModel, edit));
	//										return;
	//									}

	//	public List<Modification> test(String filePath, String inputText, EditorModel editorModel, TextEdit edit) {
	//		int lineCount = editorModel.getLineCount();
	//
	//		int[] lineEnds = new int[lineCount];
	//		// 构造 lines
	//		for (int line = 0, offset = 0; line < lineCount; line++) {
	//			// 最后一位是换行符
	//			offset += editorModel.getColumnCount(line) + 1;
	//			lineEnds[line] = offset;
	//		}
	//		System.out.println(Arrays.toString(lineEnds));
	//
	//		AppLog.println_d("****************************************");
	//		AppLog.println_d(inputText);
	//		AppLog.println_d("****************************************");
	//
	//		List<Modification> modifications = new ArrayList<>();
	//		// 都是 ReplaceEdit
	//		for (TextEdit child : edit.getChildren()) {
	//			if (!(child instanceof ReplaceEdit)) {
	//				continue;
	//			}
	//
	//			ReplaceEdit replaceEdit = (ReplaceEdit) child;
	//
	//			int startOffset = replaceEdit.getOffset();
	//
	//			int replaceEditLength = replaceEdit.getLength();
	//			if (replaceEditLength == 0) {
	//				continue;
	//			}
	//			int endOffset = startOffset + replaceEditLength;
	//
	//			if (endOffset < startOffset) {
	//				int temp = endOffset;
	//				endOffset = startOffset;
	//				startOffset = temp;
	//			}
	//
	//			int startLine = startOffset >= 0 ? Util.getLineNumber(startOffset, lineEnds, 0, lineEnds.length - 1) : 0;
	//			int startColumn = startOffset >= 0 ? Util.searchColumnNumber(lineEnds, startLine, startOffset) : 0;
	//
	//			int endLine = endOffset >= 0 ? Util.getLineNumber(endOffset, lineEnds, 0, lineEnds.length - 1) : 0;
	//
	//			int endColumn = endOffset >= 0 ? Util.searchColumnNumber(lineEnds, endLine, endOffset) : 0;
	//			// 
	//			endColumn += 1;
	//
	//			AppLog.println_d("startOffset %s, [%s, %s]", startOffset, startLine, startColumn);
	//			AppLog.println_d("endOffset %s, [%s, %s]", endOffset, endLine, endColumn);
	//			AppLog.println_d(child.toString());
	//
	//			// 从 1开始
	//			//*
	//			// startLine += 1;
	//			// endLine += 1;
	//
	//			// startColumn += 1;
	//			//*/
	//			Modification modification = new Modification(startLine, startColumn, endLine, endColumn, filePath,
	//					replaceEdit.getText(), false) {
	//			};
	//			System.out.println(modification);
	//
	//			modifications.add(modification);
	//			System.out.println();
	//		}
	//		return modifications;
	//	}

}

