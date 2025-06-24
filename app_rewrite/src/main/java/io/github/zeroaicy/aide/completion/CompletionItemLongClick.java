/**
 * @Author ZeroAicy
 * @Date
 * @AIDE AIDE+
 */
package io.github.zeroaicy.aide.completion;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aide.engine.SourceEntity;
import com.aide.ui.AIDEEditorCompletion;
import com.aide.ui.views.CompletionListView;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ReflectUtils;

import androidx.annotation.Keep;

import java.util.Objects;

import cn.iyutong.aide.Translator;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;

// Lcom/aide/ui/AIDEEditorCompletion$d继承此类
// 必须keep类名 必须实现 AdapterView.OnItemLongClickListener 接口
@Keep
public class CompletionItemLongClick implements AdapterView.OnItemLongClickListener {

    private CompletionListView completionListViewa;

    @Keep
    public CompletionItemLongClick(AIDEEditorCompletion aideEditorCompletion, CompletionListView completionListView) {
        completionListViewa = completionListView;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Context context = view.getContext();
        PopupMenu popupMenu = new PopupMenu(context, view);
        Menu menu = popupMenu.getMenu();

        if (ZeroAicySetting.isEnableTranslate()) {
            Translator.clongclick(menu, completionListViewa,view);
        }

        Object item = completionListViewa.getItemAtPosition(position);
        if (item instanceof SourceEntity sourceEntity) {
            if (sourceEntity.Ws() != null) {
                menu.add("查看文档")
                        .setOnMenuItemClickListener(
                                new MenuItem.OnMenuItemClickListener() {

                                    private void openBrowser(String title, String url) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        Uri content_url = Uri.parse(url);
                                        intent.setData(content_url);
                                        context.startActivity(Intent.createChooser(intent, title));
                                        //LogUtils.i(url);
                                    }

                                    private void viewDoc(String title, String path) {
                                        openBrowser(title, "https://developer.android.com/reference/" + path);
                                    }

                                    private void viewChineseDoc(String title, String path) {
                                        openBrowser(title, "https://developer.android.google.cn/reference/" + path);
                                    }

                                    private void viewChineseDoc2(String title, String path) {
                                        openBrowser(title, "http://www.android-doc.com/reference/" + path);
                                    }

                                    private void viewChineseDoc3(String title, String path) {
                                        openBrowser(title, "https://www.apiref.com/android-zh/" + path);
                                    }

                                    @Override
                                    public boolean onMenuItemClick(final MenuItem p1) {
                                        final String path = sourceEntity.Ws();
                                        String[] items = {"Android Api","Android Api（CN）", "Android Api（android-doc）", "Android Api（apiref）"};
                                        AlertDialog dialog =
                                                new AlertDialog.Builder(context)
                                                        .setTitle(p1.getTitle())
                                                        .setItems(
                                                                items,
                                                                new DialogInterface.OnClickListener() {

                                                                    @Override
                                                                    public void onClick(DialogInterface dia, int which) {
                                                                        switch (which) {
                                                                            case 0:
                                                                                viewDoc(Objects.requireNonNull(p1.getTitle()).toString(), path);
                                                                                break;
                                                                            case 1:
                                                                                viewChineseDoc(Objects.requireNonNull(p1.getTitle()).toString(), path);
                                                                                break;
                                                                            case 2:
                                                                                viewChineseDoc2(Objects.requireNonNull(p1.getTitle()).toString(), path);
                                                                                break;
                                                                            case 3:
                                                                                viewChineseDoc3(Objects.requireNonNull(p1.getTitle()).toString(), path);
                                                                                break;

                                                                        }
                                                                    }
                                                                })
                                                        .create();
                                        dialog.show();
                                        return false;
                                    }
                                });
            }
        }
        popupMenu.show();
        return true;
    }

}

