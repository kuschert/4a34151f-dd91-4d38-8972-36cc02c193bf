package net.kuschert.currencymonitoring.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CurrencyInfoServiceAsync {

	void getCurrencyInfo(String[] symbols, AsyncCallback<CurrencyInfo[]> callback);

}
