package de.schindlergmbh.cordova.c4;

import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Settings;

import android.nfc.Tag;
import android.view.KeyEvent;
import android.util.Log;

// import com.hhw.uhfm.R;
// import com.pda.uhfm.EPCDataModel;
// import com.pda.uhfm.TagDataModel;
import com.pda.uhfm.UHFManager;
import com.pda.uhfm.VersionInfo;

import cn.pda.serialport.Tools;

public class C4GunApi extends CordovaPlugin {
    public static final String TAG = "C4GunApi";

    // public static final int SEARCH_REQ_CODE = 0;

    // public static final String PERM_NFC = Manifest.permission.NFC;

    // public static String platform; // Device OS
    // public static String uuid; // Device UUID

    private UHFManager _uhfManager;
    private Boolean _readerInitialized;
    private String _errorLog;

    private boolean runFlag = true;
    private boolean startFlag = false;

    private String readMode = "tid"; // tid / epc

    // private ArrayList<com.pda.uhfm.TagDataModel> _listTagDataModel;
    // private ArrayList<String> _listEPC;

    private CallbackContext _uhfCallBackContext;

    /**
     * Constructor.
     * 
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
        this._errorLog = "";
        // Log.d(TAG, "Initializing C4GunApi");

        _errorLog = "";

        this._uhfManager = null;

        try {
            InitUhfManager();

        } catch (Exception e) {
            _errorLog = e.getMessage();
            e.printStackTrace();
            // Log.d(TAG, "Error: " + e.getMessage());
        }

        Thread thread = new InventoryThread();
        thread.start();
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
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (_errorLog.length() > 0) {
            callbackContext.error("ErrorLog: " + _errorLog);
            return true;
        }

        if (this._uhfManager == null) {
            callbackContext.error("UHF API not installed");
            return true;
        }

        if (this._readerInitialized == false) {
            callbackContext.error("UHF API not initialized");
            return true;
        }

        if ("getFirmware".equals(action)) {

            // final byte[] firmwareVersion = _uhfManager.getVersion();
            final com.pda.uhfm.VersionInfo firmwareVersion = _uhfManager.getVersion();

            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {

                    // String test = "test 1111";
                    callbackContext.success(firmwareVersion.SoftwareVersion);

                    // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                    // firmwareVersion);
                    // callbackContext.sendPluginResult(pluginResult);
                }

            });

            return true;
        } else if ("startInventoryTID".equals(action)) {
            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            this._uhfCallBackContext = callbackContext;

            return this.startInventoryThread("tid");

        } else if ("startInventoryEPC".equals(action)) {
            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            this._uhfCallBackContext = callbackContext;

            return this.startInventoryThread("epc");

        } else if ("stopInventory".equals(action)) {
            startFlag = false;
            return true;
        } else if ("setReadPower".equals(action)) {
            return false;
        } else {
            return false;
        }
    }

    public void onResume() {

        super.onResume(false);

        this.InitUhfManager();

        // Barcode1DManager.BaudRate = _barcodeBaudrate;
        // Barcode1DManager.Port = _barcodePort;
        // Barcode1DManager.Power = _barcodePower;

    }

    @Override
    public void onDestroy() {
        runFlag = false;
        if (this._uhfManager != null) {
            this._uhfManager.close();
        }

        // if (_barcodeManager != null) {
        // _barcodeManager.Close();
        // }

        // closeBarcode();

        super.onDestroy();
    }

    public void onPause() {
        startFlag = false;
        this._uhfManager.close();
        super.onPause(false);
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    private void InitUhfManager() {
        this._uhfManager = UHFManager.getInstance();
        this._readerInitialized = _uhfManager.initRfid();

        this._uhfManager.setProtocol(_uhfManager.PROTOCOL_ISO_18000_6C);
        this._uhfManager.setFreBand(com.pda.uhfm.FreRegion.TMR_REGION_FCC);
        this._uhfManager.setReadPower(27); // 0-30
        this._uhfManager.setWritePower(27);
    }

    private JSONArray ConvertArrayList(ArrayList<String> list) {
        org.json.JSONArray jsonArray = new org.json.JSONArray();
        for (String value : list) {
            jsonArray.put(value);
        }

        return jsonArray;
    }

    private Boolean startInventoryThread(String mode) {

        this.readMode = mode;

        // _listTagDataModel = new ArrayList<com.pda.uhfm.TagDataModel>();
        // _listEPC = new ArrayList<String>();

        // start inventory thread
        startFlag = true;

        return true;
    }

    /**
     * Inventory EPC Thread
     */
    class InventoryThread extends Thread {

        private ArrayList<String> dataList;

        @Override
        public void run() {
            super.run();
            while (runFlag) {
                if (startFlag) {
                    if ("tid".equals(readMode)) {
                        try {
                            dataList = new ArrayList<String>();

                            List<com.pda.uhfm.TagDataModel> tagDataModels = _uhfManager.ReadData(UHFManager.TID, 0, 6,
                                    new byte[4]);
                            if (tagDataModels != null && tagDataModels.size() > 0) {

                                for (com.pda.uhfm.TagDataModel tagDataModel : tagDataModels) {
                                    String tid = tagDataModel.DATA;
                                    dataList.add(tid);
                                }
                            }
                        } catch (Exception ex) {
                            if (_uhfCallBackContext != null) {
                                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                        "Fehler-GetTID: " + ex.getMessage());
                                pluginResult.setKeepCallback(true);
                                _uhfCallBackContext.sendPluginResult(pluginResult);
                            }
                        }

                    } else if ("epc".equals(readMode)) {
                        dataList = new ArrayList<String>();

                        _uhfManager.startInventory(false); // multiMode

                        byte[] bytess = _uhfManager.getEPCByteBuff();
                        if (bytess != null) {
                            com.pda.uhfm.EPCDataModel epcDataModel = _uhfManager.getEPC(bytess);
                            if (epcDataModel != null) {
                                byte[] epcdata = epcDataModel.EPC;
                                int rssi = epcDataModel.RSSI;
                                String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                                // Log.e("inventoryTask", epc) ;
                                if (dataList == null) {
                                    dataList = new ArrayList<String>();
                                }
                                dataList.add(epc);
                            }
                        }
                    }

                    if ((dataList != null) && (!dataList.isEmpty())) {
                        if (dataList.size() > 0) {
                            returnCurrentTIDs(dataList);
                        }
                    } else {
                        returnCurrentTIDs(new ArrayList<String>());
                    }

                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // startFlag

            } // while

            _uhfManager.stopInventory();

        } // run

    } // end inventory thread class

    // add TIDs to view
    private void returnCurrentTIDs(final ArrayList<String> tidList) {
        cordova.getActivity().runOnUiThread(new Runnable() {

            public void run() {
                if (_uhfCallBackContext != null) {
                    if (!tidList.isEmpty()) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, ConvertArrayList(tidList));
                        pluginResult.setKeepCallback(true);
                        _uhfCallBackContext.sendPluginResult(pluginResult);
                    } else {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                ConvertArrayList(new ArrayList<String>()));
                        pluginResult.setKeepCallback(true);
                        _uhfCallBackContext.sendPluginResult(pluginResult);
                    }

                }
            }
        });
    }
}
