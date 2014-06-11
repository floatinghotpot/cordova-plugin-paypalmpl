## PayPalMPL Plugin ##
---------------------------
Can support PayPal in cordova/phonegap app? Yes, you can!
 
This is the PayPal Cordova Plugin for Android and iOS. It provides a way to request PayPal natively from JavaScript. 
This plugin was updated for Cordova 3.0.0, and tested with the PayPal MPL iOS 2.0.1/Android 1.5.5.45 SDK.

It's for PayPal classic library MPL (Mobile Payment Library), not for new PayPal SDK. As it's stated by PayPal, new SDK is only available for US currently, not for other country yet. The SDK can be downloaded from PayPal website, or just clone to get some common used SDK: 

https://github.com/floatinghotpot/mobile-sdk.git

## How to use? ##
---------------------------
To install this plugin, follow the [Command-line Interface Guide](http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface).

    cordova plugin add https://github.com/floatinghotpot/cordova-plugin-paypalmpl.git

Check the README.md in sub folder for details.

## How to use it in javascript ##
---------------------------------

        function onDeviceReady() {
        	
            document.addEventListener('PaypalPaymentEvent.Success',function(){
            	alert('game coins purchased, enjoy gaming.');
            });

            document.addEventListener('PaypalPaymentEvent.Failed',function(){
            	alert('sorry that payment failed, please try again later.');
            });
            
            if( window.plugins && window.plugins.PayPalMPL ) {
		var ppm = window.plugins.PayPalMPL;

            	var isTesting = false;
            	var appID = isTesting ? 'APP-80W284485P519543T' : 'APP-0HN45655HA567492N';
                var appEnv = isTesting ? ppm.PaymentEnv.ENV_SANDBOX : ppm.PaymentEnv.ENV_LIVE;
				
                ppm.initWithAppID( {
            	      'appId': appID,
            	      'appEnv': appEnv,
            	      }, function(){
            	    	  window.plugins.PayPalMPL.isReady = true;
            	      }, function(){
            	      });
        	}
        }
        
        function buyGameCoin( n ) {
            if( window.plugins && window.plugins.PayPalMPL ) {
            	var ppm = window.plugins.PayPalMPL;
            	ppm.setPaymentInfo({
        			'lang' : 'en_US',
        			'paymentType' : ppm.PaymentType.TYPE_GOODS,
        			'showPayPalButton': -1,
            		'paymentCurrency' : 'USD',
            		'subTotal' : 1.99,
            		'recipient' : 'rjfun.mobile@gmail.com',
            		'description' : 'game coins (' + n + ')',
            		'merchantName' : 'rjfun'
            	}, function() {
            		ppm.pay({}, function() {
            			// alert( 'paypal pay done' );
            		}, function() {
            			alert('paypal pay failed');
            		});
            	}, function() {
            		alert('paypal setPaymentInfo failed');
            	});
            } else {
            	alert( 'PayPalMPL plugin not loaded.' );
            }
        }
        
