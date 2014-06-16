/* PayPal PhoneGap Plugin - Map JavaScript API calls to mpl library
 *
 * Copyright (C) 2011, Appception, Inc.. All Rights Reserved.
 * Copyright (C) 2011, Mobile Developer Solutions All Rights Reserved.
 */

package com.rjfun.cordova.plugin;

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
	private static final String ACTION_INIT_WITH_APP_ID = "initWithAppID";
	private static final String ACTION_GET_STATUS = "getStatus";
	private static final String ACTION_SET_PAYMENT_INFO = "setPaymentInfo";
	private static final String ACTION_PAY = "pay";
	
	private static final int REQUEST_PAYPAL_CHECKOUT = 2;
	private static final int PAYPAL_BUTTON_ID = 10001;
	
	private CallbackContext payCallback = null;
	
	@Override
	public boolean execute(String action, final JSONArray inputs, final CallbackContext callbackContext) throws JSONException {
		Log.d(LOGTAG, "Plugin Called: " + action);
		
		if (ACTION_INIT_WITH_APP_ID.equals(action)) {
			thisPlugin = this;
			
			cordova.getThreadPool().execute(new Runnable() {
	            public void run() {
	            	executeInitWithAppID(inputs, callbackContext);
	            }
	        });
			return true;
			
		} else if (ACTION_GET_STATUS.equals(action)) {
			executeGetStatus(inputs, callbackContext);
			
		} else if (ACTION_SET_PAYMENT_INFO.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {
	            	executeSetPaymentInfo(inputs, callbackContext);
	            }
			});
			return true;
		} else if (ACTION_PAY.equals(action)) {
			cordova.getActivity().runOnUiThread(new Runnable() {
	            public void run() {
	    			executePay(inputs, callbackContext );
	            }
	        });
			return true;
			
		}

		return false;
	}

	private boolean executeInitWithAppID(JSONArray inputs, CallbackContext callbackContext) {
		JSONObject args;
		
		// Get the input data.
		String strEnv = "ENV_NONE";
		try {
			args = inputs.getJSONObject(0);
			this.appId = args.getString("appId");
			strEnv = args.getString("appEnv");
		} catch (JSONException exception) {
			Log.w(LOGTAG, String.format("Got JSON Exception: %s", exception.getMessage()));
			callbackContext.sendPluginResult( new PluginResult(Status.JSON_EXCEPTION) );
			return true;
		}

		if( strEnv.equals( "ENV_LIVE" ) ) {
			this.appEnv = PayPal.ENV_LIVE;
		} else if ( strEnv.equals( "ENV_SANDBOX" ) ) {
			this.appEnv = PayPal.ENV_SANDBOX;
		} else {
			this.appEnv = PayPal.ENV_NONE;
		}

		Log.d(LOGTAG, "init paypal for " + this.appId + " with " + strEnv);

		PayPal.initWithAppID(cordova.getActivity(), this.appId, this.appEnv);
		callbackContext.sendPluginResult( new PluginResult(Status.OK) );
		
		return true;
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

	private boolean executeGetStatus(JSONArray inputs, CallbackContext callbackContext) {
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
		callbackContext.sendPluginResult( new PluginResult(Status.OK, json) );
		return true;
	}
	
	private boolean executeSetPaymentInfo(JSONArray inputs, CallbackContext callbackContext) {
		JSONObject args = null;
		
		String strType = "TYPE_GOODS";
		String strLang = "en_US";
		int nButton = PayPal.BUTTON_152x33;
		boolean bHideButton = false;
		this.ppPayment = new PayPalPayment();
		this.ppPayment.setPaymentType( this.pType );
		try {
			args = inputs.getJSONObject(0);
			
			strLang = args.getString("lang");
			strType = args.getString("paymentType");
			nButton = args.getInt("showPayPalButton");
			if( nButton < PayPal.BUTTON_152x33 || nButton > PayPal.BUTTON_294x45 ) {
				nButton = PayPal.BUTTON_152x33;
				bHideButton = true;
			}
			this.ppPayment.setCurrencyType(args.getString("paymentCurrency"));
			this.ppPayment.setRecipient(args.getString("recipient"));
		    this.ppPayment.setDescription(args.getString("description"));
		    this.ppPayment.setMerchantName(args.getString("merchantName"));
			BigDecimal amount = new BigDecimal(args.getString("subTotal"));
			amount.round(new MathContext(2, RoundingMode.HALF_UP));
			this.ppPayment.setSubtotal(amount);
			
		} catch (JSONException e) {
			Log.d(LOGTAG, "Got JSON Exception "+ e.getMessage());
			callbackContext.sendPluginResult( new PluginResult(Status.JSON_EXCEPTION) );
			return true;
		}
		
		if( strType.equals("TYPE_GOODS") ) {
			this.pType = PayPal.PAYMENT_TYPE_GOODS;
		} else if( strType.equals("TYPE_SERVICE") ) {
			this.pType = PayPal.PAYMENT_TYPE_SERVICE;
		} if( strType.equals("TYPE_PERSONAL") ) {
			this.pType = PayPal.PAYMENT_TYPE_PERSONAL;
		} else {
			this.pType = PayPal.PAYMENT_TYPE_NONE;
		}
		
		PayPal pp = PayPal.getInstance();
		pp.setLanguage( strLang );
		pp.setShippingEnabled(false);
		pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
		pp.setDynamicAmountCalculationEnabled(false);
		
		if( this.ppButton != null ) {
			webView.removeView( this.ppButton );
			this.ppButton = null;
		}
		
		// Back in the UI thread -- show the "Pay with PayPal" button
		// Generate the PayPal Checkout button and save it for later use
		this.ppButton = pp.getCheckoutButton(this.cordova.getActivity(), nButton,
				CheckoutButton.TEXT_PAY);
		
		// You'll need to have an OnClickListener for the CheckoutButton.
		this.ppButton.setOnClickListener(this);
		this.ppButton.setId(PAYPAL_BUTTON_ID);		
		webView.addView( this.ppButton );
		this.ppButton.setVisibility( bHideButton ? View.INVISIBLE : View.VISIBLE );		

		callbackContext.sendPluginResult( new PluginResult(Status.OK) );
		return true;
	}
	
	private boolean executePay(JSONArray inputs, CallbackContext callbackContext) {
		PluginResult result = null;
		
		if( this.ppButton != null ) {
			this.payCallback = callbackContext;
			checkout();
		} else {
			Log.d(LOGTAG, "PayPalMPL.pay - call setPaymentInfo first" );
			callbackContext.sendPluginResult( new PluginResult(Status.ERROR, "call setPaymentInfo") );
		}
		
		return true;
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
			
			Intent checkoutIntent = pp.checkout(
					this.ppPayment, 
					cordova.getActivity().getApplicationContext(), 
					new PayPalMPLResultDelegate() );
			
			cordova.getActivity().startActivityForResult(checkoutIntent,
					REQUEST_PAYPAL_CHECKOUT);
		} else {
			Log.d(LOGTAG, "payment info not set, call setPaymentInfo first.");
		}
	}
	
	public void onPaymentSucceeded(final String payKey, final String paymentStatus) {
		Log.i(LOGTAG, "onPaymentSucceeded");
		
		cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {

            	PayPalMPL.thisPlugin.webView.loadUrl("javascript:" +
					"(function() {" +
					"var e = document.createEvent('Events');" +
					"e.initEvent('PaypalPaymentEvent.Success');" +
					"e.payKey = '"+ payKey + "';" +
					"e.paymentStatus = '"+ paymentStatus + "';" +
					"document.dispatchEvent(e);" +
					"})();");
            }
		});
		this.payCallback.sendPluginResult( new PluginResult(Status.OK, paymentStatus) );
	}	

	public void onPaymentFailed(final String paymentStatus, final String correlationID,
			final String payKey, final String errorID, final String errorMessage) {

		Log.i(LOGTAG, "onPaymentFailed");
		
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				PayPalMPL.thisPlugin.webView.loadUrl( "javascript:" + "(function() {"
						+ "var e = document.createEvent('Events');"
						+ "e.initEvent('PaypalPaymentEvent.Failed');"
						+ "e.payKey = '" + payKey + "';"
						+ "e.paymentStatus = '" + paymentStatus + "';"
						+ "e.correlationID = '" + correlationID + "';"
						+ "e.errorID = '" + errorID + "';"
						+ "e.errorMessage = '" + errorMessage + "';"
						+ "document.dispatchEvent(e);" + "})();" );
			}
		});
		this.payCallback.sendPluginResult(new PluginResult(Status.ERROR, errorMessage));
	}

	public void onPaymentCanceled(final String paymentStatus) {
		Log.i(LOGTAG, "onPaymentCanceled");
		
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				PayPalMPL.thisPlugin.webView.loadUrl( "javascript:" + "(function() {" +
				"var e = document.createEvent('Events');" +
				"e.initEvent('PaypalPaymentEvent.Canceled');" +
				"e.paymentStatus = '"+ paymentStatus + "';" +
				"document.dispatchEvent(e);" +
				"})();");	
			}
		});
		
		this.payCallback.sendPluginResult(new PluginResult(Status.ERROR, paymentStatus));
	}
}
