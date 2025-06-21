package io.github.zeroaicy.aide.command;

import com.aide.ui.ServiceContainer;
import com.aide.ui.command.MenuItemCommand;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.rewrite.R;
import com.topjohnwu.superuser.io.SuFile;

import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class GotoResCommand implements MenuItemCommand {

    @Override
    public int getMenuItemId() {
        return R.id.filebrowserShowResFolder;
    }

    @Override
    public boolean isEnabled() {
        String currentDir = ServiceContainer.getFileBrowserService().j6();
        String currentAppHome = ZeroAicySetting.getCurrentAppHome();

        String resDir = GradleTools.yS(currentAppHome);
        SuFile resFile = new SuFile(resDir);
        SuFile currentFile = new SuFile(currentDir);


        if (!resFile.exists()||!resFile.isDirectory()) {
            return false;
        }
        return !resFile.equals(currentFile);
    }

    @Override
    public boolean run() {
        String currentAppHome = ZeroAicySetting.getCurrentAppHome();
        String resDir = GradleTools.yS(currentAppHome);
        ServiceContainer.getFileBrowserService().Hw(resDir);
        return true;
    }
}
