
/**
 * @module Camera
 */
module.exports = {
     /**
   * @description
   * Defines the output format of `Camera.getPicture` call.
   * .
   *
   * @enum {number}
   */
  PrinterType:{
    /** Choose image from the device's photo library (same as SAVEDPHOTOALBUM for Android) */
    CMD_ESC: 1, 
    CMD_TSC: 2, 
    CMD_CPCL: 3, 
    CMD_ZPL: 4, 
    CMD_PIN: 5
  },
  ConnectionType:{
    /** Choose image from the device's photo library (same as SAVEDPHOTOALBUM for Android) */
    CON_BLUETOOTH: 1, 
    CON_BLUETOOTH_BLE: 2, 
    CON_WIFI: 3, 
    CON_USB: 4, 
    CON_COM: 5
  }
};