import { Injectable } from '@angular/core';
import { Plugin, Cordova } from '@ionic-native/core';

@Plugin(
    {
        pluginName: "testplugin",
        plugin: "cordova-plugin-testplugin",
        pluginRef: "TestPlugin",
        repo: "https://github.com/Siddharth-Iotasol/TestPlugin.git",
        platforms: ['Android', 'iOS']
    }
)

@Injectable()
export class TestPluginProvider {

    @Cordova()
    isBluetoothEnabled(): Promise<string> {
        return;
    }

    @Cordova()
    getBatteryLevel(): Promise<string> {
        return;
    }

    @Cordova()
    getHardwareRevision(): Promise<string> {
        return;
    }

    @Cordova()
    getFirmwareRevision(): Promise<string> {
        return;
    }

    @Cordova()
    getModelNumber(): Promise<string> {
        return;
    }

    @Cordova()
    getSerialNumber(): Promise<string> {
        return;
    }

    @Cordova()
    startScan(): Promise<string> {
        return;
    }

    @Cordova()
    stopScan(): Promise<string> {
        return;
    }

    @Cordova()
    setDataPath(): Promise<string> {
        return;
    }

    @Cordova()
    setAppRevision(): Promise<string> {
        return;
    }

    @Cordova()
    isScanning(): Promise<string> {
        return;
    }

    @Cordova()
    isConnected(): Promise<string> {
        return;
    }

    @Cordova()
    getNewProbeFileFlag(): Promise<string> {
        return;
    }

    @Cordova()
    clearNewProbeFileFlag(): Promise<string> {
        return;
    }

    @Cordova()
    setNotification(): Promise<string> {
        return;
    }

    @Cordova()
    getState(): Promise<string> {
        return;
    }

    @Cordova()
    getLoadProgress(): Promise<string> {
        return;
    }

    @Cordova()
    startMeasurement(): Promise<string> {
        return;
    }

    @Cordova()
    stopMeasurement(): Promise<string> {
        return;
    }

    @Cordova()
    getProcessingAlgorithm(): Promise<string> {
        return;
    }

    @Cordova()
    setProcessingAlgorithm(): Promise<string> {
        return;
    }

    @Cordova()
    getMeasurementPointsPerMilliMeter(): Promise<string> {
        return;
    }

    @Cordova()
    setMeasurementPointsPerMilliMeter(): Promise<string> {
        return;
    }

    @Cordova()
    clearGpsLocation(): Promise<string> {
        return;
    }

    @Cordova()
    updateGpsLocation(): Promise<string> {
        return;
    }

    @Cordova()
    reset(): Promise<string> {
        return;
    }

    @Cordova()
    flushBleLog(): Promise<string> {
        return;
    }
}
