package cn.iyutong.aide;


import android.content.SharedPreferences;
import android.widget.TextView;

import com.aide.common.AppLog;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.iyutong.text.EnglishWordTokenizer;
import cn.iyutong.translator.BaiduWebTranslator;
import cn.iyutong.translator.BingWebTranslator;
import cn.iyutong.translator.GoogleCNTranslator;
import cn.iyutong.translator.IPLoader;
import cn.iyutong.translator.YandexWebTranslator;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;

public class Translator {

    private static List<String> list = new ArrayList<>();
    private static final String TAG = "Ytranslator";
    private static MMKV kv = MMKV.mmkvWithID("Ytranslator", MMKV.MULTI_PROCESS_MODE);

    private static final IPLoader ipLoader = new IPLoader();

    public static String text(String text) {
        String ck = kv.decodeString(text);
        if (ck != null && !ck.isEmpty() && !ck.equals("@Iyutong翻译失败@IyutongAuto")) {
            AppLog.d(TAG, "数据库中存在，直接返回");
            return ck;
        }

        if (ZeroAicySetting.isEnableTranslatesbd()){
            AppLog.d(TAG, "未开启网络翻译");
            return null;
        }

        if (list.contains(text)) {
            AppLog.d(TAG, "翻译中...");
            return "翻译中...";
        } else {
            AppLog.d(TAG, "正在翻译...");
            ThreadPoolService.getDefaultThreadPoolService().execute(() -> {
                list.add(text);
                transl(text,0);
            });
            if (ck != null && ck.equals("@Iyutong翻译失败@IyutongAuto")) {
                return "翻译失败,正在重新翻译...";
            } else {
                return "正在翻译...";
            }
        }
    }

    private static void transl(String text,int a){
        try {
            int b = Integer.parseInt(ZeroAicySetting.getTranslateyq());
            String result;
            String wz = text;
            if (ZeroAicySetting.isEnableTranslatesfg()){
                wz = EnglishWordTokenizer.joinWithSpace(EnglishWordTokenizer.smartTokenizeEnhanced(text));
            }
            switch ((b+a)%4){
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
            kv.encode(text,result);
            list.remove(text);
            AppLog.d(TAG, "翻译成功");
        } catch (Exception e) {
            e.printStackTrace();
            if (ZeroAicySetting.isEnableTranslateyq()){
                if (a>=3){
                    AppLog.d(TAG, "翻译失败");
                    list.remove(text);
                    kv.encode(text,"@Iyutong翻译失败@IyutongAuto");
                    return;
                }
                AppLog.d(TAG, "翻译失败,重试中..." + a);
                transl(text,a+1);
                return;
            }
            AppLog.d(TAG, "翻译失败...");
            list.remove(text);
            kv.encode(text,"@Iyutong翻译失败@IyutongAuto");
        }
    }

}
