package io.github.zeroaicy.aide.preference;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.aide.ui.rewrite.R;
import com.tencent.mmkv.MMKV;

import android.preference.Preference;
import android.content.Intent;
import android.app.Activity;

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

        //清楚本地数据库
        setOnPreferenceClickListener("iyutong_translate",
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("清楚本地数据库")
                                .setMessage("清楚本地数据库中已翻译的内容，清楚后下次需要重新翻译，是否确认清楚？")
                                .setPositiveButton("确定", (dialog, which) -> {
                                    MMKV kv = MMKV.mmkvWithID("Ytranslator", MMKV.MULTI_PROCESS_MODE);
                                    kv.clearAll();
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

