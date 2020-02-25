cordova.define("c4-gun-api-cordova-plugin.C4GunApiCordovaPlugin", function (require, exports, module) {

    // var argscheck = require('cordova/argscheck');
    // var channel = require('cordova/channel');
    // var utils = require('cordova/utils');
    var exec = require('cordova/exec');
    // var cordova = require('cordova');

    var PLUGIN_NAME = 'C4GunApiCordovaPlugin';

    //channel.createSticky('onCordovaInfoReady');
    // Tell cordova channel to wait on the CordovaInfoReady event
    //channel.waitForInitialization('onCordovaInfoReady');

    /**
     * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
     * phone, etc.
     * @constructor
     */
    function C4GunApiCordovaPlugin() {
        this.available = false;

        //var me = this;

        // channel.onCordovaReady.subscribe(function () {
        // console.log('C4 GUN onCordovaReady - getFirmware');

        // me.common.getFirmware(function (firmware) {
        //     me.available = true;
        //     console.log('C4 GUN Firmware:', firmware);

        //     channel.onCordovaInfoReady.fire();
        // }, function (e) {
        //     me.available = false;
        //     utils.alert('[ERROR] Error initializing C4GunApi: ' + e);
        // });
        //});
    }

    /** common */
    C4GunApiCordovaPlugin.prototype.common = {
        /**
         * Get firmware
         *
         * @param {Function} successCallback The function to call when the heading data is available
         * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
         */
        getFirmware: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('getFirmware call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'getFirmware', []);
        }
    };

    /** uhf */
    C4GunApiCordovaPlugin.prototype.uhf = {
        startInventory: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('startInventoryTID call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'startInventoryTID', []);
        },
        startInventoryEPC: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('startInventoryEPC call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'startInventoryEPC', []);
        },
        stopInventory: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('stopInventory call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'stopInventory', []);
        },
        waitForScanKey: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('stopInventory call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'waitForScanKey', []);
        },
        setReadPower: function (successCallback, errorCallback) {
            // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

            console.log('setReadPower call exec');
            exec(successCallback, errorCallback, PLUGIN_NAME, 'setReadPower', []);
        }
    };




    module.exports = new C4GunApiCordovaPlugin();

});
