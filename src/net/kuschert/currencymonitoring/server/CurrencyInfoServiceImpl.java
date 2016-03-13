package net.kuschert.currencymonitoring.server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import net.kuschert.currencymonitoring.client.CurrencyInfo;
import net.kuschert.currencymonitoring.client.CurrencyInfoService;
import net.kuschert.currencymonitoring.client.InvalidCurrencyException;
import net.kuschert.currencymonitoring.client.RemoteCurrencyException;

public class CurrencyInfoServiceImpl extends RemoteServiceServlet implements CurrencyInfoService {
	
	public static final String ACCESS_KEY = "d6d2210fc4b9bff71fb45637e490d561";
    public static final String BASE_URL = "http://apilayer.net/api/";
    public static final String ENDPOINT_LIVE = "live";
    public static final String ENDPOINT_HISTORICAL = "historical";
    
    static CloseableHttpClient httpClient = HttpClients.createDefault();
	
	@Override
	public CurrencyInfo[] getCurrencyInfo(String[] symbols) throws InvalidCurrencyException, RemoteCurrencyException {
	    
		CurrencyInfo[] currencyInfo = new CurrencyInfo[symbols.length];
		
		StringBuffer currencies = new StringBuffer();
        currencies.append("HKD");
        for (String symbol : symbols) {
        	// check for valid currency code
	    	try {
	    		Currency.getInstance(symbol);
	    	} catch (IllegalArgumentException iae) {
	    		throw new InvalidCurrencyException(symbol); 
	    	}
        	currencies.append(',').append(symbol);
        }
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date date1MonthAgo = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		//get currency exchange rates from the internet
		HttpGet getLive = new HttpGet(BASE_URL + ENDPOINT_LIVE + "?access_key=" + ACCESS_KEY + "&currencies=" + currencies);
		HttpGet getHistorical = new HttpGet(BASE_URL + ENDPOINT_HISTORICAL + "?access_key=" + ACCESS_KEY + "&currencies=" + currencies + "&date=" + dateFormat.format(date1MonthAgo));
		try{
	        CloseableHttpResponse responseLive = httpClient.execute(getLive);
	        HttpEntity entityLive = responseLive.getEntity();

	        CloseableHttpResponse responseHistorical = httpClient.execute(getHistorical);
	        HttpEntity entityHistorical = responseHistorical.getEntity();
	        
	        // Convert JSON response to java object
	        JSONObject exchangeRatesLive = new JSONObject(EntityUtils.toString(entityLive));
	        JSONObject exchangeRatesHistorical = new JSONObject(EntityUtils.toString(entityHistorical));
	        
	        // get exchange rates
	        double priceUsdHkd = exchangeRatesLive.getJSONObject("quotes").getDouble("USDHKD");
	        double priceUsdHkd1MonthAgo = exchangeRatesHistorical.getJSONObject("quotes").getDouble("USDHKD");
	        for (int i=0; i < symbols.length; i++) {
	        	//System.out.println("LIVE: " + exchangeRatesLive.getString("source") + " - " + symbols[i] + " - " + exchangeRatesLive.getJSONObject("quotes").getDouble("USD" + symbols[i]));
	        	//System.out.println("HIST: " + exchangeRatesHistorical.getString("source") + " - " + symbols[i] + " - " + exchangeRatesHistorical.getJSONObject("quotes").getDouble("USD" + symbols[i]));
	        	
	        	double priceHkd = exchangeRatesLive.getJSONObject("quotes").getDouble("USD" + symbols[i]) / priceUsdHkd;  
	        	double priceHkd1MonthAgo = exchangeRatesHistorical.getJSONObject("quotes").getDouble("USD" + symbols[i]) / priceUsdHkd1MonthAgo;
	        	
	        	currencyInfo[i] = new CurrencyInfo(symbols[i], priceHkd, priceHkd1MonthAgo);
	        }
	 
	        responseLive.close();
	        
		} catch (IOException | ParseException | JSONException e) {
			throw new RemoteCurrencyException(e.getMessage());
		}

	    return currencyInfo;
	}

}
