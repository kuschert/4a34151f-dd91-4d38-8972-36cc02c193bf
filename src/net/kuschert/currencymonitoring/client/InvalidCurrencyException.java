package net.kuschert.currencymonitoring.client;

import java.io.Serializable;

public class InvalidCurrencyException extends Exception implements Serializable {

	private String symbol;

    public InvalidCurrencyException() {}

	public InvalidCurrencyException(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
