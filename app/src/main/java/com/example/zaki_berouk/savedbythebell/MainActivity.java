package com.example.zaki_berouk.savedbythebell;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.zaki_berouk.savedbythebell.adapter.EventAdapter;
import com.example.zaki_berouk.savedbythebell.db_utils.DBHelper;
import com.example.zaki_berouk.savedbythebell.model.Event;
import com.example.zaki_berouk.savedbythebell.model.NotificationDetailsActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static android.content.Context.LOCATION_SERVICE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private String API_KEY = "AIzaSyDLnS9HcL2Ks0fJL_7pemjjBsgudmgUrAg";
    private List<Event> events = new ArrayList<>();
    private Button add_event;
    private Button get_sensor_data;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private Location lastLocation;
    private LocationManager mLocationManager;
    boolean isGPS = true;
    private String SERVER_ADDRESS = "http://192.168.0.11:1880/elim";

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();
        Properties properties = new Properties();
        AssetManager assetManager = this.getAssets();
        try {
            InputStream inputStream = assetManager.open("app.properties");
            properties.load(inputStream);
            API_KEY = properties.getProperty("api_key");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(perms, 1337);
        } else {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    100, mLocationListener);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //On crée une instance de DBHelper on l'ouvre et on appelle la méthode qui contient la requête SQL
        final DBHelper dbHelper = DBHelper.getInstance(this);
/*
        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
            events = dbHelper.getAllEvent();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        try {
            //ArrayList<Event> lists = new GetEvents().execute().get();
            events = new GetEvents().execute().get();
            for (Event e: events) {
                Log.i("date", String.valueOf(e.getDate()));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //On remplit la list view
        EventAdapter adapter = new EventAdapter(getApplicationContext(), R.layout.event_card, events);
        ListView list_event = (ListView) findViewById(R.id.event_lv);
        list_event.setAdapter(adapter);


        dbHelper.close();

        //Un peu sale mais on a un bouton qui va ouvrir un alert dialog qui va contenir le
        //formulaire d'ajout d'event
        add_event = (Button) findViewById(R.id.add_event_btn);
        add_event.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View alertLayout = inflater.inflate(R.layout.event_add_form, null);
                final TextView date = (TextView) alertLayout.findViewById(R.id.dateEvent);
                final TextView time = (TextView) alertLayout.findViewById(R.id.timeEvent);
                final Button gpsButton = (Button) alertLayout.findViewById(R.id.gpsButton);
                final Button customLocationButton = (Button) alertLayout.findViewById(R.id.customLocationButton);
                final LinearLayout customLocation = alertLayout.findViewById(R.id.customLocation);
                gpsButton.setEnabled(!isGPS);
                gpsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isGPS) {
                            return;
                        }
                        isGPS = true;
                        customLocation.setVisibility(View.GONE);
                        gpsButton.setEnabled(false);
                        customLocationButton.setEnabled(true);
                    }
                });
                customLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isGPS) {
                            return;
                        }
                        isGPS = false;
                        customLocation.setVisibility(View.VISIBLE);
                        customLocationButton.setEnabled(false);
                        gpsButton.setEnabled(true);
                    }
                });

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                // this is set the view from XML inside AlertDialog
                alert.setView(alertLayout);
                // disallow cancel of AlertDialog on click of back button and outside touch
                alert.setCancelable(true);

                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, mDateSetListener, year, month, day);
                        dialog.show();
                    }
                });

                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar cal = Calendar.getInstance();
                        TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, mTimeSetListener, Calendar.HOUR, Calendar.MINUTE, true);
                        dialog.show();
                    }
                });

                mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month + 1;
                        date.setText(String.format("%02d", day) + "/" + String.format("%02d", month) + "/" + String.format("%04d", year));
                    }
                };

                mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        time.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute));
                    }
                };

                alert.setPositiveButton("Ajouter", null);
                alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String name, String date, String category, String time, String descr
                    }
                });

                AlertDialog dialog = alert.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Calendar cal = Calendar.getInstance();

                                final TextView date = (TextView) alertLayout.findViewById(R.id.dateEvent);
                                String[] dateDetails = date.getText().toString().split("/");
                                final TextView time = (TextView) alertLayout.findViewById(R.id.timeEvent);
                                String[] timeDetails = time.getText().toString().split(":");
                                final EditText name = (EditText) alertLayout.findViewById(R.id.nameEvent);
                                if (name.getText().length() == 0) {
                                    name.setText("Event sans nom");
                                }
                                final EditText location = (EditText) alertLayout.findViewById(R.id.locationEvent);
                                final EditText description = (EditText) alertLayout.findViewById(R.id.descrEvent);
                                final EditText departure = (EditText) alertLayout.findViewById(R.id.departureLocation);

                                if (date.getText().length() == 0 || time.getText().length() == 0 || location.getText().length() == 0) {
                                    Toast.makeText(MainActivity.this, "Veuillez remplir tout les champs.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String loc;
                                if (isGPS && lastLocation == null) {
                                    Toast.makeText(MainActivity.this, "Veuillez attendre la localisation", Toast.LENGTH_SHORT).show();
                                    return;
                                } else if (isGPS) {
                                    loc = lastLocation.getLatitude() + "," + lastLocation.getLongitude();
                                } else {
                                    loc = departure.getText().toString();
                                }

                                cal.set(Integer.parseInt(dateDetails[2]),
                                        Integer.parseInt(dateDetails[1]) - 1,
                                        Integer.parseInt(dateDetails[0]),
                                        Integer.parseInt(timeDetails[0]),
                                        Integer.parseInt(timeDetails[1]));
                                Date dateEvent = cal.getTime();
                                String nameEvent = name.getText().toString();
                                String locationEvent = location.getText().toString();
                                String descrEvent = description.getText().toString();
                                Log.d(TAG, "onClick: " + dateEvent.toString());

                                //DB : table event(id, name, date, location, descr, departure_time)
                                //Event (model) :  name, date, descr, location, departure_time
                                //String name, String date, String location, String descr, int id

                                Event new_Event = new Event(nameEvent, dateEvent, locationEvent, descrEvent);
                                try {
                                    Long duration = new DurationFetcher().execute(API_KEY, loc, locationEvent, dateEvent.getTime() + "").get();
                                    Log.d(TAG, "onClick: " + duration + " secondes");
                                    cal.setTimeInMillis(cal.getTimeInMillis() - duration * 1000);
                                    Date departureDate = cal.getTime();
                                    Log.d(TAG, "onClick: " + cal.getTime().toString());
                                    new_Event.setDepartureTime(departureDate);
                                } catch (ExecutionException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                                /*
                                try {
                                    dbHelper.openDataBase();
                                    dbHelper.addEventinDB(nameEvent, dateEvent, locationEvent, descrEvent, events.size() + 1, new_Event.getDepartureTime());
                                    dbHelper.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
*/

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                                Map<String, String> postData = new HashMap<>();
                                postData.put("name", new_Event.getName());
                                postData.put("date", sdf.format(new_Event.getDate()));
                                postData.put("location", new_Event.getLocation());
                                postData.put("description", new_Event.getDescr());
                                postData.put("departureTime", sdf.format(new_Event.getDepartureTime()));
                                SendEvent task = new SendEvent(postData);
                                task.execute();

                                events.add(new_Event);
                                EventAdapter adapter = new EventAdapter(getApplicationContext(), R.layout.event_card, events);
                                ListView list_event = (ListView) findViewById(R.id.event_lv);
                                list_event.setAdapter(adapter);

                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
            }
        });
        Handler handler = new Handler();
        int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                checkDepartureTime();
                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    private void checkDepartureTime() {
        for (Event e: events) {
            Date strDate = e.getDate();
            if (new Date().after(strDate)&& (!e.hasBeenNotified())){
                e.setHasBeenNotified(true);
                triggerNotification(e);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(MainActivity.this,SensorActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    100, mLocationListener);
        } else {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            requestPermissions(perms, 1337);
        }

    }

    private void triggerNotification(Event e) {

        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "News")
                .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                .setContentTitle(e.getName())
                .setContentText(e.getLocation() +" "+ e .getDate())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId("news")
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("news","News", NotificationManager.IMPORTANCE_DEFAULT );
            notificationChannel.setDescription("get information");
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }



}
