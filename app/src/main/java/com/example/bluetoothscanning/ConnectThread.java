package com.example.bluetoothscanning;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectThread extends Thread
{
    private final BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter;

    private final UUID WELL_KNOWN_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public ConnectThread(BluetoothDevice device, BluetoothAdapter bluetoothAdapter)
    {
        // Use a temporary object that is later assigned to mmSocket,because
        // mmSocket is final
        BluetoothSocket tmp = null;
        mBluetoothAdapter= bluetoothAdapter;
       // mBluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try
        {


           tmp = device.createRfcommSocketToServiceRecord(WELL_KNOWN_UUID);
            //This is the trick
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
            tmp = (BluetoothSocket) m.invoke(device, 1);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        mmSocket = tmp;
    }

    public void run()
    {
       // DebugLog.i(TAG, "Trying to connect...");
        // Cancel discovery because it will slow down the connection
       mBluetoothAdapter.cancelDiscovery();

        try
        {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();


        } catch (IOException connectException)
        {
            // Unable to connect; close the socket and get out
           // DebugLog.e(TAG, "Fail to connect!", connectException);
            try
            {
                mmSocket.close();
            } catch (IOException closeException)
            {
              //  DebugLog.e(TAG, "Fail to close connection", closeException);
            }
            return;
        }
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel()
    {
        try
        {
            mmSocket.close();
        } catch (IOException e)
        {
        }
    }
}