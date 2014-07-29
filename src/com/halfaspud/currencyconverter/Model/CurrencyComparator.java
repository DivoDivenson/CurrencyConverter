package com.halfaspud.currencyconverter.Model;

import java.util.Comparator;

public class CurrencyComparator implements Comparator<Currency>{

	@Override
	public int compare(Currency arg0, Currency arg1) {
		return arg0.getName().compareTo(arg1.getName());
	}

}
