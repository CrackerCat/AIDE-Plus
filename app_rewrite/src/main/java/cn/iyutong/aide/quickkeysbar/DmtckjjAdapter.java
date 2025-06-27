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

        Matcher matcher = Pattern.compile("\\{([^\\s-]+)-([^\\s-]+)-([^\\s-]+)\\}").matcher(replace);
        Matcher matcher1 = Pattern.compile("\\{([^\\s-]+)-([^\\s-]+)\\}").matcher(replace);
        Matcher matcher2 = Pattern.compile("\\[([^\\s-]+)-([^\\s-]+)\\]").matcher(replace);
        Matcher matcher3 = Pattern.compile("\\(([^\\s-]+)-([^\\s-]+)\\)").matcher(replace);
        Matcher matcher4 = Pattern.compile("\\(([^\\s-]+)\\)").matcher(replace);

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
        } else if (matcher3.find()) {
            String A4 = matcher3.group(1);
            String B4 = matcher3.group(2);
            textView.setText(shijwz(B4));
            textView.setOnLongClickListener(v -> {
                shij(A4);
                return true;
            });
        } else if (matcher4.find()) {
            String A5 = matcher4.group(1);
            textView.setText(shijwz(A5));
            textView.setOnLongClickListener(v -> {
                shij(A5);
                return true;
            });
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
                return src.replaceAll("#KG", "▓").replaceAll("#ZH","-").replaceAll("#HH", "﹂");
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
                String src1 = src.replaceAll("#KG", " ").replaceAll("#ZH","-").replaceAll("#HH", "\n");
                if (src1.length() == 1) {
                    YAIDEEditor.setKey(src1);
                } else {
                    YAIDEEditor.setText(src1);
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
