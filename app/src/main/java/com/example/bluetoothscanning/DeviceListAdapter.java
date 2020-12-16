package com.example.bluetoothscanning;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>{

private LayoutInflater mLayoutInflater;
private ArrayList<BluetoothDevice> mDevices;
private int  mViewResourceId;
String currentTimeStamp;
int rssi_value;
public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices, int rssi){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        this.rssi_value=rssi;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
        }

public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentDateTime = dateFormat.format(new Date());
    int rssiValue= rssi_value;
    int count=0;
        BluetoothDevice device = mDevices.get(position);

        if (device != null) {
        TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
        TextView deviceAdress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
         TextView timeStamp=(TextView) convertView.findViewById(R.id.tvTimeStamp);
        if (deviceName != null) {
        deviceName.setText(device.getName());
        }
        if (deviceAdress != null) {
        deviceAdress.setText(device.getAddress());
        }
        if(timeStamp!=null){
            timeStamp.setText(currentDateTime);
        }

        }

        return convertView;
        }


}