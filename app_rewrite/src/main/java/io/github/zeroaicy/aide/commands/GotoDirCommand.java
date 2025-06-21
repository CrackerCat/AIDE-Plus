package io.github.zeroaicy.aide.commands;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aide.ui.ServiceContainer;
import com.aide.ui.command.MenuItemCommand;
import com.aide.ui.rewrite.R;
import com.blankj.utilcode.util.ConvertUtils;

import io.github.zeroaicy.util.ContextUtil;

public class GotoDirCommand implements MenuItemCommand {

    @Override
    public int getMenuItemId() {
        return R.id.filebrowserShowCustomFolder;
    }

    @Override
    public boolean isEnabled() {
       return true;
    }

    @Override
    public boolean run() {
        Activity activity = ContextUtil.getActivity();
        LinearLayout view = new LinearLayout(activity);
        view.setPadding(ConvertUtils.dp2px(24), ConvertUtils.dp2px(10), ConvertUtils.dp2px(24), 0);
        final EditText et = new EditText(activity);
        view.addView(et, -1, -2);
        if (activity != null) {
            et.setText(activity.getFilesDir().getAbsolutePath());
        }
        AlertDialog dialog =
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.command_files_goto_dir)
                        .setView(view)
                        .setPositiveButton(
                                android.R.string.ok,
                                (dia, which) -> ServiceContainer.getFileBrowserService().Hw(et.getText().toString()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        dialog.show();
        return true;
    }
}
