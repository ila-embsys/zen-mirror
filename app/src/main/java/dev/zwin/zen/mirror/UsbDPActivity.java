package dev.zwin.zen.mirror;

import android.app.NativeActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.SystemClock;

import java.util.HashMap;

public class UsbDPActivity extends NativeActivity {
    
    private static final String ACTION_USB_PERMISSION = ".USB_PERMISSION";

    private final BroadcastReceiver usbDetachReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
                finishAndRemoveTask();
        }
    };

    protected void configureUsbDetach() {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(getBaseContext(), 0,
                new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = new HashMap<>();

        while (deviceList.values().size() == 0){
            deviceList = usbManager.getDeviceList();
            SystemClock.sleep(500);
        }

        for (UsbDevice usbDevice : deviceList.values()) {
            usbManager.requestPermission(usbDevice, mPermissionIntent);
            while (!usbManager.hasPermission(usbDevice))
            {
                SystemClock.sleep(500);
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbDetachReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        configureUsbDetach();

        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
    }


}
