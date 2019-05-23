package cordova.plugin.testplugin;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.lang.Thread;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * The RadBleInterface class. Creates and manages interactions over BLE with a
 * RAD probe.
 */
public class RadBleInterface {

    // The RAD Sensor Service and Characteristic UUIDs
    public static final UUID RAD_SENSOR_SERVICE                      = UUID.fromString("00003600-0000-1000-8000-00805F9B34FB");
    public static final UUID COM_CMD_CHARACTERISTIC                  = UUID.fromString("00003601-0000-1000-8000-00805F9B34FB");
    public static final UUID DATA_1_CHARACTERISTIC                   = UUID.fromString("00003602-0000-1000-8000-00805F9B34FB");
    public static final UUID DATA_2_CHARACTERISTIC                   = UUID.fromString("00003603-0000-1000-8000-00805F9B34FB");
    public static final UUID DATA_3_CHARACTERISTIC                   = UUID.fromString("00003604-0000-1000-8000-00805F9B34FB");
    public static final UUID DATA_4_CHARACTERISTIC                   = UUID.fromString("00003605-0000-1000-8000-00805F9B34FB");

    // The Battery Service and Characteristic UUIDs
    public static final UUID BATTERY_SERVICE                         = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    public static final UUID BATTERY_LEVEL_CHARACTERISTIC            = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    // The Device Information Service and Characteristic UUIDs
    public static final UUID DEVICE_INFORMATION_SERVICE              = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
    public static final UUID MODEL_NUMBER_STRING_CHARACTERISTIC      = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
    public static final UUID SERIAL_NUMBER_STRING_CHARACTERISTIC     = UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB");
    public static final UUID FIRMWARE_REVISION_STRING_CHARACTERISTIC = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID HARDWARE_REVISION_STRING_CHARACTERISTIC = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
    public static final UUID MANUFACTURER_NAME_STRING_CHARACTERISTIC = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");

    // The RAD APP state values
    public static final int RAD_STATE_FAKE_DISCONNECTED     = 0;
    public static final int RAD_STATE_FAKE_SCANNING         = 1;
    public static final int RAD_STATE_DISCONNECTED          = 2;
    public static final int RAD_STATE_SCANNING              = 3;
    public static final int RAD_STATE_GET_STATE             = 4;
    public static final int RAD_STATE_IDLE                  = 5;  // BLE Equivalent State
    public static final int RAD_STATE_GET_ALGO_IDLE_UPDATE  = 6;
    public static final int RAD_STATE_SET_ALGO_IDLE_UPDATE  = 7;
    public static final int RAD_STATE_GET_PPMM_IDLE_UPDATE  = 8;
    public static final int RAD_STATE_SET_PPMM_IDLE_UPDATE  = 9;
    public static final int RAD_STATE_GET_ALGO_MEAS_START   = 10;
    public static final int RAD_STATE_SET_ALGO_MEAS_START   = 11;
    public static final int RAD_STATE_GET_PPMM_MEAS_START   = 12;
    public static final int RAD_STATE_SET_PPMM_MEAS_START   = 13;
    public static final int RAD_STATE_MEAS_START            = 14;
    public static final int RAD_STATE_MEAS                  = 15; // BLE Equivalent State
    public static final int RAD_STATE_MEAS_STOP             = 16;
    public static final int RAD_STATE_PROCESSING            = 17; // BLE Equivalent State
    public static final int RAD_STATE_GET_ALGO_NUM_DATA     = 18;
    public static final int RAD_STATE_SET_ALGO_NUM_DATA_PAD = 19;
    public static final int RAD_STATE_GET_PPMM_NUM_DATA     = 20;
    public static final int RAD_STATE_SET_PPMM_NUM_DATA_PAD = 21;
    public static final int RAD_STATE_GET_NUM_DATA          = 22; // BLE Equivalent State
    public static final int RAD_STATE_SEND_DATA             = 23; // BLE Equivalent State
    public static final int RAD_STATE_PACKET_REQUEST        = 24;
    public static final int RAD_STATE_RESET                 = 25;
    public static final int RAD_STATE_FORCE_RESET           = 26;
    public static final int RAD_STATE_PREPARE               = 27; // BLE Equivalent State
    public static final int RAD_STATE_ERROR                 = 28; // BLE Equivalent State
    public static final int RAD_STATE_GET_ERROR_CODE        = 29;

    private String[] stateDbg = new String[] {
        "Fake_Disconnected",
        "Fake_Scanning",
        "Disconnected",
        "Scanning",
        "Get_State",
        "Idle",
        "Get_Algo_Idle_Update",
        "Set_Algo_Idle_Update",
        "Get_Ppmm_Idle_Update",
        "Set_Ppmm_Idle_Update",
        "Get_Algo_Meas_Start",
        "Set_Algo_Meas_Start",
        "Get_Ppmm_Meas_Start",
        "Set_Ppmm_Meas_Start",
        "Meas_Start",
        "Meas",
        "Meas_Stop",
        "Processing",
        "Get_Algo_Num_Data",
        "Set_Algo_Num_Data_Pad",
        "Get_Ppmm_Num_Data",
        "Set_Ppmm_Num_Data_Pad",
        "Get_Num_Data",
        "Send_Data",
        "Packet_Request",
        "Reset",
        "Force_Reset",
        "Prepare",
        "Error",
        "Get_Error_Code"
    };

    private int retryCount = 0;
    private int stateRetryCount = 0;
    private final int MAX_RETRY_COUNT = 3;
    private final double PACKET_LOSS_PERCENT_THRESH = 0.0025;
    private int processingAlgorithm = -1;
    private int pointsPerMilliMeter = -1;
    private int actualAlgo = -1;
    private int actualPpmm = -1;

    // The current working path
    private String dataPath = "";
    // The current app revision
    private String appRevision = "";
    // The last number of packets value read from the probe
    private int numPackets = 0;
    // The current RAD state
    private int radState = RAD_STATE_DISCONNECTED;
    // The log file handle
    private FileWriter logWriter = null;

    // The BLE COM command characteristic
    private final int BLE_COM_CMD_STATE             = 1;
    private final int BLE_COM_CMD_MEAS_START        = 2;
    private final int BLE_COM_CMD_MEAS_STOP         = 3;
    private final int BLE_COM_CMD_RESET             = 4;
    private final int BLE_COM_CMD_SEND_DATA         = 5;
    private final int BLE_COM_CMD_NUM_DATA          = 6;
    private final int BLE_COM_CMD_MEAS_PPMM_SETTING = 7;
    private final int BLE_COM_CMD_PROC_ALGORITHM    = 8;
    private final int BLE_COM_CMD_RESERVED_09       = 9;
    private final int BLE_COM_CMD_RESERVED_10       = 10;
    private final int BLE_COM_CMD_RESERVED_11       = 11;
    private final int BLE_COM_CMD_PACKET_REQUEST    = 12;
    private final int BLE_COM_CMD_ERROR_CODE        = 13;

    String[] cmdDbg = new String[] {
        "Invalid Cmd",
        "State",
        "Meas_Start",
        "Meas_Stop",
        "Reset",
        "Send_Data",
        "Num_Data",
        "Meas_Ppmm_Setting",
        "Proc_Algorithm",
        "Reserved_09",
        "Reserved_10",
        "Reserved_11",
        "Packet_Request",
        "Error_Code"
    };

    // The BLE EXT command characteristic
    private final int BLE_EXT_CMD_WRITE             = 1;
    private final int BLE_EXT_CMD_READ              = 2;
    private final int BLE_EXT_CMD_RESPONSE          = 3;
    private final int BLE_EXT_CMD_PUSH              = 4;
    private final int BLE_EXT_CMD_ACK               = 5;
    private final int BLE_EXT_CMD_NACK              = 6;

    String[] extCmdDbg = new String[] {
        "Invalid Ext Cmd",
        "Write",
        "Read",
        "Response",
        "Push",
        "Ack",
        "Nack"
    };

    // The STATE_CHARACTERISTIC state values
    private final int BLE_STATE_IDLE                = 0;
    private final int BLE_STATE_MEAS                = 1;
    private final int BLE_STATE_PROCESSING          = 2;
    private final int BLE_STATE_READY               = 3;
    private final int BLE_STATE_SEND_DATA           = 4;
    private final int BLE_STATE_PREPARE             = 5;
    private final int BLE_STATE_ERROR               = 6;

    String[] bleStateDbg = new String[] {
        "Idle",
        "Meas",
        "Processing",
        "Ready",
        "Send_Data",
        "Prepare",
        "Error"
    };

    private void bleLog(String string) {
        try {
            if (logWriter != null) {
                logWriter.write(string + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bleLog(String tag, String string) {
        Log.i(tag, string);
        bleLog(string);
    }

    public void flushBleLog() {
        try {
            if (logWriter != null) {
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor method for creating a RAD BLE interface object.
     */
    public RadBleInterface() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(RAD_STATE_DISCONNECTED);
        gpsLocationIsValid = false;
        clearGpsLocation();
    }

    /**
     * Gets the processing algorithm setting.
     *
     * @return The processing algorithm setting.
     */
    public int getProcessingAlgorithm() {
        return processingAlgorithm;
    }

    /**
     * Sets the processing algorithm setting.
     *
     * @param newProcessingAlgorithm The new processing algorithm setting.
     */
    public void setProcessingAlgorithm(int newProcessingAlgorithm) {
        processingAlgorithm = newProcessingAlgorithm;
        // Give the SM a kick to go through update routine
        if (radState == RAD_STATE_IDLE) {
            setState(RAD_STATE_GET_STATE);
            write(COM_CMD_CHARACTERISTIC, new byte[] {
                    (byte)BLE_COM_CMD_STATE,
                    (byte)BLE_EXT_CMD_READ,
                    (byte)0,
                    (byte)0
                });
        }
    }

    /**
     * Gets the measurement points per millimeter setting.
     *
     * @return The measurement points per millimeter setting.
     */
    public int getMeasurementPointsPerMilliMeter() {
        return pointsPerMilliMeter;
    }

    /**
     * Sets the measurement points per millimeter setting.
     *
     * @param newPointsPerMilliMeter The new measurement points per millimeter setting.
     */
    public void setMeasurementPointsPerMilliMeter(int newPointsPerMilliMeter) {
        pointsPerMilliMeter = newPointsPerMilliMeter;
        // Give the SM a kick to go through update routine
        if (radState == RAD_STATE_IDLE) {
            setState(RAD_STATE_GET_STATE);
            write(COM_CMD_CHARACTERISTIC, new byte[] {
                    (byte)BLE_COM_CMD_STATE,
                    (byte)BLE_EXT_CMD_READ,
                    (byte)0,
                    (byte)0
                });
        }
    }

    /**
     * Start a measurement.
     *
     * @return True if successful, otherwise False.
     */
    public boolean startMeasurement() {
        if (radState == RAD_STATE_IDLE) {
            if (actualPpmm != pointsPerMilliMeter || actualAlgo != processingAlgorithm) {
                // Update PPMM and Algo
                setState(RAD_STATE_GET_ALGO_MEAS_START);
                write(COM_CMD_CHARACTERISTIC, new byte[] {
                        (byte)BLE_COM_CMD_PROC_ALGORITHM,
                        (byte)BLE_EXT_CMD_READ,
                        (byte)0,
                        (byte)0
                    });
            }
            else {
                setState(RAD_STATE_MEAS_START);
                write(COM_CMD_CHARACTERISTIC, new byte[] {
                        (byte)BLE_COM_CMD_MEAS_START,
                        (byte)BLE_EXT_CMD_WRITE,
                        (byte)0,
                        (byte)0
                    });
            }
            return true;
        }
        return false;
    }

    /**
     * Stop a measurement.
     *
     * @return True if successful, otherwise False.
     */
    public boolean stopMeasurement() {
        if (radState == RAD_STATE_MEAS) {
            setState(RAD_STATE_MEAS_STOP);
            write(COM_CMD_CHARACTERISTIC, new byte[] {
                    (byte)BLE_COM_CMD_MEAS_STOP,
                    (byte)BLE_EXT_CMD_WRITE,
                    (byte)0,
                    (byte)0
                });
            return true;
        }
        return false;
    }

    /**
     * Check if bluetooth is enabled.
     *
     * @return True if bluetooth is enabled, otherwise false.
     */
    public boolean isBluetoothEnabled() {
        boolean enabled = btAdapter.isEnabled();
        if (enabled == false) {
            setState(RAD_STATE_DISCONNECTED);
        }
        return enabled;
    }

    /**
     * Get the current battery level as a string.
     *
     * @return The current battery level as a string.
     */
    public String getBatteryLevel() {
        if (isConnected()) {
            return batteryLevelValue;
        } else {
            return BATTERY_LEVEL_DEFAULT_VALUE;
        }
    }

    /**
     * Get the connected probe's hardware revision.
     *
     * @return The connected probe's hardware revision as a string.
     */
    public String getHardwareRevision() {
        if (isConnected()) {
            return hardwareRevValue;
        } else {
            return HARDWARE_REV_DEFAULT_VALUE;
        }
    }

    /**
     * Get the connected probe's firmware revision.
     *
     * @return The connected probe's firmware revision as a string.
     */
    public String getFirmwareRevision() {
        if (isConnected()) {
            return firmwareRevValue;
        } else {
            return FIRMWARE_REV_DEFAULT_VALUE;
        }
    }

    /**
     * Get the connected probe's model number.
     *
     * @return The connected probe's model number as a string.
     */
    public String getModelNumber() {
        if (isConnected()) {
            return modelNumberValue;
        } else {
            return MODEL_NUMBER_DEFAULT_VALUE;
        }
    }

    /**
     * Get the connected probe's serial number.
     *
     * @return The connected probe's serial number as a string.
     */
    public String getSerialNumber() {
        if (isConnected()) {
            return serialNumberValue;
        } else {
            return SERIAL_NUMBER_DEFAULT_VALUE;
        }
    }

    /**
     * Get the connected probe's manufacturer name string.
     *
     * @return The connected probe's manufacturer name string.
     */
    public String getManufacturerName() {
        if (isConnected()) {
            return manufacturerName;
        } else {
            return MANUFACTURER_NAME_DEFAULT_VALUE;
        }
    }

    /**
     * Sets the current data path.
     *
     * @param path The current working path.
     */
    public void setDataPath(String path) {
        dataPath = path;
        try {
            logWriter = new FileWriter(dataPath + File.separator + "text" + File.separator + "ble.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bleLog(RAD_BLE_API, "setDataPath: " + path);
    }

    /**
     * Sets the current app revision.
     *
     * @param appRev The current app revision.
     */
    public void setAppRevision(String appRev) {
        bleLog(RAD_BLE_API, "setAppRevision: " + appRev);
        appRevision = appRev;
    }

    /**
     * Starts a scan for the RAD probe.
     */
    public void startScan() {
        if (radState == RAD_STATE_DISCONNECTED) {
            bleScanner = btAdapter.getBluetoothLeScanner();
            bleLog(RAD_BLE_API, "startScan");
            setState(RAD_STATE_SCANNING);
            bleScanner.startScan(mScanCb);
        }
        else if (radState == RAD_STATE_FAKE_DISCONNECTED) {
            setState(RAD_STATE_FAKE_SCANNING);
        }
    }

    /**
     * Stops a scan for the RAD probe.
     */
    public void stopScan() {
        if (radState == RAD_STATE_SCANNING) {
            bleLog(RAD_BLE_API, "stopScan");
            bleScanner.stopScan(mScanCb);
            setState(RAD_STATE_DISCONNECTED);
        }
        else if (radState == RAD_STATE_FAKE_SCANNING) {
            setState(RAD_STATE_FAKE_DISCONNECTED);
        }
    }

    /**
     * Returns the new probe file flag.
     *
     * @return true if the new probe file flag is set, otherwise false.
     */
    public boolean getNewProbeFileFlag() {
        return newProbeFile;
    }

    /**
     * Clears the new probe file flag.
     */
    public void clearNewProbeFileFlag() {
        newProbeFile = false;
    }

    /**
     * Checks to see if currently scanning for the RAD probe.
     *
     * @return true if scanning for the RAD probe, otherwise false.
     */
    public boolean isScanning() {
        if (radState < RAD_STATE_DISCONNECTED) {
            return radState == RAD_STATE_FAKE_SCANNING;
        }
        else {
            return radState == RAD_STATE_SCANNING;
        }
    }

    /**
     * Checks to see if the RAD probe is connected over BLE.
     *
     * @return true if the RAD probe is connected, otherwise false.
     */
    public boolean isConnected() {
        return radState >= RAD_STATE_IDLE;
    }

    /**
     * Gets the current data load progress as a percentage value between 0 and 1.
     *
     * @return The current load progress percentage.
     */
    public float getLoadProgress() {
        if ((radState == RAD_STATE_SEND_DATA) && (numPackets > 0)) {
            return (float)dataBuffer.size() / (float)numPackets;
        }
        else {
            return (float)0;
        }
    }

    /**
     * Gets the current BLE state.
     *
     * @return The current BLE state.
     */
    public int getState() {
        return radState;
    }

    /**
     * Clear the GPS location string.
     */
    public void clearGpsLocation() {
        gpsLocation = "N/A";
    }

    /**
     * Update the GPS location string.
     *
     * @param lat  The latitudinal coordinate.
     * @param lon  The longitudinal coordinate.
     */
    public void updateGpsLocation(String lat, String lon) {
        gpsLocation = lat + ", " + lon;
    }

    /**
     * Issues a reset command to the probe.
     */
    public void reset() {
        if (radState >= RAD_STATE_MEAS) {
            setState(RAD_STATE_FORCE_RESET);
            write(COM_CMD_CHARACTERISTIC, new byte[] {
                    (byte)BLE_COM_CMD_RESET,
                    (byte)BLE_EXT_CMD_WRITE,
                    (byte)0,
                    (byte)0
                });
        }
    }

    /**
     * Sets the notification value for a BLE characteristic. Enabling
     * notifications for a characteristic allows for the RAD probe device to
     * notify the app if the characteristic data has changed.
     *
     * @param uuidCharacteristic The UUID of the characteristic
     * @param enable             Set to true to enable notifications on
     *                           characteristic changes, otherwise false.
     * @return The return status of the descriptor write.
     */
    public int setNotification(UUID uuidCharacteristic, boolean enable) {
        // Note: This synchronized lock is intended to work in conjunction with
        // the private read/write synchronized request/response methods
        synchronized (lock) {
            waitServiceConnect(lock);
            bleLog(RAD_BLE_API, "setNotification");

            int status = -1;

            if (radState > RAD_STATE_DISCONNECTED) {
                startFakeDisconnectAlarm();
                // Note: The else/if conditionals are grouped by service to
                // ensure proper lookup of the characteristic (ie radService vs batteryService).
                if (uuidCharacteristic == COM_CMD_CHARACTERISTIC ||
                    uuidCharacteristic == DATA_1_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_2_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_3_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_4_CHARACTERISTIC) {
                    status = syncSetNotification(radService.getCharacteristic(uuidCharacteristic), enable);
                }
                else if (uuidCharacteristic == BATTERY_LEVEL_CHARACTERISTIC) {
                    status = syncSetNotification(batteryService.getCharacteristic(uuidCharacteristic), enable);
                }
            }

            return status;
        }
    }

    /**
     * Performs a synchronous read of a characteristic value.
     *
     * @param uuidCharacteristic The UUID of the characteristic
     * @return The characteristic value as a byte array.
     */
    public byte[] read(UUID uuidCharacteristic) {
        // Note: This synchronized lock is intended to work in conjunction with
        // the private read/write synchronized request/response methods
        synchronized (lock) {
            waitServiceConnect(lock);
            bleLog(RAD_BLE_API, "read");

            byte[] value = null;

            if (radState > RAD_STATE_DISCONNECTED) {
                startFakeDisconnectAlarm();
                // Note: The else/if conditionals are grouped by service to
                // ensure proper lookup of the characteristic
                // (ie radService vs batteryService vs devInfoService).
                if (uuidCharacteristic == COM_CMD_CHARACTERISTIC ||
                    uuidCharacteristic == DATA_1_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_2_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_3_CHARACTERISTIC  ||
                    uuidCharacteristic == DATA_4_CHARACTERISTIC) {
                    value = syncReadCharacteristic(radService.getCharacteristic(uuidCharacteristic));
                }
                else if (uuidCharacteristic == BATTERY_LEVEL_CHARACTERISTIC) {
                    value = syncReadCharacteristic(batteryService.getCharacteristic(uuidCharacteristic));
                }
                else if (uuidCharacteristic == MODEL_NUMBER_STRING_CHARACTERISTIC      ||
                         uuidCharacteristic == SERIAL_NUMBER_STRING_CHARACTERISTIC     ||
                         uuidCharacteristic == FIRMWARE_REVISION_STRING_CHARACTERISTIC ||
                         uuidCharacteristic == HARDWARE_REVISION_STRING_CHARACTERISTIC ||
                         uuidCharacteristic == MANUFACTURER_NAME_STRING_CHARACTERISTIC) {
                    value = syncReadCharacteristic(devInfoService.getCharacteristic(uuidCharacteristic));
                }
            }

            return value;
        }
    }

    /**
     * Performs a synchronous write of a characteristic value.
     *
     * @param uuidCharacteristic The UUID of the characteristic
     * @param value              The byte array to set the characteristic
     *                           value to
     * @return The return status of the write.
     */
    public int write(UUID uuidCharacteristic, byte[] value) {
        // Note: This synchronized lock is intended to work in conjunction with
        // the private read/write synchronized request/response methods

        synchronized (lock) {
            waitServiceConnect(lock);
            bleLog(RAD_BLE_API, "write - " + bytesToHex(value));

            if (radState > RAD_STATE_DISCONNECTED) {
                startFakeDisconnectAlarm();
                // Note: The else/if conditionals are grouped by service to
                // ensure proper lookup of the characteristic (ie radService).
                if (uuidCharacteristic == COM_CMD_CHARACTERISTIC) {
                    BluetoothGattCharacteristic characteristic = radService.getCharacteristic(uuidCharacteristic);

                    // The new probe does not like WRITE_TYPE_NO_RESPONSE
                    // characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    characteristic.setValue(value);
                    // Since this is write WITHOUT RESPONSE, we will not wait for a response so do not use
                    // syncWriteCharacteristic function.
                    mGatt.writeCharacteristic(characteristic);
                }
                // Note: The below is an example of a BLE write with response (a synchronous write, the default)
                // else if (SYNC_WRITE_CASE) {
                //     characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                //     characteristic.setValue(value);
                //     status = syncWriteCharacteristic(characteristic);
                // }
            }
        }

        return retryCount;
    }

    /**
     * The callback method for a characteristic value change. Note, this
     * function is intended to be overridden in the python layer... but
     * pyjnius is a pain and doesn't want to interrupt the python thread
     * so we'll handle it here instead.
     *
     * @param uuidCharacteristic The UUID of the characteristic
     * @param value              The characteristic value as a byte array
     */
    public void characteristicChangedCb(UUID uuidCharacteristic, byte[] value) {
        if (uuidCharacteristic.compareTo(COM_CMD_CHARACTERISTIC) == 0) {
            stopFakeDisconnectAlarm(true);

            int cmd     = value[0];
            int ext_cmd = value[1];
            int length  = value[3];

            bleLog(RAD_BLE_API, "    |-- COM_CMD_CHARACTERISTIC");
            bleLog(RAD_BLE_API, "        |-- CMD:     " + cmdDbg[cmd]);
            bleLog(RAD_BLE_API, "        |-- EXT_CMD: " + extCmdDbg[ext_cmd]);
            bleLog(RAD_BLE_API, "        |-- LENGTH:  " + length);
            switch (cmd) {
                case BLE_COM_CMD_STATE: {
                    int state = value[4];
                    bleLog(RAD_BLE_API, "        |-- STATE:   " + bleStateDbg[state]);
                    switch (state) {
                        case BLE_STATE_IDLE: {
                            numPackets = 0;
                            gpsLocationIsValid = false;
                            setState(RAD_STATE_IDLE);
                            if (actualPpmm != pointsPerMilliMeter || actualAlgo != processingAlgorithm) {
                                // Update PPMM and Algo
                                setState(RAD_STATE_GET_ALGO_IDLE_UPDATE);
                                write(COM_CMD_CHARACTERISTIC, new byte[] {
                                        (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                        (byte)BLE_EXT_CMD_READ,
                                        (byte)0,
                                        (byte)0
                                    });
                            }
                            break;
                        }

                        case BLE_STATE_MEAS: {
                            gpsLocationIsValid = true;
                            // Only update to RAD_STATE_MEAS if reading was initiated from probe
                            if (radState == RAD_STATE_IDLE) {
                                // @todo Probe Bug: Start/Stop to quickly causes freeze. Remove try-catch and sleep if resolved.
                                try {
                                    // Pause for half a second so the probe doesn't freeze
                                    Thread.sleep(500);
                                } catch (Exception e) { };

                                setState(RAD_STATE_MEAS);
                            }
                            break;
                        }

                        case BLE_STATE_PROCESSING: {
                            gpsLocationIsValid = true;
                            setState(RAD_STATE_PROCESSING);
                            break;
                        }

                        case BLE_STATE_READY: {
                            switch (radState) {
                                case RAD_STATE_SEND_DATA: {
                                    if (dataBuffer.size() > (numPackets - (numPackets * PACKET_LOSS_PERCENT_THRESH)))
                                    {
                                        bleLog(RAD_BLE_API, "        |-- Completed Read: " + dataBuffer.size() + " of " + numPackets);

                                        String probeFileName = new SimpleDateFormat("yyyy-MM-dd--HHmmss").format(new Date()) + ".csv";
                                        String probeFilePath = dataPath + File.separator + "probe_readings" + File.separator + probeFileName;
                                        String location = gpsLocationIsValid ? gpsLocation : "N/A";
                                        bleLog(RAD_BLE_API, "File: " + probeFilePath);

                                        try {
                                            // Ensure that we are correctly sorted by sequence
                                            Collections.sort(dataBuffer, new Comparator<String>() {
                                                    @Override
                                                    public int compare(String s1, String s2) {
                                                        int sequenceNum1 = Integer.parseInt(s1.split(",")[0]);
                                                        int sequenceNum2 = Integer.parseInt(s2.split(",")[0]);
                                                        if (sequenceNum1 < sequenceNum2) {
                                                            return -1;
                                                        }
                                                        else if (sequenceNum1 > sequenceNum2) {
                                                            return 1;
                                                        }
                                                        else {
                                                            return 0;
                                                        }
                                                    }
                                                });

                                            FileWriter fileWriter = new FileWriter(probeFilePath);
                                            fileWriter.write("\"VERSION=APP_BETA_FMT_2\"\n");
                                            bleLog("\"VERSION=APP_BETA_FMT_2\"");
                                            fileWriter.write("\"PROBE NAME=" + mGatt.getDevice().getName() + "\"\n");
                                            bleLog("\"PROBE NAME=" + mGatt.getDevice().getName() + "\"");
                                            fileWriter.write("\"LOCATION=" + location + "\"\n");
                                            bleLog("\"LOCATION=" + location + "\"");
                                            fileWriter.write("\"APP REVISION=" + appRevision + "\"\n");
                                            bleLog("\"APP REVISION=" + appRevision + "\"");
                                            fileWriter.write("\"MANUFACTURER NAME=" + getManufacturerName() + "\"\n");
                                            bleLog("\"MANUFACTURER NAME=" + getManufacturerName() + "\"");
                                            fileWriter.write("\"HARDWARE REVISION=" + getHardwareRevision() + "\"\n");
                                            bleLog("\"HARDWARE REVISION=" + getHardwareRevision() + "\"");
                                            fileWriter.write("\"FIRMWARE REVISION=" + getFirmwareRevision() + "\"\n");
                                            bleLog("\"FIRMWARE REVISION=" + getFirmwareRevision() + "\"");
                                            fileWriter.write("\"MODEL NUMBER=" + getModelNumber() + "\"\n");
                                            bleLog("\"MODEL NUMBER=" + getModelNumber() + "\"");
                                            fileWriter.write("\"SERIAL NUMBER=" + getSerialNumber() + "\"\n");
                                            bleLog("\"SERIAL NUMBER=" + getSerialNumber() + "\"");
                                            fileWriter.write("\"POINTS PER MILLIMETER=" + actualPpmm + "\"\n");
                                            bleLog("\"POINTS PER MILLIMETER=" + actualPpmm + "\"");
                                            fileWriter.write("\"PROCESSING ALGORITHM=" + actualAlgo + "\"\n");
                                            bleLog("\"PROCESSING ALGORITHM=" + actualAlgo + "\"");
                                            fileWriter.write("SAMPLE,DEPTH,SENSOR 1,SENSOR 2,SENSOR 3,SENSOR 4\n");
                                            bleLog("SAMPLE,DEPTH,SENSOR 1,SENSOR 2,SENSOR 3,SENSOR 4");
                                            for (String str: dataBuffer) {
                                                Log.i(RAD_BLE_API, str);
                                                fileWriter.write(str + "\n");
                                            }

                                            fileWriter.close();
                                            bleLog(RAD_BLE_API, "" + dataBuffer.size() + " of " + numPackets);
                                            bleLog(RAD_BLE_API, "LOCATION: " + location);
                                            // Set the new probe file flag
                                            newProbeFile = true;

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        setState(RAD_STATE_RESET);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_RESET,
                                                (byte)BLE_EXT_CMD_WRITE,
                                                (byte)0,
                                                (byte)0
                                            });

                                        break;
                                    }
                                    /* Disable packet retry until ready and verified.
                                    else if (dataBuffer.size() > 0) {
                                        // Ensure that we are correctly sorted by sequence
                                        Collections.sort(dataBuffer, new Comparator<String>() {
                                                @Override
                                                public int compare(String s1, String s2) {
                                                    int sequenceNum1 = Integer.parseInt(s1.split(",")[0]);
                                                    int sequenceNum2 = Integer.parseInt(s2.split(",")[0]);
                                                    if (sequenceNum1 < sequenceNum2) {
                                                        return -1;
                                                    }
                                                    else if (sequenceNum1 > sequenceNum2) {
                                                        return 1;
                                                    }
                                                    else {
                                                        return 0;
                                                    }
                                                }
                                            });

                                        int stopIndex  = 0;
                                        int startIndex = 0;
                                        for (; startIndex < dataBuffer.size(); startIndex++) {
                                            stopIndex = Integer.parseInt(dataBuffer.get(startIndex).split(",")[0]);
                                            if (startIndex != stopIndex) {
                                                stopIndex--;
                                                break;
                                            }
                                            else if (startIndex == (dataBuffer.size() - 1)) {
                                                stopIndex = numPackets - 1;
                                                break;
                                            }
                                        }

                                        packetRetryRequest = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
                                        packetRetryRequest.put((byte)BLE_COM_CMD_PACKET_REQUEST);
                                        packetRetryRequest.put((byte)BLE_EXT_CMD_WRITE);
                                        packetRetryRequest.put((byte)0);
                                        packetRetryRequest.put((byte)8);
                                        packetRetryRequest.putInt(startIndex);
                                        packetRetryRequest.putInt(stopIndex);

                                        setState(RAD_STATE_PACKET_REQUEST);
                                        write(COM_CMD_CHARACTERISTIC, packetRetryRequest.array());
                                        break;
                                    }
                                    */

                                    // Intentional fall through
                                }
                                case RAD_STATE_GET_STATE:
                                case RAD_STATE_PROCESSING: {
                                    bleLog(RAD_BLE_API, "        |-- Starting New Read");
                                    if (actualPpmm < 0 || actualAlgo < 0) {
                                        // Get PPMM and Algo
                                        setState(RAD_STATE_GET_ALGO_NUM_DATA);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                                (byte)BLE_EXT_CMD_READ,
                                                (byte)0,
                                                (byte)0
                                            });
                                    }
                                    else {
                                        setState(RAD_STATE_GET_NUM_DATA);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_NUM_DATA,
                                                (byte)BLE_EXT_CMD_READ,
                                                (byte)0,
                                                (byte)0
                                            });
                                    }
                                    break;
                                }

                                case RAD_STATE_FORCE_RESET: {
                                    setState(RAD_STATE_RESET);
                                    write(COM_CMD_CHARACTERISTIC, new byte[] {
                                            (byte)BLE_COM_CMD_RESET,
                                            (byte)BLE_EXT_CMD_WRITE,
                                            (byte)0,
                                            (byte)0
                                        });

                                    break;
                                }

                                default: {
                                    bleLog(RAD_BLE_API, "(!)-- INVALID RESPONSE FOR STATE");
                                    break;
                                }
                            }
                            break;
                        }

                        case BLE_STATE_SEND_DATA: {
                            setState(RAD_STATE_SEND_DATA);
                            break;
                        }

                        case BLE_STATE_PREPARE: {
                            gpsLocationIsValid = false;
                            setState(RAD_STATE_PREPARE);
                            break;
                        }

                        case BLE_STATE_ERROR: {
                            setState(RAD_STATE_GET_ERROR_CODE);
                            write(COM_CMD_CHARACTERISTIC, new byte[] {
                                    (byte)BLE_COM_CMD_ERROR_CODE,
                                    (byte)BLE_EXT_CMD_READ,
                                    (byte)0,
                                    (byte)0
                                });
                            break;
                        }

                        default: {
                            bleLog(RAD_BLE_API, "(!)-- INVALID STATE");
                            break;
                        }
                    }
                    break;
                }

                case BLE_COM_CMD_MEAS_START: {
                    switch (radState) {
                        case RAD_STATE_MEAS_START: {
                            if (ext_cmd == BLE_EXT_CMD_ACK) {
                                // @todo Probe Bug: Start/Stop to quickly causes freeze. Remove try-catch and sleep if resolved.
                                try {
                                    // Pause for half a second so the probe doesn't freeze
                                    Thread.sleep(500);
                                } catch (Exception e) { };

                                stateRetryCount = 0;
                                setState(RAD_STATE_MEAS);
                            }
                            else if (stateRetryCount == MAX_RETRY_COUNT) {
                                stateRetryCount = 0;
                                bleLog(RAD_BLE_API, "        |-- State Retry Count Max Hit");
                                setState(RAD_STATE_IDLE);
                            }
                            else {
                                stateRetryCount++;
                                write(COM_CMD_CHARACTERISTIC, new byte[] {
                                        (byte)BLE_COM_CMD_MEAS_START,
                                        (byte)BLE_EXT_CMD_WRITE,
                                        (byte)0,
                                        (byte)0
                                    });
                            }
                            break;
                        }
                    }
                    break;
                }

                case BLE_COM_CMD_MEAS_STOP:
                case BLE_COM_CMD_RESET:
                case BLE_COM_CMD_SEND_DATA:
                case BLE_COM_CMD_PACKET_REQUEST: {
                    // All ignored for now
                    break;
                }

                case BLE_COM_CMD_PROC_ALGORITHM: {
                    switch (radState) {
                        case RAD_STATE_GET_ALGO_IDLE_UPDATE:
                        case RAD_STATE_GET_ALGO_NUM_DATA:
                        case RAD_STATE_GET_ALGO_MEAS_START: {
                            if (ext_cmd == BLE_EXT_CMD_RESPONSE) {
                                ByteBuffer bb = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
                                actualAlgo = (int)bb.get(4);
                                if ((actualAlgo != processingAlgorithm) && (radState != RAD_STATE_GET_ALGO_NUM_DATA)) {
                                    if (stateRetryCount == MAX_RETRY_COUNT) {
                                        stateRetryCount = 0;
                                        bleLog(RAD_BLE_API, "        |-- State Retry Count Max Hit");
                                        setState(RAD_STATE_IDLE);
                                    }
                                    else {
                                        stateRetryCount++;
                                        bleLog(RAD_BLE_API, "        |-- Desired Algo (" + processingAlgorithm + ") != Current Algo (" + actualAlgo + ")");
                                        setState(radState + 1);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                                (byte)BLE_EXT_CMD_WRITE,
                                                (byte)0,
                                                (byte)1,
                                                (byte)processingAlgorithm
                                            });
                                    }
                                }
                                else if (actualAlgo == processingAlgorithm || radState == RAD_STATE_GET_ALGO_NUM_DATA) {
                                    stateRetryCount = 0;
                                    bleLog(RAD_BLE_API, "        |-- Desired Algo (" + processingAlgorithm + "), Current Algo (" + actualAlgo + ")");
                                    setState(radState + 2);
                                    write(COM_CMD_CHARACTERISTIC, new byte[] {
                                            (byte)BLE_COM_CMD_MEAS_PPMM_SETTING,
                                            (byte)BLE_EXT_CMD_READ,
                                            (byte)0,
                                            (byte)0
                                        });
                                }
                            }
                            else {
                                // Expected ext_cmd is RESPONSE (3)
                                bleLog(RAD_BLE_API, "(!)-- Expected EXT_CMD (3) != Actual EXT_CMD (" + ext_cmd + ")");
                            }
                            break;
                        }

                        case RAD_STATE_SET_ALGO_IDLE_UPDATE:
                        case RAD_STATE_SET_ALGO_MEAS_START: {
                            // Desire ext_cmd of ACK (5)
                            bleLog(RAD_BLE_API, "        |-- Desire EXT_CMD (5), got EXT_CMD of (" + ext_cmd + ") .. Ignored");
                            setState(radState - 1);
                            write(COM_CMD_CHARACTERISTIC, new byte[] {
                                    (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                    (byte)BLE_EXT_CMD_READ,
                                    (byte)0,
                                    (byte)0,
                                });
                            break;
                        }

                        default: {
                            bleLog(RAD_BLE_API, "(!)-- INVALID RESPONSE FOR STATE");
                            break;
                        }
                    }
                    break;
                }

                case BLE_COM_CMD_MEAS_PPMM_SETTING: {
                    switch (radState) {
                        case RAD_STATE_GET_PPMM_IDLE_UPDATE:
                        case RAD_STATE_GET_PPMM_NUM_DATA:
                        case RAD_STATE_GET_PPMM_MEAS_START: {
                            if (ext_cmd == BLE_EXT_CMD_RESPONSE) {
                                ByteBuffer bb = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
                                actualPpmm = (int)bb.get(4);
                                if ((actualPpmm != pointsPerMilliMeter) && (radState != RAD_STATE_GET_PPMM_NUM_DATA)) {
                                    if (stateRetryCount == MAX_RETRY_COUNT) {
                                        stateRetryCount = 0;
                                        bleLog(RAD_BLE_API, "        |-- State Retry Count Max Hit");
                                        setState(RAD_STATE_IDLE);
                                    }
                                    else {
                                        stateRetryCount++;
                                        bleLog(RAD_BLE_API, "        |-- Desired PPMM (" + pointsPerMilliMeter + ") != Current PPMM (" + actualPpmm + ")");
                                        setState(radState + 1);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_MEAS_PPMM_SETTING,
                                                (byte)BLE_EXT_CMD_WRITE,
                                                (byte)0,
                                                (byte)1,
                                                (byte)pointsPerMilliMeter
                                            });
                                    }
                                }
                                else if ((actualPpmm == pointsPerMilliMeter) || (radState == RAD_STATE_GET_PPMM_NUM_DATA)) {
                                    stateRetryCount = 0;
                                    bleLog(RAD_BLE_API, "        |-- Desired PPMM (" + pointsPerMilliMeter + "), Current PPMM (" + actualPpmm + ")");
                                    if (radState == RAD_STATE_GET_PPMM_MEAS_START) {
                                        setState(RAD_STATE_MEAS_START);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_MEAS_START,
                                                (byte)BLE_EXT_CMD_WRITE,
                                                (byte)0,
                                                (byte)0
                                            });
                                    }
                                    else if (radState == RAD_STATE_GET_PPMM_NUM_DATA) {
                                        setState(RAD_STATE_GET_NUM_DATA);
                                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                                (byte)BLE_COM_CMD_NUM_DATA,
                                                (byte)BLE_EXT_CMD_READ,
                                                (byte)0,
                                                (byte)0
                                            });
                                    }
                                    else {
                                        setState(RAD_STATE_IDLE);
                                    }
                                }
                            }
                            else {
                                // Expected ext_cmd is RESPONSE (3)
                                bleLog(RAD_BLE_API, "(!)-- Expected EXT_CMD (3) != Actual EXT_CMD (" + ext_cmd + ")");
                            }
                            break;
                        }

                        case RAD_STATE_SET_PPMM_IDLE_UPDATE:
                        case RAD_STATE_SET_PPMM_MEAS_START: {
                            // Desire ext_cmd of ACK (5)
                            bleLog(RAD_BLE_API, "        |-- Desire EXT_CMD (5), got EXT_CMD of (" + ext_cmd + ") .. Ignored");
                            setState(radState - 1);
                            write(COM_CMD_CHARACTERISTIC, new byte[] {
                                    (byte)BLE_COM_CMD_MEAS_PPMM_SETTING,
                                    (byte)BLE_EXT_CMD_READ,
                                    (byte)0,
                                    (byte)0,
                                });
                            break;
                        }

                        default: {
                            bleLog(RAD_BLE_API, "(!)-- INVALID RESPONSE FOR STATE");
                            break;
                        }
                    }
                    break;
                }

                case BLE_COM_CMD_NUM_DATA: {
                    if (radState == RAD_STATE_GET_NUM_DATA) {
                        if (ext_cmd == BLE_EXT_CMD_RESPONSE) {
                            numPackets = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
                            bleLog(RAD_BLE_API, "        |-- Num Packets: " + numPackets);

                            if (numPackets > 0) {
                                dataBuffer = new ArrayList<String>();
                                setState(RAD_STATE_SEND_DATA);
                                write(COM_CMD_CHARACTERISTIC, new byte[] {
                                        (byte)BLE_COM_CMD_SEND_DATA,
                                        (byte)BLE_EXT_CMD_WRITE,
                                        (byte)0,
                                        (byte)0
                                    });
                            }
                            else {
                                setState(RAD_STATE_RESET);
                                write(COM_CMD_CHARACTERISTIC, new byte[] {
                                        (byte)BLE_COM_CMD_RESET,
                                        (byte)BLE_EXT_CMD_WRITE,
                                        (byte)0,
                                        (byte)0
                                    });
                            }
                        }
                        else {
                            // Expected ext_cmd is RESPONSE (3)
                            bleLog(RAD_BLE_API, "(!)-- Expected EXT_CMD (3) != Actual EXT_CMD (" + ext_cmd + ")");
                        }
                    }
                    else {
                        bleLog(RAD_BLE_API, "(!)-- INVALID RESPONSE FOR STATE");
                    }
                    break;
                }

                case BLE_COM_CMD_ERROR_CODE: {
                    if (radState == RAD_STATE_GET_ERROR_CODE) {
                        if (ext_cmd == BLE_EXT_CMD_RESPONSE) {
                            int errorCode = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
                            bleLog(RAD_BLE_API, "        |-- Error Code: " + errorCode);
                            setState(RAD_STATE_RESET);
                            write(COM_CMD_CHARACTERISTIC, new byte[] {
                                    (byte)BLE_COM_CMD_RESET,
                                    (byte)BLE_EXT_CMD_WRITE,
                                    (byte)0,
                                    (byte)0
                                });
                        }
                        else {
                            // Expected ext_cmd is RESPONSE (3)
                            bleLog(RAD_BLE_API, "(!)-- Expected EXT_CMD (3) != Actual EXT_CMD (" + ext_cmd + ")");
                        }
                    }
                    else {
                        bleLog(RAD_BLE_API, "(!)-- INVALID RESPONSE FOR STATE");
                    }
                    break;
                }

                default: {
                    bleLog(RAD_BLE_API, "(!)-- ERROR: INVALID PACKET RECEIVED");
                    break;
                }
            }
        }
        else if ((uuidCharacteristic.compareTo(DATA_1_CHARACTERISTIC) == 0) ||
                 (uuidCharacteristic.compareTo(DATA_2_CHARACTERISTIC) == 0) ||
                 (uuidCharacteristic.compareTo(DATA_3_CHARACTERISTIC) == 0) ||
                 (uuidCharacteristic.compareTo(DATA_4_CHARACTERISTIC) == 0)) {
            if (numPackets > 0) {
                ByteBuffer bb = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN);
                int sequence = (int)bb.getInt();
                Log.i(RAD_BLE_API, "        |-- Packet: " + sequence + ", Buf: " + dataBuffer.size() + " of " + (numPackets - 1));
                String data = "" + sequence + "," + (int)bb.getInt() + "," +
                    (int)bb.getChar() + "," + (int)bb.getChar() + "," + (int)bb.getChar() + "," + (int)bb.getChar();
                dataBuffer.add(data);
            }
            else {
                Log.i(RAD_BLE_API, "        |-- Packet REJECTED");
                setState(RAD_STATE_FORCE_RESET);
                write(COM_CMD_CHARACTERISTIC, new byte[] {
                        (byte)BLE_COM_CMD_RESET,
                        (byte)BLE_EXT_CMD_WRITE,
                        (byte)0,
                        (byte)0
                    });
            }
        }
        else if (uuidCharacteristic.compareTo(BATTERY_LEVEL_CHARACTERISTIC) == 0) {
            batteryLevelValue = value[0] + " %";
        }
        else {
            bleLog(RAD_BLE_API, "characteristicChangedCb - IGNORE");
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //                              BEGIN PRIVATE BLE INTERFACE SECTION                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    // Note: It is advised to keep the below functionality out of the python
    // pyjnius layer, and instead make the necessary changes within the java
    // file. This is mostly due to the thread synchronization that needs to
    // occur between the BLE requests and responses.

    // The BLE profile object
    private BluetoothGatt mGatt;
    // BLE service handles
    private BluetoothGattService radService;
    private BluetoothGattService batteryService;
    private BluetoothGattService devInfoService;
    // The BLE adapter and scanner objects
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bleScanner;
    // Android context
    private Context mContext;
    // The return value array from the last BLE read
    private byte[] readValue;
    // The return status of the last BLE write
    private int writeStatus;
    // The probe read data buffer
    private ArrayList<String> dataBuffer = new ArrayList<String>();

    // Flag indicating a BLE response is pending
    private boolean respPending = false;
    // Flag indicating a new probe file was written
    private boolean newProbeFile = false;
    // Object for thread lock synchronizatoin
    private final Object lock = new Object();
    // Conversion array for debug printouts of byte value arrays
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    // The RAD BLE main interface adb tag string
    private final String RAD_BLE_API = "RAD_BLE_ITF_mainIf";
    // The packet retry request byte buffer
    private ByteBuffer packetRetryRequest;

    private final String BATTERY_LEVEL_DEFAULT_VALUE     = "XX %";
    private final String HARDWARE_REV_DEFAULT_VALUE      = "XX.X";
    private final String FIRMWARE_REV_DEFAULT_VALUE      = "XX.X";
    private final String MODEL_NUMBER_DEFAULT_VALUE      = "XXXX.XX.XXXX";
    private final String SERIAL_NUMBER_DEFAULT_VALUE     = "XXXX.XX.XXXX";
    private final String MANUFACTURER_NAME_DEFAULT_VALUE = "XXXX-XX-XXXX";

    private String batteryLevelValue = BATTERY_LEVEL_DEFAULT_VALUE;
    private String hardwareRevValue  = HARDWARE_REV_DEFAULT_VALUE;
    private String firmwareRevValue  = FIRMWARE_REV_DEFAULT_VALUE;
    private String modelNumberValue  = MODEL_NUMBER_DEFAULT_VALUE;
    private String serialNumberValue = SERIAL_NUMBER_DEFAULT_VALUE;
    private String manufacturerName  = MANUFACTURER_NAME_DEFAULT_VALUE;

    private boolean gpsLocationIsValid = false;
    private String gpsLocation = "N/A";

    private Timer timer;
    // Early disconnect detection
    class FakeDisconnectAlarm extends TimerTask {
        public void run() {
            bleLog(RAD_BLE_API, "\nX-- FakeDisconnectAlarm");
            timer.cancel();

            if (retryCount < MAX_RETRY_COUNT) {
                bleLog(RAD_BLE_API, "Retry: " + stateDbg[radState]);
                switch (radState) {
                    case RAD_STATE_GET_ALGO_NUM_DATA:
                    case RAD_STATE_GET_ALGO_IDLE_UPDATE:
                    case RAD_STATE_GET_ALGO_MEAS_START: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                (byte)BLE_EXT_CMD_READ,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_SET_ALGO_IDLE_UPDATE:
                    case RAD_STATE_SET_ALGO_MEAS_START: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_PROC_ALGORITHM,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)1,
                                (byte)processingAlgorithm
                            });
                        break;
                    }

                    case RAD_STATE_GET_PPMM_NUM_DATA:
                    case RAD_STATE_GET_PPMM_IDLE_UPDATE:
                    case RAD_STATE_GET_PPMM_MEAS_START: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_MEAS_PPMM_SETTING,
                                (byte)BLE_EXT_CMD_READ,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_SET_PPMM_IDLE_UPDATE:
                    case RAD_STATE_SET_PPMM_MEAS_START: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_MEAS_PPMM_SETTING,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)1,
                                (byte)pointsPerMilliMeter
                            });
                        break;
                    }

                    case RAD_STATE_MEAS_START: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_MEAS_START,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_MEAS_STOP: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_MEAS_STOP,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_GET_NUM_DATA: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_NUM_DATA,
                                (byte)BLE_EXT_CMD_READ,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_FORCE_RESET:
                    case RAD_STATE_RESET: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_RESET,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_SEND_DATA: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_SEND_DATA,
                                (byte)BLE_EXT_CMD_WRITE,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    case RAD_STATE_PACKET_REQUEST: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, packetRetryRequest.array());
                        break;
                    }

                    case RAD_STATE_GET_ERROR_CODE: {
                        retryCount++;
                        write(COM_CMD_CHARACTERISTIC, new byte[] {
                                (byte)BLE_COM_CMD_ERROR_CODE,
                                (byte)BLE_EXT_CMD_READ,
                                (byte)0,
                                (byte)0
                            });
                        break;
                    }

                    default: {
                        bleLog(RAD_BLE_API, "(!)-- INVALID RETRY STATE");
                    }
                }
            }
            else if (radState != RAD_STATE_DISCONNECTED)
            {
                retryCount = 0;
                stateRetryCount = 0;
                bleLog(RAD_BLE_API, "|-- Retry: Max Attempts Hit");
                setState(RAD_STATE_FAKE_DISCONNECTED);
            }
        }
    }

    // Start the fake disconnect alarm
    private void startFakeDisconnectAlarm() {
        // Schedule the alarm for 3 seconds to ensure we dont hang
        timer = new Timer();
        timer.schedule(new FakeDisconnectAlarm(), 3000);
        bleLog(RAD_BLE_API, "|-- startFakeDisconnectAlarm");
    }

    private void stopFakeDisconnectAlarm(boolean shifted) {
        timer.cancel();
        if (shifted) {
            bleLog(RAD_BLE_API, "    |-- stopFakeDisconnectAlarm");
        }
        else {
            bleLog(RAD_BLE_API, "|-- stopFakeDisconnectAlarm");
        }
    }

    // Stop the fake disconnect alarm
    private void stopFakeDisconnectAlarm() {
        stopFakeDisconnectAlarm(false);
    }

    // Simple debug function that converts a byte array into a string representation
    private String bytesToHex(byte[] bytes) {
        if (bytes != null) {
            String byteStr = "";
            for ( int j = 0; j < bytes.length; j++ ) {
                int v = bytes[j] & 0xFF;
                char[] hexChars = new char[2];
                hexChars[0] = hexArray[v >>> 4];
                hexChars[1] = hexArray[v & 0x0F];
                byteStr += "\\x" + new String(hexChars);
            }
            return byteStr;
        }
        return "";
    }

    // Sets the current RAD state
    private void setState(int state) {
        if (state != radState) {
            retryCount = 0;
        }
        if (radState != RAD_STATE_FORCE_RESET || state == RAD_STATE_IDLE) {
            bleLog(RAD_BLE_API, "\nState Update: " + stateDbg[radState] + " -> " + stateDbg[state]);
            radState = state;
        }
        else if ((state >= RAD_STATE_RESET || state < RAD_STATE_IDLE) && state != radState) {
            bleLog(RAD_BLE_API, "\nState Update: " + stateDbg[radState] + " -> " + stateDbg[state]);
            radState = state;
        }
        else {
            bleLog(RAD_BLE_API, "\nState Update: " + stateDbg[radState] + " -> SKIPPED: " + stateDbg[state]);
        }
    }

    // The BLE scan callback implementation.
    private final ScanCallback mScanCb = new ScanCallback() {

            private final String SCAN_CB_TAG = "RAD_BLE_ITF_scanCb";

            private boolean connect(BluetoothDevice mDevice) {
                boolean result = true;
                try {
                    if (radState == RAD_STATE_SCANNING)
                    {
                        if (mDevice.getName().substring(0, 3).equals("RAD")) {
                            bleLog(SCAN_CB_TAG, "|   |-- RAD Probe Found!");
                            bleScanner.stopScan(mScanCb);
                            mGatt = mDevice.connectGatt(mContext, false, mGattCb);
                            batteryLevelValue = read(BATTERY_LEVEL_CHARACTERISTIC)[0] + " %";
                            hardwareRevValue  = new String(read(HARDWARE_REVISION_STRING_CHARACTERISTIC)).replaceAll("\\x00", "");
                            firmwareRevValue  = new String(read(FIRMWARE_REVISION_STRING_CHARACTERISTIC)).replaceAll("\\x00", "");
                            modelNumberValue  = new String(read(MODEL_NUMBER_STRING_CHARACTERISTIC)).replaceAll("\\x00", "");
                            serialNumberValue = new String(read(SERIAL_NUMBER_STRING_CHARACTERISTIC)).replaceAll("\\x00", "");
                            manufacturerName  = new String(read(MANUFACTURER_NAME_STRING_CHARACTERISTIC)).replaceAll("\\x00", "");
                            setNotification(BATTERY_LEVEL_CHARACTERISTIC, true);
                            setNotification(COM_CMD_CHARACTERISTIC, true);
                            setNotification(DATA_1_CHARACTERISTIC, true);
                            setNotification(DATA_2_CHARACTERISTIC, true);
                            setNotification(DATA_3_CHARACTERISTIC, true);
                            setNotification(DATA_4_CHARACTERISTIC, true);

                            if (radState != RAD_STATE_FORCE_RESET) {
                                setState(RAD_STATE_GET_STATE);
                                write(COM_CMD_CHARACTERISTIC, new byte[] {
                                        (byte)BLE_COM_CMD_STATE,
                                        (byte)BLE_EXT_CMD_READ,
                                        (byte)0,
                                        (byte)0
                                    });
                            }
                        }
                        else {
                            bleLog(SCAN_CB_TAG, "|   |-- Skipped: " + mDevice.getName());
                        }
                    }
                    else {
                        bleLog(SCAN_CB_TAG, "|   |-- RAD Probe Already Connected.");
                    }
                    result = isConnected();
                } catch(NullPointerException e) {
                    bleLog(SCAN_CB_TAG, "|   |-- Error Occurred (null device received).");
                }

                return result;
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                bleLog(SCAN_CB_TAG, "|-- onScanResult");
                connect(result.getDevice());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                bleLog(SCAN_CB_TAG, "|-- onBatchScanResults");
                for (ScanResult result : results) {
                    if (connect(result.getDevice())) {
                        break;
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                bleLog(SCAN_CB_TAG, "|-- onScanFailed");
            }
        };

    // The BLE device interaction callback implementation
    private final BluetoothGattCallback mGattCb = new BluetoothGattCallback() {

            private final String GATT_CB_TAG = "RAD_BLE_ITF_gattCb";

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "X-- onConnectionStateChange");

                    switch (newState) {

                        case BluetoothProfile.STATE_CONNECTED: {
                            bleLog(GATT_CB_TAG, "    |-- Connected to GATT server.");
                            bleLog(GATT_CB_TAG, "    |-- Scan Provided Services.");
                            gatt.discoverServices();
                            respPending = true;
                            break;
                        }

                        case BluetoothProfile.STATE_DISCONNECTED: {
                            bleLog(GATT_CB_TAG, "    |-- Disconnected from GATT server.");
                            stopFakeDisconnectAlarm();
                            bleProbeServicesReset();
                            gatt.close();
                            respPending = false;
                            retryCount = 0;
                            stateRetryCount = 0;
                            if (radState == RAD_STATE_FAKE_SCANNING) {
                                setState(RAD_STATE_DISCONNECTED);
                                startScan();
                            }
                            else {
                                setState(RAD_STATE_DISCONNECTED);
                            }
                            lock.notify();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "|-- onCharacteristicRead");
                    stopFakeDisconnectAlarm();

                    UUID uuid = characteristic.getUuid();
                    readValue = characteristic.getValue();
                    bleLog(GATT_CB_TAG, "|   |-- UUID: " + uuid.toString());
                    bleLog(GATT_CB_TAG, "|   |-- Characteristic Value: " + bytesToHex(readValue));

                    respPending = false;
                    lock.notify();
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "|-- onCharacteristicWrite");

                    UUID uuid = characteristic.getUuid();
                    writeStatus = status;
                    bleLog(GATT_CB_TAG, "|   |-- UUID: " + uuid.toString());
                    bleLog(GATT_CB_TAG, "|   |-- Characteristic Status: " + status);

                    respPending = false;
                    lock.notify();
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "|-- onDescriptorRead");
                    stopFakeDisconnectAlarm();

                    UUID uuid = descriptor.getUuid();
                    readValue = descriptor.getValue();
                    bleLog(GATT_CB_TAG, "|   |-- UUID: " + uuid.toString());
                    bleLog(GATT_CB_TAG, "|   |-- Characteristic Value: " + bytesToHex(readValue));

                    respPending = false;
                    lock.notify();
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "|-- onDescriptorWrite");
                    stopFakeDisconnectAlarm();

                    UUID uuid = descriptor.getUuid();
                    writeStatus = status;
                    bleLog(GATT_CB_TAG, "|   |-- UUID: " + uuid.toString());
                    bleLog(GATT_CB_TAG, "|   |-- Characteristic Status: " + status);

                    respPending = false;
                    lock.notify();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.i(GATT_CB_TAG, "X-- onCharacteristicChanged");

                UUID uuid = characteristic.getUuid();
                byte[] value = characteristic.getValue();
                Log.i(GATT_CB_TAG, "    |-- UUID: " + uuid.toString());
                Log.i(GATT_CB_TAG, "    |-- Characteristic Value: " + bytesToHex(value));

                if ((uuid.compareTo(DATA_1_CHARACTERISTIC) != 0) &&
                    (uuid.compareTo(DATA_2_CHARACTERISTIC) != 0) &&
                    (uuid.compareTo(DATA_3_CHARACTERISTIC) != 0) &&
                    (uuid.compareTo(DATA_4_CHARACTERISTIC) != 0)) {
                    bleLog(GATT_CB_TAG, "X-- onCharacteristicChanged");
                    bleLog(GATT_CB_TAG, "    |-- UUID: " + uuid.toString());
                    bleLog(GATT_CB_TAG, "    |-- Characteristic Value: " + bytesToHex(value));
                }

                characteristicChangedCb(uuid, value);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                bleLog(GATT_CB_TAG, "|-- onReliableWriteCompleted");
                bleLog(GATT_CB_TAG, "|   |-- Characteristic Status: " + status);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                synchronized (lock) {
                    bleLog(GATT_CB_TAG, "X-- onServicesDiscovered");
                    bleLog(GATT_CB_TAG, "    |-- Characteristic Status: " + status);
                    bleProbeServicesInit(gatt);

                    respPending = false;
                    lock.notify();
                }
            }
        };

    // @brief Save the RAD probe BLE services to their appropriate handles
    //
    // @param gatt  The BLE gatt profile object
    private void bleProbeServicesInit(BluetoothGatt gatt) {
        radService     = gatt.getService(RAD_SENSOR_SERVICE);
        batteryService = gatt.getService(BATTERY_SERVICE);
        devInfoService = gatt.getService(DEVICE_INFORMATION_SERVICE);
    }

    // @brief Clear the saved RAD probe BLE services
    private void bleProbeServicesReset() {
        radService     = null;
        batteryService = null;
        devInfoService = null;
    }

    // @brief Check to see if all RAD probe BLE services have been found
    // @return true if the BLE services are matched, otherwise false
    private boolean bleProbeServicesFound() {
        return (radService != null     &&
                batteryService != null &&
                devInfoService != null);
    }

    // @brief Thread synchronization, where the caller waits for a response
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param lock  The synchronized lock object
    private void waitForResponse(Object lock) {
        respPending = true;
        while (respPending) {
            try {
                lock.wait();
            } catch(InterruptedException e) { }
        }
    }

    // @brief Thread synchronization, where the caller waits for the connection to complete
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param lock  The synchronized lock object
    private void waitServiceConnect(Object lock) {
        while (!bleProbeServicesFound()) {
            try {
                lock.wait();
            } catch(InterruptedException e) { }
        }
    }

    // @brief Provides a synchronous write characteristic, blocks until response is received.
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param characteristic  The BLE characteristic
    // @return The return status of the characteristic write
    private int syncWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
        mGatt.writeCharacteristic(characteristic);
        waitForResponse(lock);
        return writeStatus;
    }

    // @brief Provides a synchronous read characteristic, blocks until response is received.
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param characteristic  The BLE characteristic
    // @return The characteristic value as a byte array.
    private byte[] syncReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        mGatt.readCharacteristic(characteristic);
        waitForResponse(lock);
        return readValue;
    }

    // @brief Provides a synchronous write descriptor, blocks until response is received.
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param descriptor  The BLE descriptor
    // @return The return status of the descriptor write
    private int syncWriteDescriptor(BluetoothGattDescriptor descriptor) {
        mGatt.writeDescriptor(descriptor);
        waitForResponse(lock);
        return writeStatus;
    }

    // @brief Provides a synchronous write descriptor, blocks until response is received.
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param descriptor  The BLE descriptor
    // @return The descriptor value as a byte array.
    private byte[] syncReadDescriptor(BluetoothGattDescriptor descriptor) {
        mGatt.readDescriptor(descriptor);
        waitForResponse(lock);
        return readValue;
    }

    // @brief Provides a synchronous set notification, blocks until response is received.
    //
    // Note: The below method is intended to be called from within a synchronized block
    // on the lock object.
    //
    // @param characteristic  The BLE characteristic
    // @param enable          Set to true to enable notifications on characteristic changes, otherwise false
    // @return The return status of the descriptor write
    private int syncSetNotification(BluetoothGattCharacteristic characteristic, boolean enable) {
        mGatt.setCharacteristicNotification(characteristic, enable);

        // 0x2902 Client Characteristic Configuration Descriptor
        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return syncWriteDescriptor(descriptor);
    }

    /**
     * Show Alert function
     * Function Call
     * this.showAlert(" closeLock success ", callbackContext)
     */
    private void showAlert(String message, CallbackContext callbackContext) {
		if (callbackContext == null) {
			return;
		}

		if (message != null && message.length() > 0) {
			callbackContext.success(message);
		} else {
			callbackContext.error("Empty string !");
		}
	}
}
