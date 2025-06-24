package cn.iyutong.aide;

import cn.iyutong.text.EnglishWordTokenizer;
import cn.iyutong.translator.BaiduWebTranslator;
import cn.iyutong.translator.BingWebTranslator;
import cn.iyutong.translator.GoogleCNTranslator;
import cn.iyutong.translator.IPLoader;
import cn.iyutong.translator.YandexWebTranslator;
import com.aide.common.AppLog;
import com.tencent.mmkv.MMKV;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import android.text.TextUtils;

public class Translator {

	private static final Set<String> transl_text_set = Collections.synchronizedSet(new HashSet<>());
	private static final String TAG = "Ytranslator";
	private static final String TranslatorFail = "@Iyutong翻译失败@IyutongAuto";
	private static final MMKV kv = MMKV.mmkvWithID("Ytranslator", MMKV.SINGLE_PROCESS_MODE);

	private static final IPLoader ipLoader = new IPLoader();

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

	private static void transl(String text, int a) {
		try {
			int b = Integer.parseInt(ZeroAicySetting.getTranslateyq());
			String result;
			String wz = text;
			if (ZeroAicySetting.isEnableTranslatesfg()) {
				wz = EnglishWordTokenizer.joinWithSpace(EnglishWordTokenizer.smartTokenizeEnhanced(text));
			}
			switch ((b + a) % 4) {
				case 0 :
					result = BingWebTranslator.translate(wz, "auto", "zh-CN");
					break;
				case 1 :
					result = GoogleCNTranslator.translate(ipLoader, wz, "auto", "zh");
					break;
				case 2 :
					result = YandexWebTranslator.translate(wz, "auto", "zh");
					break;
				case 3 :
					result = BaiduWebTranslator.translate(wz, "auto", "zh");
					break;
				default :
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

