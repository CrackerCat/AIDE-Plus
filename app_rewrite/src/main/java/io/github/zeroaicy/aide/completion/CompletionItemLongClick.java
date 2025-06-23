/**
 * @Author ZeroAicy
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.completion;

import android.view.View;
import android.widget.AdapterView;
import com.aide.ui.AIDEEditorCompletion;
import com.aide.ui.views.CompletionListView;
import androidx.annotation.Keep;

// Lcom/aide/ui/AIDEEditorCompletion$d继承此类
// 必须keep类名 必须实现 AdapterView.OnItemLongClickListener 接口
@Keep
public class CompletionItemLongClick implements AdapterView.OnItemLongClickListener {
	
	@Keep
	public CompletionItemLongClick(AIDEEditorCompletion aideEditorCompletion, CompletionListView completionListView) {

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return false;
	}

}

