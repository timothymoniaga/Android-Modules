package com.example.printermodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.companion.BluetoothDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_ENABLE_BLUETOOTH = 1;
  private static final int REQUEST_LOCATION_PERMISSION = 2;
  private static final int REQUEST_BLUETOOTH_PERMISSIONS = 3;

  EditText input;
  ListView listview;
  Button print;
  TextView deviceText;
  //BluetoothService bluetooth;
  BluetoothAdapter bluetoothAdapter;
  Set<BluetoothDevice> pairedDevices;
  ArrayAdapter<BluetoothDevice> listAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    input = findViewById(R.id.inputbox);
    print = findViewById(R.id.btn_print);
    deviceText = findViewById(R.id.textView);
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    listview = findViewById(R.id.listView);
    pairedDevices = new HashSet<>();
    listAdapter = new ArrayAdapter<>(this, R.layout.listview_layout, new ArrayList<>(pairedDevices));
    //  = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(set));


    BluetoothDiscoveryReceiver receiver = new BluetoothDiscoveryReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(BluetoothDevice.ACTION_FOUND);
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    registerReceiver(receiver, filter);
    listview.setAdapter(listAdapter);

    requestBluetoothPermissions();

  }


  public void printButton(View view) {


    startSearching();
    // BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

 /*   if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
      BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
      if (bluetoothLeScanner != null) {
        List<ScanFilter> filters = new ArrayList<>();
        //filters.add(new ScanFilter.Builder().setBonded(true).build());

        ScanSettings settings = new ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
          .build();

        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
        }
        scanner.startScan(filters, settings, new ScanCallback() {
          @Override
          public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            deviceText.setText("hello");
          }
        });
      }
    }

  */
/*
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        filterBuilder = new BluetoothDeviceFilter.Builder();
        //filterBuilder.setBondedDevices();
        BluetoothDeviceFilter deviceFilter = filterBuilder.build();
      }

      BluetoothDevice device = null;
      if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

          bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null) {
          for (BluetoothDevice d : pairedDevices) {
            if (d.getName().equals("MP80-04")) {
              device = d;
              deviceText.setText("Connected");
              break;
            }
          }
        }
        if (device == null) {
          deviceText.setText("Not Connected");
        }
      }
      
 */
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
      // If request is cancelled, the result arrays are empty.
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        // Permissions granted, continue with Bluetooth functionality
        // ...
      } else {
        // Permissions denied, show a message to the user
        Toast.makeText(this, "Bluetooth permissions are required to use this app", Toast.LENGTH_SHORT).show();
      }
    }
  }


  private void startSearching() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
      if (bluetoothAdapter.startDiscovery()) {
        deviceText.setText("Searching");
        //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

      } else {
        Snackbar.make(findViewById(R.id.device), "Failed to start searching", Snackbar.LENGTH_INDEFINITE)
          .setAction("Try Again", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startSearching();
            }
          }).show();
      }


    }
  }

  private final BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // A Bluetooth device was found
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // Add the device to the list
        pairedDevices.add(device);
        //bluetoothAdapter.notifyDataSetChanged();
      }
    }
  };

  public class BluetoothDiscoveryReceiver extends BroadcastReceiver {
    private Context context;
    public void setContext(Context context){
      this.context=context;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // A Bluetooth device has been found
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        pairedDevices.add(device);
        updateList();
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        // The discovery process has finished
        // Do something, such as updating the UI
      }

    }
  }

/*  @Override
  protected void onPause() {
    super.onPause();

    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }

    if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }
  }*/

  private void requestBluetoothPermissions() {
    if (ContextCompat.checkSelfPermission(this,
      Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this,
        Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
      // Permission already granted
      return;
    }

    // Permission not yet granted, request it
    ActivityCompat.requestPermissions(this,
      new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH},
      REQUEST_BLUETOOTH_PERMISSIONS);

  }

  private void updateList() {
    listAdapter.clear();
    listAdapter.addAll(pairedDevices);
    listAdapter.notifyDataSetChanged();
  }


}
