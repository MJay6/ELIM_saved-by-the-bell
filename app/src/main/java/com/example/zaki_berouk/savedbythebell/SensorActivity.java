package com.example.zaki_berouk.savedbythebell;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.zaki_berouk.savedbythebell.model.NotificationDetailsActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SensorActivity extends AppCompatActivity implements  SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private boolean isRecording = false;
    private Button startStopRecord;
    private String DB_FILE = "sensor_data";
    private String listSensorData="";
    private String lastKnownSample = "";
    private RadioGroup labelGroup;
    private String label = "unknown";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);



        labelGroup  = findViewById(R.id.labelRadioGroup);
        labelGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioButtonUnknownLabel:
                        label = "unknown";
                        Toast.makeText(getBaseContext(), "Set label to unknown",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButtonDrivingLabel:
                        label = "driving";
                        Toast.makeText(getBaseContext(), "Set label to driving",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButtonBicyclingLabel:
                        label = "bicycling";
                        Toast.makeText(getBaseContext(), "Set label to bicycling",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButtonWalkingLabel:
                        label = "walking";
                        Toast.makeText(getBaseContext(), "Set label to walking",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener( this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener( this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        startStopRecord = (Button) findViewById(R.id.startStopRecord);
        startStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(isRecording){
                    isRecording = false;
                    for (int i = 0; i < labelGroup.getChildCount(); i++) {
                        labelGroup.getChildAt(i).setEnabled(true);
                    }
                    startStopRecord.setText("Start Record");
                    writeFile();
                    listSensorData="";
                    Log.i("File", readFile());
                }else{
                    isRecording = true;
                    for (int i = 0; i < labelGroup.getChildCount(); i++) {
                        labelGroup.getChildAt(i).setEnabled(false);
                    }
                    clean();
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
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if(isRecording){
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                float accel_x = sensorEvent.values[0];
                float accel_y = sensorEvent.values[1];
                float accel_z = sensorEvent.values[2];

                listSensorData += label + ", "+ accel_x +", "+ accel_y +", "+ accel_z + ", ";

                lastKnownSample = label + ", "+ accel_x +", "+ accel_y +", "+ accel_z + ", ";
            }
            if(sensor.getType() == Sensor.TYPE_GYROSCOPE){

                float gyro_x = sensorEvent.values[0];
                float gyro_y = sensorEvent.values[1];
                float gyro_z = sensorEvent.values[2];

                //Log.i("gyro x ", String.valueOf(gyro_x));
                listSensorData += gyro_x +", "+gyro_y+", "+gyro_z + "\n";

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

    public void clean() {
        try {
            File textfile = new File(Environment.getExternalStorageDirectory(),DB_FILE);
            FileOutputStream fOut = new FileOutputStream(textfile);
            fOut.write("".getBytes());
            fOut.close();
            Toast.makeText(getBaseContext(), "Fichier clean",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
