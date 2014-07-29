package com.halfaspud.currencyconverter.View;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.halfaspud.currencyconverter.R;
import com.halfaspud.currencyconverter.Controller.CurrencyDBHelper;
import com.halfaspud.currencyconverter.Controller.SharedPreferencesHelper;
import com.halfaspud.currencyconverter.Model.Currency;
import com.halfaspud.currencyconverter.Model.CurrencyComparator;



public class EditCurrencyListActivity extends Activity {

	private static final String log_name = "Currency Converter";
	
	Intent intent;

	//Using this instead of default Preferences way,
	//might get rid of Preferences and just put this in the menu in Main
	SharedPreferencesHelper prefs;
	private LinkedHashSet<String> selectedCurrencies;

	private EditListAdapter adapter;
	private ListView currencyListView;


	private LinkedList<Currency> currencies;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_currency_list);
		
		intent = getIntent();

		prefs = new SharedPreferencesHelper(this.getApplicationContext());
		selectedCurrencies = prefs.getSet(prefs.SELECTED_CURRENCIES);
		String baseCurrency = prefs.getBase();
		Log.d(log_name, "Edit "+selectedCurrencies.toString());
		currencies = getCurrencies();
		//Maybe a bit clunky, but the user should set the base currency elsewhere
		currencies.remove(baseCurrency);
		
		Collections.sort(currencies, new CurrencyComparator());
		 

		initGUI();
	}


	//TODO: Background this or something, maybe
	private LinkedList<Currency> getCurrencies(){
		CurrencyDBHelper db = new CurrencyDBHelper(getApplicationContext());
		return new LinkedList<Currency>(db.getAllCurrencies());
	}


	private void initGUI(){
		initList();
		
		EditText search = (EditText) findViewById(R.id.edit_search);
		search.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				adapter.setSelected(selectedCurrencies);
				adapter.getFilter().filter(arg0);			
			}
			
		});
	}

	private void initList(){
		String[] selected = new String[selectedCurrencies.size()];
		selectedCurrencies.toArray(selected);

		adapter = new EditListAdapter(this,
				R.layout.edit_currency_list_entry, currencies, selectedCurrencies);
		currencyListView = (ListView) findViewById(R.id.edit_currencies_list);
		currencyListView.setAdapter(adapter);

		for(int i = selected.length -1; i >= 0; i--){
			Currency c = new Currency(selected[i], 0.0f);
			int index = adapter.getPosition(c);
			if(index != -1){
				//Currency c = currencies.get(index);
				adapter.onRearrangeRequest(index, 0);
			}else{
				selectedCurrencies.remove(selected[i]); //Shouldn't be checking data here but whateves
    			Log.e(log_name, "EditCurrencyListActivity: " + selected[i] + " invalid");
    		}
		}
		//adapter.onRearrangeRequest(39, 1);

		//currencyListView.setClickable(true);

	}
	

	public void onClick(String in, boolean state){
		Log.d(log_name, "Checked " + in + " " + state);
		if(state){ //Add to selected
			selectedCurrencies.add(in);
			int index = adapter.getPosition((new Currency(in, 0.0f)));
			adapter.onRearrangeRequest(index, selectedCurrencies.size() - 1);
		}else{ //Remove from selected
			selectedCurrencies.remove(in);
			int index = adapter.getPosition((new Currency(in, 0.0f)));
			int too = adapter.findCodePosition(adapter.getItem(index),
					selectedCurrencies.size() +1);
			adapter.onRearrangeRequest(index, too);			
		}
		adapter.setSelected(selectedCurrencies);
	}
	
	@Override
	public void onBackPressed(){
		prefs.putList(prefs.SELECTED_CURRENCIES, selectedCurrencies);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	
}


