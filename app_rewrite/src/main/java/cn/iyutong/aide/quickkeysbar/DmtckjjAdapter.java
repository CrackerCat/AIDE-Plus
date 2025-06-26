package cn.iyutong.aide.quickkeysbar;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aide.ui.QuickKeysBar;
import com.aide.ui.ServiceContainer;
import com.aide.ui.rewrite.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.iyutong.aide.YAIDEEditor;
import cn.iyutong.tool.adapter.recyclerview.BaseRecyclerAdapter;
import cn.iyutong.tool.adapter.recyclerview.RecyclerViewHolder;

public class DmtckjjAdapter extends BaseRecyclerAdapter<String> {
    public DmtckjjAdapter(String[] data) {
        super(data);
    }

    @Override
    protected int getItemLayoutId(int viewType) {
        return R.layout.yquickkeysbar_key;
    }

    @Override
    protected void bindData(@NonNull RecyclerViewHolder holder, int position, String item) {
        TextView textView = holder.findViewById(R.id.quickKeyBarButton);
        String replace = item.replace("s", " ");

        String regex = "\\{([^\\s-]+)-([^\\s-]+)-([^\\s-]+)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(replace);

        String regex1 = "\\{([^\\s-]+)-([^\\s-]+)\\}";
        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(replace);

        String regex2 = "\\[([^\\s-]+)-([^\\s-]+)\\]";
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(replace);

        if (matcher.find()) {
            String A1 = matcher.group(1);
            String B1 = matcher.group(2);
            String C1 = matcher.group(3);
            textView.setText(shijwz(C1));
            textView.setOnClickListener(v -> shij(A1));
            textView.setOnLongClickListener(v -> {
                shij(B1);
                return true;
            });
        } else if (matcher1.find()) {
            String A2 = matcher1.group(1);
            String B2 = matcher1.group(2);
            textView.setText(shijwz(A2) + shijwz(B2));
            textView.setOnClickListener(v -> shij(A2));
            textView.setOnLongClickListener(v -> {
                shij(B2);
                return true;
            });
        } else if (matcher2.find()) {
            String A3 = matcher2.group(1);
            String B3 = matcher2.group(2);
            textView.setText(shijwz(B3));
            textView.setOnClickListener(v -> shij(A3));
        } else {
            textView.setText(shijwz(replace));
            textView.setOnClickListener(v -> shij(replace));
        }

    }

    private String shijwz(String src) {
        if (src.trim().isEmpty()){
            return "Tab";
        }
        switch (src) {
            case "#LEFT":
                return "←";
            case "#RIGHT":
                return "→";
            case "#UP":
                return "↑";
            case "#DOWN":
                return "↓";
            case "#HOME":
                return "◀";
            case "#END":
                return "▶";
            default:
                return src;
        }
    }

    private void shij(String src) {
        if (src.trim().isEmpty()){
            YAIDEEditor.setKey(src);
            return;
        }
        switch (src) {
            case "#LEFT":
                simulateDirectionKey(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case "#RIGHT":
                simulateDirectionKey(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case "#UP":
                simulateDirectionKey(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case "#DOWN":
                simulateDirectionKey(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case "#HOME":
                simulateDirectionKey(KeyEvent.KEYCODE_MOVE_HOME);
                break;
            case "#END":
                simulateDirectionKey(KeyEvent.KEYCODE_MOVE_END);
                break;
            default:
                if (src.length() == 1) {
                    YAIDEEditor.setKey(src);
                } else {
                    YAIDEEditor.setText(src);
                }
                break;
        }
    }

    private void simulateDirectionKey(int keyCode) {
        KeyEvent eventDown = new KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_DOWN,
                keyCode,
                0
        );
        ServiceContainer.getMainActivity().dispatchKeyEvent(eventDown);
        KeyEvent eventUp = new KeyEvent(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                KeyEvent.ACTION_UP,
                keyCode,
                0
        );
        ServiceContainer.getMainActivity().dispatchKeyEvent(eventUp);
    }
}
