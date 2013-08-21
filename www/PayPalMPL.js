
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

var argscheck = require('cordova/argscheck'), exec = require('cordova/exec');

var mplExport = {};

/*
 * Events to listen to after a user touches the payment button
 */
mplExport.PaymentEvent = {
	/**
	 * Listen for this event to signify Paypal payment success. The event object
	 * will have these properties: transactionID - a string value
	 */
	Success : 'PaypalPaymentEvent.Success',
	/**
	 * Listen for this event to signify Paypal payment canceled. The event
	 * object will have these properties: [no properties available]
	 */
	Canceled : 'PaypalPaymentEvent.Canceled',
	/**
	 * Listen for this event to signify Paypal payment failed. The event object
	 * will have these properties: errorType - an integer value
	 */
	Failed : 'PaypalPaymentEvent.Failed'
};

mplExport.PaymentEnv = {
	ENV_SANDBOX : 'ENV_SANDBOX',
	ENV_LIVE : 'ENV_LIVE',
	ENV_NONE : 'ENV_NONE'
};
/**
 * Prepare the payment.
 * 
 * @param {!Object}
 *            arg arguments to init the MPL. { 'appId': 'APP-80W284485P519543T',
 *            'appEnv': PaymentEnv.ENV_SANDBOX } The PayPal server to be used,
 *            and the the ID of your application received from PayPal.
 * @param {function()}
 *            successCallback The function to call if the banner was created
 *            successfully.
 * @param {function()}
 *            failureCallback The function to call if create banner was
 *            unsuccessful.
 */

mplExport.initWithAppID = function(arg, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 
			'PayPalMPL', 'initWithAppID', [ arg ]);
};

/**
 * get status of paypal init.
 * 
 * @param {function()}
 *            successCallback The function to call if called successfully.
 * @param {function()}
 *            failureCallback The function to call if unsuccessful.
 */
mplExport.getStatus = function(successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 
			'PayPalMPL', 'getStatus', []);
};

/**
 * This enum represents PayPal's supported payment type.  Use one of these
 * constants as the ptype when calling prepare.
 * @const
 */
mplExport.PaymentType = {
	TYPE_GOODS : 'TYPE_GOOD',
	TYPE_SERVICE : 'TYPE_SERVICE',
	TYPE_PERSONAL : 'TYPE_PERSONAL',
	TYPE_NONE : 'TYPE_NONE'
};

/**
 * setPaymentInfo.  
 * 
 * @param {!Object} arg The arguments used to set payment info.
 *
 *        {
 *        	'lang' : 'en_US',
 *        	'paymentType' : 'TYPE_GOODS',
 *        	'showPayPalButton' : -1, // 0-3 to show, -1 to hide
 *          'paymentCurrency': 'USD',
 *          'subTotal': 9.99,
 *          'recipient': 'you@gmail.com',
 *          'description': 'desc',
 *          'merchantName': 'Your Company'
 *        }
 *
 * @param {function()} successCallback The function to call if an ad was
 *        requested successfully.
 * @param {function()} failureCallback The function to call if an ad failed
 *        to be requested.
 */

mplExport.setPaymentInfo = function(arg, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 
			'PayPalMPL', 'setPaymentInfo', [ arg ]);
};

/**
 * pay.
 * 
 * @param {json} arg Json object reserved.
 * @param {function()} successCallback The function to call if an ad was
 *        requested successfully.
 * @param {function()} failureCallback The function to call if an ad failed
 *        to be requested.
 */
mplExport.pay = function(arg, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 
			'PayPalMPL', 'pay', [ arg ]);
};

module.exports = mplExport;
