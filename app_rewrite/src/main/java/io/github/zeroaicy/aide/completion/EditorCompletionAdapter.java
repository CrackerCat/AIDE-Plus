package io.github.zeroaicy.aide.completion;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.iyutong.aide.translator.Translator;
import com.aide.common.AIDEHelpActivityStarter;
import com.aide.common.AppLog;
import com.aide.engine.SourceEntity;
import com.aide.ui.AIDEEditor;
import com.aide.ui.MainActivity;
import com.aide.ui.ServiceContainer;
import com.aide.ui.activities.a;
import com.aide.ui.rewrite.R;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import io.github.zeroaicy.aide.ui.services.ThreadPoolService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EditorCompletionAdapter extends ArrayAdapter<Object> {
	private static final int maxinitApiVersionSize = 0x1000;
	private static Map<String, ApiVersionInfo> infoMap = new ConcurrentHashMap<>(maxinitApiVersionSize * 2);

	private AIDEEditor aideEditor;

	private final List<SourceEntity> sourceEntitys;
	private final List<QuickCode> quickCodes = new ArrayList<>();

    public EditorCompletionAdapter(AIDEEditor aideEditor, List<SourceEntity> sourceEntitys) {
        super(aideEditor.getContext(), R.layout.completion_list_entry, new ArrayList<>());
		this.aideEditor = aideEditor;

		ApiVersionCompletion.preLoad(getContext());

		this.sourceEntitys = sourceEntitys;

		initApiVersionInfoAsync(this.sourceEntitys);

    }

	public static void setEditCurInput(ArrayAdapter<?> arrayAdapter, String editCurInput) {
		if( arrayAdapter instanceof EditorCompletionAdapter){
			((EditorCompletionAdapter)arrayAdapter).setEditCurInput(editCurInput);
		}
	}

	private void setEditCurInput(String editCurInput) {
		if(!this.quickCodes.isEmpty()){
			this.quickCodes.clear();
		}
		// 忽略大小写
		editCurInput = editCurInput.toLowerCase();
		// 重新匹配 QuickCode
		for(QuickCode quickCode : QuickCode.getAll()){
			String name = quickCode.getNameLowerCase();
			if( name.startsWith(editCurInput) ){
				this.quickCodes.add(quickCode);
			}
		}
		notifyDataSetChanged();
	}

    private void DW(TextView textView, int start, int end, int color) {
		((Spannable) textView.getText()).setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void j6(TextView textView, int start, int end) {
		((Spannable) textView.getText()).setSpan(new StyleSpan(1), start, end, 33);
    }


	static class ViewHolder {
		View rootView;

		TextView completionEntryName;
		TextView completionEntryNamefy;
		TextView completionEntryNamebf;
		ImageView completionEntryImage;
		ImageView completionHelpButton;
		public ViewHolder(View rootView) {
			this.rootView = rootView;

			completionEntryName = findViewById(R.id.completionEntryName);
			completionEntryImage = findViewById(R.id.completionEntryImage);
			completionHelpButton = findViewById(R.id.completionHelpButton);
			completionEntryNamefy = findViewById(R.id.completionEntryNamefy);
			completionEntryNamebf = findViewById(R.id.completionEntryNamebf);

		}

		public final <T extends View> T findViewById(int id) {
			return this.rootView.findViewById(id);
		}
	}


	@Override
	public void clear() {
		// 清除
		this.quickCodes.clear();
		this.sourceEntitys.clear();
	}

	// 忽略没有被调用
	@Override
	public void add(Object object) {
		if( object instanceof SourceEntity){
			this.sourceEntitys.add((SourceEntity)object);
		}
	}


	@Override
	public void addAll(Collection<? extends Object> collection) {
		this.sourceEntitys.addAll((List<SourceEntity>)collection);
		notifyDataSetChanged();
	}


	// 从 add -> addAll 减少 notifyDataSetChanged调用次数
	@Override
	public void notifyDataSetChanged() {
		initApiVersionInfoAsync(this.sourceEntitys);
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return this.quickCodes.size() + this.sourceEntitys.size();
	}
	@Override
	public Object getItem(int position) {
		int quickCodesSize = this.quickCodes.size();
		if( position < quickCodesSize){
			return this.quickCodes.get(position);
		}
		return this.sourceEntitys.get(position - quickCodesSize);
	}

	private static final int QuickCodeType = -1;
	private static final int SourceEntityType = 1;
	@Override
	public int getItemViewType(int position) {
		int quickCodesSize = this.quickCodes.size();
		if( position < quickCodesSize){
			return QuickCodeType;
		}
		return SourceEntityType;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View entryView;

		ViewHolder viewholder;
        if (convertView == null) {
			entryView = LayoutInflater.from(getContext()).inflate(R.layout.completion_list_entry, parent, false);
			viewholder = new ViewHolder(entryView);
			entryView.setTag(viewholder);     //将viewholder存储在view中
		} else {
			entryView = convertView;

			viewholder = (ViewHolder) entryView.getTag();
		}

		Object item = getItem(position);
		if (item == null) {
			viewholder.completionEntryName.setText("No matches");
			viewholder.completionEntryNamefy.setVisibility(View.GONE);
			viewholder.completionEntryImage.setImageResource(R.drawable.browser_empty);
			viewholder.completionHelpButton.setVisibility(View.GONE);
			return entryView;
		}
		int itemViewType = getItemViewType(position);
		if( itemViewType == QuickCodeType){
			showQuickCode((QuickCode)item, viewholder);
		}else if( itemViewType == SourceEntityType){			
			showSourceEntity((SourceEntity)item, viewholder);
		}else{
			viewholder.completionEntryName.setText("No matches");
			viewholder.completionEntryNamefy.setVisibility(View.GONE);
			viewholder.completionEntryImage.setImageResource(R.drawable.browser_empty);
			viewholder.completionHelpButton.setVisibility(View.GONE);
		}
		return entryView;

    }

	private void showQuickCode(QuickCode quickCode, EditorCompletionAdapter.ViewHolder viewholder) {
		ImageView completionEntryImage = viewholder.completionEntryImage;
		TextView entryNameView = viewholder.completionEntryName;


		completionEntryImage.setImageResource(R.drawable.objects);
		String name = quickCode.getName();

		String text = name + " -\n" +quickCode.getCodeText();
		entryNameView.setText(text, TextView.BufferType.SPANNABLE);
		DW(entryNameView, name.length(), text.length(), getContext().getColor(R.color.browser_label_gray));

		viewholder.completionHelpButton.setVisibility(View.GONE);
		viewholder.completionEntryNamefy.setVisibility(View.GONE);

	}

	private void showSourceEntity(SourceEntity sourceEntity, ViewHolder viewholder) {

		int sourceEntityType = sourceEntityTypes[sourceEntity.j3().ordinal()];

		TextView entryNameView = viewholder.completionEntryName;

		// entityName
		String entityName = sourceEntity.Mr();

		viewholder.completionEntryNamebf.setText(entityName);

		switch (sourceEntityType) {
			case MethodType: 
			case FieldType: 
			case VariableType: {
					// : + typeName
					String typeNameSuffix = sourceEntity.a8();
					if (typeNameSuffix != null) {
						String text = entityName + typeNameSuffix;
						entryNameView.setText(text, TextView.BufferType.SPANNABLE);
						DW(entryNameView, entityName.length(), text.length(), getContext().getColor(R.color.browser_label_gray));
					} else {
						entryNameView.setText(entityName);
					}
				}
				break;
			case ClassType: {
					//
					if (sourceEntity.gW()) {
						// sourceEntity.J8() 包名
						String text = entityName + " - " + sourceEntity.J8();
						entryNameView.setText(text, TextView.BufferType.SPANNABLE);
						DW(entryNameView, entityName.length(), text.length(), getContext().getColor(R.color.browser_label_gray));
					} else {
						entryNameView.setText(entityName);
					}
				}
				break;
			case KeywordType: {
					entryNameView.setText(entityName, TextView.BufferType.SPANNABLE);
					j6(entryNameView, 0, entityName.length());
				}
				break;
			default:
				entryNameView.setText(entityName);
				break;
		}

		if (ZeroAicySetting.isEnableTranslate()){
			String text = Translator.text(entityName);
			if (TextUtils.isEmpty(text)){
				viewholder.completionEntryNamefy.setVisibility(View.GONE);
			} else {
				viewholder.completionEntryNamefy.setText(text);
				viewholder.completionEntryNamefy.setVisibility(View.VISIBLE);
			}
		}

		// 追加 api 版本信息
		final String docUrl = sourceEntity.Ws();
		ApiVersionInfo info = docUrl == null ? ApiVersionInfo.Empty : infoMap.get(docUrl);
		setTo(entryNameView, info);


		ImageView completionEntryImage = viewholder.completionEntryImage;
		switch (sourceEntityType) {
			case MethodType: {
					if (sourceEntity.er()) {
						completionEntryImage.setImageResource(R.drawable.box_light_red);
					} else {
						completionEntryImage.setImageResource(R.drawable.box_red);
					}
				}
				break;
			case FieldType:
				if (sourceEntity.er()) {
					completionEntryImage.setImageResource(R.drawable.box_light_blue);
				} else {
					completionEntryImage.setImageResource(R.drawable.box_blue);
				}
				break;
			case VariableType:
				completionEntryImage.setImageResource(R.drawable.box_blue);
				break;
			case ClassType:
				if (sourceEntity.er()) {
					completionEntryImage.setImageResource(R.drawable.objects_light);
				} else {
					completionEntryImage.setImageResource(R.drawable.objects);
				}
				break;
			case PackageType:
				completionEntryImage.setImageResource(R.drawable.pakage);					
				break;
			default:
				completionEntryImage.setImageResource(R.drawable.browser_empty);
				break;
		}

		View completionHelpButton = viewholder.completionHelpButton;

		completionHelpButton.setVisibility(docUrl != null ? View.VISIBLE : View.GONE);
		if (docUrl != null) {
			completionHelpButton.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						MainActivity mainActivity = ServiceContainer.getMainActivity();
						mainActivity.getAIDEEditorPager().Eq();
						a v5 = com.aide.ui.activities.a.v5();
						AIDEHelpActivityStarter.DW(mainActivity, docUrl, v5.toString());
					}
				});
		}
	}

	private static void initApiVersionInfoAsync(List<SourceEntity> sourceEntitys) {
		if (sourceEntitys.isEmpty()) {
			return;
		}
		final ArrayList<SourceEntity> sourceEntitysCopy = new ArrayList<SourceEntity>(sourceEntitys);

		ThreadPoolService.getDefaultThreadPoolService()
			.submit(new Runnable(){
				@Override
				public void run() {
					initApiVersionInfo(sourceEntitysCopy, infoMap);
				}
			});
	}

	private static void initApiVersionInfo(List<SourceEntity> sourceEntitys, Map<String, ApiVersionInfo> infoMap) {
		if (infoMap == null) {
			return;
		}

		// 防止 infoMap 一直增长
		int infoMapSize = infoMap.size();
		if( infoMapSize > maxinitApiVersionSize){
			int needRemoveNumber = infoMapSize - maxinitApiVersionSize;
			Iterator<Map.Entry<String, ApiVersionInfo>> iterator = infoMap.entrySet().iterator();
			while (iterator.hasNext() && needRemoveNumber > 0) {
				Map.Entry<String, ApiVersionInfo> entry = iterator.next();
				ApiVersionInfo value = entry.getValue();
				if (value == ApiVersionInfo.Empty || value.memberInfo == null) {
					iterator.remove();
					needRemoveNumber--;
				}else if( needRemoveNumber % 8 == 0 ) {
					// 带 memberInfo 构造起来耗时
					iterator.remove();
					needRemoveNumber--;
				}
			}
		}
		for (SourceEntity sourceEntity : sourceEntitys) {
			if (sourceEntity == null) {
				continue;
			}
			final String docUrl = sourceEntity.Ws();
			if (docUrl == null) {
				continue;
			}
			if (infoMap.containsKey(docUrl)) {
				continue;
			}
			ApiVersionInfo apiVersionInfo = getApiVersionInfo(sourceEntity);
			// ConcurrentHashMap 不允许 value 为 null
			if (apiVersionInfo == null) {
				apiVersionInfo = ApiVersionInfo.Empty;
			}
			infoMap.put(docUrl, apiVersionInfo);

		}
	}

	private static ApiVersionInfo getApiVersionInfo(SourceEntity sourceEntity) {
		if (sourceEntity == null) {
			return null;
		}
		int sourceEntityType = sourceEntityTypes[sourceEntity.j3().ordinal()];

		if (sourceEntityType != ClassType
			&& sourceEntityType != MethodType
			&& sourceEntityType != FieldType) {
			return null;
		}

		final String docUrl = sourceEntity.Ws();
		if (TextUtils.isEmpty(docUrl)) {
			return null;
		}

		int typeNameEnd = docUrl.indexOf(".html");
		String typeName = docUrl.substring(0, typeNameEnd).replace('.', '$');

		int memberInfoEnd = docUrl.indexOf("#") + 1;
		String memberInfo = memberInfoEnd > 0 ? docUrl.substring(memberInfoEnd) : "";
		switch (sourceEntityType) {
			case ClassType:
				return ApiVersionCompletion.getApiVersionInfo(typeName);
			case FieldType:
				return ApiVersionCompletion.getFieldApiVersionInfo(typeName, memberInfo);
			case MethodType:
				// 没有返回类型信息
				return ApiVersionCompletion.getMethodApiVersionInfo(typeName, memberInfo);
		}
		return null;
	}

	public static void setTo(TextView completionEntryName, ApiVersionInfo result) {
		// 消除 缓存view遗留的Paint.STRIKE_THRU_TEXT_FLAG
		if (result == null || result == ApiVersionInfo.Empty) {
			int flags = completionEntryName.getPaint().getFlags();
			if ((flags & Paint.STRIKE_THRU_TEXT_FLAG) == 0) {
				return;
			}
			flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
			completionEntryName.getPaint().setFlags(flags);
			completionEntryName.invalidate();
			return;
		} else if (result.isRemoved()) {
			int flags = completionEntryName.getPaint().getFlags();
			flags |= Paint.STRIKE_THRU_TEXT_FLAG;
			completionEntryName.getPaint().setFlags(flags);
			completionEntryName.invalidate();
		}

		CharSequence info = result.getInfo(completionEntryName.getContext());
		if (info.length() > 0) {
			SpannableString text = new SpannableString(info);
			text.setSpan(new ForegroundColorSpan(0xFFAAAAAA), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			text.setSpan(new AbsoluteSizeSpan(12, true), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			completionEntryName.append("\n");
			completionEntryName.append(text);

			// View parent = (View) completionEntryName.getParent();
			// if (parent.getVisibility() != View.VISIBLE) parent.setVisibility(View.VISIBLE);
		}


	}


	public static final int MethodType = 1;
	public static final int FieldType = 2;

	public static final int VariableType = 3;
	public static final int ClassType = 4;
	public static final int KeywordType = 5;
	public static final int PackageType = 6;


	static final int[] sourceEntityTypes;
	static {
		sourceEntityTypes = new int[SourceEntity.b.values().length];
		try {
			sourceEntityTypes[SourceEntity.b.Method.ordinal()] = MethodType;
		} catch (Throwable e) {
		}
		try {
			sourceEntityTypes[SourceEntity.b.Field.ordinal()] = FieldType;
		} catch (Throwable e) {
		}
		try {
			sourceEntityTypes[SourceEntity.b.Variable.ordinal()] = VariableType;
		} catch (Throwable e) {
		}
		try {
			sourceEntityTypes[SourceEntity.b.Class.ordinal()] = ClassType;
		} catch (Throwable e) {
		}
		try {
			sourceEntityTypes[SourceEntity.b.Keyword.ordinal()] = KeywordType;
		} catch (Throwable e) {
		}
		try {
			sourceEntityTypes[SourceEntity.b.Package.ordinal()] = PackageType;
		} catch (Throwable e) {
		}
	}

}

