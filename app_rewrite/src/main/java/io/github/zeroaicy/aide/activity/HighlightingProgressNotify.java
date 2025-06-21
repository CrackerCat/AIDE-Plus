/**
 * @Date 
 * @AIDE AIDE+ 
 */
package io.github.zeroaicy.aide.activity;

import android.view.View;
import com.aide.engine.FileHighlightings;
import com.aide.ui.AIDEAnalysisProgressBar;
import com.aide.ui.AIDEEditorPager;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.rewrite.R;
import com.aide.ui.services.OpenFileService;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;

public class HighlightingProgressNotify implements Runnable, OpenFileService.FileHighlightingsCallback {

	AIDEAnalysisProgressBar analysisProgressBar;
	AIDEEditorPager aideEditorPager;

	OpenFileService openFileService;
	
	int waitCodeAnalysisCount;

	int highlightingNumber;

	int openedFileNumber;

	boolean isHide;

	int maxPoll = 200;
	int curPoll = 0;

	@Override
	public void run() {
		if (this.analysisProgressBar == null) {
			MainActivity mainActivity = ServiceContainer.getMainActivity();
			this.aideEditorPager = mainActivity.getAIDEEditorPager();
			this.openedFileNumber = this.aideEditorPager.getFileEditors().size();
			this.analysisProgressBar = mainActivity.findViewById(R.id.mainErrorProgress);
			
			this.openFileService = ServiceContainer.getOpenFileService();
			
			// add FileHighlightingsCallback
			ServiceContainer.getOpenFileService().FH(this);

			this.analysisProgressBar.setVisibility(View.VISIBLE);
			this.analysisProgressBar.setProgress(0);
		}

		// 应当有两部分 
		// 一部分是 等待 代码分析进程启动的时间 占 50
		// 一部分是 接收 高亮信息的时间

		int waitMaxValue = this.openedFileNumber == 0 ? 100 : 80;

		// 没有接收高亮信息 且 小与 最大值
		this.waitCodeAnalysisCount = (this.highlightingNumber == 0 && waitCodeAnalysisCount < waitMaxValue)
				? waitCodeAnalysisCount + 2
				: waitMaxValue;

		// 没有打开的文件 就只管等待
		int progress = this.openedFileNumber == 0
				? waitCodeAnalysisCount
				: waitCodeAnalysisCount + (this.highlightingNumber * 20 / openedFileNumber);

		this.analysisProgressBar.setProgress(progress);

		if (this.isHide) {
			// 退出
			this.analysisProgressBar.setVisibility(View.INVISIBLE);
			// 移除监听器
			if( this.openFileService != null ) this.openFileService.SI(this);
			return;
		}

		// 高亮信息全部已接收
		if (waitCodeAnalysisCount >= waitMaxValue && this.highlightingNumber >= this.openedFileNumber) {
			// 标记 轮询结束
			this.isHide = true;
		}

		// 防止死循环
		if (curPoll >= maxPoll || this.analysisProgressBar.getVisibility() == View.INVISIBLE) {
			// 标记 轮询结束
			this.isHide = true;
		}

		curPoll++;
		// 轮询
		ThreadPoolService.postDelayedOfUi(this, 500);
	}

	@Override
	public void accept(FileHighlightings fileHighlightings) {
		this.highlightingNumber++;
	}
}

