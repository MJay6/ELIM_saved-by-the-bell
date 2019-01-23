package com.example.zaki_berouk.savedbythebell.db_utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zaki_berouk.savedbythebell.model.Event;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "saved_by_the_bell.db";

    private SQLiteDatabase myDataBase;
    private final Context myContext;
    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    //S'il y a un problème, regardez si c'est bien le bon SQLException qui a été importé...
    public void openDataBase() throws SQLException, IOException {
        //Open the database
        String myPath = myContext.getDatabasePath(DB_NAME).getAbsolutePath();
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {
                // Copy the database in assets to the application database.
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database", e);
            }
        }
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = myContext.getDatabasePath(DB_NAME).getAbsolutePath();
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //database doesn't exist yet.

        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = myContext.getDatabasePath(DB_NAME).getAbsolutePath();
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    //Il n'y a pas d'attribut id dans l'objet s, l'id d'un objet est sa position dans la liste
    //pour utiliser le param id, faut récupérer la taille de la liste contenant tous les events et la mettre en param :)
    //DB : table event(id, name, date, location, descr, departure_time)
    //Event (model) :  name, date, descr, location, departure_time
    public List<Event> getAllEvent() {
        List<Event> events = new ArrayList<>();
        Cursor cursor = myDataBase.rawQuery("SELECT * FROM event ORDER BY date;", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            events.add(new Event(cursor.getString(1),
                    new Date(cursor.getLong(2)),
                    cursor.getString(3),
                    cursor.getString(4),
                    new Date(cursor.getLong(5))));
            cursor.moveToNext();
        }
        cursor.close();
        return events;
    }

    //Event(String name, String date, String category, String time, String descr)
    //INSERT INTO event VALUES(4,'name', "date","location", "descr", "departure");
    public void addEventinDB(String name, Date date, String location, String descr, int id, Date departure_time) {
        myDataBase.execSQL("INSERT INTO event VALUES(" + id + ", \"" + name + "\", \"" + date.getTime() + "\", \"" + location + "\", \"" + descr + "\", \"" + departure_time.getTime()
                + "\");");
    }

    public void addDataSensorinDB(int id, float accel_x,float accel_y,float accel_z) {
        myDataBase.execSQL("INSERT INTO mobile_sensor VALUES(" + id + ", " + accel_x + ", " + accel_y + ", " + accel_z
                + ");");
    }
}
