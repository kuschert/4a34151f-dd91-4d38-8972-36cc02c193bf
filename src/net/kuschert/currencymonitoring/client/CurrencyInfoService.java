package net.kuschert.currencymonitoring.client;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("currencyInfos")
public interface CurrencyInfoService extends RemoteService {

	CurrencyInfo[] getCurrencyInfo(String[] symbols) throws InvalidCurrencyException, RemoteCurrencyException;
}
