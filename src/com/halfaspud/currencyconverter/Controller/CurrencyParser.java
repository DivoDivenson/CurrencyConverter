package com.halfaspud.currencyconverter.Controller;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.halfaspud.currencyconverter.Model.Currency;

/**
 * Read in JSON file and generate currency objects
 * @author divo
 *
 */
public class CurrencyParser {

	private static final String log_name = "Currency Converter";

	
	public HashMap<String, Currency> ParseJsonData(String data){
		HashMap<String, Currency> result = null;
		try {
			result = new HashMap<String, Currency>();

			JSONObject obj = new JSONObject(data);
			Iterator iterator = obj.keys();
			while(iterator.hasNext()){
				String key = (String)iterator.next();
				JSONObject item = obj.getJSONObject(key);
				
				//String coupling or whatever, forget if it's important to fix
				//not in this case anyway
				Currency currency = new Currency();
				String code = item.getString("code");
				
				currency.setCode(code);
				currency.setName(item.getString("name"));
				currency.setName_pl(item.getString("name_plural"));
				currency.setSymbol(item.getString("symbol"));
				currency.setSym_native(item.getString("symbol_native"));
				currency.setDigits(item.getInt("decimal_digits"));
				currency.setRounding(item.getInt("rounding"));
				
				result.put(code, currency);
			}		
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(log_name, "Error parsing currencie data");
		}
		
		return result;
	}
}
