//
//  CDVPayPalMPL.m
//  Paypal Plugin for PhoneGap
//
//  Created by shazron on 10-10-08.
//  Copyright 2010 Shazron Abdullah. All rights reserved.

#import "CDVPayPalMPL.h"

#import "PayPal.h"
#import "PayPalPayment.h"
#import "PayPalInvoiceItem.h"
#import "PayPalAddress.h" // use for dynamic amount calculation
#import "PayPalAmounts.h" // use for dynamic amount calculation

@implementation CDVPayPalMPL

@synthesize ppButton, ppPayment, pType, pStatus, payCallbackId;

#define NO_APP_ID	@"dummy"

/* Get one from Paypal at developer.paypal.com */
#define PAYPAL_APP_ID	@"APP-80W284485P519543T"

/* valid values are ENV_SANDBOX, ENV_NONE (offline) and ENV_LIVE */
#define PAYPAL_APP_ENV	ENV_SANDBOX


-(CDVPlugin*) initWithWebView:(UIWebView*)theWebView
{
    self = (CDVPayPalMPL*)[super initWithWebView:(UIWebView*)theWebView];
    if (self) {
		//if ([PAYPAL_APP_ID isEqualToString:NO_APP_ID]) {
		//	NSLog(@"WARNING: You are using a dummy PayPal App ID.");
		//}
		//if (PAYPAL_APP_ENV == ENV_NONE) {
		//	NSLog(@"WARNING: You are using the offline PayPal ENV_NONE environment.");
		//}
		
		//[PayPal initializeWithAppID:PAYPAL_APP_ID forEnvironment:PAYPAL_APP_ENV];
        //NSLog( @"PayPalMPL init: buildVersion = %@", [PayPal buildVersion] );
    }
    return self;
}

- (void) construct:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult;
    NSString *callbackId = command.callbackId;
    NSArray* arguments = command.arguments;

    NSDictionary * args = nil;
    if ([arguments objectAtIndex:PAYMENT_INFO_ARG_INDEX]) {
        args = [NSDictionary dictionaryWithDictionary:[arguments objectAtIndex:PAYMENT_INFO_ARG_INDEX]];
    }
    
    NSString* appId = [args valueForKey:@"appId"];
    if(! appId) appId = PAYPAL_APP_ID;
        
    NSString* appEnv = [args valueForKey:@"appEnv"];
    if(! appEnv) appEnv = @"0";
    NSInteger nEnv = [appEnv intValue];
    switch( nEnv ) {
        case ENV_LIVE:
        case ENV_SANDBOX:
        case ENV_NONE:
            break;
        default:
            nEnv = ENV_LIVE;
    }
    
    [PayPal initializeWithAppID:appId forEnvironment:nEnv];
    NSLog( @"PayPalMPL init: buildVersion = %@, appId:%@, appEnv:%d", [PayPal buildVersion], appId, nEnv );
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) getStatus:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult;
    NSString *callbackId = command.callbackId;

    NSString* msg = nil;
    switch( [PayPal initializationStatus] ) {
        case STATUS_NOT_STARTED:
            msg = [NSString stringWithFormat:@"%d: PayPal init not started", STATUS_NOT_STARTED];
            break;
        case STATUS_COMPLETED_SUCCESS:
            msg = [NSString stringWithFormat:@"%d: PayPal init okay", STATUS_COMPLETED_SUCCESS];
            break;
        case STATUS_COMPLETED_ERROR:
            msg = [NSString stringWithFormat:@"%d: PayPal init error", STATUS_COMPLETED_ERROR];
            break;
        case STATUS_INPROGRESS:
            msg = [NSString stringWithFormat:@"%d: PayPal init in progress", STATUS_INPROGRESS];
            break;
    }
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:msg];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) prepare:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult;
    NSString *callbackId = command.callbackId;
    NSArray* arguments = command.arguments;
    
/*
    switch( [PayPal initializationStatus] ) {
        case STATUS_COMPLETED_SUCCESS:
            NSLog(@"PayPal initialized");
            break;
        case STATUS_COMPLETED_ERROR:
            NSLog(@"PayPal init failed");
            break;
        case STATUS_INPROGRESS:
            NSLog(@"PayPal init in progress");
            break;
        case STATUS_NOT_STARTED:
            NSLog(@"PayPal init not started");
            break;
    }
*/
	int argc = [arguments count];
	if (argc < 1) {
		NSLog(@"PayPalMPL.prepare - missing first argument for paymentType (integer).");
		return;
	}
	
	NSInteger paymentType = [[arguments objectAtIndex:PAYMENT_TYPE_ARG_INDEX] intValue];

	if (self.ppButton != nil) {
		[self.ppButton removeFromSuperview];
		self.ppButton = nil;
	}
	
    self.ppButton = [ [PayPal getPayPalInst] getPayButtonWithTarget:self
                                                      andAction:@selector(checkout)
                                                      andButtonType:BUTTON_152x33
                                                      andButtonText:BUTTON_TEXT_PAY ];
    if(self.ppButton == nil) {
        NSLog(@"PayPalMPL.prepare - ppButton = nil, failed calling getPayButtonWithTarget?");
    }
    
	[super.webView addSubview:self.ppButton];
	self.ppButton.hidden = YES;

	NSLog(@"PayPalMPL.prepare - set paymentType: %d", paymentType);
    
    PayPal* pp = [PayPal getPayPalInst];
    pp.shippingEnabled = FALSE;
    pp.dynamicAmountUpdateEnabled = NO;
    pp.feePayer = FEEPAYER_EACHRECEIVER;

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) setPaymentInfo:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *pluginResult;
    NSString *callbackId = command.callbackId;
    NSArray* arguments = command.arguments;
    
    NSLog(@"PayPalMPL.setPaymentInfo - called");

    self.ppPayment = nil;
    self.ppPayment = [[PayPalPayment alloc] init];

    NSDictionary * payinfo = nil;
    if ([arguments objectAtIndex:PAYMENT_INFO_ARG_INDEX]) {
        payinfo = [NSDictionary dictionaryWithDictionary:[arguments objectAtIndex:PAYMENT_INFO_ARG_INDEX]];
    }
    
    self.ppPayment.paymentType = self.pType;
    self.ppPayment.paymentCurrency = [payinfo valueForKey:@"paymentCurrency"];
    self.ppPayment.recipient = [payinfo valueForKey:@"recipient"];
    self.ppPayment.description = [payinfo valueForKey:@"description"];
    self.ppPayment.merchantName = [payinfo valueForKey:@"merchantName"];
    NSString* amount = [payinfo valueForKey:@"subTotal"];
    
    if(! self.ppPayment.paymentCurrency) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                         messageAsString:@"CDVPayPalMPL: paymentCurrency missing"];
    } else if(! amount) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                         messageAsString:@"CDVPayPalMPL: subTotal missing"];
    } else if(! self.ppPayment.recipient ) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                         messageAsString:@"CDVPayPalMPL: recipient missing"];
    } else {
        NSDecimalNumberHandler *roundPlain = [NSDecimalNumberHandler
                                           decimalNumberHandlerWithRoundingMode:NSRoundPlain
                                           scale:2
                                           raiseOnExactness:NO
                                           raiseOnOverflow:NO
                                           raiseOnUnderflow:NO
                                           raiseOnDivideByZero:YES];
        self.ppPayment.subTotal = [[[NSDecimalNumber alloc] initWithFloat:[amount floatValue] ] decimalNumberByRoundingAccordingToBehavior:roundPlain ];
        
/*      self.ppPayment.invoiceData = [[PayPalInvoiceData alloc] init];
        self.ppPayment.invoiceData.totalTax = [NSDecimalNumber decimalNumberWithString:[NSString stringWithFormat:@"%.2f",0.00]];
        self.ppPayment.invoiceData.invoiceItems = [NSMutableArray array];
        
        PayPalInvoiceItem *item = [[PayPalInvoiceItem alloc] init];
        item.totalPrice = self.ppPayment.subTotal;
        item.name = self.ppPayment.description;
        item.itemId = [payinfo valueForKey:@"itemId"];
        if(! item.itemId) item.itemId = @"10001";
        [self.ppPayment.invoiceData.invoiceItems addObject:item];
*/        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) pay:(CDVInvokedUrlCommand *)command
{
    NSString *callbackId = command.callbackId;

    NSLog(@"PayPalMPL.pay - called");
    
	if (self.ppButton != nil) {
		//[self.ppButton sendActionsForControlEvents:UIControlEventTouchUpInside];
        payCallbackId = command.callbackId;
        [self checkout];
	} else {
		NSLog( @"PayPalMPL.pay - call prepare(paymentType) first" );
        
        NSString * msg = @"PayPalMPL.pay - call prepare(paymentType) first";
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString:msg];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
	}
}

- (void) checkout
{
    NSLog(@"PayPalMPL.checkout - triggered");

	if (self.ppPayment) {
		NSLog(@"PayPalMPL.payWithPaypal - payment sent. currency:%@ amount:%@ desc:%@ recipient:%@ merchantName:%@",
			  self.ppPayment.paymentCurrency, self.ppPayment.subTotal, self.ppPayment.description,
			  self.ppPayment.recipient, self.ppPayment.merchantName);

		[[PayPal getPayPalInst] checkoutWithPayment:self.ppPayment];
        
	} else {
		NSLog(@"PayPalMPL.payWithPaypal - no payment info. call setPaymentInfo first");
        
        NSString * msg = @"PayPalMPL.payWithPaypal - no payment info. call setPaymentInfo first";
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString:msg];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:payCallbackId];	}
}

#pragma mark -
#pragma mark Paypal delegates

- (void)paymentSuccessWithKey:(NSString *)payKey andStatus:(PayPalPaymentStatus)paymentStatus 
{
	NSString* jsString = 
	@"(function() {"
	"var e = document.createEvent('Events');"
	"e.initEvent('PaypalPaymentEvent.Success');"
	"e.payKey = '%@';"
    "e.paymentStatus = %d;"
	"document.dispatchEvent(e);"
	"})();";
	
	[super writeJavascript:[NSString stringWithFormat:jsString, payKey, paymentStatus]];
	
	NSLog(@"PayPalMPL.paymentSuccess - payKey:%@", payKey);
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:payCallbackId];
    
    pStatus = PAYMENTSTATUS_SUCCESS;
}

- (void) paymentFailedWithCorrelationID:(NSString *)correlationID
{
	NSString* jsString =
	@"(function() {"
	"var e = document.createEvent('Events');"
	"e.initEvent('PaypalPaymentEvent.Failed');"
	"e.correlationID = '%@';"
	"document.dispatchEvent(e);"
	"})();";
	
	[super writeJavascript:[NSString stringWithFormat:jsString, correlationID]];	

	NSLog(@"PayPalMPL.paymentFailed - correlationID:%@", correlationID);

    NSString *severity = [[PayPal getPayPalInst].responseMessage objectForKey:@"severity"];
    NSLog(@"severity: %@", severity);
    NSString *category = [[PayPal getPayPalInst].responseMessage objectForKey:@"category"];
    NSLog(@"category: %@", category);
    NSString *errorId = [[PayPal getPayPalInst].responseMessage objectForKey:@"errorId"];
    NSLog(@"errorId: %@", errorId);
    NSString *message = [[PayPal getPayPalInst].responseMessage objectForKey:@"message"];
    NSLog(@"message: %@", message);
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:payCallbackId];
    
    pStatus = PAYMENTSTATUS_FAILED;
}

- (void) paymentCanceled
{
	NSString* jsString =
	@"(function() {"
	"var e = document.createEvent('Events');"
	"e.initEvent('PaypalPaymentEvent.Canceled');"
	"document.dispatchEvent(e);"
	"})();";
	
	[super writeJavascript:jsString];
    
    NSLog( @"PayPalMPL.paymentCanceled" );
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:payCallbackId];
    
    pStatus = PAYMENTSTATUS_CANCELED;
}

- (void)paymentLibraryExit
{
    
}

@end
