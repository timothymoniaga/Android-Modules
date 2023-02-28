package com.example.printermodule;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import android.os.Handler;

import androidx.core.app.ActivityCompat;

import java.io.InputStream;

/**
 * Bluetooth: Used to communicate to printers via bluetooth
 * @author Michael Nguyen
 */

public class BluetoothService implements Runnable {
  private int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter mBluetoothAdapter;
  private static String macAddressPrinter;
  private static String namePrinter;
  private static String deviceInfo;
  public BluetoothSocket btSocket = null;
  private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  public OutputStream outStream = null;

  public static final String PARAM_IN_MSG = "imsg";
  public static final String PARAM_OUT_MSG = "omsg";

  public String msg;

  private Context context;

  InputStream mmInputStream;
  Thread workerThread;
  byte[] readBuffer;
  int readBufferPosition;
  volatile boolean stopWorker;

  @SuppressLint("MissingPermission")
  public BluetoothService(Context context, String message) {
    this.context = context;

    msg = message;
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*Method getUuidsMethod;  // Comment it out by Rudy Moniaga
        try
        {
            getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);

            //for (ParcelUuid uuid: uuids) {
            //Log.d("SDSD", "UUID: " + uuid.getUuid().toString());
            //}

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    //mBluetoothAdapter.cancelDiscovery(); // cancel any discovery to not lag

    // Check if device has BT capabilities
    if (mBluetoothAdapter != null)
    {
      // If BT is off, continually turn it on
      // Reason: if just enable, can start looking for connection even
      // before BT has fully turned on
      while (!mBluetoothAdapter.isEnabled())
      {
        // TODO: May need this later - open intent to turn on BT/Pair
        // device/PIN.
        // Intent enableBtIntent = new
        // Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        mBluetoothAdapter.enable();
      }

      // List of previously paired devices
      Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

      // check if this is working - this is going through previously
      // paired devices?
      // If there are paired devices
      if (pairedDevices.size() > 0)
      {
        // Loop through paired devices
        for (BluetoothDevice device : pairedDevices)
        {
          //Log.d("Hehehe", device.getName());
            // Toast.makeText(getApplicationContext(), "Connecting "
            // + device.getName() + " on " + device.getAddress(),
            // Toast.LENGTH_LONG).show();
            macAddressPrinter = device.getAddress().toString();
            namePrinter = device.getName().toString();
            deviceInfo = device.toString();
            //Log.d("Bluetooth", "MAC Address of BT device connected: " + macAddressPrinter);
        }
      }

    }

  }

  public void beginListenForData()
  {
    final Handler handler = new Handler();
    final byte delimiter = 10; //This is the ASCII code for a newline character

    stopWorker = false;
    readBufferPosition = 0;
    readBuffer = new byte[1024];
    workerThread = new Thread(new Runnable()
    {
      public void run()
      {
        while(!Thread.currentThread().isInterrupted() && !stopWorker)
        {
          try
          {
            int bytesAvailable = mmInputStream.available();
            if(bytesAvailable > 0)
            {
              byte[] packetBytes = new byte[bytesAvailable];
              mmInputStream.read(packetBytes);
              for(int i=0;i<bytesAvailable;i++)
              {
                byte b = packetBytes[i];
                if(b == delimiter)
                {
                  byte[] encodedBytes = new byte[readBufferPosition];
                  System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                  final String data = new String(encodedBytes, "US-ASCII");
                  readBufferPosition = 0;

                  handler.post(new Runnable()
                  {
                    public void run()
                    {

                      Log.d("Rcv Bluetooth", data);
                    }
                  });
                }
                else
                {
                  readBuffer[readBufferPosition++] = b;
                }
              }
            }
          }
          catch (IOException ex)
          {
            stopWorker = true;
          }
        }
      }
    });

    workerThread.start();
  }

  @SuppressLint("MissingPermission")
  public void run()
  {
    BluetoothDevice device;

    try
    {
      //Log.d("dddd", macAddressPrinter);
      if (macAddressPrinter.isEmpty())
      {
        throw new Exception();
      }

      device = mBluetoothAdapter.getRemoteDevice(macAddressPrinter);

      //Log.d("Bluetooth", "Name of connected device: " + device.getName());

      btSocket = device.createRfcommSocketToServiceRecord(uuid);

      btSocket.connect();

      //Log.d("Bluetooth", "Sending message to printer: " + msg);
      byte[] msgBuffer = msg.getBytes();

      outStream = btSocket.getOutputStream();
      mmInputStream = btSocket.getInputStream();
      outStream.flush();

      //Log.d("Bluetooth", "MsgToSend:" + msg);
      outStream.write(msgBuffer);
      //outStream.write(msgBuffer,0,msgBuffer.length);
      //Log.d("Bluetooth", "Just after write stream");
      //beginListenForData();
      Thread.sleep(1200); // Make sure all data has been send out before kill the thread
      kill();  // Temporary remove

    }
    catch (Exception e)
    {
      e.printStackTrace();
      //Log.d("sda", e.getMessage() + "sd");
      // Log.d("Errr", "error");
    }

  }

  public void kill()
  {
    if (outStream != null)
    {
      try
      {
        outStream.flush();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    try
    {
      btSocket.close();
    }
    catch (IOException e2)
    {
      e2.printStackTrace();
    }
  }


//  public String getConnectedPrinterMD5()
//  {
//    // TODO Auto-generated method stub
//    return Utils.createMd5(Utils.getMACAddress(context) + macAddressPrinter + Registration.KEY_REGISTRATION_SEED);
//    //return null;
//  }

}

