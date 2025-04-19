package io.github.zeroaicy.aide.highlight;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aide.ui.rewrite.R;
import java.util.Locale;
import net.margaritov.preference.colorpicker.ColorPickerPanelView;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;
import android.widget.RadioGroup;
import android.graphics.Typeface;
import android.view.inputmethod.EditorInfo;
import android.graphics.Color;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;

public class ColorKindEditDialog extends AlertDialog
		implements
			ColorPickerView.OnColorChangedListener,
			View.OnClickListener,
			ViewTreeObserver.OnGlobalLayoutListener,
			RadioGroup.OnCheckedChangeListener {

	private ColorPickerView mColorPicker;
	private ColorStateList mHexDefaultTextColor;
	private EditText mHexVal;
	private boolean mHexValueEnabled;
	private View mLayout;
	private OnColorChangedListener mListener;
	private ColorPickerPanelView mNewColor;
	private ColorPickerPanelView mOldColor;
	private int mOrientation;

	private ViewGroup typefaceStyleEditView;
	private RadioGroup typefaceStyleRadioGroup;
	public ColorKindEditDialog(Context context, int color) {
		super(context);

		this.mHexValueEnabled = false;

		init(color);

	}

	private void init(int color) {
		getWindow().setFormat(1);
		setUp(color);
	}

	@Override
	public void onGlobalLayout() {
		if (getContext().getResources().getConfiguration().orientation != this.mOrientation) {
			int oldcolor = this.mOldColor.getColor();
			int newcolor = this.mNewColor.getColor();
			this.mLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			setUp(oldcolor);
			this.mNewColor.setColor(newcolor);
			this.mColorPicker.setColor(newcolor);
		}
	}

	private void setUp(int color) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
		this.mLayout = inflater.inflate(R.layout.dialog_color_theme_edit, (ViewGroup) null);

		this.mLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);

		this.mOrientation = getContext().getResources().getConfiguration().orientation;

		setView(this.mLayout);

		this.mColorPicker = this.mLayout.findViewById(R.id.color_picker_view);
		this.mOldColor = this.mLayout.findViewById(R.id.old_color_panel);
		this.mNewColor = this.mLayout.findViewById(R.id.new_color_panel);

		this.mHexVal = this.mLayout.findViewById(R.id.hex_val);
		this.mHexVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		this.typefaceStyleEditView = this.mLayout.findViewById(R.id.typeface_style_edit_root_view);
		this.typefaceStyleRadioGroup = this.mLayout.findViewById(R.id.typeface_style_radio_group);
		typefaceStyleRadioGroup.setOnCheckedChangeListener(this);

		this.mHexDefaultTextColor = this.mHexVal.getTextColors();
		this.mHexVal.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_DONE) {
					return false;
				}
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				String colorText = mHexVal.getText().toString();
				if (colorText.length() > 5 || colorText.length() < 10) {
					try {
						int c = ColorPickerPreference.convertToColorInt(colorText);
						mColorPicker.setColor(c, true);
						mHexVal.setTextColor(mHexDefaultTextColor);
					} catch (IllegalArgumentException e) {
						mHexVal.setTextColor(Color.RED);
					}
				} else {
					mHexVal.setTextColor(Color.RED);
				}
				return true;
			}
		});
		this.mHexVal.addTextChangedListener(new TextWatcher() {
			String lastText;
			@Override
			public void afterTextChanged(Editable editable) {
				
				int length = editable.length();
				int fit = getAlphaSliderVisible() ? 9 : 7;
				
				if (fit == length ) {
					String text = editable.toString();
					// 没有改变
					if( text.equals(this.lastText)){
						return;
					}
					// 记录 防止递归
					this.lastText = text;
					
					try {
						int color = ColorPickerPreference.convertToColorInt(text);
						// 会再此触发 afterTextChanged
						mColorPicker.setColor(color, true);
						mHexVal.setTextColor(mHexDefaultTextColor);
					} catch (IllegalArgumentException e) {
						mHexVal.setTextColor(Color.RED);
					}
				} else {
					mHexVal.setTextColor(Color.RED);
					// 记录一下
					this.lastText = editable.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO: Implement this method
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		ViewGroup parent = (ViewGroup) this.mOldColor.getParent();
		parent.setPadding(Math.round(this.mColorPicker.getDrawingOffset()), 0,
				Math.round(this.mColorPicker.getDrawingOffset()), 0);
		this.mOldColor.setOnClickListener(this);
		this.mNewColor.setOnClickListener(this);
		this.mColorPicker.setOnColorChangedListener(this);
		this.mOldColor.setColor(color);
		this.mColorPicker.setColor(color, true);
	}

	@Override
	public void onColorChanged(int color) {
		this.mNewColor.setColor(color);
		if (this.mHexValueEnabled) {
			updateHexValue(color);
		}
	}

	public void setHexValueEnabled(boolean enable) {
		this.mHexValueEnabled = enable;
		if (enable) {
			this.mHexVal.setVisibility(View.VISIBLE);
			updateHexLengthFilter();
			updateHexValue(getColor());
			return;
		}
		this.mHexVal.setVisibility(View.GONE);
	}

	int typefaceStyleValue;
	public void setTypefaceStyleValue(int typefaceStyleValue) {
		this.typefaceStyleValue = typefaceStyleValue;

		int checkedRadioButtonId;
		switch (typefaceStyleValue) {
			case Typeface.NORMAL :
			default :
				checkedRadioButtonId = R.id.typeface_style_radio_normal;
				break;
			case Typeface.BOLD :
				checkedRadioButtonId = R.id.typeface_style_radio_bold;
				break;
			case Typeface.ITALIC :
				checkedRadioButtonId = R.id.typeface_style_radio_italic;
				break;
			case Typeface.BOLD_ITALIC :
				checkedRadioButtonId = R.id.typeface_style_radio_bold_italic;
				break;
		}
		this.typefaceStyleRadioGroup.check(checkedRadioButtonId);
	}

	public int getTypefaceStyleValue() {
		return this.typefaceStyleValue;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.typeface_style_radio_normal) {
			this.typefaceStyleValue = Typeface.NORMAL;

		} else if (checkedId == R.id.typeface_style_radio_bold) {
			this.typefaceStyleValue = Typeface.BOLD;

		} else if (checkedId == R.id.typeface_style_radio_italic) {
			this.typefaceStyleValue = Typeface.ITALIC;

		} else if (checkedId == R.id.typeface_style_radio_bold_italic) {
			this.typefaceStyleValue = Typeface.BOLD_ITALIC;

		} else {
			this.typefaceStyleValue = Typeface.NORMAL;
		}
	}

	public void setTypefaceStyleEditEnabled(boolean enable) {
		//this.typefaceStyleEditView = enable;
		if (enable) {
			this.typefaceStyleEditView.setVisibility(View.VISIBLE);

			return;
		}
		this.mHexVal.setVisibility(View.GONE);
	}

	public boolean getHexValueEnabled() {
		return this.mHexValueEnabled;
	}

	private void updateHexLengthFilter() {
		if (getAlphaSliderVisible()) {
			this.mHexVal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
		} else {
			this.mHexVal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
		}
	}

	private void updateHexValue(int color) {
		if (getAlphaSliderVisible()) {
			this.mHexVal.setText(ColorPickerPreference.convertToARGB(color).toUpperCase(Locale.getDefault()));
		} else {
			this.mHexVal.setText(ColorPickerPreference.convertToRGB(color).toUpperCase(Locale.getDefault()));
		}
		this.mHexVal.setTextColor(this.mHexDefaultTextColor);
	}

	public void setAlphaSliderVisible(boolean visible) {
		this.mColorPicker.setAlphaSliderVisible(visible);
		if (this.mHexValueEnabled) {
			updateHexLengthFilter();
			updateHexValue(getColor());
		}
	}

	public boolean getAlphaSliderVisible() {
		return this.mColorPicker.getAlphaSliderVisible();
	}

	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.mListener = listener;
	}

	public int getColor() {
		return this.mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
		OnColorChangedListener onColorChangedListener;
		if (v.getId() == R.id.new_color_panel && (onColorChangedListener = this.mListener) != null) {
			onColorChangedListener.onColorChanged(this.mNewColor.getColor());
		}
		dismiss();
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt("old_color", this.mOldColor.getColor());
		state.putInt("new_color", this.mNewColor.getColor());
		return state;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.mOldColor.setColor(savedInstanceState.getInt("old_color"));
		this.mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
	}

	public interface OnColorChangedListener {
		void onColorChanged(int color);
	}

}

