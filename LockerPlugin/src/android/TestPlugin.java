package cordova.plugin.testplugin;

import android.content.Context;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.coolu.blelibrary.*;
import com.coolu.blelibrary.impl.AndroidBle;
import com.coolu.blelibrary.inter.IBLE;
import com.coolu.blelibrary.inter.OnConnectionListener;
import com.coolu.blelibrary.inter.OnDeviceSearchListener;
import com.coolu.blelibrary.inter.OnResultListener;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

public class TestPlugin extends CordovaPlugin {
	private Context context;
	private CallbackContext callbackContext = null;
    private RadBleInterface _radBleInterface = null;

	IBLE ible = null;

	public TestPlugin() {
        _radBleInterface = new RadBleInterface();
		// this.able.init(context);
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		if (action.equals("isBluetoothEnabled")) {
			_radBleInterface.isBluetoothEnabled();
			return true;
		} else if (action.equals("getBatteryLevel")) {
			_radBleInterface.getBatteryLevel();
			return true;
		} else if (action.equals("getHardwareRevision")) {
			_radBleInterface.getHardwareRevision();
			return true;
		} else if (action.equals("getFirmwareRevision")) {
			_radBleInterface.getFirmwareRevision();
			return true;
		} else if (action.equals("getModelNumber")) {
			_radBleInterface.getModelNumber();
			return true;
		} else if (action.equals("getSerialNumber")) {
			_radBleInterface.getSerialNumber();
			return true;
		} else if (action.equals("startScan")) {
			_radBleInterface.startScan();
			return true;
		} else if (action.equals("stopScan")) {
			_radBleInterface.stopScan();
			return true;
		} else if (action.equals("setDataPath")) {
			_radBleInterface.setDataPath();
			return true;
		} else if (action.equals("setAppRevision")) {
			_radBleInterface.setAppRevision();
			return true;
		} else if (action.equals("isScanning")) {
			_radBleInterface.isScanning();
			return true;
		} else if (action.equals("isConnected")) {
			_radBleInterface.isConnected();
			return true;
		} else if (action.equals("getNewProbeFileFlag")) {
			_radBleInterface.getNewProbeFileFlag();
			return true;
		} else if (action.equals("clearNewProbeFileFlag")) {
			_radBleInterface.clearNewProbeFileFlag();
			return true;
		} else if (action.equals("setNotification")) {
			_radBleInterface.setNotification();
			return true;
		} else if (action.equals("getState")) {
			_radBleInterface.getState();
			return true;
		} else if (action.equals("getLoadProgress")) {
			_radBleInterface.getLoadProgress();
			return true;
		} else if (action.equals("startMeasurement")) {
			_radBleInterface.startMeasurement();
			return true;
		} else if (action.equals("stopMeasurement")) {
			_radBleInterface.stopMeasurement();
			return true;
		} else if (action.equals("getProcessingAlgorithm")) {
			_radBleInterface.getProcessingAlgorithm();
			return true;
		} else if (action.equals("setProcessingAlgorithm")) {
			_radBleInterface.setProcessingAlgorithm();
			return true;
		} else if (action.equals("getMeasurementPointsPerMilliMeter")) {
			_radBleInterface.getMeasurementPointsPerMilliMeter();
			return true;
		} else if (action.equals("setMeasurementPointsPerMilliMeter")) {
			_radBleInterface.setMeasurementPointsPerMilliMeter();
			return true;
		} else if (action.equals("clearGpsLocation")) {
			_radBleInterface.clearGpsLocation();
			return true;
		} else if (action.equals("updateGpsLocation")) {
			_radBleInterface.updateGpsLocation();
			return true;
		} else if (action.equals("reset")) {
			_radBleInterface.reset();
			return true;
		} else if (action.equals("flushBleLog")) {
			_radBleInterface.flushBleLog();
			return true;
		}
		return false;
	}
}