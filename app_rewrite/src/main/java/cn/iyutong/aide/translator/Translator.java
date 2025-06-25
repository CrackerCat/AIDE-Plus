package cn.iyutong.aide.translator;

import cn.iyutong.text.EnglishWordTokenizer;
import cn.iyutong.translator.BaiduWebTranslator;
import cn.iyutong.translator.BingWebTranslator;
import cn.iyutong.translator.GoogleCNTranslator;
import cn.iyutong.translator.IPLoader;
import cn.iyutong.translator.YandexWebTranslator;
import com.aide.common.AppLog;
import com.aide.ui.rewrite.R;
import com.aide.ui.views.CompletionListView;
import com.blankj.utilcode.util.SizeUtils;
import com.tencent.mmkv.MMKV;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Translator {

	private static final Set<String> transl_text_set = Collections.synchronizedSet(new HashSet<>());
	private static final String TAG = "Ytranslator";
	private static final String TranslatorFail = "@Iyutong翻译失败@IyutongAuto";
	private static final MMKV kv = MMKV.mmkvWithID("Ytranslator", MMKV.SINGLE_PROCESS_MODE);

	public static final IPLoader ipLoader = new IPLoader();

	//获取翻译内容
	public static String text(String text) {
		if (TextUtils.isEmpty(text.trim())) {
			return null;
		}
		String ck = kv.decodeString(text);
		if (!TextUtils.isEmpty(ck) && !ck.equals(TranslatorFail)) {
			return ck;
		}

		if (ZeroAicySetting.isEnableTranslatesbd()) {
			return null;
		}

		if (transl_text_set.contains(text)) {
			return "翻译中...";
		} else {
			transl_text_set.add(text);
			ThreadPoolService.getDefaultThreadPoolService().submit(() -> {
				transl(text, 0);
				transl_text_set.remove(text);
			});
            if (ck != null && ck.equals(TranslatorFail)) {
                return "翻译失败,正在重新翻译...";
            }
            return "正在翻译...";
		}
	}

	//长按事件
	public static void clongclick(Menu menu, CompletionListView completionListView,View view) {
		Context context = view.getContext();
		BaseAdapter adapter = (BaseAdapter) completionListView.getAdapter();
		TextView textView = view.findViewById(R.id.completionEntryNamebf);
		String text = textView.getText().toString();
		String ck = kv.decodeString(text);
		boolean isTransl = !TextUtils.isEmpty(ck) && !ck.equals(TranslatorFail);
		menu.add(isTransl?"修改翻译":"翻译内容")
				.setOnMenuItemClickListener(
                        p1 -> {
                            final ScrollView dialogView = new ScrollView(context);
                            LinearLayout rootView = new LinearLayout(context);
                            rootView.setOrientation(LinearLayout.VERTICAL);
                            rootView.setPadding(SizeUtils.dp2px(24), SizeUtils.dp2px(10), SizeUtils.dp2px(24), 0);
                            EditText mEditText1 = new EditText(context);
                            mEditText1.setText(text);
                            mEditText1.setMaxLines(2);
                            mEditText1.setEllipsize(TextUtils.TruncateAt.END);
                            TextView mText2 = new TextView(context);
                            mText2.setText("翻译后：");
                            mText2.setPadding(0, SizeUtils.dp2px(16), 0, SizeUtils.dp2px(5));
                            EditText mEditText2 = new EditText(context);
                            if (isTransl) {
                                mEditText2.setText(ck);
                            }
                            rootView.addView(mEditText1);
                            rootView.addView(mText2);
                            rootView.addView(mEditText2);
                            final EditText editor = mEditText2;
                            dialogView.addView(rootView, -1, -1);
                            dialogView.setFillViewport(true);
                            AlertDialog dialog =
                                    new AlertDialog.Builder(context)
                                            .setTitle(p1.getTitle())
                                            .setView(dialogView)
                                            .setPositiveButton(
                                                    "保存",
                                                    (dialog1, which) -> {
                                                        String txt = editor.getText().toString();
                                                        kv.encode(text, txt);
                                                        adapter.notifyDataSetChanged();
                                                    })
                                            .setNegativeButton(android.R.string.cancel, null)
                                            .create();
                            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            dialog.show();
                            editor.selectAll();
                            editor.requestFocus();
                            return false;
                        });
		if (isTransl) {
			menu.add("删除翻译")
					.setOnMenuItemClickListener(
                            p1 -> {
                                kv.removeValueForKey(text);
                                adapter.notifyDataSetChanged();
                                return false;
                            });
		}
	}

	//清楚所有翻译
	public static void clearall() {
		kv.clearAll();
	}

	//翻译
	private static void transl(String text, int a) {
		try {
			int b = Integer.parseInt(ZeroAicySetting.getTranslateyq());
			String wz = text;
			if (ZeroAicySetting.isEnableTranslatesfg()) {
				wz = EnglishWordTokenizer.joinWithSpace(EnglishWordTokenizer.smartTokenizeEnhanced(text));
			}
			String result;
			switch ((b + a) % 4) {
				case 0:
					result = BingWebTranslator.translate(wz, "auto", "zh-CN");
					break;
				case 1:
					result = GoogleCNTranslator.translate(ipLoader, wz, "auto", "zh");
					break;
				case 2:
					result = YandexWebTranslator.translate(wz, "auto", "zh");
					break;
				case 3:
					result = BaiduWebTranslator.translate(wz, "auto", "zh");
					break;
				default:
					result = "错误";
					break;
			}
			kv.encode(text, result);
		} catch (Throwable e) {
			if (!ZeroAicySetting.isEnableTranslateyq()) {
				AppLog.e(TAG, "翻译失败", e);
				kv.encode(text, TranslatorFail);
			}

			if (a >= 3) {
				AppLog.d(TAG, "翻译失败");
				kv.encode(text, TranslatorFail);
				return;
			}
			AppLog.d(TAG, "翻译失败,重试中...." + a);
			transl(text, a + 1);
		}
	}

}

