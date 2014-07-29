package com.halfaspud.currencyconverter.View;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.halfaspud.currencyconverter.R;
import com.halfaspud.currencyconverter.Model.Currency;


public class CurrencyListAdapter extends ArrayAdapter<Currency> {

	private static final String log_name = "Currency Converter";

	
	//private static LayoutInflater inflater=null;
	
	Context context;
	int layoutResourceId;
	ArrayList<Currency> data;
	
	public CurrencyListAdapter(Context context, int layoutResourceId, 
			ArrayList<Currency> data, Currency base){
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;		
		this.data = data; 
		//Don't want to have base currency in the list, this is the quickest,
		//hackiest way to do it
		if(data.contains(base)){ 
			data.remove(base);
		}
		
		/*for(int i =0; i < data.size(); i++){
			if(data.get(i).getCode().compareTo(base.getCode()) == 0){
				data.remove(i);
				break;
			}
		}*/
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Currency getItem(int arg0) {
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	@Override
	public int getPosition(Currency item){
		return data.indexOf(item);
	}
	
	@Override
	public int getViewTypeCount(){
		return 1; //No types + 1
	}
	
	@Override 
	public int getItemViewType(int position){
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		CurrencyHolder holder = null;
		
		if(v == null){
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(layoutResourceId, parent, false);
			
			holder = new CurrencyHolder();
			holder.currencyTitle = (TextView) v.findViewById(R.id.currency_name);
			holder.currencyAmount = (TextView) v.findViewById(R.id.currency_amount);
			holder.currencyCode = (TextView) v.findViewById(R.id.currency_code);
			holder.currencyFlag = (ImageView) v.findViewById(R.id.list_image);
			
			v.setTag(holder);
		}else{
			holder = (CurrencyHolder)v.getTag();
		}
		
		Currency c = data.get(position);
		
		if(c != null){
			holder.currencyTitle.setText(c.getName());
			String format_string = new String("%1$,." +
					Integer.toString(c.getDigits()) + "f");
			if(c.getRate() == 0.0f){
				holder.currencyAmount.setText("No data");
			}else{
				holder.currencyAmount.setText(c.getSym_native() + " " + String.format(format_string, c.getAmount()));
			}
			holder.currencyCode.setText(c.getCode());
			int id = context.getResources().getIdentifier(c.getCode().toLowerCase(Locale.US),
					"drawable", context.getPackageName());
			holder.currencyFlag.setImageResource(id);
		}
		
		return v;
	}
	
	
	static class CurrencyHolder{
		TextView currencyTitle;
		TextView currencyAmount;
		TextView currencyCode;
		ImageView currencyFlag;
	}
	

}
