package com.rjfun.cordova.plugin;

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
		PayPalMPL.thisPlugin.onPaymentSucceeded(payKey, paymentStatus);;		
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
		PayPalMPL.thisPlugin.onPaymentFailed(paymentStatus, correlationID, payKey, errorID, errorMessage);
	}

	/**
	 * Notification that the payment was canceled.
	 * 
	 * @param paymentStatus		the status of the transaction
	 */
	public void onPaymentCanceled(String paymentStatus) {
		PayPalMPL.thisPlugin.onPaymentCanceled(paymentStatus);
	}
}
