package com.halfaspud.currencyconverter.View;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class ExtendedEditText extends EditText {
	
	private TextWatcher currentWatcher = null;

	public ExtendedEditText(Context context) {
		super(context);
	}
	
	public ExtendedEditText(Context context, AttributeSet attributes) {
		super(context, attributes);
	}
	
	public ExtendedEditText(Context context, AttributeSet attributes, int style) {
		super(context, attributes, style);
	}
	
	@Override
	public void addTextChangedListener(TextWatcher watcher){
		if(currentWatcher != null){
			super.removeTextChangedListener(currentWatcher);
		}
		
		super.addTextChangedListener(watcher);
	}

}
