package com.example.zaki_berouk.savedbythebell;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zaki_berouk.savedbythebell.db_utils.DBHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class SensorActivity extends AppCompatActivity implements  SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isRecording = false;
    private final DBHelper dbHelper = DBHelper.getInstance(this);
    private Button startStopRecord;
    private String dbFile = "sensor_data";
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
                    save(listSensorData);
                    listSensorData="";
                    //Toast.makeText(getBaseContext(), read(),Toast.LENGTH_LONG).show();
                }else{
                    isRecording = true;
                    startStopRecord.setText("Stop Record");
                }
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

    public void save(String texte) {

        String texte_a_garder = read()+"\n"+texte;
        try {
            FileOutputStream fOut = openFileOutput(dbFile, MODE_PRIVATE );
            fOut.write(texte_a_garder.getBytes());
            fOut.close();
            Toast.makeText(getBaseContext(), "Stockage fichier",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String read(){
        try{
            FileInputStream fin = openFileInput(dbFile);
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
}
