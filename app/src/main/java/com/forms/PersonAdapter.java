package com.forms;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created on 9/16/2016.
 */
public class PersonAdapter extends ArrayAdapter<PersonObject> {

    public PersonAdapter(Context context, int resource, PersonObject[] objects) {
        super(context, resource, objects);
    }

    public PersonAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        if (getItem(position).isDraft()) {
            tv.setText("[Draft] " + getItem(position).getTitle());
        } else {
            tv.setText(getItem(position).getTitle());
        }
        return view;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
}
