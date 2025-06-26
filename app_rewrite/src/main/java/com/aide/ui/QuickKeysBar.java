/**
 * @Date
 * @AIDE AIDE+
 */
package com.aide.ui;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.aide.common.AndroidHelper;
import com.aide.ui.rewrite.R;

import cn.iyutong.aide.YAIDEEditor;
import cn.iyutong.aide.quickkeysbar.YQuickKeysBar;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;

@Keep
public class QuickKeysBar {

    YQuickKeysBar yQuickKeysBar;
    QuickKeysBarFormAide quickKeysBarFormAide;

    @Keep
    public QuickKeysBar(MainActivity mainActivity) {
        if (ZeroAicySetting.isEnabledblxys()) {
            yQuickKeysBar = new YQuickKeysBar(mainActivity);

            return;
        }
        quickKeysBarFormAide = new QuickKeysBarFormAide(mainActivity);
    }

    @Keep
    public void gn(String s) {
        if (yQuickKeysBar != null) {
            yQuickKeysBar.gn(s);
            return;
        }
        quickKeysBarFormAide.gn(s);
    }

    @Keep
    public int v5() {
        if (yQuickKeysBar != null) {
            return yQuickKeysBar.v5();
        }
        return quickKeysBarFormAide.v5();
    }

    @Keep
    public void show(boolean show) {
        if (YAIDEEditor.isyuwenjian()) {
            switch (ZeroAicySetting.isEnabledblxyszt()) {
                case "始终显示":
                    show = true;
                    break;
                case "始终隐藏":
                    show = false;
                    break;
            }
        }
        if (yQuickKeysBar != null) {
            yQuickKeysBar.show(show);
            return;
        }
        quickKeysBarFormAide.show(show);
    }

    public static class QuickKeysBarFormAide {


        private String FH;

        private boolean Hw;

        private MainActivity mainActivity;

        private View quickkeysbarView;

        private KeyCharacterMap v5;

        static KeyCharacterMap DW(QuickKeysBarFormAide quickKeysBar) {
            return quickKeysBar.v5;
        }

        static KeyCharacterMap FH(QuickKeysBarFormAide quickKeysBar, KeyCharacterMap keyCharacterMap) {
            quickKeysBar.v5 = keyCharacterMap;
            return keyCharacterMap;
        }

        public QuickKeysBarFormAide(MainActivity mainActivity) {
            this.FH = "";
            this.mainActivity = mainActivity;
            this.quickkeysbarView = LayoutInflater.from(mainActivity).inflate(R.layout.quickkeysbar,
                    (ViewGroup) mainActivity.findViewById(R.id.mainQuickKeyBarContainer));
            VH(AndroidHelper.getVerticalScreenWidthInDp(mainActivity) >= 360.0f
                    && AndroidHelper.isNotTelevisionMode(mainActivity));
            this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VH(true);
                }
            });
            this.quickkeysbarView.findViewById(R.id.quickKeyBarCloseButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VH(false);
                }
            });
        }

        public void VH(boolean z) {
            this.Hw = z;
            if (z) {
                this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(View.GONE);
                this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(View.VISIBLE);
            } else {
                this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(View.VISIBLE);
                this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(View.GONE);
            }
        }

        public void gn(String str) {
            float f;
            float f2;
            if (this.quickkeysbarView == null || str == null || this.FH.equals(str)) {
                return;
            }
            if (ZeroAicySetting.isEnabledblxyszt().equals("始终显示"))
                this.quickkeysbarView.findViewById(R.id.quickKeyBar).setVisibility(View.VISIBLE);
            this.FH = str;
            LayoutInflater from = LayoutInflater.from(this.mainActivity);
            if (AndroidHelper.getVerticalScreenWidthInDp(this.mainActivity) >= 400.0f) {
                f = 60.0f;
                f2 = this.mainActivity.getResources().getDisplayMetrics().density;
            } else {
                f = 30.0f;
                f2 = this.mainActivity.getResources().getDisplayMetrics().density;
            }
            int i = (int) (f2 * f);
            int i2 = (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
            ViewGroup viewGroup = (ViewGroup) this.quickkeysbarView.findViewById(R.id.quickKeyBarList);
            viewGroup.removeAllViews();
            for (String str2 : str.split(" ")) {
                String replace = str2.replace("s", " ");
                TextView textView = (TextView) from.inflate(R.layout.quickkeysbar_key, (ViewGroup) null);
                if (replace.trim().length() == 0) {
                    textView.setText("⇥");
                } else {
                    textView.setText(replace);
                }
                viewGroup.addView(textView, new LinearLayout.LayoutParams(i, i2));
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (QuickKeysBarFormAide.DW(QuickKeysBarFormAide.this) == null) {
                            QuickKeysBarFormAide.FH(QuickKeysBarFormAide.this, KeyCharacterMap.load(-1));
                        }
                        KeyEvent[] events = QuickKeysBarFormAide.DW(QuickKeysBarFormAide.this)
                                .getEvents(replace.toCharArray());
                        if (events != null) {
                            for (KeyEvent keyEvent : events) {
                                mainActivity.dispatchKeyEvent(keyEvent);
                            }
                        }
                    }
                });
            }
        }

        public void show(boolean z) {
            if (this.quickkeysbarView != null) {
                this.quickkeysbarView.findViewById(R.id.quickKeyBar).setVisibility(z ? View.VISIBLE : View.INVISIBLE);
            }
        }

        public int v5() {
            if (this.Hw) {
                return (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
            }
            return 0;
        }
    }

}

