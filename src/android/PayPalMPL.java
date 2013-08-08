/* PayPal PhoneGap Plugin - Map JavaScript API calls to mpl library
 *
 * Copyright (C) 2011, Appception, Inc.. All Rights Reserved.
 * Copyright (C) 2011, Mobile Developer Solutions All Rights Reserved.
 */

package org.apache.cordova.plugin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalInvoiceData;
import com.paypal.android.MEP.PayPalInvoiceItem;
import com.paypal.android.MEP.PayPalPayment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class PayPalMPL extends CordovaPlugin implements OnClickListener {

	public static PayPalMPL thisPlugin;
	
	private int appEnv = PayPal.ENV_NONE;
	private String appId = "APP-80W284485P519543T";
	
	private int pType = PayPal.PAYMENT_TYPE_GOODS;
	private PayPalPayment ppPayment = null;

	private CheckoutButton ppButton = null;
	
	/** Common tag used for logging statements. */
	private static final String LOGTAG = "PayPalMPL";

	/** Cordova Actions. */
	private static final String ACTION_CONSTRUCT = "construct";
	private static final String ACTION_GET_STATUS = "getStatus";
	private static final String ACTION_PREPARE = "prepare";
	private static final String ACTION_SET_PAYMENT_INFO = "setPaymentInfo";
	private static final String ACTION_PAY = "pay";
	
	private static final int REQUEST_PAYPAL_CHECKOUT = 2;
	private static final int PAYPAL_BUTTON_ID = 10001;
	
	@Override
	public boolean execute(String action, final JSONArray inputs, final CallbackContext callbackContext) throws JSONException {
		Log.d(LOGTAG, "Plugin Called: " + action);
		
		if (ACTION_CONSTRUCT.equals(action)) {
			thisPlugin = this;
			
			cordova.getThreadPool().execute(new Runnable() {
	            public void run() {
	            	callbackContext.sendPluginResult( executeConstruct(inputs) );
	            }
	        });
			return true;
			
		} else if (ACTION_GET_STATUS.equals(action)) {
			callbackContext.sendPluginResult( executeGetStatus(inputs) );
			
		} else if (ACTION_PREPARE.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {
	            	callbackContext.sendPluginResult( executePrepare(inputs) );
	            }
	        });
			return true;
			
		} else if (ACTION_SET_PAYMENT_INFO.equals(action)) {
			callbackContext.sendPluginResult( executeSetPaymentInfo(inputs) );
			
		} else if (ACTION_PAY.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {
	    			callbackContext.sendPluginResult( executePay(inputs) );
	            }
	        });
			return true;
			
		}

		return false;
	}

	private PluginResult executeConstruct(JSONArray inputs) {
		JSONObject args;
		
		// Get the input data.
		try {
			args = inputs.getJSONObject(0);
			this.appId = args.getString("appId");
			this.appEnv = args.getInt("appEnv");
		} catch (JSONException exception) {
			Log.w(LOGTAG,
					String.format("Got JSON Exception: %s",
							exception.getMessage()));
			return new PluginResult(Status.JSON_EXCEPTION);
		}

		switch (this.appEnv) {
		case PayPal.ENV_LIVE:
		case PayPal.ENV_SANDBOX:
		case PayPal.ENV_NONE:
			break;
		default:
			this.appEnv = PayPal.ENV_LIVE;
		}

		Log.d(LOGTAG, "init paypal for " + this.appId + " with " + this.appEnv);

		PayPal.initWithAppID(cordova.getActivity(), this.appId, this.appEnv);
		return new PluginResult(Status.OK);
	}

 	public boolean isOnline() {
 		Activity act = this.cordova.getActivity();
 		ConnectivityManager cm = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	} 	

	private PluginResult executeGetStatus(JSONArray inputs) {
		String status = "0";
		PayPal pp = PayPal.getInstance();
		Log.i("mpl", "getStatus: after instance");
		if( (pp != null) && pp.isLibraryInitialized() ) {
			status = "1";
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put("str", status);
		} catch (JSONException e) {
		}
		return new PluginResult(Status.OK, json);		
	}
	
	private PluginResult executePrepare(JSONArray inputs) {
		try {
			this.pType = inputs.getInt(0);
		} catch (JSONException e) {
		}

		switch( this.pType ) {
		case PayPal.PAYMENT_TYPE_GOODS:
		case PayPal.PAYMENT_TYPE_SERVICE:
		case PayPal.PAYMENT_TYPE_PERSONAL:
		case PayPal.PAYMENT_TYPE_NONE:
			break;
		default:
			this.pType = PayPal.PAYMENT_TYPE_GOODS;
		}
		
		PayPal pp = PayPal.getInstance();
		pp.setLanguage("en_US");
		pp.setShippingEnabled(false);
		pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
		pp.setDynamicAmountCalculationEnabled(false);
		
		if( this.ppButton != null ) {
			webView.removeView( this.ppButton );
			this.ppButton = null;
		}
		
		// Back in the UI thread -- show the "Pay with PayPal" button
		// Generate the PayPal Checkout button and save it for later use
		this.ppButton = pp.getCheckoutButton(this.cordova.getActivity(), PayPal.BUTTON_278x43,
				CheckoutButton.TEXT_PAY);
		// You'll need to have an OnClickListener for the CheckoutButton.
		this.ppButton.setOnClickListener(this);
		this.ppButton.setId(PAYPAL_BUTTON_ID);		
		webView.addView( this.ppButton );
		
		return new PluginResult(Status.OK);		
	}
	
	private PluginResult executeSetPaymentInfo(JSONArray inputs) {
		JSONObject args = null;
		
		this.ppPayment = new PayPalPayment();
		this.ppPayment.setPaymentType( this.pType );
		try {
			args = inputs.getJSONObject(0);
			
			this.ppPayment.setCurrencyType(args.getString("paymentCurrency"));
			this.ppPayment.setRecipient(args.getString("recipient"));
		    this.ppPayment.setDescription(args.getString("description"));
		    this.ppPayment.setMerchantName(args.getString("merchantName"));
			BigDecimal amount = new BigDecimal(args.getString("subTotal"));
			amount.round(new MathContext(2, RoundingMode.HALF_UP));
			this.ppPayment.setSubtotal(amount);
			
		} catch (JSONException e) {
			Log.d(LOGTAG, "Got JSON Exception "+ e.getMessage());
			return new PluginResult(Status.JSON_EXCEPTION);
		}

		// PayPalInvoiceData can contain tax and shipping amounts. It also
		// contains an ArrayList of PayPalInvoiceItem which can
		// be filled out. These are not required for any transaction.
		PayPalInvoiceData invoice = new PayPalInvoiceData();
		BigDecimal tax = new BigDecimal(0.0);
		tax = tax.setScale(2, RoundingMode.HALF_UP);
		invoice.setTax(tax);

		// PayPalInvoiceItem has several parameters available to it. None of these parameters is required.
		PayPalInvoiceItem item1 = new PayPalInvoiceItem();
		// Sets the name of the item.
    	item1.setName("game coins");
    	// Sets the ID. This is any ID that you would like to have associated with the item.
    	item1.setID("1234");
    	// Sets the total price which should be (quantity * unit price). The total prices of all PayPalInvoiceItem should add up
    	// to less than or equal the subtotal of the payment.
    	item1.setTotalPrice(this.ppPayment.getSubtotal());
    	// Sets the unit price.
    	item1.setUnitPrice(this.ppPayment.getSubtotal());
    	// Sets the quantity.
    	item1.setQuantity(1);
    	// Add the PayPalInvoiceItem to the PayPalInvoiceData. Alternatively, you can create an ArrayList<PayPalInvoiceItem>
    	// and pass it to the PayPalInvoiceData function setInvoiceItems().
    	invoice.getInvoiceItems().add(item1);

		// Sets the PayPalPayment invoice data.
		this.ppPayment.setInvoiceData(invoice);
		
		return new PluginResult(Status.OK);
	}
	
	private PluginResult executePay(JSONArray inputs) {
		PluginResult result = null;
		
		if( this.ppPayment != null ) {
			checkout();
			
			result = new PluginResult(Status.OK);
		} else {
			result = new PluginResult(Status.ERROR, "No payment info, call setPaymentInfo");
		}
		
		return result;
	}

	@Override
	public void onClick(View v) {
		if (v == (CheckoutButton) webView.findViewById(PAYPAL_BUTTON_ID)) {
			Log.d(LOGTAG, "paypal button clicked.");
			
			checkout();
		}		
	}

	private void checkout() {
		if (this.ppPayment != null) {
			PayPal pp = PayPal.getInstance();
			Intent checkoutIntent = pp.checkout(this.ppPayment, 
					cordova.getActivity().getApplicationContext(),
					new PayPalMPLResultDelegate());
			cordova.getActivity().startActivityForResult(checkoutIntent,
					REQUEST_PAYPAL_CHECKOUT);
		} else {
			Log.d(LOGTAG, "payment info not set, call setPaymentInfo first.");
		}
	}


}
