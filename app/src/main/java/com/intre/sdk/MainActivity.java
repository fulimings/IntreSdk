package com.intre.sdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.intre.mylibrary.serialport.SerialPort;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean ret = SerialPort.getInstance().initSerialPort("/dev/ttyMT5",9600);
       
    }
}
