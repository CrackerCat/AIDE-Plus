package io.github.zeroaicy.aide.commands;

import com.aide.ui.ServiceContainer;
import com.aide.ui.command.MenuCommand;
import com.aide.ui.project.internal.GradleTools;
import com.aide.ui.rewrite.R;
import com.topjohnwu.superuser.io.SuFile;

import io.github.zeroaicy.aide.preference.ZeroAicySetting;

public class GotoJavaCommand implements MenuCommand {

    @Override
    public int getMenuItemId() {
        return R.id.filebrowserShowJavaFolder;
    }

    @Override
    public boolean isEnabled() {
        String currentDir = ServiceContainer.getFileBrowserService().j6();
        String currentAppHome = ZeroAicySetting.getCurrentAppHome();

        String[] javaDir = GradleTools.getFlavourSourceDir(currentAppHome,null);
        SuFile javaFile = new SuFile(javaDir[0]);
        SuFile currentFile = new SuFile(currentDir);


        if (!javaFile.exists()||!javaFile.isDirectory()) {
            return false;
        }
        return !javaFile.equals(currentFile);
    }

    @Override
    public boolean run() {
        String currentAppHome = ZeroAicySetting.getCurrentAppHome();
        String[] javaDir = GradleTools.getFlavourSourceDir(currentAppHome,null);
        ServiceContainer.getFileBrowserService().Hw(javaDir[0]);
        return true;
    }

    @Override
    public boolean isVisible(boolean b) {
        return isEnabled();
    }
}
