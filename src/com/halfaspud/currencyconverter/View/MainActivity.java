package com.halfaspud.currencyconverter.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.halfaspud.currencyconverter.R;
import com.halfaspud.currencyconverter.Controller.CurrencyDBHelper;
import com.halfaspud.currencyconverter.Controller.CurrencyParser;
import com.halfaspud.currencyconverter.Controller.RatesFetcher;
import com.halfaspud.currencyconverter.Controller.SharedPreferencesHelper;
import com.halfaspud.currencyconverter.Model.Currency;


public class MainActivity extends Activity {

	private static final String log_name = "Currency Converter";
	private static final String INIT_BASE = "USD";


	SharedPreferencesHelper prefs;

	MenuItem editBaseItem;

	private LinkedHashSet<String> selectedCurrencies = 
			new LinkedHashSet<String>(Arrays.asList
					("EUR", "CAD", "GBP"));

	private static CurrencyDBHelper db;

	private ListView currencyListView;

	private Currency base;
	private Currency USD; //All conversions done in terms of usd, saves having to refetch data on new base
	HashMap<String, Currency> currencies;
	CurrencyListAdapter adapter;
	LinearLayout progress_bar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);



		prefs = new SharedPreferencesHelper(this.getApplicationContext());
		//TODO: Break networking bit off elsewhere
		//http://stackoverflow.com/questions/12240821/android-httpclient-execute-exception
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);

		progress_bar = (LinearLayout) findViewById(R.id.header_progress_bar);

		db = initDB(); //For some reason I took this (returning the dbhelper) out, maybe it caused problems
		Log.d(log_name, "DB init");

		if(prefs.getFirstRun()){
			Log.i(log_name, "First run");
			prefs.putList(SharedPreferencesHelper.SELECTED_CURRENCIES, selectedCurrencies);
			prefs.putBase(INIT_BASE);
			 
			prefs.setRan();
			
		}

		String baseCode = prefs.getBase();

		getCurrencies();


		setBaseCurrency(currencies.get(baseCode)); //Hack

		initGUI(currencies);

		getRates(currencies);
		

		//initGUI("USD", "EUR", currencies );

	}

	private void getCurrencies(){
		selectedCurrencies = prefs.getSet(SharedPreferencesHelper.SELECTED_CURRENCIES);
		Log.d(log_name, "Selected currencies fetched from local storage");

		String baseCode = prefs.getBase();
		selectedCurrencies.add(baseCode);


		boolean remove = selectedCurrencies.add(INIT_BASE);


		currencies = db.buildCurrencyMap(
				(ArrayList<Currency>) db.getCurrencies(selectedCurrencies));
		USD = currencies.get(INIT_BASE);
		//This hack is really bad
		if(remove){
			selectedCurrencies.remove(INIT_BASE);
			currencies.remove(INIT_BASE);
		}
		Log.d(log_name, "Currency data fetched from DB");
	}
	
	private void getRates(HashMap<String, Currency> currencies){
		if(isNetworkConnected()){
			showProgressBar();

			new GetRatesTask().execute(currencies);
			Log.d(log_name, "Network connected");
		}else{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No network connection")
			       .setCancelable(true)
			       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
			Log.d(log_name, "No network");
		}
	}
	
	private void showProgressBar(){
		progress_bar.setVisibility(View.VISIBLE);
		LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);

		for(int i = 0; i < main_layout.getChildCount(); i++){
			View view = main_layout.getChildAt(i);
			view.setEnabled(false);
		}
	}
	
	private void hideProgressBar(){
		LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);
		for(int i = 0; i < main_layout.getChildCount(); i++){
			View v = main_layout.getChildAt(i);
			v.setEnabled(true);
		}
		progress_bar.setVisibility(View.GONE);

	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	private class GetRatesTask extends AsyncTask<HashMap, Void, HashMap>{

		@Override
		protected HashMap doInBackground(HashMap... arg0) {
			//Log.d(log_name, "Bar " + progress_bar.toString());

			RatesFetcher fetcher = new RatesFetcher();
			return fetcher.getCurrencyRates(arg0[0]);
		}

		@Override
		protected void onPostExecute(HashMap result){
			currencies = result;
			Log.i(log_name, "Rates fetched");
			//Shouldn't be doing UI stuff here
			hideProgressBar();
			setBaseCurrency(currencies.get(base.getCode()));
			//Maybe put the following above
			db.updateRates(currencies);
			//Write them into db here
		}

	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == 1){
			if(resultCode == RESULT_OK){
				Float user_amount = data.getFloatExtra("user_amount", 0.0f);
				Currency selected_curr = data.getParcelableExtra("selected");
				Log.i(log_name,"User amount " + Float.toString(user_amount));
				updateListAmount(user_amount, selected_curr);	
			}
		}else if(requestCode == 2){
			if(resultCode == RESULT_OK){
				getCurrencies();

				initGUI(currencies);
			}

		}

	}
	
	private void updateListAmount(float user_amount, Currency selected_curr){
		TextView amount = (TextView) findViewById(R.id.currency_amount);
		String format_string = new String("%1$,." +
				Integer.toString(base.getDigits()) + "f");
		if((selected_curr.compareTo(base)) != 0){
			user_amount = (user_amount / selected_curr.getRate()) * base.getRate();  
		}
		amount.setText(base.getSym_native() + 
				String.format(format_string, user_amount));
		base.setAmount(user_amount);


		for(int i = 0; i < adapter.getCount(); i++){
			Currency c = adapter.getItem(i);
			float c_amount = (user_amount / base.getRate()) * c.getRate();
			c.setAmount(c_amount);

		}

		adapter.notifyDataSetChanged();

		Log.d(log_name, "User amount: " + Float.toString(user_amount));
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		editBaseItem = menu.findItem(R.id.action_set_base);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		int id = getResources().getIdentifier(base.getCode().toLowerCase(Locale.US),
				"drawable", getPackageName());


		editBaseItem.setIcon(id);
		return true;
	}


	private void setBaseCurrency(Currency base){
		this.base = base;
		//Log.d(log_name, "Base" + base);
		//if(currencies.containsKey(base.getCode())){
		//currencies.remove(base.getCode());
		//}

		int id = getResources().getIdentifier(base.getCode().toLowerCase(Locale.US),
				"drawable", getPackageName());

		//Menu
		ActivityCompat.invalidateOptionsMenu(this);

		//List
		TextView name = (TextView) findViewById(R.id.currency_name);
		name.setText(base.getName());

		TextView code = (TextView) findViewById(R.id.currency_code);
		code.setText(base.getCode());

		TextView amount = (TextView) findViewById(R.id.currency_amount);
		String format_string = new String("%1$,." +
				Integer.toString(base.getDigits()) + "f");
		amount.setText(base.getSym_native() + " " + String.format(format_string, base.getAmount()));


		ImageView flag = (ImageView) findViewById(R.id.list_image);
		flag.setImageResource(id);

		//final Currency hack = new Currency(base);
		RelativeLayout base_layout = (RelativeLayout) findViewById(R.id.list_entry_layout);
		base_layout.setOnClickListener(new BaseLayoutClickListener());
		
		EditText amount_edit = (EditText) findViewById(R.id.currency_amount_edit);
		amount_edit.addTextChangedListener(new AmountEditWatcher());
		amount_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
	        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		        if (hasFocus) {
		        	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		        }else{
		        	imm.hideSoftInputFromWindow(v.getWindowToken(),0); 
		        }
		    }
		});
		
		Button okButton = (Button) findViewById(R.id.currency_amount_cancel);
		okButton.setOnClickListener( new OnClickListener(){
			
			@Override
			public void onClick(View v){
				TextView amnt = (TextView) findViewById(R.id.currency_amount);
				EditText edt = (EditText) findViewById(R.id.currency_amount_edit);
				Button cancel = (Button) findViewById(R.id.currency_amount_cancel);
				
				amnt.setVisibility(View.VISIBLE);
				edt.setVisibility(View.GONE);
				edt.clearFocus();
				cancel.setVisibility(View.GONE);
			}
			
		});

	}
	
	private class BaseLayoutClickListener implements OnClickListener{
		@Override
		public void onClick(View arg0) {
			
			TextView amnt = (TextView) findViewById(R.id.currency_amount);
			updateListAmount(0.0f, base);
			EditText edt = (EditText) findViewById(R.id.currency_amount_edit);
			edt.setText("");
			Button cancel = (Button) findViewById(R.id.currency_amount_cancel);
			
			amnt.setVisibility(View.GONE);
			edt.setVisibility(View.VISIBLE);
			edt.requestFocus();
			cancel.setVisibility(View.VISIBLE);
		}

			
	}
	
	private class AmountEditWatcher implements TextWatcher{

		@Override
		public void afterTextChanged(Editable arg0) {}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {}
		
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			if(arg0.length() != 0){
				Float amount = 0.0f;
				//Exception handeling for control flow and you cant stop me
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
					//Intent intent = new Intent("com.halfaspud.currencyconverter.View.NyanActivity");
					//MainActivity.this.startActivity(intent);
				}
				
				updateListAmount(amount, base);
			}else{
				updateListAmount(0.0f, base);
			}
			
		}
		
	}
	
	private void launchNyan(){
		EditText amount_edit = (EditText) findViewById(R.id.currency_amount_edit);
		amount_edit.setEnabled(false);
		Intent intent = new Intent(getApplicationContext(), NyanActivity.class);
		startActivity(intent);
	}
	
	private boolean checkFloat(Float arg){
		if(arg == Float.POSITIVE_INFINITY || arg == Float.NaN || arg == Float.NEGATIVE_INFINITY){
			return true;
		}else{
			return false;
		}
	}
	
	

	private void initGUI(HashMap<String, Currency> currencies){	


		adapter = new CurrencyListAdapter(getApplicationContext(),
				R.layout.list_entry, new ArrayList<Currency>(currencies.values()), base);
		Log.d(log_name, "Adapter created");

		currencyListView = (ListView) findViewById(R.id.currencyListView);
		currencyListView.setAdapter(adapter);
		currencyListView.setOnItemClickListener(new ConvertListener());


	}

	class ConvertListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(getApplicationContext(), ConverterActivity.class);
			Currency selected = (Currency) parent.getAdapter().getItem(position);
			intent.putExtra("selected_currency", selected);
			intent.putExtra("base_currency", base);
			intent.putExtra("usd_currency", USD);
			startActivityForResult(intent, 1);

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_get_data:
			getRates(currencies);
			return true;
		case R.id.action_edit_currencies:
			//startActivity(new Intent(this, PrefsActivity.class));
			startActivityForResult(new Intent(this, EditCurrencyListActivity.class), 2);
			return true;
		case R.id.action_set_base:
			LinearLayout layout = (LinearLayout) findViewById(R.id.select_notifier);
			layout.setVisibility(View.VISIBLE);

			currencyListView = (ListView) findViewById(R.id.currencyListView);
			currencyListView.setOnItemClickListener(new SetBaseListener());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	class SetBaseListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//TODO:Moving base currency is messy, abstract it
			Currency selected = (Currency) parent.getAdapter().getItem(position);

			//Add base into selected
			selectedCurrencies.add(base.getCode());
			prefs.putList(SharedPreferencesHelper.SELECTED_CURRENCIES, selectedCurrencies);
			currencies.put(base.getCode(), base);
			adapter.add(base);

			//Set new base
			prefs.putBase(selected.getCode());
			setBaseCurrency(selected);

			//Remove new base from list
			currencies.remove(base.getCode());
			adapter.remove(selected);

			currencyListView = (ListView) findViewById(R.id.currencyListView);
			currencyListView.setOnItemClickListener(new ConvertListener());

			LinearLayout layout = (LinearLayout) findViewById(R.id.select_notifier);
			layout.setVisibility(View.GONE);


		}

	}



	/**
	 * Create a db instance. If database is not found it is created and populated
	 * with data from currencies.json
	 * @return
	 */
	private CurrencyDBHelper initDB(){
		CurrencyDBHelper db = new CurrencyDBHelper(this.getApplicationContext());

		//If no database read in the currencies and populate database
		//This works after CurrencyDBHelper is created because the database is not really
		//created until the first access.
		if(!checkDatabase()){ 
			Toast.makeText(this, "Initializing", Toast.LENGTH_SHORT).show();
			CurrencyParser parser = new CurrencyParser();
			HashMap<String, Currency> currencies = 
					parser.ParseJsonData(loadJSONFromAsset("currencies.json"));
			Log.i(log_name, currencies.toString());

			int i = 0;
			for(Currency item : currencies.values()){
				db.addCurrency(item);
				i++;
			}
			Log.i(log_name, "Currency Table created with "
					+ Integer.toString(i) + "entries");
		}

		return db;
	}


	//Check if DB exists to save reading in JSON
	//TODO: No idea if this works
	public boolean checkDatabase(){
		File database = getDatabasePath("CurrencyDB");

		if (!database.exists()) {
			// Database does not exist so copy it from assets here
			Log.i(log_name, "Database Not Found");
			return false;
		} else {
			Log.i(log_name, "Database Found");
			return true;
		}
	}

	public String loadJSONFromAsset(String name) {
		String result = null;

		try{
			InputStream is = getAssets().open(name);
			int size = is.available();
			byte[] buffer = new byte[size];

			is.read(buffer);
			is.close();

			result = new String(buffer, "UTF-8");

		}catch(IOException e){
			e.printStackTrace();
			return null;
		}

		return result;
	}

}
