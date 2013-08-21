//
//  MPLPlugin.h
//  Paypal Plugin for PhoneGap
//
//  Created by shazron on 10-10-08.
//  Copyright 2010 Shazron Abdullah. All rights reserved.

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "PayPal.h"

#define APP_ID_ARG_INDEX    0
#define APP_ENV_ARG_INDEX   1

#define PAYMENT_TYPE_ARG_INDEX    0
#define LANG_ARG_INDEX            1

#define PAYMENT_INFO_ARG_INDEX    0

typedef enum PaymentStatuses {
    PAYMENTSTATUS_SUCCESS,
    PAYMENTSTATUS_FAILED,
    PAYMENTSTATUS_CANCELED,
} PaymentStatus;

@interface CDVPayPalMPL : CDVPlugin<PayPalPaymentDelegate> {
	UIButton* ppButton;
    PayPalPaymentType pType;
    PayPalPayment*  ppPayment;
    PaymentStatus pStatus;
    NSString * payCallbackId;
}

@property (nonatomic, retain) UIButton* ppButton;
@property (nonatomic, retain) PayPalPayment*  ppPayment;
@property (nonatomic, assign) PayPalPaymentType pType;
@property (nonatomic, assign) PaymentStatus pStatus;
@property (nonatomic, retain) NSString * payCallbackId;

- (void) initWithAppID:(CDVInvokedUrlCommand *)command;
- (void) getStatus:(CDVInvokedUrlCommand *)command;

- (void) prepare:(CDVInvokedUrlCommand *)command;
- (void) setPaymentInfo:(CDVInvokedUrlCommand *)command;
- (void) pay:(CDVInvokedUrlCommand *)command;

- (void) checkout;

@end
