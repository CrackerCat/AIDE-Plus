/**
 * @Date
 * @AIDE AIDE+
 */
package cn.iyutong.aide.translator;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aide.common.AppLog;
import com.aide.ui.ServiceContainer;
import com.aide.ui.command.MenuCommand;
import com.aide.ui.rewrite.R;

import cn.iyutong.aide.YAIDEEditor;
import cn.iyutong.text.EnglishWordTokenizer;
import cn.iyutong.translator.BaiduWebTranslator;
import cn.iyutong.translator.BingWebTranslator;
import cn.iyutong.translator.GoogleCNTranslator;
import cn.iyutong.translator.YandexWebTranslator;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;

public class Translate implements MenuCommand {

    private static final String TAG = "翻译";

    @Override
    public int getMenuItemId() {
        return R.id.yttranslate;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private int yqa = 0;
    private int lxa = 0;
    private TextView fy;

    @Override
    public boolean run() {
        Context context = ServiceContainer.getMainActivity();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dlialog_translate_yt, null);
        //翻译选择
        TextView yq = dialogView.findViewById(R.id.yt_translate_tc_yq);
        yq.setPaintFlags(yq.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //翻译类型
        TextView lx = dialogView.findViewById(R.id.yt_translate_tc_fy_lx);
        lx.setPaintFlags(lx.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //刷新翻译
        LinearLayout sx = dialogView.findViewById(R.id.yt_translate_tc_sx);
        //翻译结果
        fy = dialogView.findViewById(R.id.yt_translate_tc_fy);
        fy.setText("正在翻译...");
        yqa = Integer.parseInt(ZeroAicySetting.getTranslatetcyq());
        lxa = Integer.parseInt(ZeroAicySetting.getTranslatetclx());
        String[] items1 = context.getResources().getStringArray(R.array.iyutong_translate_yq_xs);
        String[] items2 = context.getResources().getStringArray(R.array.iyutong_translate_yq_lx);
        lx.setText(items2[lxa]);
        yq.setText(items1[yqa]);

        yq.setOnClickListener(v -> {

            AlertDialog dialog =
                    new AlertDialog.Builder(context)
                            .setTitle("选择翻译引擎")
                            .setItems(items1, (dia, which) -> {
                                yqa = which;
                                yq.setText(items1[yqa]);
                                if (ZeroAicySetting.isTranslatetcEnable()){
                                    ZeroAicySetting.setTranslatetcyq(yqa+"");
                                }
                                fy.setText("正在翻译...");
                                ThreadPoolService.getDefaultThreadPoolService().submit(this::transl);
                            })
                            .create();
            dialog.show();
        });

        lx.setOnClickListener(v -> {
            AlertDialog dialog =
                    new AlertDialog.Builder(context)
                            .setTitle("选择翻译语言")
                            .setItems(items2, (dia, which) -> {
                                lxa = which;
                                lx.setText(items2[lxa]);
                                if (ZeroAicySetting.isTranslatetcEnable()){
                                    ZeroAicySetting.setTranslatetclx(lxa+"");
                                }
                                fy.setText("正在翻译...");
                                ThreadPoolService.getDefaultThreadPoolService().submit(this::transl);
                            }).create();
            dialog.show();
        });

        sx.setOnClickListener(v -> {
            fy.setText("正在翻译...");
            ThreadPoolService.getDefaultThreadPoolService().submit(this::transl);
        });

        AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView)
                .setPositiveButton("关闭", null)
                .setNegativeButton("复制结果", (dialog1, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard == null) {
                        Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ClipData clip = ClipData.newPlainText("翻译结果", fy.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("替换原文", (dialog1, which) -> {
                    YAIDEEditor.setText(fy.getText().toString());
                    Toast.makeText(context, "替换成功", Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        ThreadPoolService.getDefaultThreadPoolService().submit(this::transl);
        return true;
    }

    private void transl() {
        String from;
        switch (lxa) {
            case 0:
            case 1:
                from = "auto";
                break;
            case 2:
                from = "en";
                break;
            case 3:
                from = "zh";
                break;
            default:
                from = null;
                break;
        }
        String to;
        switch (lxa) {
            case 0:
            case 2:
                to = "zh";
                break;
            case 1:
            case 3:
                to = "en";
                break;
            default:
                to = null;
                break;
        }
        if (from == null) {
            ThreadPoolService.postOfUi(() -> fy.setText("翻译语言设置错误"));
            return;
        }
        try {
            String wz = YAIDEEditor.getText();
            AppLog.d(TAG,wz);
            if (ZeroAicySetting.isEnableTranslatctf()&&wz.matches("^[^\\s\\n]*$")) {
                AppLog.d(TAG,wz);
                wz = EnglishWordTokenizer.joinWithSpace(EnglishWordTokenizer.smartTokenizeEnhanced(wz));
            }
            String result;
            switch (yqa) {
                case 0:
                    result = BingWebTranslator.translate(wz, from, to);
                    break;
                case 1:
                    result = GoogleCNTranslator.translate(Translator.ipLoader, wz, from, to);
                    break;
                case 2:
                    result = YandexWebTranslator.translate(wz, from, to);
                    break;
                case 3:
                    result = BaiduWebTranslator.translate(wz, from, to);
                    break;
                default:
                    result = "错误";
                    break;
            }
            ThreadPoolService.postOfUi(() -> fy.setText(result));
        } catch (Throwable e) {
            AppLog.e(TAG,e);
            ThreadPoolService.postOfUi(() -> fy.setText("选择的文字太多会报错哦。\n尝试换个一个翻译引擎吧！\nBing翻译最多1000字符\n谷歌翻译最多5000字符\n" + e.toString()));
        }
    }

    @Override
    public boolean isVisible(boolean b) {
        return !TextUtils.isEmpty(YAIDEEditor.getText().trim());
    }
}

