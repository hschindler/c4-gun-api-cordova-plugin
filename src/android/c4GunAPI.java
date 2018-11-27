
package de.schindlergmbh.cordova.c4.c4_gun_api;

import java.util.TimeZone;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings;

import android.nfc.Tag;
import android.view.KeyEvent;
import android.util.Log;

import com.hhw.uhfm.R;
import com.pda.uhfm.EPCDataModel;
import com.pda.uhfm.TagDataModel;
import com.pda.uhfm.UHFManager;

import cn.pda.serialport.Tools;

public class C4GunApi extends CordovaPlugin {
    public static final String TAG = "C4GunApi";

    // public static String platform; // Device OS
    // public static String uuid; // Device UUID

    private UhfManager _uhfManager;

    private String _errorLog;

    /**
     * Constructor.
     */
    public C4GunApi() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like get
     * file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing C4GunApi");

        _errorLog = "";

        this._uhfManager = null;
        UhfManager.Port = _uhfPort;

        try {
            _uhfManager = UhfManager.getInstance();
        } catch (Exception e) {
            _errorLog = e.getMessage();
            // e.printStackTrace();
            // Log.d(TAG, "Error: " + e.getMessage());
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into
     *                        JavaScript.
     * @return True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (_errorLog.length() > 0) {
            callbackContext.error(_errorLog);
            return true;
        }

        if (this._uhfManager == null) {
            callbackContext.error("UHF API not installed");
            return true;
        }

        if ("getFirmware".equals(action)) {

            final byte[] firmwareVersion = _uhfManager.getFirmware();

            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {

                    // String test = "test 1111";
                    callbackContext.success(firmwareVersion);

                    // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                    // firmwareVersion);
                    // callbackContext.sendPluginResult(pluginResult);
                }

            });

            return true;
        } else {
            return false;
        }
        return true;
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    /**
     * Get the OS name.
     *
     * @return
     */
    public String getPlatform() {
        String platform;
        if (isAmazonDevice()) {
            platform = AMAZON_PLATFORM;
        } else {
            platform = ANDROID_PLATFORM;
        }
        return platform;
    }

    /**
     * Get the device's Universally Unique Identifier (UUID).
     *
     * @return
     */
    public String getUuid() {
        String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        return uuid;
    }

    public String getModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    public String getProductName() {
        String productname = android.os.Build.PRODUCT;
        return productname;
    }

    public String getManufacturer() {
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer;
    }

    public String getSerialNumber() {
        String serial = android.os.Build.SERIAL;
        return serial;
    }

    /**
     * Get the OS version.
     *
     * @return
     */
    public String getOSVersion() {
        String osversion = android.os.Build.VERSION.RELEASE;
        return osversion;
    }

    public String getSDKVersion() {
        @SuppressWarnings("deprecation")
        String sdkversion = android.os.Build.VERSION.SDK;
        return sdkversion;
    }

    public String getTimeZoneID() {
        TimeZone tz = TimeZone.getDefault();
        return (tz.getID());
    }

    /**
     * Function to check if the device is manufactured by Amazon
     *
     * @return
     */
    public boolean isAmazonDevice() {
        if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
            return true;
        }
        return false;
    }

    public boolean isVirtual() {
        return android.os.Build.FINGERPRINT.contains("generic") || android.os.Build.PRODUCT.contains("sdk");
    }

}
