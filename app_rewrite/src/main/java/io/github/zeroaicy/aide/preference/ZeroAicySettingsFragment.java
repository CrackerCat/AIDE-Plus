package io.github.zeroaicy.aide.preference;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.aide.ui.rewrite.R;

import android.preference.Preference;
import android.content.Intent;
import android.app.Activity;

import cn.iyutong.aide.translator.Translator;
import io.github.zeroaicy.aide.highlight.HighlightActivity;

import android.net.Uri;
import android.widget.Toast;

public class ZeroAicySettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加ZeroAicy扩展设置
        addPreferencesFromResource(R.xml.preferences_setting_zeroaicy);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        setOnPreferenceClickListener("zero_aicy_preference_highlight", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                activity.startActivity(
                        new Intent(activity, HighlightActivity.class).putExtra("title", preference.getTitle()));
                //  getActivity().overridePendingTransition(android.R.anim.fade_in,
                // android.R.anim.fade_out);
                //getActivity().overridePendingTransition(0, 0);

                return false;
            }
        });

        setOnPreferenceClickListener("iyutong_project_Punctuation", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("使用说明与注意事项")
                        .setMessage("每个按键用空格或者换行隔开，注意旧的底部栏只支持单字符输入并且只支持 #TAB 这一个转义符，其他均不支持，不支持的东西将不显示，转义符必须使用全大写\n\n特殊转义符号含义：\n\n" +
                                "功能按键：\n" +
                                "#TAB        Tab键\n" +
                                "#LEFT       方向键左\n" +
                                "#RIGHT      方向键右\n" +
                                "#UP         方向键上\n" +
                                "#DOWN       方向键下\n" +
                                "#HOME       光标移到行首\n" +
                                "#END        光标移到行尾\n\n" +
                                "替换符号：\n" +
                                "#KG         空格\n" +
                                "#ZH         -符号\n" +
                                "#HH         换行\n\n" +
                                "事件按键：\n" +
                                "{单击输入-长按输入}\n" +
                                "{单击输入-长按输入-按键文字}\n" +
                                "[点击输入-按键文字]\n" +
                                "(长按输入-按键文字))\n" +
                                "(长按输入)")
                        .setPositiveButton("知道了", (dialog, which) -> {
                        })
                        .show();
                return false;
            }
        });

        //清楚本地数据库
        setOnPreferenceClickListener("iyutong_translate",
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("清楚本地数据库")
                                .setMessage("清楚本地数据库中已翻译的内容，清楚后下次需要重新翻译，是否确认清楚？")
                                .setPositiveButton("确定", (dialog, which) -> {
                                    Translator.clearall();
                                    Toast.makeText(getActivity(), "数据库清楚完成", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("取消", (dialog, which) -> {

                                })
                                .show();
                        return false;
                    }
                });

        // 官网
        setOnPreferenceClickListener("zero_aicy_relationship_official_website",
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Activity activity = getActivity();
                        openUrl(activity, "https://plus.androidide.cn");
                        return false;
                    }
                });
        // QQ群
        setOnPreferenceClickListener("zero_aicy_relationship_qq_group", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                openUrl(activity,
                        "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=487145957&card_type=group");
                return false;
            }
        });
        // QQ频道
        setOnPreferenceClickListener("zero_aicy_relationship_qq_guild", new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Activity activity = getActivity();
                openUrl(activity,
                        "mqq://forward/url?src_type=web&version=1&url_prefix=aHR0cHM6Ly9wZC5xcS5jb20vcy9ianA4b3F4bTA=");
                return false;
            }
        });

        // 开源地址 github
        setOnPreferenceClickListener("zero_aicy_relationship_open_source_github",
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Activity activity = getActivity();
                        openUrl(activity, "https://github.com/ZeroAicy/AIDE-Plus");
                        return false;
                    }
                });
        // 开源地址 gitee
        setOnPreferenceClickListener("zero_aicy_relationship_open_source_gitee",
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Activity activity = getActivity();
                        openUrl(activity, "https://gitee.com/ZeroAicy/AIDE-Plus");
                        return false;
                    }
                });

    }

    private void setOnPreferenceClickListener(String key,
                                              Preference.OnPreferenceClickListener onPreferenceClickListener) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setOnPreferenceClickListener(onPreferenceClickListener);
        } else {
            //Toasty.error(String.format("找不到%s",key)).show();
        }
    }

    public static void openUrl(Activity activity, String url) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

}

