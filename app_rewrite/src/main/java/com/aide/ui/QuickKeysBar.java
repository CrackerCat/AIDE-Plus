/**
 * @Date 
 * @AIDE AIDE+ 
 */
package com.aide.ui;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Keep;
import com.aide.common.AndroidHelper;
import com.aide.ui.MainActivity;
import com.aide.ui.rewrite.R;

@Keep
public class QuickKeysBar {

	QuickKeysBarFormAide2 quickKeysBarFormAide;

	@Keep
	public QuickKeysBar(MainActivity mainActivity) {
		quickKeysBarFormAide = new QuickKeysBarFormAide2(mainActivity);
	}

	@Keep
	public void gn(String s) {
		quickKeysBarFormAide.gn(s);
	}

	@Keep
	public int v5() {
		return quickKeysBarFormAide.v5();
	}

	@Keep
	public void show(boolean show) {
		quickKeysBarFormAide.show(show);
	}

	public static class QuickKeysBarFormAide2 {

		
		private String FH;

		private boolean Hw;

		private MainActivity mainActivity;

		private View quickkeysbarView;

		private KeyCharacterMap v5;
		
		static KeyCharacterMap DW(QuickKeysBarFormAide2 quickKeysBar) {
			return quickKeysBar.v5;
		}

		static KeyCharacterMap FH(QuickKeysBarFormAide2 quickKeysBar, KeyCharacterMap keyCharacterMap) {
			quickKeysBar.v5 = keyCharacterMap;
			return keyCharacterMap;
		}
		
		public QuickKeysBarFormAide2(MainActivity mainActivity) {
			try {
				this.FH = "";
				this.mainActivity = mainActivity;
				this.quickkeysbarView = LayoutInflater.from(mainActivity).inflate(0x7f0a003b,
						(ViewGroup) mainActivity.findViewById(0x7f080120));
				VH(AndroidHelper.getVerticalScreenWidthInDp(mainActivity) >= 360.0f
						&& AndroidHelper.isNotTelevisionMode(mainActivity));
				this.quickkeysbarView.findViewById(0x7f080144).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							VH(true);
						}
					});
				this.quickkeysbarView.findViewById(0x7f080141).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							VH(false);
						}
					});
			} catch (Throwable th) {
				throw th;
			}
		}

		public void VH(boolean z) {
			try {
				this.Hw = z;
				if (z) {
					this.quickkeysbarView.findViewById(0x7f080145).setVisibility(8);
					this.quickkeysbarView.findViewById(0x7f080142).setVisibility(0);
				} else {
					this.quickkeysbarView.findViewById(0x7f080145).setVisibility(0);
					this.quickkeysbarView.findViewById(0x7f080142).setVisibility(8);
				}
			} catch (Throwable th) {
				throw th;
			}
		}

		public void gn(String str) {
			float f;
			float f2;
			try {
				if (this.quickkeysbarView == null || str == null || this.FH.equals(str)) {
					return;
				}
				this.FH = str;
				LayoutInflater from = LayoutInflater.from(this.mainActivity);
				if (AndroidHelper.getVerticalScreenWidthInDp(this.mainActivity) >= 400.0f) {
					f = 60.0f;
					f2 = this.mainActivity.getResources().getDisplayMetrics().density;
				} else {
					f = 30.0f;
					f2 = this.mainActivity.getResources().getDisplayMetrics().density;
				}
				int i = (int) (f2 * f);
				int i2 = (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
				ViewGroup viewGroup = (ViewGroup) this.quickkeysbarView.findViewById(0x7f080143);
				viewGroup.removeAllViews();
				for (String str2 : str.split(" ")) {
					String replace = str2.replace("s", " ");
					TextView textView = (TextView) from.inflate(0x7f0a003c, (ViewGroup) null);
					if (replace.trim().length() == 0) {
						textView.setText("⇥");
					} else {
						textView.setText(replace);
					}
					viewGroup.addView(textView, new LinearLayout.LayoutParams(i, i2));
					textView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (QuickKeysBarFormAide2.DW(QuickKeysBarFormAide2.this) == null) {
									QuickKeysBarFormAide2.FH(QuickKeysBarFormAide2.this, KeyCharacterMap.load(-1));
								}
								KeyEvent[] events = QuickKeysBarFormAide2.DW(QuickKeysBarFormAide2.this)
									.getEvents(replace.toCharArray());
								if (events != null) {
									for (KeyEvent keyEvent : events) {
										mainActivity.dispatchKeyEvent(keyEvent);
									}
								}
							}
						});
				}
			} catch (Throwable th) {
				throw th;
			}
		}

		public void show(boolean z) {
			try {
				if (this.quickkeysbarView != null) {
					this.quickkeysbarView.findViewById(0x7f08013f).setVisibility(z ? 0 : 4);
				}
			} catch (Throwable th) {
				throw th;
			}
		}

		public int v5() {
			try {
				if (this.Hw) {
					return (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
				}
				return 0;
			} catch (Throwable th) {
				throw th;
			}
		}
	}

	public static class QuickKeysBarFormAide {

		private String FH;

		private boolean Hw;

		private MainActivity mainActivity;

		private View quickkeysbarView;

		private KeyCharacterMap v5;

		public QuickKeysBarFormAide(MainActivity mainActivity) {
			this.FH = "";
			this.mainActivity = mainActivity;
			this.quickkeysbarView = LayoutInflater.from(mainActivity).inflate(R.layout.quickkeysbar,
					mainActivity.findViewById(0x7f080120));
			VH(AndroidHelper.getVerticalScreenWidthInDp(mainActivity) >= 360.0f
					&& AndroidHelper.isNotTelevisionMode(mainActivity));
			this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButton)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							j6(QuickKeysBarFormAide.this, true);
						}
					});
			this.quickkeysbarView.findViewById(R.id.quickKeyBarCloseButton)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							QuickKeysBarFormAide.j6(QuickKeysBarFormAide.this, false);
						}
					});
		}

		static KeyCharacterMap DW(QuickKeysBarFormAide quickKeysBar) {
			return quickKeysBar.v5;
		}

		static KeyCharacterMap FH(QuickKeysBarFormAide quickKeysBar, KeyCharacterMap keyCharacterMap) {
			quickKeysBar.v5 = keyCharacterMap;
			return keyCharacterMap;
		}

		static MainActivity Hw(QuickKeysBarFormAide quickKeysBar) {
			return quickKeysBar.mainActivity;
		}

		private void VH(boolean z) {
			this.Hw = z;
			if (z) {
				this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(8);
				this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(0);
			} else {
				this.quickkeysbarView.findViewById(R.id.quickKeyBarOpenButtonContainer).setVisibility(0);
				this.quickkeysbarView.findViewById(R.id.quickKeyBarKeysContainer).setVisibility(8);
			}
		}

		static void j6(QuickKeysBarFormAide quickKeysBar, boolean z) {
			quickKeysBar.VH(z);
		}

		public void gn(String str) {
			float f;
			float f2;

			if (this.quickkeysbarView == null || str == null || this.FH.equals(str)) {
				return;
			}
			this.FH = str;
			LayoutInflater from = LayoutInflater.from(this.mainActivity);
			if (AndroidHelper.getVerticalScreenWidthInDp(this.mainActivity) >= 400.0f) {
				f = 60.0f;
				f2 = this.mainActivity.getResources().getDisplayMetrics().density;
			} else {
				f = 30.0f;
				f2 = this.mainActivity.getResources().getDisplayMetrics().density;
			}
			int i = (int) (f2 * f);
			int i2 = (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
			ViewGroup viewGroup = (ViewGroup) this.quickkeysbarView.findViewById(R.id.quickKeyBarList);
			viewGroup.removeAllViews();
			for (String str2 : str.split(" ")) {
				String replace = str2.replace("s", " ");
				TextView textView = (TextView) from.inflate(R.layout.quickkeysbar_key, (ViewGroup) null);
				if (replace.trim().length() == 0) {
					textView.setText("⇥");
				} else {
					textView.setText(replace);
				}
				viewGroup.addView(textView, new LinearLayout.LayoutParams(i, i2));
				textView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (QuickKeysBarFormAide.DW(QuickKeysBarFormAide.this) == null) {
							QuickKeysBarFormAide.FH(QuickKeysBarFormAide.this, KeyCharacterMap.load(-1));
						}
						KeyEvent[] events = QuickKeysBarFormAide.DW(QuickKeysBarFormAide.this)
								.getEvents(str2.toCharArray());
						if (events != null) {
							for (KeyEvent keyEvent : events) {
								QuickKeysBarFormAide.Hw(QuickKeysBarFormAide.this).dispatchKeyEvent(keyEvent);
							}
						}
					}
				});
			}

		}

		public void show(boolean z) {
			if (this.quickkeysbarView != null) {
				this.quickkeysbarView.findViewById(0x7f08013f).setVisibility(z ? 0 : 8);
			}
		}

		public int v5() {
			if (this.Hw) {
				return (int) (this.mainActivity.getResources().getDisplayMetrics().density * 40.0f);
			}
			return 0;
		}
	}

}

