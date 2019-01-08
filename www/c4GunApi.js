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

var argscheck = require('cordova/argscheck');
var channel = require('cordova/channel');
var utils = require('cordova/utils');
var exec = require('cordova/exec');
var cordova = require('cordova');

var PLUGIN_NAME = 'C4GunApi';

channel.createSticky('onCordovaInfoReady');
// Tell cordova channel to wait on the CordovaInfoReady event
channel.waitForInitialization('onCordovaInfoReady');

/**
 * This represents the mobile device, and provides properties for inspecting the model, version, UUID of the
 * phone, etc.
 * @constructor
 */
function C4GunApi() {
    this.available = false;

    var me = this;

    channel.onCordovaReady.subscribe(function () {
        console.log('C4 GUN onCordovaReady - getFirmware');

        me.getFirmware(function (firmware) {
            me.available = true;
            console.log('C4 GUN Firmware:', firmware);

            channel.onCordovaInfoReady.fire();
        }, function (e) {
            me.available = false;
            utils.alert('[ERROR] Error initializing C4GunApi: ' + e);
        });
    });
}

/**
 * Get firmware
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
C4GunApi.prototype.getFirmware = function (successCallback, errorCallback) {
    // argscheck.checkArgs('fF', 'C4GunAPI.getFirmware', arguments);

    console.log('getFirmware call exec');
    exec(successCallback, errorCallback, PLUGIN_NAME, 'getFirmware', []);
};

module.exports = new C4GunApi();
