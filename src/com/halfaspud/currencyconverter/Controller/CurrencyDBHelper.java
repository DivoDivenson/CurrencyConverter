package com.halfaspud.currencyconverter.Controller;

//http://hmkcode.com/android-simple-sqlite-database-tutorial/

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.halfaspud.currencyconverter.Model.Currency;
import com.halfaspud.currencyconverter.Model.CurrencyComparator;


/**
 * Strong coupling everywhere
 * @author divo
 *
 */
//TODO:Dataset is incomplete, add remaning currencies later
public class CurrencyDBHelper extends SQLiteOpenHelper {
	

	private static final String log_name = "Currency Converter";

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "CurrencyDB";
	
	public static final String TABLE_CURRENCIES = "currencies";
	public static final String COL_ID = "_id"; //Feck it, do it this way
	public static final String COL_CODE = "code";
	public static final String COL_NAME = "name";
	public static final String COL_SYM = "symbol";
	public static final String COL_SYM_NATIVE = "sym_native";
	public static final String COL_NAME_PL = "name_pl";
	public static final String COL_ROUNDING = "rounding";
	public static final String COL_DIGITS = "digits";
	public static final String COL_RATE = "rate";
	public static final String COL_AMOUNT = "amount";
	
	private static final String[] COLUMNS = {COL_ID, COL_CODE, COL_NAME, COL_SYM, 
		COL_SYM_NATIVE, COL_NAME_PL, COL_ROUNDING, COL_DIGITS, COL_RATE, COL_AMOUNT};
	
	
	public static final String DATABASE_CREATE = "create table "
			+ TABLE_CURRENCIES
			+ "("
			+ COL_ID + " integer primary key autoincrement,"
			+ COL_CODE 			+ " text not null, "
			+ COL_NAME 			+ " text not null, "
			+ COL_SYM 			+ " text not null, "
			+ COL_SYM_NATIVE 	+ " text not null, "
			+ COL_NAME_PL		+ " text not null, "
			+ COL_ROUNDING		+ " integer not null, "
			+ COL_DIGITS		+ " integer not null, "
			+ COL_RATE			+ " real,"
			+ COL_AMOUNT		+ " real"
			+ ");";


	public CurrencyDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
		Log.d(log_name, "DB init, created on access");
		//Populate here?
		
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(log_name, "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCIES);
		    onCreate(db);
		
	}
	
	
	//---------------------CRUD operations-----------------
	//TODO:Writes should really return booleans
	
	public void updateRate(Currency currency){
		Log.d(log_name, "Updating rate for " + currency.toString());
		//update currencies set rate=1.0 where code="USD"
		/*String query = "update " + TABLE_CURRENCIES + " set " + COL_RATE
				+ "=" + Float.toString(currency.getRate()) + " where "
				+ COL_CODE + "=" + currency.getCode() + ";";*/
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(COL_RATE, currency.getRate());
		
		db.update(TABLE_CURRENCIES, 
				values, 
				COL_CODE+" = ?", 
				new String[] {currency.getCode()} );
		db.close();
	}
	
	public void updateRates(HashMap<String, Currency> currencies){
		for(String s : currencies.keySet()){
			updateRate(currencies.get(s));
		}
	}
	
	public void addCurrency(Currency currency){
		Log.d(log_name, "Adding Currency: " + currency.toDebugString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(COL_CODE, currency.getCode());
		values.put(COL_NAME, currency.getName());
		values.put(COL_SYM, currency.getSymbol());
		values.put(COL_SYM_NATIVE, currency.getSym_native());
		values.put(COL_NAME_PL, currency.getName_pl());
		values.put(COL_ROUNDING, currency.getRounding());
		values.put(COL_DIGITS, currency.getDigits());
		values.put(COL_RATE, currency.getRate());
		values.put(COL_AMOUNT, currency.getAmount());

		
		
		db.insert(TABLE_CURRENCIES, null, values);
		db.close();
			
	}
	
	/**
	 * 
	 * @param code Currency Code is used to fetch currency
	 * @return
	 */
	public Currency getCurrency(String code){
		Currency result = null;

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor =
				db.query(TABLE_CURRENCIES,
						COLUMNS,
						" code = ?", 
						new String[] { code }, 
						null, null,	null, null);

		if(cursor != null){
			cursor.moveToFirst();

			result = parseDbRecord(cursor);

		}

		Log.d(log_name, result.toDebugString());

		return result;
	}
	
	public List<Currency> getAllCurrencies(){
		List<Currency> currencies = new ArrayList<Currency>();
		
		String query = "SELECT * from " + TABLE_CURRENCIES;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query,  null);
		
		if(cursor.moveToFirst()){
			do{
				currencies.add(parseDbRecord(cursor));
			}while(cursor.moveToNext());
		}
		
		Log.d(log_name, "Currencies fetched " + currencies.toString());
		
		return currencies;
	}
	
	
	
	public List<Currency> getCurrencies(Set<String> currencies){
		List<Currency> result = new ArrayList<Currency>();
		
		SQLiteDatabase db = this.getWritableDatabase();
		
		String query = "SELECT * FROM  currencies where ";
		Iterator<String> it = currencies.iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			query += "code='" + key + "'";
			if(it.hasNext()){ //Lazy way
				query += " OR ";
			}
		}
		query += ";";
	
		Cursor cursor = db.rawQuery(query, null);
		if(cursor.moveToFirst()){
			do{				
				result.add(parseDbRecord(cursor));
			}while(cursor.moveToNext());
		}
		Log.d(log_name, "Currencies fetched " + currencies.toString());
		
		return result;
	}
	
	//Convert list to hashmap because sometimes it makes sense to have a map
	public HashMap<String, Currency> buildCurrencyMap(List<Currency> currencies){
		HashMap<String, Currency> result = new HashMap<String, Currency>();
		
		Iterator<Currency> it = currencies.iterator();
		while(it.hasNext()){
			Currency currency = (Currency) it.next();
			result.put(currency.getCode(), currency);
		}
		
		return result;
	}
	
	public LinkedHashMap<String, Currency> buildLinkedCurrencyMap(List<Currency> currencies){
		LinkedHashMap<String, Currency> result = new LinkedHashMap<String, Currency>();
		Collections.sort(currencies, new CurrencyComparator());
		Iterator<Currency> it = currencies.iterator();
		while(it.hasNext()){
			Currency currency = (Currency) it.next();
			result.put(currency.getCode(), currency);
		}
		
		return result;
	}
	
	
	
	private Currency parseDbRecord(Cursor cursor){
		Currency currency = new Currency();
		currency.setCode(cursor.getString(1));
		currency.setName(cursor.getString(2));
		currency.setSymbol(cursor.getString(3));
		currency.setSym_native(cursor.getString(4));
		currency.setName_pl(cursor.getString(5));
		currency.setRounding(Integer.parseInt(cursor.getString(6)));
		currency.setDigits(Integer.parseInt(cursor.getString(7)));
		currency.setRate(Float.parseFloat(cursor.getString(8)));
		currency.setAmount(Float.parseFloat(cursor.getString(9)));
		return currency;
	}



}
