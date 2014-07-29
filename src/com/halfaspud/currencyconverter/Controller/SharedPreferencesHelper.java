package com.halfaspud.currencyconverter.Controller;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class SharedPreferencesHelper {
	
	
	public static final String SELECTED_CURRENCIES = "selectedCurrencies";
	public static final String BASE_CURRENCY = "baseCurrency";

	Context context;
	SharedPreferences prefs;
	
	public SharedPreferencesHelper(Context context){
		this.context = context;
		this.prefs = 
				context.getSharedPreferences("com.halfaspud.currencyconverter", 
						context.MODE_PRIVATE);
	}
	
	
	public boolean getFirstRun(){
		return prefs.getBoolean("firstRun", true);
	}
	
	public void setRan(){
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean("firstRun", false);
		edit.commit();
	}
	
	public String getBase(){
		return prefs.getString(BASE_CURRENCY, "none");
	}
	
	public void putBase(String base){
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(BASE_CURRENCY, base);
		edit.commit();
	}
	
	/**
	 * Strings only, no commas
	 */
	public void putList(String name, LinkedHashSet<String> input){
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(name, TextUtils.join(",", input));
		edit.commit();
	}
	
	public List<String> getList(String name){
		return Arrays.asList(TextUtils.split(prefs.getString(name, null), ","));
	}
	
	public LinkedHashSet<String> getSet(String name){
		return new LinkedHashSet<String>(getList(name));
	}

}
