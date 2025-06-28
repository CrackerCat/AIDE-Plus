package com.aide.ui.browsers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aide.common.AppLog;
import com.aide.common.KeyStrokeDetector;
import com.aide.common.ListAdapterBase;
import com.aide.ui.AppCommands;
import com.aide.ui.AppFileIcons;
import com.aide.ui.GlobalKeyCommand;
import com.aide.ui.MainActivity;
import com.aide.ui.QuickActionMenu;
import com.aide.ui.ServiceContainer;
import com.aide.ui.command.FileBrowserCommand;
import com.aide.ui.firebase.FireBaseLogEvent;
import com.aide.ui.rewrite.R;
import com.aide.ui.services.FileBrowserService;
import com.aide.ui.util.FileSystem;
import com.aide.ui.views.CustomKeysListView;
import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.caverock.androidsvg.SVG;
import com.github.promeg.pinyinhelper.Pinyin;
import com.topjohnwu.superuser.io.SuFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.github.zeroaicy.aide.ui.view.BreadcrumbView;
import io.github.zeroaicy.aide.utils.FilesSystem;
import io.github.zeroaicy.aide.utils.FilesSystemKt;
import io.github.zeroaicy.util.ContextUtil;
import jaxp.sun.org.apache.xalan.internal.templates.Constants;
import me.zhanghai.android.appiconloader.AppIconLoader;

public class FileBrowser extends LinearLayoutCompat implements
        FileBrowserService.a,
        a {


    private final QuickActionMenu WB;
    private final KeyStrokeDetector.KeyStrokeHandler jw;
    private final QuickActionMenu mb;
    protected View fY;
    private Adapter qp;
    private String lastFolder = null;

    public FileBrowser(Context context) {
        super(context);
        this.WB = new QuickActionMenu(ServiceContainer.getMainActivity(), R.menu.filebrowser_context_menu);
        this.mb = new QuickActionMenu(ServiceContainer.getMainActivity(), R.menu.git_context_menu);
        this.jw = (KeyStrokeDetector.KeyStrokeHandler) new GlobalKeyCommand(AppCommands.VH());
        EQ();
    }


    public FileBrowser(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.WB = new QuickActionMenu(ServiceContainer.getMainActivity(), R.menu.filebrowser_context_menu);
        this.mb = new QuickActionMenu(ServiceContainer.getMainActivity(), R.menu.git_context_menu);
        this.jw = (KeyStrokeDetector.KeyStrokeHandler) new GlobalKeyCommand(AppCommands.VH());
        EQ();
    }

    public static boolean we(String str) {
        if (!str.startsWith(".") && !"bin".equals(str) && !"obj".equals(str) && !"build".equals(str)) {
            return "gradle".equals(str);
        }
        return true;
    }

    private static boolean isSpecialDir(File file) {
        String name = file.getName();
        File parent = file.getParentFile();

        if ("bin".equals(name)) {
            if (parent != null) {
                if (new File(parent, ".classpath").exists() || new File(parent, "AndroidManifest.xml").exists()) {
                    return true;
                }
                File grandParent = parent.getParentFile();
                if (grandParent != null && new File(grandParent, "build.gradle").exists()) {
                    return true;
                }
                if (grandParent != null && new File(grandParent, "build.gradle.kts").exists()) {
                    return true;
                }
            }
        }

        if ("obj".equals(name) && parent != null && new File(parent, "AndroidManifest.xml").exists()) {
            return true;
        }

        if ("build".equals(name) && parent != null && new File(parent, "build.gradle").exists()) {
            return true;
        }
        return "build".equals(name) && parent != null && new File(parent, "build.gradle.kts").exists();
    }

    public static boolean isFileInArchive(File file) {
        while ((file = file.getParentFile()) != null) {
            if (file.isFile()) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("InflateParams")
    private void EQ() {
        setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        LayoutInflater from = LayoutInflater.from(getContext());
        View inflate = from.inflate(R.layout.layout_file_browser, null);
        removeAllViews();
        addView(inflate);
        getListView().addHeaderView(from.inflate(R.layout.browser_header_new, null), null, false);
        ServiceContainer.getFileBrowserService().Zo(this);
        CustomKeysListView listView = getListView();
        Adapter gVar = new Adapter(this, null);
        this.qp = gVar;
        listView.setAdapter(gVar);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Mr((Data) listView.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Mr(null);
            }
        });
        listView.setOnKeyEventListener(new CustomKeysListView.OnKeyEventListener() {
            @Override
            public boolean onKeyDown(int i, KeyEvent keyEvent) {
                return ServiceContainer.getMainActivity().getKeyStrokeDetector().onKeyDown(i, keyEvent, jw);

            }

            @Override
            public boolean onKeyUp(int i, KeyEvent keyEvent) {
                return ServiceContainer.getMainActivity().getKeyStrokeDetector().onKeyUp(i, keyEvent, jw);
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Data hVar = (Data) listView.getItemAtPosition(position);
            if (hVar == null) {
                return true;
            }
            Mr(hVar);
            XL(view, true);
            return true;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Data hVar = (Data) listView.getItemAtPosition(position);
            if (hVar == null) {
                return;
            }
            if (hVar.Hw != null) {
                fY = view;
                FireBaseLogEvent.tp("Browser command: " + hVar.Hw.getNameId());
                if (hVar.Hw.isVisible(false)) {
                    hVar.Hw.run();
                    return;
                }
                return;
            }
            if (hVar.DW) {
                getActivity().openFile(hVar.FH);
                return;
            }
            if (hVar.DW()) {
                String str = hVar.FH;
                if (str != null) {
                    ServiceContainer.getFileBrowserService().Hw(str);
                }
                getListView().setSelection(0);
                return;
            }
            String str2 = hVar.FH;
            if (str2 != null) {
                J8(str2);
            }
            getListView().setSelection(0);

        });
        getSwipeRefreshLayout().setColorSchemeColors(Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE);
        getSwipeRefreshLayout().setOnRefreshListener(() -> {
            String j6 = ServiceContainer.getFileBrowserService().j6();
            J8(Objects.requireNonNullElseGet(j6, () -> Environment.getExternalStorageDirectory().getAbsolutePath()));
            getSwipeRefreshLayout().setRefreshing(false);
        });
        QX();
    }

    public CustomKeysListView getListView() {
        return findViewById(R.id.filebrowserFileList);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return findViewById(R.id.swipeRefresh);
    }

    public MainActivity getActivity() {
        return (MainActivity) getContext();
    }

    public void J8(String str) {

        if (ServiceContainer.getProjectService().isInCurrentProjectDirectory(str)) {
            while (true) {
                List<String> listFiles = FileSystem.listFiles(str);
                if (listFiles.size() != 1 || !FileSystem.notPreProcessorFile(listFiles.get(0))) {
                    break;
                } else {
                    str = listFiles.get(0);
                }
            }
        }
        ServiceContainer.getFileBrowserService().Hw(str);
    }

    public void Mr(Data data) {

        String str = null;
        if (data != null && (data.DW || data.j6())) {
            str = data.FH;
        }
        ServiceContainer.getFileBrowserService().VH(str);
    }

    public void XL(View view, boolean z) {
        this.WB.QX(view, z);
    }

    private void aM(View view) {
        this.mb.QX(view, true);
    }

    @Keep /* 用于给菜单上的一个按钮使用的 */
    public void J0() {
        Object selectedItem = getListView().getSelectedItem();
        if (selectedItem instanceof Data) {
            Mr((Data) selectedItem);
            XL(getListView().getSelectedView(), false);
        }
    }

    @Keep /* 外部请求使用了该api */
    public void QX() {
        String j6 = ServiceContainer.getFileBrowserService().j6();
        ServiceContainer.getFileBrowserService().VH(null);
        CustomKeysListView listView = getListView();
        /*((TextView) listView.findViewById(R.id.browserHeaderLabel)).setText(j6);*/

        // 新增面包屑列表
        BreadcrumbView breadcrumbView = listView.findViewById(R.id.browserHeaderBreadcrumb);

        breadcrumbView.setPath(createList(j6), (item, index) -> {

            J8(item.getFullPath());
            //ToastUtils.showLong("点击了："+item.getName()+"路径"+item.getFullPath());

        });

        ((ImageView) listView.findViewById(R.id.browserHeaderIcon)).setImageResource(R.drawable.folder_open);
        ImageView imageView = listView.findViewById(R.id.browserHeaderMenuButton);
        imageView.setOnClickListener(v -> {
            ServiceContainer.getFileBrowserService().VH(null);
            XL(imageView, true);
        });
        List<Data> arrayList = new ArrayList<>();
        String parent = FileSystem.getParent(j6);
        if (parent != null) {
            arrayList.add(new Data(this, parent, Constants.ATTRVAL_PARENT, false));
        }
        for (FileBrowserCommand fileBrowserCommand : AppCommands.getFileBrowserCommands()) {
            if (fileBrowserCommand.isVisible(false)) {
                arrayList.add(new Data(this, fileBrowserCommand));
            }
        }
        try {


            SuFile suFile = new SuFile(FilesSystemKt.fixApi30(j6, true));
            if (suFile.isDirectory()) {
                File[] files = suFile.listFiles();

                List<String> result = FilesSystem.INSTANCE.processPaths(j6);

                List<File> newResult = new ArrayList<>();

                for (String string : result) {
                    newResult.add(new SuFile(string));
                }


                if (files != null) {
                    newResult.addAll(Arrays.asList(files));
                }


                List<File> folderList = new ArrayList<>();
                List<File> specialFolderList = new ArrayList<>();
                List<File> hideFolderList = new ArrayList<>();
                List<File> fileList = new ArrayList<>();
                List<File> hideFileList = new ArrayList<>();


                for (File file : newResult) {

                    if (file.isDirectory()) {
                        if (file.isHidden()) {
                            hideFolderList.add(file);
                        } else {
                            if (isSpecialDir(file)) {
                                specialFolderList.add(file);
                            } else {
                                folderList.add(file);
                            }
                        }
                    } else {
                        if (file.isHidden()) {
                            hideFileList.add(file);
                        } else {
                            fileList.add(file);
                        }
                    }
                }


                Collections.sort(folderList, new FileComparator(folderList, 1));
                Collections.sort(specialFolderList, new FileComparator(specialFolderList, 2));
                Collections.sort(hideFolderList, new FileComparator(hideFolderList, 3));
                Collections.sort(fileList, new FileComparator(fileList, 4));
                Collections.sort(hideFileList, new FileComparator(hideFileList, 5));

                List<File> newList = folderList;

                // newList.addAll(folderList);
                newList.addAll(specialFolderList);
                newList.addAll(hideFolderList);
                newList.addAll(fileList);
                newList.addAll(hideFileList);


                for (File file1 : newList) {
                    arrayList.add(new Data(this, file1.getAbsolutePath(), file1.getName(), FileSystem.KD(file1.getAbsolutePath())));
                }
            } else {
                List<String> listFiles = FileSystem.listFiles(j6);
                Collections.sort(listFiles, (str, str2) -> {
                    boolean notPreProcessorFile = FileSystem.notPreProcessorFile(str);
                    boolean notPreProcessorFile2 = FileSystem.notPreProcessorFile(str2);
                    String lowerCase = FileSystem.getName(str).toLowerCase();
                    String lowerCase2 = FileSystem.getName(str2).toLowerCase();
                    if (notPreProcessorFile && !notPreProcessorFile2) {
                        return -1;
                    }
                    if (!notPreProcessorFile && notPreProcessorFile2) {
                        return 1;
                    }
                    if (!notPreProcessorFile || !notPreProcessorFile2) {
                        int lastIndexOf = lowerCase.lastIndexOf(".");
                        String substring = lastIndexOf > 0 ? lowerCase.substring(lastIndexOf) : "";
                        int lastIndexOf2 = lowerCase2.lastIndexOf(".");
                        int compareTo = substring.compareTo(lastIndexOf2 > 0 ? lowerCase2.substring(lastIndexOf2) : "");
                        return compareTo != 0 ? compareTo : lowerCase.compareTo(lowerCase2);
                    }
                    boolean we = FileBrowser.we(lowerCase);
                    boolean we2 = FileBrowser.we(lowerCase2);
                    if (we && !we2) {
                        return 1;
                    }
                    if (we || !we2) {
                        return lowerCase.compareTo(lowerCase2);
                    }
                    return -1;

                });
                for (String str : listFiles) {
                    arrayList.add(new Data(this, str, FileSystem.getName(str), FileSystem.KD(str)));
                }

            }
        } catch (Exception e2) {
            AppLog.e(e2);
        }
        this.qp.DW(arrayList);

        if (lastFolder != null) {
            arrayList.forEach(it -> {
                if (it.FH != null) {
                    if (it.FH.equals(lastFolder)) {
                        int position = arrayList.indexOf(it);
                        getListView().smoothScrollToPosition(position + 3);
                    }
                }
            });
        }
        lastFolder = j6;

    }

    public List<BreadcrumbView.BreadcrumbItem> createList(String fullPath) {
        List<BreadcrumbView.BreadcrumbItem> items = new ArrayList<>();
        if (fullPath == null || fullPath.isEmpty()) return items;

        String[] segments = fullPath.replaceAll("^/+", "").split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String segment : segments) {
            currentPath.append("/").append(segment);
            items.add(new BreadcrumbView.BreadcrumbItem(segment, currentPath.toString()));
        }
        items.add(0,new BreadcrumbView.BreadcrumbItem("/", "/"));
        return items;
    }

    @Keep /* 外部请求使用了该api */
    public void Ws() {
        aM(this.fY);
    }

    @Keep
    @Override
    public void DW() {
        getListView().requestFocus();
    }

    @Keep
    @Override
    public void FH() {

    }

    @Override
    public void j6() {
        QX();
    }

    @Override
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (ServiceContainer.getMainActivity().getKeyStrokeDetector().onKeyDown(i, keyEvent, this.jw)) {
            return true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    @Override
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (ServiceContainer.getMainActivity().getKeyStrokeDetector().onKeyUp(i, keyEvent, this.jw)) {
            return true;
        }
        return super.onKeyUp(i, keyEvent);
    }

    static class Adapter extends ListAdapterBase<Data> {

        FileBrowser fileBrowser;

        public Adapter(FileBrowser fileBrowser) {
            this.fileBrowser = fileBrowser;
        }

        public Adapter(FileBrowser fileBrowser, AdapterView.OnItemSelectedListener listener) {
            this(fileBrowser);
        }


        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = convertView == null ? LayoutInflater.from(fileBrowser.getContext()).inflate(R.layout.item_filebrowser_entry, parent, false) : convertView;
            Data j6 = j6(position);

//            String currentDir = ServiceContainer.getFileBrowserService().j6();
//            String currentEditor = ServiceContainer.getMainActivity().getAIDEEditorPager().getVisibleFile();


            ImageView icon = inflate.findViewById(R.id.filebrowserEntryFileImage);
            TextView title = inflate.findViewById(R.id.filebrowserEntryName);
            ImageView smallIcon = inflate.findViewById(R.id.small_icon);
            TextView subtitle = inflate.findViewById(R.id.subtitle);
            View barStart = inflate.findViewById(R.id.bar_start);
            View bgView = inflate.findViewById(R.id.bg_view);


            smallIcon.setVisibility(View.GONE);
            subtitle.setText("");
            barStart.setVisibility(View.GONE);
            bgView.setVisibility(View.GONE);

            int iconResId = j6.v5;
            String title_j6 = j6.j6;

            if (j6.FH != null && iconResId != R.drawable.folder_open)
                label:{
                    SuFile file = new SuFile(j6.FH);

                    boolean isFileInArchive = isFileInArchive(file);

                    title.setText(file.getName());
                    icon.setImageResource(iconResId);
                    if (isFileInArchive) {
                        SuFile currentEditorFile = new SuFile(ServiceContainer.getMainActivity().getAIDEEditorPager().getVisibleFile());
                        if (file.getAbsolutePath().equals(currentEditorFile.getAbsolutePath())) {
                            barStart.setVisibility(View.VISIBLE);
                            bgView.setVisibility(View.VISIBLE);
                        } else {
                            barStart.setVisibility(View.GONE);
                            bgView.setVisibility(View.GONE);
                        }
                        break label;
                    }
                    if (file.isDirectory()) {
                        smallIcon.setVisibility(View.VISIBLE);
                        smallIcon.setImageDrawable(null);
                    } else if (file.isFile()) {
                        String currentPath = ServiceContainer.getMainActivity().getAIDEEditorPager().getVisibleFile();
                        if (currentPath != null) {
                            SuFile currentEditorFile = new SuFile(currentPath);
                            if (file.getAbsolutePath().equals(currentEditorFile.getAbsolutePath())) {
                                barStart.setVisibility(View.VISIBLE);
                                bgView.setVisibility(View.VISIBLE);
                            } else {
                                barStart.setVisibility(View.GONE);
                                bgView.setVisibility(View.GONE);
                            }
                        }


                        String filename = file.getName();
                        if (filename.endsWith(".apk")
                                || filename.endsWith(".apk.bak")) {
                            try {

                                Context context = ContextUtil.getApplication();
                                int iconSize = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);

                                AppIconLoader mLoader = new AppIconLoader(iconSize, false, context);
                                PackageManager pm = Utils.getApp().getPackageManager();
                                if (pm == null) return null;
                                PackageInfo packageInfo = pm.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                                Bitmap icon_pkg = null;
                                if (packageInfo != null) {
                                    assert packageInfo.applicationInfo != null;
                                    icon_pkg = mLoader.loadIcon(packageInfo.applicationInfo);
                                }
                                icon.setImageBitmap(icon_pkg);
                                // icon.setImageDrawable(Objects.requireNonNull(AppUtils.getApkInfo(file)).getIcon());
                            } catch (Throwable e) {
                                icon.setImageResource(iconResId);
                            }
                        } else if (filename.endsWith(".svg")) {
                            FileInputStream is = null;
                            try {
                                is = new FileInputStream(file);
                                SVG svg = SVG.getFromInputStream(is);
                                Picture pic = svg.renderToPicture();
                                if (pic != null) {
                                    icon.setImageDrawable(new PictureDrawable(pic));
                                }
                            } catch (Throwable e) {
                                icon.setImageResource(iconResId);
                            } finally {
                                CloseUtils.closeIOQuietly(is);
                            }
                        } else if (filename.endsWith(".png")
                                || filename.endsWith(".jpg")
                                || filename.endsWith(".jpeg")
                                || filename.endsWith(".webp")) {
                            Glide.with(icon)
                                    .load(file)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(false)
                                    .dontAnimate()
                                    .thumbnail(0.1f)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(icon);
                        } else if (filename.endsWith(".gif")) {
                            Glide.with(inflate.getContext())
                                    .asGif()
                                    .load(file)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .transition(DrawableTransitionOptions.withCrossFade(0))
                                    .into(icon);
                        } else if (filename.endsWith(".zip")
                                || filename.endsWith(".7z")
                                || filename.endsWith(".tar")
                                || filename.endsWith(".tar.gz")
                                || filename.endsWith(".tar.xz")
                                || filename.endsWith(".gzip")
                                || filename.endsWith(".tar.bz2")
                                || filename.endsWith(".bzip2")
                                || filename.endsWith(".jar")
                                || filename.endsWith(".rar")
                                || filename.endsWith(".aar")
                        ) {
                            icon.setImageResource(R.drawable.file_type_zip);
                        } else if (filename.endsWith(".pom")) {
                            icon.setImageResource(R.drawable.file_type_xml);
                        }
                    }


                }
            else {
                icon.setImageResource(iconResId);
                title.setText(title_j6);
            }


            if (subtitle.getText().toString().length() == 0) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
            }


            final View finalconverView = inflate;
            finalconverView.getViewTreeObserver().

                    addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            final ViewTreeObserver.OnGlobalLayoutListener THIS = this;
                            inflate.post(() -> {
                                int width = finalconverView.getWidth();
                                int height = finalconverView.getHeight();
                                ViewGroup.LayoutParams lp = bgView.getLayoutParams();
                                lp.width = width;
                                lp.height = height;
                                bgView.setLayoutParams(lp);
                                ViewGroup.LayoutParams lp2 = barStart.getLayoutParams();
                                lp2.height = height;
                                barStart.setLayoutParams(lp2);
                                finalconverView.getViewTreeObserver().removeOnGlobalLayoutListener(THIS);
                            });
                        }
                    });


/*
            ((TextView) inflate.findViewById(R.id.filebrowserEntryName)).setText(j6.j6);
            ((ImageView) inflate.findViewById(R.id.filebrowserEntryFileImage)).setImageResource(j6.v5);*/
            return inflate;

        }
    }


    static class Data {
        final FileBrowser Zo;
        public boolean DW;
        public String FH;
        public FileBrowserCommand Hw;
        public String j6;

        public int v5;

        public Data(FileBrowser fileBrowser, String str, String str2, boolean z) {
            this.Zo = fileBrowser;
            this.FH = str;
            this.j6 = str2;
            this.DW = z;
            if (z) {
                this.v5 = AppFileIcons.j6(str);
                return;
            }
            if (DW()) {
                this.v5 = R.drawable.folder_open; // 上一级
            } else if (FileBrowser.we(str2)) {
                this.v5 = R.drawable.folder_hidden; // 隐藏文件夹
            } else {
                this.v5 = R.drawable.folder;
            }

        }

        public Data(FileBrowser fileBrowser, FileBrowserCommand fileBrowserCommand) {
            this.Zo = fileBrowser;
            this.Hw = fileBrowserCommand;
            this.v5 = fileBrowserCommand.getIconId();
            int nameId = fileBrowserCommand.getNameId();
            if (nameId != 0) {
                this.j6 = fileBrowser.getContext().getResources().getString(nameId);
            }
        }


        public boolean DW() {
            if (!this.DW) {
                if (this.j6.equals(Constants.ATTRVAL_PARENT)) {
                    return true;
                }
            }
            return false;
        }

        public boolean j6() {
            if (!this.DW) {
                if (!DW()) {
                    return true;
                }
            }
            return false;
        }


    }


    static class FileComparator implements Comparator<File> {
        private final List<File> list;
        private final int type;

        public FileComparator(List<File> list, int type) {
            this.list = list;
            this.type = type;
        }

        @Override
        public int compare(File s1, File s2) {
            try {
                String sa1 = getFileName(s1);
                String sa2 = getFileName(s2);

                String name1 = getNameFromString(sa1);
                String name2 = getNameFromString(sa2);
                int result = name1.compareToIgnoreCase(name2);

                if (result == 0) {
                    int num1 = getIntFromString(sa1);
                    int num2 = getIntFromString(sa2);
                    result = Integer.compare(num1, num2);
                }

                return result;
            } catch (Throwable e) {
                LogUtils.e(e);
            }
            return 0;
        }

        private String getNameFromString(String source) {
            String replace = source.replaceAll("\\d+", "");
            if (replace.isEmpty()) return source;
            return Pinyin.toPinyin(replace, " ");
        }

        private int getIntFromString(String s) {
            String num = s.replaceAll("\\D", "");
            if (num.isEmpty()) {
                return -1;
            } else {
                return Integer.parseInt(num);
            }
        }

        private String getFileName(File file) {
            String name = file.getName();
            int index = name.lastIndexOf("/");
            if (index == -1) return name;
            name = name.substring(index + 1);
            return name;
        }

        public int getType() {
            return type;
        }

        public List<File> getList() {
            return list;
        }
    }

}
