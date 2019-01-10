package com.example.zaki_berouk.savedbythebell;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zaki_berouk.savedbythebell.db_utils.DBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SensorActivity extends AppCompatActivity implements  SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isRecording = false;
    private Button startStopRecord;
    private String DB_FILE = "sensor_data";
    private String listSensorData="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        startStopRecord = (Button) findViewById(R.id.startStopRecord);
        startStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isRecording){
                    isRecording = false;
                    startStopRecord.setText("Start Record");
                    writeFile();
                    listSensorData="";

                }else{
                    isRecording = true;
                    startStopRecord.setText("Stop Record");
                }


                Log.i("sucess","created");
            }
        });

    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(isRecording){
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                Log.i("accel x ", String.valueOf(x));

                listSensorData += "label, "+x +", "+y+", "+z+"\n";
            }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void writeFile(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},23
                );
            }
        }

        if(isExternalStorageWritable()){
            String text_to_keep = readFile() + listSensorData;
            File textfile = new File(Environment.getExternalStorageDirectory(),DB_FILE);


            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(textfile);
                fo.write(text_to_keep.getBytes());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFile(){
        try{
            File textfile = new File(Environment.getExternalStorageDirectory(),DB_FILE);
            FileInputStream fin= new FileInputStream(textfile);
            Log.i("path2",Environment.getExternalStorageDirectory().getAbsolutePath() + DB_FILE);
            int c;
            String temp="";
            while( (c = fin.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            Log.d("message", temp);
            Toast.makeText(getBaseContext(),"Lecture fichier",
                    Toast.LENGTH_SHORT).show();
            return temp;
        }catch(Exception e){
            return "";
        }

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
