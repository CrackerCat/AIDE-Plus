/**
 * @Author ZeroAicy
 * @Date
 * @AIDE AIDE+
 */
package io.github.zeroaicy.aide.completion;

import android.view.View;
import android.widget.AdapterView;
import androidx.annotation.Keep;
import com.aide.engine.SourceEntity;
import com.aide.ui.AIDEEditorCompletion;
import com.aide.ui.ServiceContainer;
import com.aide.ui.views.CompletionListView;
import com.aide.ui.AIDEEditorExtend;
import com.aide.ui.AIDEEditor;
import com.aide.ui.MainActivity;
import com.aide.ui.services.EngineService;

// Lcom/aide/ui/AIDEEditorCompletion$d继承此类
// 必须keep类名 必须实现 AdapterView.OnonItemClickListener 接口
@Keep
public class CompletionItemClick implements AdapterView.OnItemClickListener {

	private final AIDEEditorCompletion aideEditorCompletion;
	private CompletionListView completionListView;
	@Keep
	public CompletionItemClick(AIDEEditorCompletion aideEditorCompletion, CompletionListView completionListView) {
		this.aideEditorCompletion = aideEditorCompletion;
		this.completionListView = completionListView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Object itemAtPosition = this.completionListView.getItemAtPosition(position);
		if (itemAtPosition instanceof SourceEntity) {
			this.aideEditorCompletion.EQ();
			SourceEntity sourceEntity = (SourceEntity) itemAtPosition;
			if (sourceEntity.yS()) {
				// AIDEEditorExtend.
				AIDEEditor aideEditor = AIDEEditorExtend.getAIDEEditor(this.aideEditorCompletion);
				int hw = AIDEEditorExtend.Hw(this.aideEditorCompletion);
				int v5 = AIDEEditorExtend.v5(this.aideEditorCompletion);
				int zo = AIDEEditorExtend.Zo(this.aideEditorCompletion);
				aideEditor.aj(hw, v5, hw, zo, "");
				
				MainActivity mainActivity = ServiceContainer.getMainActivity();
				mainActivity.delayedShowAnalyzingProgressDialog();
				EngineService engineService = ServiceContainer.getEngineService();
				String visibleFile = ServiceContainer.getOpenFileService().getVisibleFile();
				
				engineService.BT(visibleFile, hw, v5, sourceEntity);
			} else {
				if (sourceEntity.gW()) {
					AIDEEditorExtend.VH(this.aideEditorCompletion, sourceEntity.J0());
					MainActivity mainActivity = ServiceContainer.getMainActivity();
					mainActivity.delayedShowAnalyzingProgressDialog();
					EngineService engineService = ServiceContainer.getEngineService();
					String visibleFile = ServiceContainer.getOpenFileService().getVisibleFile();
					engineService.FH(visibleFile, sourceEntity);
					return;
				}
				AIDEEditorExtend.VH(this.aideEditorCompletion, sourceEntity.J0());
			}
		}
	}

}

