import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { TestPluginProvider } from './test-plugin';

@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {
    addresses: Array<string> = ["FF:FF:98:02:43:B1", "FF:FF:95:02:66:40"];
    randomInt: number = 1;
    constructor(
        public navCtrl: NavController,
        public testPluginProvider: TestPluginProvider
    ) {

    }
    isBluetoothEnabled() {
        this.testPluginProvider.isBluetoothEnabled().then((responseName) => {
            alert('isBluetoothEnabled success ' + responseName);
        }, error => {
            alert('isBluetoothEnabled error ' + error);
        })
    }

    getBatteryLevel() {
        this.testPluginProvider.isBluetoothEnabled().then((responseName) => {
            alert('getBatteryLevel success ' + responseName);
        }, error => {
            alert('getBatteryLevel error ' + error);
        })
    }

    getHardwareRevision() {
        this.testPluginProvider.getHardwareRevision().then((responseName) => {
            alert('getHardwareRevision success ' + responseName);
        }, error => {
            alert('getHardwareRevision error ' + error);
        })
    }

    getFirmwareRevision() {
        this.testPluginProvider.getFirmwareRevision().then((responseName) => {
            alert('getFirmwareRevision success ' + responseName);
        }, error => {
            alert('getFirmwareRevision error ' + error);
        })
    }

    getModelNumber() {
        this.testPluginProvider.getModelNumber().then((responseName) => {
            alert('getModelNumber success ' + responseName);
        }, error => {
            alert('getModelNumber error ' + error);
        })
    }

    getSerialNumber() {
        this.testPluginProvider.getSerialNumber().then((responseName) => {
            alert('getSerialNumber success ' + responseName);
        }, error => {
            alert('getSerialNumber error ' + error);
        })
    }

    startScan() {
        this.testPluginProvider.startScan().then((responseName) => {
            alert('startScan success ' + responseName);
        }, error => {
            alert('startScan error ' + error);
        })
    }

    stopScan() {
        this.testPluginProvider.stopScan().then((responseName) => {
            alert('stopScan success ' + responseName);
        }, error => {
            alert('stopScan error ' + error);
        })
    }

    setDataPath() {
        this.testPluginProvider.setDataPath().then((responseName) => {
            alert('setDataPath success ' + responseName);
        }, error => {
            alert('setDataPath error ' + error);
        })
    }

    setAppRevision() {
        this.testPluginProvider.setAppRevision().then((responseName) => {
            alert('setAppRevision success ' + responseName);
        }, error => {
            alert('setAppRevision error ' + error);
        })
    }

    isScanning() {
        this.testPluginProvider.isScanning().then((responseName) => {
            alert('isScanning success ' + responseName);
        }, error => {
            alert('isScanning error ' + error);
        })
    }

    isConnected() {
        this.testPluginProvider.isConnected().then((responseName) => {
            alert('isConnected success ' + responseName);
        }, error => {
            alert('isConnected error ' + error);
        })
    }

    getNewProbeFileFlag() {
        this.testPluginProvider.getNewProbeFileFlag().then((responseName) => {
            alert('getNewProbeFileFlag success ' + responseName);
        }, error => {
            alert('getNewProbeFileFlag error ' + error);
        })
    }

    clearNewProbeFileFlag() {
        this.testPluginProvider.clearNewProbeFileFlag().then((responseName) => {
            alert('clearNewProbeFileFlag success ' + responseName);
        }, error => {
            alert('clearNewProbeFileFlag error ' + error);
        })
    }

    setNotification() {
        this.testPluginProvider.setNotification().then((responseName) => {
            alert('setNotification success ' + responseName);
        }, error => {
            alert('setNotification error ' + error);
        })
    }

    getState() {
        this.testPluginProvider.getState().then((responseName) => {
            alert('getState success ' + responseName);
        }, error => {
            alert('getState error ' + error);
        })
    }

    getLoadProgress() {
        this.testPluginProvider.getLoadProgress().then((responseName) => {
            alert('getLoadProgress success ' + responseName);
        }, error => {
            alert('getLoadProgress error ' + error);
        })
    }

    startMeasurement() {
        this.testPluginProvider.startMeasurement().then((responseName) => {
            alert('startMeasurement success ' + responseName);
        }, error => {
            alert('startMeasurement error ' + error);
        })
    }

    stopMeasurement() {
        this.testPluginProvider.stopMeasurement().then((responseName) => {
            alert('stopMeasurement success ' + responseName);
        }, error => {
            alert('stopMeasurement error ' + error);
        })
    }

    getProcessingAlgorithm() {
        this.testPluginProvider.getProcessingAlgorithm().then((responseName) => {
            alert('getProcessingAlgorithm success ' + responseName);
        }, error => {
            alert('getProcessingAlgorithm error ' + error);
        })
    }

    setProcessingAlgorithm() {
        this.testPluginProvider.setProcessingAlgorithm().then((responseName) => {
            alert('setProcessingAlgorithm success ' + responseName);
        }, error => {
            alert('setProcessingAlgorithm error ' + error);
        })
    }

    setMeasurementPointsPerMilliMeter() {
        this.testPluginProvider.setMeasurementPointsPerMilliMeter().then((responseName) => {
            alert('setMeasurementPointsPerMilliMeter success ' + responseName);
        }, error => {
            alert('setMeasurementPointsPerMilliMeter error ' + error);
        })
    }

    getMeasurementPointsPerMilliMeter() {
        this.testPluginProvider.getMeasurementPointsPerMilliMeter().then((responseName) => {
            alert('getMeasurementPointsPerMilliMeter success ' + responseName);
        }, error => {
            alert('getMeasurementPointsPerMilliMeter error ' + error);
        })
    }

    clearGpsLocation() {
        this.testPluginProvider.clearGpsLocation().then((responseName) => {
            alert('clearGpsLocation success ' + responseName);
        }, error => {
            alert('clearGpsLocation error ' + error);
        })
    }

    updateGpsLocation() {
        this.testPluginProvider.updateGpsLocation().then((responseName) => {
            alert('updateGpsLocation success ' + responseName);
        }, error => {
            alert('updateGpsLocation error ' + error);
        })
    }

    reset() {
        this.testPluginProvider.reset().then((responseName) => {
            alert('reset success ' + responseName);
        }, error => {
            alert('reset error ' + error);
        })
    }

    flushBleLog() {
        this.testPluginProvider.flushBleLog().then((responseName) => {
            alert('flushBleLog success ' + responseName);
        }, error => {
            alert('flushBleLog error ' + error);
        })
    }
}
