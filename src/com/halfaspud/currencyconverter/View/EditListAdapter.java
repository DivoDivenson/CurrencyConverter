package com.halfaspud.currencyconverter.View;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.halfaspud.currencyconverter.R;
import com.halfaspud.currencyconverter.Model.Currency;


public class EditListAdapter extends ArrayAdapter<Currency> implements Filterable{
	private static final String log_name = "Currency Converter";

	Context context;
	int layoutResourceId;
	LinkedList<Currency> data;
	LinkedList<Currency> originalData;
	Set<String> selectedCurrencies;


	public EditListAdapter(Context context, int layoutResourceId,
			LinkedList<Currency> data, Set<String> selected){
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
		this.originalData = new LinkedList<Currency>(data);
		this.selectedCurrencies = selected;	
	}

	public void setSelected(Set<String> selected){
		this.selectedCurrencies = selected;
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


	public int findCodePosition(Currency c, int offset){
		int i;
		for(i = offset; i < data.size(); i++){
			if(c.compareTo(data.get(i)) < 0){
				break;
			}
		}
		return i-1;
	}

	public boolean moveItem(Currency c, int too){
		int from = getPosition(c);
		return onRearrangeRequest(from, too);

	}

	public boolean onRearrangeRequest(int from, int too){
		if(too >= 0 && from < getCount()){
			Currency item = getItem(from);

			remove(item);
			insert(item, too);
			notifyDataSetChanged();

			return true;
		}

		return false;
	}

	

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View v = convertView;
		CurrencyHolder holder = null;
		Currency c = data.get(position);


		if(v == null){
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(layoutResourceId, parent, false);

			holder = new CurrencyHolder();
			holder.currencyFlag = (ImageView) v.findViewById(R.id.list_image);
			holder.currencyName = (TextView) v.findViewById(R.id.currency_name);
			holder.currencyCode = (TextView) v.findViewById(R.id.currency_code);
			holder.currencySelect = (CheckBox) v.findViewById(R.id.currency_selected);


			holder.currencySelect.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					//Super strong coupling
					//Log.d(log_name, "Checked" + cb.getTag());
					((EditCurrencyListActivity) context).onClick((String) cb.getTag(), cb.isChecked());

				}

			});
			v.setTag(holder);
		}else{
			holder = (CurrencyHolder) v.getTag();
		}


		if(c != null){
			holder.currencyCode.setText(c.getCode());
			holder.currencyName.setText(c.getName());
			int id = context.getResources().getIdentifier(c.getCode().toLowerCase(Locale.US),
					"drawable", context.getPackageName());
			holder.currencyFlag.setImageResource(id);

			if(selectedCurrencies.contains(c.getCode())){
				holder.currencySelect.setChecked(true);
			}else{
				holder.currencySelect.setChecked(false);
			}

			holder.currencySelect.setTag(c.getCode());

		}

		return v;
	}


	static class CurrencyHolder{
		ImageView currencyFlag;
		TextView currencyCode;
		TextView currencyName;
		CheckBox currencySelect;
	}

	@Override
	public Filter getFilter(){

		Filter filter = new Filter(){

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				LinkedList<Currency> filteredCurrencies = new LinkedList<Currency>();

				if(constraint.length() == 0){
					filteredCurrencies = originalData;
					
					String[] selected = new String[selectedCurrencies.size()];
					selectedCurrencies.toArray(selected);

					for(int i = selected.length -1; i >= 0; i--){
						Currency c = new Currency(selected[i], 0.0f);
						int index = filteredCurrencies.indexOf(c);
						if(index != -1){
							//Currency c = currencies.get(index);
							Currency item = filteredCurrencies.get(index);

							filteredCurrencies.remove(item);
							filteredCurrencies.add(0, item);
							
						}
					}
					
				}else{

					constraint = constraint.toString().toLowerCase(Locale.US);
					for(int i = 0; i < originalData.size(); i++){
						Currency c = originalData.get(i);
						if(c.getName().toLowerCase(Locale.US).contains(constraint.toString())
								|| (c.getCode().toLowerCase(Locale.US).contains(constraint.toString()))){
							filteredCurrencies.add(c);
						}
					}

				}

				results.count = filteredCurrencies.size();
				results.values = filteredCurrencies;

				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				data = (LinkedList<Currency>) results.values;
				notifyDataSetChanged();


			}

		};
		return filter;
	}


}
