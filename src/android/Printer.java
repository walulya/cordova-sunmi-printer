package pebuu.printer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.bean.WiFiConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.CpclFactory;
import com.rt.printerlibrary.cmd.EscCmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.cmd.ZplFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.connect.WiFiFactory;
import com.rt.printerlibrary.factory.printer.LabelPrinterFactory;
import com.rt.printerlibrary.factory.printer.PinPrinterFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.utils.FuncUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

/**
 * This class echoes a string called from JavaScript.
 */
public class Printer extends CordovaPlugin {
    private static final String LOG_TAG = "RTPrinter";

    @BaseEnum.ConnectType
    private int checkedConType = BaseEnum.CON_BLUETOOTH;
    BluetoothDevice currentBTDevice;
    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    //private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private PrinterInterface curPrinterInterface = null;
    private Object configObj;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            listBT(callbackContext);
            return true;
        }
        if (action.equals("list")) {
            listBT(callbackContext);
            return true;
        }
        if (action.equals("performAdd")) {
            int arg1 = args.getInt(0);
            int arg2 = args.getInt(1);
            /* Indicating success is failure is done by calling the appropriate method on the 
            callbackContext.*/
            int result = arg1 + arg2;
            callbackContext.success("result calculated in Java: " + result);
            return true;
        }
        return false;
    }


    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void setPrinterType(int type, CallbackContext callbackContext) {
        String errMsg = null;
        boolean set = true;
        switch(type){
            case BaseEnum.CMD_PIN:
                printerFactory = new PinPrinterFactory();

                rtPrinter = printerFactory.create();
                rtPrinter.setPrinterInterface(curPrinterInterface);
            break;
            case BaseEnum.CMD_ESC:
                printerFactory = new ThermalPrinterFactory();
                rtPrinter = printerFactory.create();
                rtPrinter.setPrinterInterface(curPrinterInterface);             
            break;
            case BaseEnum.CMD_TSC:
                printerFactory = new LabelPrinterFactory();
                rtPrinter = printerFactory.create();
                rtPrinter.setPrinterInterface(curPrinterInterface);
            break;
            case BaseEnum.CMD_CPCL:
                printerFactory = new LabelPrinterFactory();
                rtPrinter = printerFactory.create();
                rtPrinter.setPrinterInterface(curPrinterInterface);
            break;
            case BaseEnum.CMD_ZPL:
                printerFactory = new LabelPrinterFactory();
                rtPrinter = printerFactory.create();
                rtPrinter.setPrinterInterface(curPrinterInterface);
            break;
            default:
                errMsg = "Unknown printer type selected";
				callbackContext.error(errMsg);
                set = false;
            break;
        }
        if (set) {
            callbackContext.success("Printer type set successfully");
        }
    }

    private void getPrinterType( CallbackContext callbackContext) {
        callbackContext.success(checkedConType);
    }
    

    private void setConnectionType(int type, CallbackContext callbackContext) {
        String errMsg = null;
        boolean set = true;
        switch(type){
            case BaseEnum.CON_WIFI:
                checkedConType = BaseEnum.CON_WIFI;
            break;
            case BaseEnum.CON_BLUETOOTH:
                checkedConType = BaseEnum.CON_BLUETOOTH;
            break;
            case BaseEnum.CON_USB:
                checkedConType = BaseEnum.CON_USB;
            break;
            case BaseEnum.CON_COM:
                checkedConType = BaseEnum.CON_COM;
            break;
            default:
                errMsg = "Unknown connection type";
				callbackContext.error(errMsg);
                set = false;
            break;
        } 
        if (set){
            callbackContext.success("Connection type set successfully");
         }
    }
    private void getConnectionType( CallbackContext callbackContext) {
        callbackContext.success(checkedConType);
    }

    //This will return the array list of paired bluetooth printers
	void listBT(CallbackContext callbackContext) {
		BluetoothAdapter mBluetoothAdapter = null;
		String errMsg = null;
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				errMsg = "No bluetooth adapter available";
				//Log.e(LOG_TAG, errMsg);
				callbackContext.error(errMsg);
				return;
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
			}
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				JSONArray json = new JSONArray();
				for (BluetoothDevice device : pairedDevices) {
					
					Hashtable map = new Hashtable();
					map.put("type", device.getType());
					map.put("address", device.getAddress());
					map.put("name", device.getName());
					JSONObject jObj = new JSONObject(map);
					json.put(jObj);
					//json.put(device.getName());
				}
				callbackContext.success(json);
			} else {
				callbackContext.error("No Bluetooth Device Found");
			}
			//Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getName());
		} catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
	}

    boolean setBlueToothPrinter(String name, CallbackContext callbackContext) {
        BluetoothAdapter mBluetoothAdapter = null;
		String errMsg = null;
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Log.e(LOG_TAG, "No bluetooth adapter available");
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
			}
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					if (device.getName().equalsIgnoreCase(name)) {
						currentBTDevice = device;
                        configObj = new BluetoothEdrConfigBean(currentBTDevice);
                        callbackContext.success("Bluetooth Device Connected: " + currentBTDevice.getName());
						return true;
					}
				}
			}
			Log.d(LOG_TAG, "Bluetooth Device Found: " + currentBTDevice.getName());
		} catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

    private void doConnect(String tag, CallbackContext callbackContext) {
        String errMsg = null;

        switch (checkedConType) {
            case BaseEnum.CON_WIFI:
                //WiFiConfigBean wiFiConfigBean = (WiFiConfigBean) configObj;
                //connectWifi(wiFiConfigBean);
                break;
            case BaseEnum.CON_BLUETOOTH:
                //TimeRecordUtils.record("RT连接start：", System.currentTimeMillis());
                BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
                connectBluetooth(bluetoothEdrConfigBean, callbackContext);
                break;
            case BaseEnum.CON_USB:
                //UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
                //connectUSB(usbConfigBean);
                break;
            default:
                errMsg = "No printer type selected";
				callbackContext.error(errMsg);
                            
                break;
        }

    }

    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean, CallbackContext callbackContext) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
            callbackContext.success("Printer connected successfully");
        } catch (Exception e) {
            String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
        } finally {

        }
    }
}
