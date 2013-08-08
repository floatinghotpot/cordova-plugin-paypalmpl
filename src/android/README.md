
# PhoneGap (Cordova) PayPal-Plugin #
by Shazron Abdullah
Re-written by Liming Xie

## Adding the Plugin to your project ##

Using this plugin requires Cordova and the PayPal Mobile Payments Library. The PayPal Mobile Payments Library can be downloaded [here](https://www.x.com/community/ppx/xspaces/mobile/mep).

1. Make sure your Cordova project has been updated for Cordova 3.0.0.
2. Add the PayPal_MPL.jar in "MPL" folder to your project (put in a suitable location under your project, then drag and drop it in)
3. Add the .java files to your Plugins folder in your project (as a Group "yellow folder" not a Reference "blue folder")
4. Add the .js files to your "www" folder on disk, and add reference(s) to the .js files as &lt;script&gt; tags in your html file(s)
5. Add following content to AndroidManifest.xml:
    <activity android:configChanges="keyboardHidden|orientation" android:name="com.paypal.android.MEP.PayPalActivity" android:theme="@android:style/Theme.Translucent.NoTitleBar" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
6. Make sure you check the **"RELEASE NOTES"** section below!

## RELEASE NOTES ##

### 20130729 ###
- Rewritten for Cordova 3.0.0
- Follow the Cordova plugin spec
- Recommended to use Cordova command line tool to manage the plugin
- Tested with MPL Android SDK 1.5.5.45 and Cordova 3.0.0.
- Not tested with earlier version, so compatibility is not guaranteed.


