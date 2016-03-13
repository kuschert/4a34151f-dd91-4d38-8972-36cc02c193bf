package net.kuschert.currencymonitoring.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CurrencyMonitoring implements EntryPoint {
	
	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable currenciesFlexTable = new FlexTable();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox addCurrencySymbolTextBox = new TextBox();
	private Button addCurrencyButton = new Button("Add");
	private HorizontalPanel refreshPanel = new HorizontalPanel();
	private Button refreshCurrencyButton = new Button("Refresh");
	private Label lastUpdatedLabel = new Label();
	private Label errorMsgLabel = new Label();
	private Label inputErrorMsgLabel = new Label();
  
	private ArrayList<String> currencies = new ArrayList<String>();
  
	private CurrencyInfoServiceAsync currencyInfoService = GWT.create(CurrencyInfoService.class);

  
	public void onModuleLoad() {
		// add Currencies Table headers
		currenciesFlexTable.setText(0, 0, "Symbol");
		currenciesFlexTable.setText(0, 1, "Price(HKD)");
		currenciesFlexTable.setText(0, 2, "% change over past 1 month");
		currenciesFlexTable.setText(0, 3, "Remove");
		
		// format Currencies Table 
		currenciesFlexTable.getRowFormatter().addStyleName(0, "currencyListHeader");
		currenciesFlexTable.addStyleName("currencyList");
		currenciesFlexTable.getCellFormatter().addStyleName(0, 1, "currencyListNumericColumn");
		currenciesFlexTable.getCellFormatter().addStyleName(0, 2, "currencyListNumericColumn");
		currenciesFlexTable.getCellFormatter().addStyleName(0, 3, "currencyListRemoveColumn");
		currenciesFlexTable.setCellPadding(6);

		// assemble add panel
		addPanel.add(addCurrencySymbolTextBox);
		addPanel.add(addCurrencyButton);
		addPanel.add(inputErrorMsgLabel);
		addPanel.addStyleName("addPanel");
  
		// assemble refresh panel
		lastUpdatedLabel.addStyleName("lastUpdatedLabel");
		refreshPanel.add(refreshCurrencyButton);
		refreshPanel.add(lastUpdatedLabel);
		
		//set error labels style and hide them
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);
		
		inputErrorMsgLabel.setStyleName("inputErrorMsgLabel");
		inputErrorMsgLabel.setVisible(false);
	  
		// Assemble main panel
		mainPanel.add(errorMsgLabel);
		mainPanel.add(currenciesFlexTable);
		mainPanel.add(addPanel);
		mainPanel.add(refreshPanel);
	  
		RootPanel.get("currencyList").add(mainPanel);
	  
		// set focus to addCurrencySymbolTextBox
		addCurrencySymbolTextBox.setFocus(true);
	  
		// click event listener for addCurrencyButton 
		addCurrencyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addCurrency();
			}
		});
	  
		// key down event listener for addCurrencySymbolTextBox
		addCurrencySymbolTextBox.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					addCurrency();
				}
			}
		});
		
		// click event listener for refreshCurrencyButton 
		refreshCurrencyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// clear input and hide error msg
				inputErrorMsgLabel.setVisible(false);
				addCurrencySymbolTextBox.setText("");
				
				// refresh list
				refreshCurrencyList();
			}
		});
  }
  
	
	private void addCurrency() {
		// get currency symbol in upper case
		final String symbol = addCurrencySymbolTextBox.getText().toUpperCase().trim();
		addCurrencySymbolTextBox.setFocus(true);

		// Currency symbol must be 3 chars that are letters A-Z.
		if (!symbol.matches("^[A-Z]{3}$")) {
			inputErrorMsgLabel.setText("'" + symbol + "' is not a valid symbol!");
			inputErrorMsgLabel.setVisible(true);
			addCurrencySymbolTextBox.selectAll();
			return;
		} else {
			inputErrorMsgLabel.setVisible(false);
		}

		// clear addCurrencySymbolTextBox
		addCurrencySymbolTextBox.setText("");

		// Don't add a currency thats on the table.
		if (currencies.contains(symbol)) {
			inputErrorMsgLabel.setText("Currency '" + symbol + "' is already on the table!");
			inputErrorMsgLabel.setVisible(true);
			return;
		}
      
		// add currency to table
		int row = currenciesFlexTable.getRowCount();
		currencies.add(symbol);
		currenciesFlexTable.setText(row, 0, symbol);
		currenciesFlexTable.setWidget(row, 2, new Label());
		currenciesFlexTable.getCellFormatter().addStyleName(row, 1, "currencyListNumericColumn");
		currenciesFlexTable.getCellFormatter().addStyleName(row, 2, "currencyListNumericColumn");
		currenciesFlexTable.getCellFormatter().addStyleName(row, 3, "currencyListRemoveColumn");
      
		// add remove currency button
		Button removeCurrencyButton = new Button("X");
		removeCurrencyButton.addStyleDependentName("remove");
		removeCurrencyButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int removedIndex = currencies.indexOf(symbol);
				currencies.remove(removedIndex);
				currenciesFlexTable.removeRow(removedIndex + 1);
			}
		});
		currenciesFlexTable.setWidget(row, 3, removeCurrencyButton);
		
		// refresh currency list and get new currency info
		refreshCurrencyList();
	}
  
	private void refreshCurrencyList() {
		// initialize currencyInfoService
		if (currencyInfoService == null) {
			currencyInfoService = GWT.create(CurrencyInfoService.class);
	    }

		// set async callback
	    AsyncCallback<CurrencyInfo[]> callback = new AsyncCallback<CurrencyInfo[]>() {
	      public void onFailure(Throwable caught) {
	    	    String details = caught.getMessage();
	    	    if (caught instanceof InvalidCurrencyException) {
	    	      details = "Currency '" + ((InvalidCurrencyException)caught).getSymbol() + "' is invalid!";
	    	    } else if(caught instanceof RemoteCurrencyException) {
	    	    	details = "Error in fetching reomte currency data: " + ((RemoteCurrencyException)caught).getMsg();
	    	    }

	    	    errorMsgLabel.setText("Error: " + details);
	    	    errorMsgLabel.setVisible(true);
	      }

	      public void onSuccess(CurrencyInfo[] result) {
	        updateCurrencyTable(result);
	      }
	    };

	    // call currencyInfoService
	    currencyInfoService.getCurrencyInfo(currencies.toArray(new String[0]), callback);
	}
 
	private void updateCurrencyTable(CurrencyInfo[] currencyInfos) {
		// update currency table per currency
		for (int i = 0; i < currencyInfos.length; i++) {
			updateCurrencyTable(currencyInfos[i]);
		}
		
		// update timestamp of last refresh
		DateTimeFormat dateFormat = DateTimeFormat.getFormat(
		DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);
		lastUpdatedLabel.setText("Last update : " + dateFormat.format(new Date()));
	  
		// hide error message
		errorMsgLabel.setVisible(false);
  }
  
	private void updateCurrencyTable(CurrencyInfo currencyInfo) {
		// check if currency in currencies list
		if (!currencies.contains(currencyInfo.getSymbol())) {
			return;
		}

		int row = currencies.indexOf(currencyInfo.getSymbol()) + 1;

		// format PriceHkd and percentageChange1Month.
		String priceHkdText = NumberFormat.getFormat("#,##0.0000").format(currencyInfo.getPriceHkd());
		NumberFormat percentageChangeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String percentageChange1MonthText = percentageChangeFormat.format(currencyInfo.getPercentageChange1Month());

		// populate the PriceHkd and percentageChange1Month with new data.
		currenciesFlexTable.setText(row, 1, priceHkdText);
		Label percentageChange1MonthWidget = (Label)currenciesFlexTable.getWidget(row, 2);
		percentageChange1MonthWidget.setText(percentageChange1MonthText + "%");
   
		// change text color style in percentageChange1Month based on its value.
		String changeStyleName = "noChange";
		if (currencyInfo.getPercentageChange1Month() < -0.1f) {
			changeStyleName = "negativeChange";
		}
		else if (currencyInfo.getPercentageChange1Month() > 0.1f) {
			changeStyleName = "positiveChange";
		}
		percentageChange1MonthWidget.setStyleName(changeStyleName);
  }
}