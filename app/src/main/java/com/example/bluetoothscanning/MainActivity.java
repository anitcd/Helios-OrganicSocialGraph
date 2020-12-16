package com.example.bluetoothscanning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Node;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SensorEventListener {
    private static final int REQUEST_LOCATION = 1;
    Button onOffButton, wifiOnOffButton;
    Button EnableDiscoverable_on_off;
    ListView lvNewDevices;
    private static final String TAG = "Mainactivity";
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> mproximityBluetoothDevice = new ArrayList<>();
    public ArrayList<String> maddressValue = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    BluetoothAdapter mBluetoothAdapter;
    int rssi;
    Handler handler;
    String currentDateTime;
    PowerManager.WakeLock wakeLock;  //Declaration of Instance variable.
    int frequncy = 0;
    private boolean waitingForBonding;
    Runnable r;
    Sensor accelerometer, gyrometer, magnometer, banometer, light, temperature, gravity;
    LocationManager locManager;
    double latitude;
    double longitude;
    String addressValue;

    private final BroadcastReceiver bondStateChanged = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                //1.already a bond
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "bondStateChanged: BOND_BONDED");

                }
                //2.Creating a bond
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "bondStateChanged: BOND_BONDING");
                    bluetoothDevice.createBond();
                }
                //3.the bond is broken
                if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "bondStateChanged: BOND_NONE");


                }
            }

        }
    };

    public final BroadcastReceiver discoverUnpairedDevices = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d(TAG, "OnReceive: Action found");
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {

                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);


                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                currentDateTime = dateFormat.format(new Date());
                BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
                if (bluetoothClass.getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {


                    locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,
                            500.0f, locationListener);
                    Location location = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                         addressValue=getAddress(latitude,longitude);
                         maddressValue.add(addressValue);
                    }

                    ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad("", "user_00001", null);
                    Node user1 = cen.getEgo();
                    mBTDevices.add(bluetoothDevice);

                    if(mBTDevices.contains(bluetoothDevice) && maddressValue.contains(addressValue))
                    {
                        frequncy= Collections.frequency(mBTDevices, bluetoothDevice);

                        if(frequncy > 1)
                        {

                            mproximityBluetoothDevice.add(bluetoothDevice);

                           /* Boolean isBonded = false;
                            try {
                                isBonded = createBond(bluetoothDevice);
                                if(isBonded)
                                {
                                    Log.i("Log","Paired");
                                }
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }*/

                            Node user2= cen.getOrCreateNode("user-0002",currentDateTime);
                            Node user3= cen.getOrCreateNode("user-0003",currentDateTime);
                            Node user4= cen.getOrCreateNode("user-0004",currentDateTime);
                            eu.h2020.helios_social.core.contextualegonetwork.Context context1= cen.getOrCreateContext("Test context");
                            context1.getOrAddEdge(user1,user2);
                            context1.getOrAddEdge(user1,user3);
                            context1.getOrAddEdge(user1,user4);
                            cen.save();

                            //activityTimer();
                        }
                       // chooserDialog();

                    }


                    WriteBtn(bluetoothDevice.getAddress(), rssi, currentDateTime,frequncy,addressValue);


                }


                Log.d(TAG, "OnReceive:" + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress() + "" + rssi);
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices, rssi);
                lvNewDevices.setAdapter(mDeviceListAdapter);


            }

        }
    };


    public void chooserDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("We have few devices in your proximity, would you like to make a connection?");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                              stopRepeatingTask();
                                int counter=0;
                                for(counter=0; counter < mproximityBluetoothDevice.size();counter++){
                                    mproximityBluetoothDevice.get(counter).createBond();
                                }
                                //activityTimer();
                            }
                        });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  /*  public void createBond(BluetoothDevice btDevice) {
       *//* try {
            Log.d(TAG, "Start Pairing...");

        waitingForBonding = true;

        Method m = btDevice.getClass()
                .getMethod("createBond", (Class[]) null);
        m.invoke(btDevice, (Object[]) null);

            Log.d(TAG, "Pairing finished.");
    } catch (Exception e) {
        Log.e(TAG, e.getMessage());
    }*//*
        *//*try {
            Method m = btDevice.getClass().getMethod("createBond",(Class[]) null);
          m.invoke(btDevice,(Object[]) null );
            *//**//*Log.d("pairDevice()", "Start Pairing...");
            Method m = btDevice.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(btDevice, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");*//**//*
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }*//*

    }
*/
    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public String getAddress(double lat, double lng) {
        String add="";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void WriteBtn(String address, int rssi, String currentDateTime, int frequncy,String addressValue) {
        try {
            File file = new File(this.getExternalFilesDir(null), "BluetoothScannedDevice.txt");
            FileOutputStream fileOutput = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
            outputStreamWriter.write("Address : " + address);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("DB :" + rssi);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("TimeStamp:" + currentDateTime);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("Frequency:" + frequncy);
            outputStreamWriter.write("\n\n");
            outputStreamWriter.write("Location:" + addressValue);
            outputStreamWriter.write("\n\n");
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);


                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                       stopRepeatingTask();
                        Log.d(TAG, "onReceive: STATE OFF");
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE TURNING OFF");
                    case BluetoothAdapter.STATE_ON:
                       startRepeatingTask();
                        Log.d(TAG, "onReceive: STATE ON");
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: STATE TURNING ON");

                }
            }
        }
    };

    private final BroadcastReceiver bluetoothDiscoverableBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);


                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "bluetoothDiscoverableBroadcastReceiver: Discoverable Enabled");
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "bluetoothDiscoverableBroadcastReceiver: Discoverablity Enabled. Able to receive connections.");
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "bluetoothDiscoverableBroadcastReceiver: Discoverablity Disabled. Not Able to receive connections.");
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "bluetoothDiscoverableBroadcastReceiver:Connecting...");
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "bluetoothDiscoverableBroadcastReceiver:Connected...");

                }
            }
        }
    };

    @SuppressLint("InvalidWakeLockTag")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onOffButton = findViewById(R.id.OnOffButton);
        wifiOnOffButton= findViewById(R.id.wifiScanning);
        EnableDiscoverable_on_off = findViewById(R.id.EnableDiscoverable_on_off);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Method getUuidsMethod = null;
        try {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        ParcelUuid[] uuids = new ParcelUuid[0];
        try {
            uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        /*for (ParcelUuid uuid: uuids) {
            Log.d(TAG, "UUID: " + uuid.getUuid().toString());
        }*/
        lvNewDevices = findViewById(R.id.lvNewDevices);
        handler = new Handler();
        checkWritePermission();
        checkBTPermission();
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "Wake Lock");
        wakeLock.acquire();
        wakeLock.setReferenceCounted(false);

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        onOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Enabling/Disabling bluetooth");

                enableDisableBT();
            }
        });

         wifiOnOffButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Log.d(TAG, "Enabling/Disabling WIFI");
                 Toast.makeText(MainActivity.this, "you have choose to scan through wifi", Toast.LENGTH_LONG).show();
                 Intent wifiIntent= new Intent(MainActivity.this, WifiScanActivity.class);
                 startActivity(wifiIntent);

             }
         });

        // use a linear layout manage
        Log.d(TAG,"Initializing sensor services");
        SensorManager sensorManager;
        sensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        accelerometer= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer!=null){
          sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
          Log.d(TAG,"Registered accelerometer listener");
            }
       else{
          Log.d(TAG,"Accelerometer sensor is not supported");
           }
        gyrometer= sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(gyrometer!=null){
            sensorManager.registerListener(MainActivity.this,gyrometer,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered gyrometer listener");
        }
        else{
            Log.d(TAG,"Gyrometer sensor is not supported");
        }

        magnometer= sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magnometer!=null){
            sensorManager.registerListener(MainActivity.this,magnometer,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered magnometer listener");
        }
        else{
            Log.d(TAG,"Magnometer sensor is not supported");
        }

        banometer= sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if(banometer!=null){
            sensorManager.registerListener(MainActivity.this,banometer,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered banometer listener");
        }
        else{
            Log.d(TAG,"banometer sensor is not supported");
        }

        light=sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(light!=null){
            sensorManager.registerListener(MainActivity.this,light,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered Light Sensor");
        }
        else{
            Log.d(TAG,"Light sensor is not supported");

        }

        temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if(temperature!=null){
            sensorManager.registerListener(MainActivity.this,light,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered Temperature Sensor");
        }
        else{
            Log.d(TAG,"Temperature sensor is not supported");

        }
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(temperature!=null){
            sensorManager.registerListener(MainActivity.this,gravity,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG,"Registered gravity Sensor");
        }
        else{
            Log.d(TAG,"gravity sensor is not supported");

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
        stopHandler();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");

        super.onDestroy();
        unregisterReceiver(bluetoothStateBroadcastReceiver);
        unregisterReceiver(bluetoothDiscoverableBroadcastReceiver);
        unregisterReceiver(discoverUnpairedDevices);
        unregisterReceiver(bondStateChanged);

    }


    public void enableDisableBT() {
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "EnableDiabled: Does not have bluetooth capabalities");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStateBroadcastReceiver, BTIntent);

        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStateBroadcastReceiver, BTIntent);
        }
    }

    public void btnEnableDisable_Discoverable(View view) {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter discoverableConnectionIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(bluetoothDiscoverableBroadcastReceiver, discoverableConnectionIntent);


    }

    Runnable mStatusChecker = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            try {

                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Cancel Discovery");

                    mBluetoothAdapter.startDiscovery();

                }

                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();

                }
                int count =0;
                while(mproximityBluetoothDevice.size()>count){

                    BluetoothDevice btdevice= mproximityBluetoothDevice.get(count);
                  ConnectThread ct = new ConnectThread(btdevice,mBluetoothAdapter);
                  ct.start();
                    count++;
                }
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(discoverUnpairedDevices, discoverDevicesIntent);


            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                handler.postDelayed(mStatusChecker, 1*60*1000);
            }
        }
    };


    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }

    private void activityTimer() {

        handler = new Handler();
        r = new Runnable() {


            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //    Toast.makeText(context, "user is inactive from last 5 minutes",Toast.LENGTH_SHORT).show();
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Cancel Discovery");

                    mBluetoothAdapter.startDiscovery();

                }

                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();

                }
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(discoverUnpairedDevices, discoverDevicesIntent);
            }
        };
        startHandler();
    }
    private void stopHandler() {
        handler.removeCallbacks(r);
    }

    private void startHandler() {
        handler.postDelayed(r, 1*60 * 1000); //for 5 minutes
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int permissionCHeck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCHeck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if (permissionCHeck != 0) {
                this.requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 1001);
            } else {
                Log.d(TAG, "checkPersmisions::No need to check");
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWritePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int permissionCHeck = this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            permissionCHeck += this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");

            if (permissionCHeck != 0) {
                this.requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 1);
            } else {
                Log.d(TAG, "checkPersmisions::No need to check");
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        mBluetoothAdapter.cancelDiscovery();


        Log.d(TAG, "YOu clicked the device");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "Device Name" + deviceName);
        Log.d(TAG, "Device Address" + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with" + deviceName);
            try {
                Log.d(TAG, "Start Pairing... with: " + mBTDevices.get(i).getName());

                mBTDevices.get(i).createBond();


            } catch (Exception e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }

        }
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor= sensorEvent.sensor;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        if(sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG,"ACCELEROMETER " + "X:" + sensorEvent.values[0] + "Y:"+sensorEvent.values[1] + "Z:" +sensorEvent.values[2]);

          WriteSensorData("Accelerometer.txt",sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2],currentDateTime);
        }
        else if(sensor.getType()==Sensor.TYPE_GYROSCOPE){
            Log.d(TAG,"GYROMETER " + "X:" + sensorEvent.values[0]+"Y:"+sensorEvent.values[1]+ "Z:" +sensorEvent.values[2]);

            WriteSensorData("Gyrometer.txt",sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2],currentDateTime);

        }
        else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            Log.d(TAG,"Magnometer " + "X:" + sensorEvent.values[0]+"Y:"+sensorEvent.values[1]+ "Z:" +sensorEvent.values[2]);

            WriteSensorData("Magnometer.txt",sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2],currentDateTime);

        }
        else if(sensor.getType()==Sensor.TYPE_LIGHT){
            Log.d(TAG,"LIGHT " + sensorEvent.values[0]);
            WriteSensorData("Light.txt",sensorEvent.values[0],0,0,currentDateTime);

        }

        else if(sensor.getType()== Sensor.TYPE_AMBIENT_TEMPERATURE){
            Log.d(TAG,"TEMPERATURE " + sensorEvent.values[0]);
            WriteSensorData("Temperature.txt",sensorEvent.values[0],0,0,currentDateTime);

        }
        else if(sensor.getType()== Sensor.TYPE_PRESSURE){
            Log.d(TAG,"BANOMETER " + sensorEvent.values[0]);

            WriteSensorData("Banometer.txt",sensorEvent.values[0],0,0,currentDateTime);

        }
        else if(sensor.getType()== Sensor.TYPE_GRAVITY) {
            Log.d(TAG,"GRAVITY " + sensorEvent.values[0]);

            WriteSensorData("Gravity.txt",sensorEvent.values[0],0,0,currentDateTime);

        }
    }
    public void WriteSensorData(String name, float x, float y, float z,String currentDateTime) {


        try {
            File file = new File(this.getExternalFilesDir(null), name);
            FileOutputStream fileOutput = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
            if(x!=0){
                outputStreamWriter.write("X:" + x);
            }
            if(y!=0){
                outputStreamWriter.append(" , ");
                outputStreamWriter.write("Y:"+ y);
                outputStreamWriter.append(" , ");

            }
            if(z!=0){
                outputStreamWriter.write("Z:" + z);
            }
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("Timestamp:" + currentDateTime);
            outputStreamWriter.write("\n\n");
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private final LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

           // updateWithNewLocation(location);

            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + location.getLatitude() + " Lng: "
                            + location.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + location.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + location.getLatitude();
            Log.v(TAG, latitude);
        }

        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void updateWithNewLocation(Location location) {
       // TextView myLocationText = (TextView) findViewById(R.id.text);
        String latLongString = "";
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latLongString = "Lat:" + lat + "\nLong:" + lng;
        } else {
            latLongString = "No location found";
        }

    }

    public void btnDiscover(View view)
    {
       //  activityTimer();
    }
}


