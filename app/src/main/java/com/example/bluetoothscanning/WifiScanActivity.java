package com.example.bluetoothscanning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class WifiScanActivity extends ListActivity {


    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;


    Button wifiTurnOnOff;
    Handler handler;
    WifiManager mainWifiObj;
 WifiScanReceiver  wifiReciever;
     ListView list;
     String[] wifis;
     String[] filtered;
     String currentDateTime;
    private Context context;
    Calendar calendar;
     String SSid;
     String address ;
     String key_management;
     String strength;
     String timestamp;
    PowerManager.WakeLock wakeLock;  //Declaration of Instance variable.

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_scanned_devices);
        list = getListView();
        handler = new Handler();
        this.context = this;
        calendar=Calendar.getInstance();
        wifiTurnOnOff = findViewById(R.id.WIFIOnOff);
        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        checkWritePermission();
        checkWifiPermission();
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "Wake Lock");
        wakeLock.acquire();
        if (!mainWifiObj.isWifiEnabled())
        {
            mainWifiObj.setWifiEnabled(true);
        }

        wifiTurnOnOff.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(WifiScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            WifiScanActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                       startRepeatingTask();
                } else {
                    startRepeatingTask();
                }
            }

        });

    }
 Runnable mStatusChecker = new Runnable() {
        @SuppressLint("InvalidWakeLockTag")
        @Override
        public void run() {
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                currentDateTime = dateFormat.format(new Date());
                mainWifiObj.startScan();
                registerReceiver(wifiReciever,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                //  updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                handler.postDelayed(mStatusChecker, 2*60*1000);
            }
        }
    };

@SuppressLint("InvalidWakeLockTag")
private void wakeUpLock(){

}
    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        handler.removeCallbacks(mStatusChecker);
    }


    @Override
    protected void onResume() {
        //  startRepeatingTask();
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopRepeatingTask();
        unregisterReceiver(wifiReciever);

    }

    class WifiScanReceiver extends  BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {

            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            wifis = new String[wifiScanList.size()];
            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = ((wifiScanList.get(i)).toString());
            }
            filtered = new String[wifiScanList.size()];



            int counter = 0;
            for (String eachWifi : wifis) {

                String[] temp = eachWifi.split(",");

                filtered[counter] = temp[0].substring(5).trim()+"\n" + temp[1].substring(6).trim() +"\n"+ temp[2].substring(12).trim()+"\n" + temp[3].substring(6).trim()+"\n" +currentDateTime;//0->SSID, 2->Key Management 3-> Strength
                SSid=temp[0].substring(5).trim();
                address =temp[1].substring(5).trim();
               key_management= temp[2].substring(12).trim();
                strength = temp[3].substring(6).trim();
                timestamp= currentDateTime;

                WriteBtn(SSid,address,key_management,strength,timestamp);
                counter++;


            }
          list.setAdapter(new ArrayAdapter<String>(context.getApplicationContext(),android.R.layout.simple_list_item_1, filtered));


        }
        }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkWifiPermission(){


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

    public void WriteBtn(String ssid, String address, String key_management, String strength, String timestamp) {


        try {
            File file = new File(context.getExternalFilesDir(null), "WifiScannedDevices.txt");
            FileOutputStream fileOutput = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutput);
            outputStreamWriter.write("SSID : " + ssid);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("MAC : " + address);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("Key Management :" + key_management);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("Strength:" + strength);
            outputStreamWriter.append(" , ");
            outputStreamWriter.write("TimeStamp:" + timestamp);
            outputStreamWriter.append(" , ");
           // outputStreamWriter.write("Frequency:" + frequncy);
            outputStreamWriter.write("\n\n");
            outputStreamWriter.flush();
            fileOutput.getFD().sync();
            outputStreamWriter.close();
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{file.getAbsolutePath()},
                    null,
                    null);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkWritePermission() {

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


}
