package io.github.zeroaicy.aide.commands;

import com.aide.ui.ServiceContainer;
import com.aide.ui.command.FileBrowserCommand;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.rewrite.R;
import com.topjohnwu.superuser.io.SuFile;

import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class IconManagerCommand implements FileBrowserCommand {
    @Override
    public int getIconId() {
        return R.drawable.ic_vectoric_design;
    }

    @Override
    public int getNameId() {
        return R.string.command_files_icon_manager;
    }

    @Override
    public boolean isVisible(boolean b) {
        String currentDir = ServiceContainer.getFileBrowserService().j6();
        String currentAppHome = ZeroAicySetting.getCurrentAppHome();

        String resDir = GradleTools.yS(currentAppHome);
        SuFile resFile = new SuFile(resDir);
        SuFile currentFile = new SuFile(currentDir);


        if (!resFile.exists()||!resFile.isDirectory()) {
            return false;
        }
        return currentFile.equals(resFile);
    }

    @Override
    public boolean run() {

        return true;
    }
}
