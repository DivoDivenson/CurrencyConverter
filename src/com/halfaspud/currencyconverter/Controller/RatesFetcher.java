package com.halfaspud.currencyconverter.Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

import com.halfaspud.currencyconverter.Model.Currency;

/**
 * Pull down conversion rates from yahoo
 * @author divo
 *
 */
public class RatesFetcher {
	
	private static final String log_name = "Currency Converter";
	
	public HashMap<String, Currency> getCurrencyRates(HashMap<String, Currency> currencies)
	throws NullPointerException{
		HashMap<String, Currency> currencyRates = 
				parseResponse(getCurrencyRatesData(new ArrayList<Currency>(currencies.values())));
		
		Iterator<String> it = currencies.keySet().iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			if(currencyRates.get(key) != null){
				currencies.get(key).setRate(currencyRates.get(key).getRate());
			}else{
				throw new NullPointerException(key + " not found");
			}
			
		}
		
		return currencies;
	}
	
	private String getCurrencyRatesData(List<Currency> currencies){
		DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
		HttpGet httpGet = new HttpGet(buildQuery(currencies));	
		httpGet.setHeader("Content-type", "application/json");
		
		InputStream is = null;
		String result = null;

		try {
			HttpResponse response = httpClient.execute(httpGet);
			//Show response status
			Log.i("CC", response.getStatusLine().toString());

			//Get response entity
			HttpEntity entity = response.getEntity();
			
			if(entity != null){
			
				is = entity.getContent();
			
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();
			
				String line = null;
				while((line = reader.readLine()) != null){
					sb.append(line + "\n");
				}
				result = sb.toString();
			}
			
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.e(log_name, "Error fetching currency data");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(log_name, "Error fetching currency data");
		} finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(log_name, "That should not have happened");
				}
			}
		}
		
		
		
		return result;
	}
	
	
	private HashMap<String, Currency> parseResponse(String input){
		HashMap<String, Currency> result = null;
		
		try {
			result = new HashMap<String, Currency>();
			JSONObject obj = new JSONObject(input);
			
			obj = obj.getJSONObject("query")
					.getJSONObject("results");
			JSONArray data = obj.getJSONArray("rate");
			for(int i = 0; i < data.length(); i++){
				obj = data.getJSONObject(i);
				String code = obj.getString("id").substring(3);
				float rate = (float) obj.getDouble("Rate"); //meh
				String date = obj.getString("Date"); //Make into proper date later
				String time = obj.getString("Time");
				
				Currency currency = new Currency(code, rate);
				result.put(code, currency);
				
			}
			
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(log_name, "Error parsing currency response");
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param currencies Currencies to fetch exchange rates for, passed in by the ui
	 * @return
	 */
	private String buildQuery(List<Currency> currencies){
	
		String codes = new String("(");
		Iterator<Currency> it = currencies.iterator();
		while(it.hasNext()){
			Currency cur = it.next();
			codes += "\"USD" + cur.getCode() + "\""; //Everything is based off USD
			if(it.hasNext()) //Lazy way to do this
			{
				codes += ",";
				
			}
		}
		codes += ")";
		
		Uri.Builder builder = new Uri.Builder();//Uri.parse(result).buildUpon();
		builder.scheme("http").authority("query.yahooapis.com").appendPath("v1")
		.appendPath("public").appendPath("yql")
		.appendQueryParameter("q", "select * from yahoo.finance.xchange where pair in " + codes)
		.appendQueryParameter("format", "json")
		.appendQueryParameter("env", "store://datatables.org/alltableswithkeys");
		
		return builder.build().toString();
	}

}
