package org.apache.cordova.plugin;

import java.io.Serializable;

import android.util.Log;

import com.paypal.android.MEP.PayPalResultDelegate;

public class PayPalMPLResultDelegate implements PayPalResultDelegate,
		Serializable {

	private static final long serialVersionUID = 10001L;
	private static final String LOGTAG = "PayPalMPL";
	
	/**
	 * Notification that the payment has been completed successfully.
	 * 
	 * @param payKey			the pay key for the payment
	 * @param paymentStatus		the status of the transaction
	 */
	public void onPaymentSucceeded(String payKey, String paymentStatus) {
		Log.i(LOGTAG, "onPaymentSucceeded");
		
		PayPalMPL.thisPlugin.webView.loadUrl("javascript:" +
				"(function() {" +
				"var e = document.createEvent('Events');" +
				"e.initEvent('PaypalPaymentEvent.Success');" +
				"e.payKey = '"+ payKey + "';" +
				"e.paymentStatus = '"+ paymentStatus + "';" +
				"document.dispatchEvent(e);" +
				"})();");		
	}

	/**
	 * Notification that the payment has failed.
	 * 
	 * @param paymentStatus		the status of the transaction
	 * @param correlationID		the correlationID for the transaction failure
	 * @param payKey			the pay key for the payment
	 * @param errorID			the ID of the error that occurred
	 * @param errorMessage		the error message for the error that occurred
	 */
	public void onPaymentFailed(String paymentStatus, String correlationID,
			String payKey, String errorID, String errorMessage) {

		Log.i(LOGTAG, "onPaymentFailed");
		
		String js = "javascript:" +
				"(function() {" +
				"var e = document.createEvent('Events');" +
				"e.initEvent('PaypalPaymentEvent.Failed');" +
				"e.payKey = '"+ payKey + "';" +
				"e.paymentStatus = '"+ paymentStatus + "';" +
				"e.correlationID = '"+ correlationID + "';" +
				"e.errorID = '"+ errorID + "';" +
				"e.errorMessage = '"+ errorMessage + "';" +
				"document.dispatchEvent(e);" +
				"})();";
		Log.d(LOGTAG, js);
		PayPalMPL.thisPlugin.webView.loadUrl( js );	
	}

	/**
	 * Notification that the payment was canceled.
	 * 
	 * @param paymentStatus		the status of the transaction
	 */
	public void onPaymentCanceled(String paymentStatus) {
		Log.i(LOGTAG, "onPaymentCanceled");
		
		PayPalMPL.thisPlugin.webView.loadUrl("javascript:" +
				"(function() {" +
				"var e = document.createEvent('Events');" +
				"e.initEvent('PaypalPaymentEvent.Canceled');" +
				"e.paymentStatus = '"+ paymentStatus + "';" +
				"document.dispatchEvent(e);" +
				"})();");	
	}

}
