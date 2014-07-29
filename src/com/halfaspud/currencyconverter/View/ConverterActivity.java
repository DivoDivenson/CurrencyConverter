package com.halfaspud.currencyconverter.View;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.halfaspud.currencyconverter.R;
import com.halfaspud.currencyconverter.Model.Currency;

public class ConverterActivity extends Activity {

	private static final String log_name = "Currency Converter";
	Intent intent;
	Float amount = 0.0f;
	Currency selected;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.converter_layout);
		Log.d(log_name, "Starting activity " + this.toString());

		intent = getIntent();

		selected = intent.getParcelableExtra("selected_currency");
		Currency base = new Currency();
		base.setRate(1.0f); //More hacks
		Currency usd = intent.getParcelableExtra("usd_currency");
		//So this is hacky but I'm not bothered to subclass or write another one
		if(intent.hasExtra("base_currency")){
			base = intent.getParcelableExtra("base_currency");
		}else{
			hideBaseLayout();
		}
		Log.d(log_name, selected.toDebugString());
		Log.d(log_name, base.toDebugString());

		initGUI(selected, base, usd);

		Log.i(log_name, "Selected " + selected.toDebugString());
		Log.i(log_name, "Base " + base.toDebugString());
		Log.i(log_name, "USD " + usd.toDebugString());
	}

	@Override
	public void onBackPressed(){
		intent.putExtra("user_amount", amount);
		intent.putExtra("selected", selected);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.converter, menu);
		return true;
	}

	private void hideBaseLayout(){
		RelativeLayout baseLayout = (RelativeLayout) findViewById(R.id.base_layout);
		baseLayout.setVisibility(RelativeLayout.GONE);
	}

	/** Init currency conversion ui **/
	private void initGUI(Currency selected, Currency base, Currency USD){
		float sel = selected.getRate(); //Will cause bug later
		int digits = base.getDigits();
		float base_rate = base.getRate();

		TextView name = (TextView) findViewById(R.id.selected_currency_name);
		name.setText(selected.getName());

		ImageView selected_image = (ImageView) findViewById(R.id.selected_image);
		int id = this.getResources().getIdentifier
				(selected.getCode().toLowerCase(Locale.US), "drawable", this.getPackageName());
		selected_image.setImageResource(id);

		//More hax
		if(base.getName() != null){

			name = (TextView) findViewById(R.id.base_currency_name);
			name.setText(base.getName());

			ImageView base_image = (ImageView) findViewById(R.id.base_image);
			id = this.getResources().getIdentifier
					(base.getCode().toLowerCase(Locale.US), "drawable", this.getPackageName());
			base_image.setImageResource(id);

		}


		EditText textField = (EditText) findViewById(R.id.EditText01); //Hack that will probably blow up later


		textField.addTextChangedListener(new AmountEditWatcher(sel, digits, base_rate));

		Button clearButton = (Button) findViewById(R.id.clear_button);
		clearButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				((EditText) findViewById(R.id.EditText01)).setText("");
				amount = 0.0f;
			}

		});
	}
	
	private void updateAmount(Float amount, Float sel, 
			Float base_rate, TextView resultText, int digits){
		Float result = (amount / sel) * base_rate;

		String format_string = new String("%1$,." 
				+ Integer.toString(digits) + "f"); //Also rounds
		resultText.setText(String.format(format_string, result));
		Log.i(log_name, "Final usd " + base_rate + " selected " + sel);

		
	}
	
	private boolean checkFloat(Float arg){
		if(arg == Float.POSITIVE_INFINITY || arg == Float.NaN || arg == Float.NEGATIVE_INFINITY){
			return true;
		}else{
			return false;
		}
	}
	
	private void launchNyan(){
		EditText amount_edit = (EditText) findViewById(R.id.EditText01);
		amount_edit.setEnabled(false);
		Intent intent = new Intent(getApplicationContext(), NyanActivity.class);
		startActivity(intent);
	}
	
	private class AmountEditWatcher implements TextWatcher{
		float amount;
		float sel;
		int digits;
		float base_rate;
		
		public AmountEditWatcher(Float sel, int digits, Float base_rate){
			this.sel = sel;
			this.digits = digits;
			this.base_rate = base_rate;
		}
		
		@Override
		public void afterTextChanged(Editable arg0) {}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1,
				int arg2, int arg3){}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3){

			TextView resultText = (TextView) findViewById(R.id.resultText);

			if(arg0.length() != 0){
				try{
					amount = Float.parseFloat(arg0.toString());
				}catch(Exception e){
					amount = 0.0f;
					if(e instanceof NumberFormatException){
						Log.e(log_name, "Number format exception: " + e);
					}else{
						Log.e(log_name, e.toString());
					}
					
				}
				if(checkFloat(amount)){
					Log.i(log_name, "NAN");
					amount = 0.0f;
					launchNyan();
				}
				
				updateAmount(amount, sel, base_rate, resultText, digits);
				
			}else{
				resultText.setText("");
			}


		}
	}
	
	

}
