package com.example.zaki_berouk.savedbythebell.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zaki_berouk.savedbythebell.R;
import com.example.zaki_berouk.savedbythebell.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends ArrayAdapter<Event> {

    private final Context mContext;
    private List<Event> mEvents;
    public TextView name, date, location, descr, departure_time;

    public EventAdapter(Context context, int resource, List<Event> objects) {
        super(context, resource, objects);
        this.mContext = context;
        mEvents = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.event_card, parent, false);
        } else {
            convertView = (ConstraintLayout) convertView;
        }

        name = (TextView) convertView.findViewById(R.id.nameEvent);
        date = (TextView) convertView.findViewById(R.id.dateEvent);
        location = (TextView) convertView.findViewById(R.id.locationEvent);
        descr = (TextView) convertView.findViewById(R.id.descrEvent);
        departure_time = (TextView)  convertView.findViewById(R.id.departure_timeEvent);

        Event event = mEvents.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.FRANCE);

        name.setText(event.getName());
        date.setText(dateFormat.format(event.getDate()));
        location.setText(event.getLocation());
        if(event.getDescr() == null || event.getDescr() == ""){
            descr.setText("Il n'y a pas de description...");
        } else {
            descr.setText(event.getDescr());
        }

        if(event.getDepartureTime() == null || event.getDepartureTime().toString() == ""){
            departure_time.setText("A calculer...");
        } else {
            departure_time.setText(dateFormat.format(event.getDepartureTime()));
        }


        return convertView;
    }
}