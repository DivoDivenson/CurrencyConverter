package com.halfaspud.currencyconverter.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Currency implements Parcelable, Comparable{
	
	
	private String code;
	private String name;
	private String symbol;
	private String sym_native;
	private String name_pl;
	private int rounding;
	private int digits;
	private float rate;
	private float amount;
	
	//Lazy
	public Currency(String code, float rate){
		this.code = code;
		this.rate = rate;
	}
	
	public Currency(String code, String name){
		this.code = code;
		this.name = name;
	}
	
	
	public Currency(Parcel in){
		this.code = in.readString();
		this.name = in.readString();
		this.symbol = in.readString();
		this.sym_native = in.readString();
		this.name_pl = in.readString();
		this.rounding = in.readInt();
		this.digits = in.readInt();
		this.rate = in.readFloat();
		this.amount = in.readFloat();
	}
	
	public Currency(){
		
	}
	
	public Currency(Currency c) {
		super();
		this.code = c.code;
		this.name = c.name;
		this.symbol = c.symbol;
		this.sym_native = c.sym_native;
		this.name_pl = c.name_pl;
		this.rounding = c.rounding;
		this.digits = c.digits;
		this.rate = c.rate;
		this.amount = c.amount;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(code);
		dest.writeString(name);
		dest.writeString(symbol);
		dest.writeString(sym_native);
		dest.writeString(name_pl);
		dest.writeInt(rounding);
		dest.writeInt(digits);
		dest.writeFloat(rate);
		dest.writeFloat(amount);
		
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
		@Override
		public Currency createFromParcel(Parcel in){
			return new Currency(in);
		}

		@Override
		public Object[] newArray(int size) {
			return new Currency[size];
		}
	};
	

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSym_native() {
		return sym_native;
	}
	public void setSym_native(String sym_native) {
		this.sym_native = sym_native;
	}
	public String getName_pl() {
		return name_pl;
	}
	public void setName_pl(String name_pl) {
		this.name_pl = name_pl;
	}
	public int getRounding() {
		return rounding;
	}
	public void setRounding(int rounding) {
		this.rounding = rounding;
	}
	public int getDigits() {
		return digits;
	}
	public void setDigits(int digits) {
		this.digits = digits;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}
	
	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public String toDebugString() {
		return "Currency [code=" + code + ", name=" + name + ", symbol="
				+ symbol + ", sym_native=" + sym_native + ", name_pl="
				+ name_pl + ", rounding=" + rounding + ", digits=" + digits
				+ ", rate=" + rate + ", amount=" + amount
				+ "]";
	}
	
	//Little bit of a hack to easily populate the list
	@Override
	public String toString(){
		return code;
		//return toDebugString();
	}
	
	@Override
	public boolean equals(Object c){
		if(c instanceof Currency){
			return this.code.equals(((Currency) c).getCode());
		}else{
			return false;
		}
	
	}
	
	
	
	@Override
	public int compareTo(Object c){
		if(c instanceof Currency){
			return this.name.compareTo(((Currency) c).getName());
		}
		return 0; //meh
	}

	
	
}
