package net.kuschert.currencymonitoring.client;

import java.io.Serializable;

public class CurrencyInfo implements Serializable {
	
	private String symbol;
	private double priceHkd;
	private double priceHkd1MonthAgo;

	public CurrencyInfo() {}

	public CurrencyInfo(String symbol, double priceHkd, double priceHkd1MonthAgo) {
		super();
		this.symbol = symbol;
		this.priceHkd = priceHkd;
		this.priceHkd1MonthAgo = priceHkd1MonthAgo;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getPriceHkd() {
		return priceHkd;
	}

	public void setPriceHkd(double priceHkd) {
		this.priceHkd = priceHkd;
	}
	
	public double getPriceHkd1MonthAgo() {
		return priceHkd1MonthAgo;
	}

	public void setPriceHkd1MonthAgo(double priceHkd1MonthAgo) {
		this.priceHkd1MonthAgo = priceHkd1MonthAgo;
	}

	public double getPercentageChange1Month() {
		return (this.priceHkd - this.priceHkd1MonthAgo) / this.priceHkd * 100.0;
	}	  
}
