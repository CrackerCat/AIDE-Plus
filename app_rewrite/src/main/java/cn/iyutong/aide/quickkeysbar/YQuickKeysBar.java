package cn.iyutong.aide.quickkeysbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aide.common.AndroidHelper;
import com.aide.ui.MainActivity;
import com.aide.ui.rewrite.R;

import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class YQuickKeysBar {

    private MainActivity mainActivity;
    private View quickkeysbarView;

    public YQuickKeysBar(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.quickkeysbarView = LayoutInflater.from(mainActivity).inflate(R.layout.yquickkeysbar,
                (ViewGroup) mainActivity.findViewById(R.id.mainQuickKeyBarContainer));
        zk(AndroidHelper.getVerticalScreenWidthInDp(mainActivity) >= 360.0f
                && AndroidHelper.isNotTelevisionMode(mainActivity));
        this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButton).setOnClickListener(v -> zk(true));
        this.quickkeysbarView.findViewById(R.id.quickKeyBarCloseButton).setOnClickListener(v -> zk(false));

    }

    private boolean zhsj;

    public void zk(boolean zhsj) {
        this.zhsj = zhsj;
        if (zhsj) {
            this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(View.GONE);
            this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(View.VISIBLE);
        } else {
            this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(View.VISIBLE);
            this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(View.GONE);
        }
    }

    //显示
    public void show(boolean z){
        if (this.quickkeysbarView != null) {
            this.quickkeysbarView.findViewById(R.id.quickKeyBar).setVisibility(z ? View.VISIBLE : View.INVISIBLE);
        }
    }

    //传入快捷符号
    public void gn(String str) {
        if (this.quickkeysbarView == null || str == null) {
            return;
        }
        if (ZeroAicySetting.isEnabledblxyszt().equals("始终显示"))
            this.quickkeysbarView.findViewById(R.id.quickKeyBar).setVisibility(View.VISIBLE);
        RecyclerView recyclerView = quickkeysbarView.findViewById(R.id.dmtckjj);
        DmtckjjAdapter dmtckjjAdapter = new DmtckjjAdapter(str.split(" "));
        recyclerView.setAdapter(dmtckjjAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false));
    }

    //高度
    public int v5(){
        if (this.zhsj){
            return this.quickkeysbarView.findViewById(R.id.quickKeyBar).getMeasuredHeight();
        }
        return 0;
    }

}
