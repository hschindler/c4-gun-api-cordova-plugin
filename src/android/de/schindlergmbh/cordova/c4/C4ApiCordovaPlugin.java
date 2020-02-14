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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
// import com.hhw.uhfm.R;
// import com.pda.uhfm.EPCDataModel;
// import com.pda.uhfm.TagDataModel;
import com.pda.uhfm.UHFManager;
import com.pda.uhfm.VersionInfo;

import cn.pda.serialport.Tools;

public class C4ApiCordovaPlugin extends CordovaPlugin {
    public static final String TAG = "C4ApiCordovaPlugin";

    // public static final int SEARCH_REQ_CODE = 0;

    // public static final String PERM_NFC = Manifest.permission.NFC;

    // public static String platform; // Device OS
    // public static String uuid; // Device UUID

    private UHFManager _uhfManager;
    private Boolean _readerInitialized;
    private String _errorLog;

    private boolean runFlag = true;
    private boolean startFlag = false;
    private boolean waitingForScanKey = false;

    private String readMode = "tid"; // tid / epc

    private int _outputPower = 0;

    private Thread _scanThread;

    // private ArrayList<com.pda.uhfm.TagDataModel> _listTagDataModel;
    // private ArrayList<String> _listEPC;

    private CallbackContext _uhfCallBackContext;
    private CallbackContext _scanKeyCallBackContext;

    private KeyReceiver keyReceiver;

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

        // this._uhfManager = null;

        // try {
        // InitUhfManager();
        // registerReceiver();
        // } catch (Exception e) {
        // _errorLog = e.getMessage();
        // e.printStackTrace();
        // // Log.d(TAG, "Error: " + e.getMessage());
        // }

        // if (this._uhfManager != null) {
        // Thread thread = new InventoryThread();
        // thread.start();
        // }

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

        // if (this._uhfManager == null) {
        // callbackContext.error("UHF API not installed");
        // return true;
        // }

        // if (this._readerInitialized == false) {
        // callbackContext.error("UHF API not initialized");
        // return true;
        // }

        if ("getFirmware".equals(action)) {
            try {
                this.InitUhfManager();
            } catch (Exception e) {
                // TODO: handle exception
            }
            // final byte[] firmwareVersion = _uhfManager.getVersion();

            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            final com.pda.uhfm.VersionInfo firmwareVersion = this._uhfManager.getVersion();

            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {

                    // String test = "test 1111";
                    if (firmwareVersion != null) {
                        callbackContext.success(firmwareVersion.SoftwareVersion);
                    }

                    // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                    // firmwareVersion);
                    // callbackContext.sendPluginResult(pluginResult);
                }

            });

            return true;
        } else if ("startInventoryTID".equals(action)) {
            // if (this._uhfManager == null) {
            // // callbackContext.error("UHF API not installed");
            // callbackContext.error("UHF API not installed");
            // return true;
            // }

            this._uhfCallBackContext = callbackContext;

            this.StartInventoryThread("tid");

            return true;

        } else if ("startInventoryEPC".equals(action)) {
            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            this._uhfCallBackContext = callbackContext;

            this.StartInventoryThread("epc");

            return true;

        } else if ("stopInventory".equals(action)) {

            // if (this._uhfManager == null) {
            // // callbackContext.error("UHF API not installed");
            // callbackContext.error("UHF API not installed");
            // return true;
            // }
            this._uhfCallBackContext = callbackContext;
            startFlag = false;
            return true;

        } else if ("waitForScanKey".equals(action)) {

            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            waitingForScanKey = true;
            this._scanKeyCallBackContext = callbackContext;
            return true;

        } else if ("setReadPower".equals(action)) {
            // if (this._uhfManager == null) {
            // // callbackContext.error("UHF API not installed");
            // callbackContext.error("UHF API not installed");
            // return true;
            // }

            // int power = args.getInt(0); // 0 bis 30
            // boolean result = false;

            // if (power >= 0 && power <= 30) {
            // result = this._uhfManager.setReadPower(power);
            // }

            // return result;

            if (args == null) {
                return false;
            }

            this._outputPower = args.getInt(0);
            return true;
        } else {
            return false;
        }
    }

    public void onResume(boolean multitasking) {
        // TODO Auto-generated method stub
        // super.onResume(multitasking);

        Log.d(TAG, "onResume - runFlag: " + String.valueOf(startFlag));

        // this.initializeUHFManager();

        if (this.runFlag == true) {
            this.StartInventoryThread(this.readMode);
        }

    }

    public void onRestart() {
        // TODO Auto-generated method stub
        // super.onRestart();

        Log.d(TAG, "onRestart");

        // this.initializeUHFManager();

        if (this.runFlag == true) {
            this.StartInventoryThread(this.readMode);
        }
    }

    public void onDestroy() {

        this.StopInventoryThread();

        this.disposeUHFManager();

        // if (_barcodeManager != null) {
        // _barcodeManager.Close();
        // }

        // closeBarcode();

        _scanKeyCallBackContext = null;
        _uhfCallBackContext = null;

        unregisterReceiver();

        // super.onDestroy();
    }

    public void onPause() {
        startFlag = false;
        waitingForScanKey = false;
        this._uhfManager.close();
        super.onPause(false);
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    private void disposeUHFManager() {

        if (this._uhfManager != null) {
            Log.d(TAG, "disposeUHFManager");

            try {
                this._uhfManager.close();
            } catch (Exception e) {
                _errorLog = e.getMessage();
            }

            this._uhfManager = null;
        }
    }

    private void InitUhfManager() {

        if (this._uhfManager == null) {

            try {
                this._uhfManager = UHFManager.getInstance();
                this._readerInitialized = this._uhfManager.initRfid();

                this._uhfManager.setProtocol(_uhfManager.PROTOCOL_ISO_18000_6C);
                this._uhfManager.setFreBand(com.pda.uhfm.FreRegion.TMR_REGION_Europea_Union_3);

                if (this._outputPower > 0) {
                    boolean result = this._uhfManager.setReadPower(this._outputPower);
                } else {
                    this._uhfManager.setReadPower(27); // 0-30
                }

                this._uhfManager.setWritePower(27);
            } catch (Exception e) {
                _errorLog = e.getMessage();
                e.printStackTrace();
                // Log.d(TAG, "Error: " + e.getMessage());
            }

        }

    }

    private JSONArray ConvertArrayList(ArrayList<String> list) {
        org.json.JSONArray jsonArray = new org.json.JSONArray();
        for (String value : list) {
            jsonArray.put(value);
        }

        return jsonArray;
    }

    private void StartInventoryThread(String mode) {

        this.readMode = mode;

        Log.d(TAG, "StartInventoryThread");

        // start inventory thread
        startFlag = true;
        runFlag = true;

        if (this._scanThread == null) {
            Log.d(TAG, "StartInventoryThread - create new thread");
            this._scanThread = new InventoryThread();
        }

        Log.d(TAG, "StartInventoryThread - start thread");
        this._scanThread.start();
    }

    private void StopInventoryThread() {
        runFlag = false;
        startFlag = false;
    }

    private void registerReceiver() {
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        cordova.getActivity().registerReceiver(keyReceiver, filter);
    }

    private void unregisterReceiver() {
        try {
            cordova.getActivity().unregisterReceiver(keyReceiver);
        } catch (Exception e) {

        }

    }

    private static Toast toast;

    private class KeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {// ����H941
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
                if (toast == null) {
                    toast = Toast.makeText(cordova.getActivity(), "KeyReceiver:keyCode = " + keyCode,
                            Toast.LENGTH_SHORT);
                } else {
                    toast.setText("KeyReceiver:keyCode = " + keyCode);
                }
                toast.show();
                switch (keyCode) {
                case KeyEvent.KEYCODE_F1:

                    break;
                case KeyEvent.KEYCODE_F2:

                    break;
                case KeyEvent.KEYCODE_F3:
                    if (waitingForScanKey == true) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "scan clicked");
                        pluginResult.setKeepCallback(true);
                        _scanKeyCallBackContext.sendPluginResult(pluginResult);
                    }

                    break;
                case KeyEvent.KEYCODE_F4:

                    break;
                case KeyEvent.KEYCODE_F5:

                    break;
                }
            }

        }
    }

    /**
     * Inventory Thread
     */
    class InventoryThread extends Thread {

        private ArrayList<String> dataList;

        @Override
        public void run() {
            super.run();
            Log.d(TAG, "InventoryThread starting...");

            InitUhfManager();

            Log.d(TAG, "InventoryThread startflag = " + String.valueOf(startFlag));

            while (startFlag) {

                Log.d(TAG, "Waiting for timeout..");

                if (_uhfManager != null) {

                    if ("tid".equals(readMode)) {
                        try {
                            dataList = new ArrayList<String>();

                            List<com.pda.uhfm.TagDataModel> tagDataModels = _uhfManager.ReadData(UHFManager.TID, 0, 6,
                                    new byte[4]);

                            if (tagDataModels != null) {

                                for (com.pda.uhfm.TagDataModel tagDataModel : tagDataModels) {
                                    // String tid = tagDataModel.DATA;
                                    String tid = tagDataModel.DATA;

                                    if (tid.length() == 0) {
                                        byte[] bEpc = Tools.HexString2Bytes(tagDataModel.Epc);

                                        byte[] bTid = _uhfManager.readDataByEPC(bEpc, new byte[4], 2, 0, 4);
                                        if (bTid != null) {
                                            tid = Tools.Bytes2HexString(bTid, bTid.length);
                                            if (tid.length() > 1) {
                                                dataList.add(tid);
                                            } else {
                                                tid = "epc: " + tagDataModel.Epc;
                                                dataList.add(tid);
                                            }
                                        }
                                        // } else {
                                        // tid = "bTid = null - epc: " + tagDataModel.Epc + " bEpc ="
                                        // + Tools.Bytes2HexString(bEpc, bEpc.length);
                                        // dataList.add(tid);
                                        // }

                                    } else {
                                        dataList.add(tid);
                                    }
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

                }

                if ((dataList != null) && (!dataList.isEmpty())) {
                    if (dataList.size() > 0) {
                        returnCurrentTIDs(dataList);
                    }
                }
                // else {
                // returnCurrentTIDs(new ArrayList<String>());
                // }

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } // while startFlag

            Log.d(TAG, "InventoryThread is closing...");

            disposeUHFManager();

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
