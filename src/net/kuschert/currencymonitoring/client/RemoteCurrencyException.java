package net.kuschert.currencymonitoring.client;

import java.io.Serializable;

public class RemoteCurrencyException extends Exception implements Serializable {

	private String msg;

    public RemoteCurrencyException() {}

	public RemoteCurrencyException(String msg) {
		super();
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
