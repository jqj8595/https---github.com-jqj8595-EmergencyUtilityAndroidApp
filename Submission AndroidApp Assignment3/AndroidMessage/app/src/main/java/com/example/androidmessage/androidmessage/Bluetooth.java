package com.example.androidmessage.androidmessage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class Bluetooth extends AppCompatActivity implements AdapterView.OnItemClickListener{
        static final int ACTION_ENABLE_BT = 101;
        TextView mTextMsg;
        EditText mEditData;
        BluetoothAdapter mBTAdapter;
        ListView mListDevice;
        ArrayList<String> mArDevice;
        static final String  BLUE_NAME = "BluetoothEx";

        private ArrayAdapter<String> adapter;

        static final UUID BLUE_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        ClientThread mCThread = null;
        ServerThread mSThread = null;
        SocketThread mSocketThread = null;




        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bluetooth);

            mTextMsg = (TextView)findViewById(R.id.textMessage);
            mEditData = (EditText)findViewById(R.id.editData);

            // to initialize the ListView
            initListView();

            boolean isBTEnable = canUseBluetooth();
            if(isBTEnable){
                getPairedDevice();
            }
        }

        public void initListView(){
            mArDevice = new ArrayList<String>();
            adapter = new ArrayAdapter<String>(this,
                    R.layout.device_found_list, mArDevice);

            mListDevice = (ListView) findViewById(R.id.device_list);
            mListDevice.setAdapter(adapter);
            mListDevice.setOnItemClickListener(this);
        }

        public boolean canUseBluetooth(){
            mBTAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBTAdapter == null){
                mTextMsg.setText("Device not found");
                return false;
            }

            mTextMsg.setText("Device is exit"); // pass

            if(mBTAdapter.isEnabled()){         // pass
                mTextMsg.append("\nDevice can use");
                return true;
            }

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ACTION_ENABLE_BT);   // -> it will call the method onActivityResult
            return false;
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data){
            if(requestCode == ACTION_ENABLE_BT){

                if(resultCode == RESULT_OK){
                    mTextMsg.append("\nDevice can use");    // pass

                    getPairedDevice();                      // to get the paired devices.
                }else{
                    mTextMsg.append("\nDevice can not use");
                }
            }
        }

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                //if(intent.getAction() == BluetoothDevice.ACTION_FOUND){
                if(BluetoothDevice.ACTION_FOUND.equals(action) && mBTAdapter.isDiscovering()){
                    Toast.makeText(getApplicationContext(), "Find the device", Toast.LENGTH_SHORT).show();

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(device.getBondState() != BluetoothDevice.BOND_BONDED){

                        addDeviceToList(device.getName(), device.getAddress());
                    }
                }
            }
        };

        public void startFindDevice(){

            stopFindDevice();

            mBTAdapter.startDiscovery();

            IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, intent);
        }

        public void stopFindDevice(){
            if(mBTAdapter.isDiscovering()){
                mBTAdapter.cancelDiscovery();

                unregisterReceiver(mReceiver);
            }
        }



        public void addDeviceToList(String name, String address){
            String deviceInfo = name + " - " + address;
            Log.d("tag1", "Device Find: " + deviceInfo);
            mArDevice.add(deviceInfo);

            //adapter = (ArrayAdapter)mListDevice.getAdapter();
            adapter.notifyDataSetChanged();
        }



        public void setDiscoverable(){
            if(mBTAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
                return;

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(intent);
        }

        public void getPairedDevice(){
            if(mSThread != null) return;

            mSThread = new ServerThread();
            mSThread.start();

            Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();

            for(BluetoothDevice device : pairedDevices){
                addDeviceToList(device.getName(), device.getAddress());
            }   // pass

            startFindDevice();

            setDiscoverable();
        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String strItem = mArDevice.get(position);

            int pos = strItem.indexOf(" - ");
            if(pos <= 0) return;
            String address = strItem.substring(pos + 3);
            mTextMsg.setText("Sel Device: " + address);

            stopFindDevice();

            mSThread.cancel();
            mSThread = null;

            if(mCThread != null) return;

            BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

            mCThread = new ClientThread(device);
            mCThread.start();
        }

        private class ClientThread extends Thread{
            private final BluetoothSocket mmCSocket;

            public ClientThread(BluetoothDevice device){
                BluetoothSocket tmp = null;

                try{
                    tmp = device.createRfcommSocketToServiceRecord(BLUE_UUID);
                }catch (IOException e) {}
                mmCSocket = tmp;
            }
            /*
            public ClientThread(BluetoothDevice device){
                BluetoothSocket tmp = null;
                try{
                    tmp = device.createInsecureRfcommSocketToServiceRecord(BLUE_UUID);
                }catch (IOException e){
                    showMessage("Create Client Socket error");
                    return;
                }
                mmCSocket = tmp;
            }
            */
            public void run(){
                mBTAdapter.cancelDiscovery();

                try{
                    mmCSocket.connect();
                }catch (IOException e){
                    showMessage("Connect to server error");
                    try{
                        mmCSocket.close();
                    }catch (IOException e2){
                        showMessage("Client Socket close error");
                    }
                    return;
                }

                onConnected(mmCSocket);
            }

            public void cancel(){
                try{
                    mmCSocket.close();
                }catch (IOException e){
                    showMessage("Client Socket close error");
                }
            }
        }

        private class ServerThread extends Thread{
            private final BluetoothServerSocket mmSSocket;

            public ServerThread(){
                BluetoothServerSocket tmp = null;
                try{
                    tmp = mBTAdapter.listenUsingInsecureRfcommWithServiceRecord(BLUE_NAME, BLUE_UUID);
                }catch (IOException e){
                    showMessage("Get Server Socket Error");
                }
                mmSSocket = tmp;
            }

            public void run(){
                BluetoothSocket cSocket = null;

                try{
                    cSocket = mmSSocket.accept();
                }catch (IOException e){
                    showMessage("Socket Accept Error");
                    return;
                }

                onConnected(cSocket);
            }

            public void cancel(){
                try{
                    mmSSocket.close();
                }catch (IOException e){
                    showMessage("Server Socket close error");
                }
            }
        }

        public void showMessage(String strMsg){
            Message msg = Message.obtain(mHandler, 0, strMsg);
            mHandler.sendMessage(msg);
            Log.d("tag1", strMsg);
        }

        Handler mHandler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what == 0){
                    String strMsg = (String)msg.obj;
                    mTextMsg.setText(strMsg);
                }
            }
        };

        public void onConnected(BluetoothSocket socket){
            showMessage("Socket connected");

            if(mSocketThread != null)
                mSocketThread = null;

            mSocketThread = new SocketThread(socket);
            mSocketThread.start();
        }

        private class SocketThread extends Thread{
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public SocketThread(BluetoothSocket socket){
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try{
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                }catch (IOException e){
                    showMessage("Get Stream error");
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run(){
                byte[] buffer = new byte[1024];
                int bytes;

                while(true){
                    try{
                        bytes = mmInStream.read(buffer);
                        String strBuf = new String(buffer, 0, bytes);
                        showMessage("Receive: " + strBuf);
                        SystemClock.sleep(1);
                    }catch (IOException e){
                        showMessage("Socket disconnected");
                        break;
                    }
                }
            }

            public void write(String strBuf){
                try{
                    byte[] buffer = strBuf.getBytes();
                    mmOutStream.write(buffer);
                    showMessage("Send: " + strBuf);
                }catch (IOException e){
                    showMessage("Socket write error");
                }
            }
        }

        public void buttonToSendMessage(View v){
            if(mSocketThread == null)return;

            String strBuf = mEditData.getText().toString();
            if(strBuf.length() < 1)return;
            mEditData.setText("");
            mSocketThread.write(strBuf);
        }
        /*
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btnSend : {
                    if(mSocketThread == null)return;

                    String strBuf = mEditData.getText().toString();
                    if(strBuf.length() < 1)return;
                    mEditData.setText("");
                    mSocketThread.write(strBuf);
                    break;
                }
            }
        }
        */
        public void onDestroy(){
            super.onDestroy();

            stopFindDevice();

            if(mCThread != null){
                mCThread.cancel();
                mCThread = null;
            }
            if(mSThread != null){
                mSThread.cancel();
                mSThread = null;
            }
            if(mSocketThread != null)
                mSocketThread = null;
        }
    }




