package com.example.macaddress;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;
  TextView deviceMAC;
  ListView devices;
  List<String> deviceNamesList;
  Set<BluetoothDevice> pairedDevices;
  BluetoothAdapter mBluetoothAdapter;
  ArrayAdapter<String> deviceNamesAdapter;
  private HashMap<String, BluetoothDevice> deviceMap;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    deviceMAC = findViewById(R.id.macText);
    devices = findViewById(R.id.devicesList);
    deviceNamesList = new ArrayList<>();
    deviceMap = new HashMap<>();

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    requestBluetoothPermissions();
    deviceNamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNamesList);
    devices.setAdapter(deviceNamesAdapter);

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
      pairedDevices = mBluetoothAdapter.getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        if(device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
          String deviceName = null;
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            deviceName = device.getAlias();
          }
          deviceNamesList.add(deviceName);
          deviceMap.put(deviceName, device);
        }
      }
      deviceNamesAdapter.notifyDataSetChanged();
    }


    devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String deviceName = (String) parent.getItemAtPosition(position);

        deviceMAC.setText( "MAC Address for "+ deviceName + " is " + deviceMap.get(deviceName));
      }
    });

  }

  private void requestBluetoothPermissions() {
    if (ContextCompat.checkSelfPermission(this,
      android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
      // Permission already granted
      return;
    }
    // Permission not yet granted, request it
    ActivityCompat.requestPermissions(this,
      new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_CONNECT},
      REQUEST_BLUETOOTH_PERMISSIONS);

  }
}
