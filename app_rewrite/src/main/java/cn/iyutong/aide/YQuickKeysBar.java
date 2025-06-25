package cn.iyutong.aide;

import com.aide.ui.MainActivity;
import com.aide.ui.QuickKeysBar;

public class YQuickKeysBar {

    private QuickKeysBar quickKeysBar;

    public YQuickKeysBar(MainActivity mainActivity){
        quickKeysBar = new QuickKeysBar(mainActivity);
    }

    //显示
    public void show(boolean z){
        quickKeysBar.show(z);
    }

    //
    public void gn(String str) {
        quickKeysBar.gn(str);
    }

    //
    public int v5(){
        return quickKeysBar.v5();
    }

}
