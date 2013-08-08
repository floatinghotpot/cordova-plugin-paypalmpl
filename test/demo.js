/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
        

		if (window.AdMob) {
			var adIdiOS = 'a151e6d43c5a28f';
			var adIdAndroid = 'a151e6d65b12438';
			var adId = (navigator.userAgent.indexOf('Android') >= 0) ? adIdAndroid
					: adIdiOS;

			var am = window.AdMob;
			am.createBannerView({
				'publisherId' : adId,
				'adSize' : am.AD_SIZE.BANNER,
				'positionAtTop' : true
			}, function() {
				am.requestAd({
					'isTesting' : true
				}, function() {

				}, function() {
					alert('Error requesting Ad');
				});
			}, function() {
				alert('Error create Ad Banner');
			});
		} else {
			alert('AdMob plugin not loaded.');
		}
        
        if( window.PayPalMPL ) {
        	var ppm = window.PayPalMPL;

    		ppm.construct( {
    			'appId': 'APP-80W284485P519543T',
    			'appEnv': ppm.PaymentEnv.ENV_SANDBOX,
    		}, function(){
    			ppm.prepare( ppm.PaymentType.GOODS, function(){}, function(){} );
    			
    		}, function(){
    			alert( 'paypal init failed' );
    		});        	
        } else {
        	alert( 'PayPalMPL plugin not loaded.' );
        }
    }
};

function buyGold( n ) {
	if( ! window.PayPalMPL ) return;
	var ppm = window.PayPalMPL;
	

	ppm.setPaymentInfo({
		'paymentCurrency' : 'USD',
		'subTotal' : 1.99,
		'recipient' : 'rnjsoft.mobile@gmail.com',
		'description' : 'game coins (' + n + ')',
		'merchantName' : 'rnjsoft'
	}, function() {
		ppm.pay({}, function() {
			// alert( 'paypal pay done' );
		}, function() {
			alert('paypal pay failed');
		});
	}, function() {
		alert('paypal setPaymentInfo failed');
	});
}

document.addEventListener('PaypalPaymentEvent.Success',function(){
                          alert('game coins purchased, enjoy gaming.');
                          });

document.addEventListener('PaypalPaymentEvent.Failed',function(){
                          alert('sorry that payment failed, please try again later.');
                          });
