package com.forms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created on 10/12/2016.
 */

public class FormsAdapter extends ArrayAdapter<FormObject> {


    public FormsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public FormsAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public FormsAdapter(Context context, int resource, FormObject[] objects) {
        super(context, resource, objects);
    }

    public FormsAdapter(Context context, int resource, int textViewResourceId, FormObject[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public FormsAdapter(Context context, int resource, List<FormObject> objects) {
        super(context, resource, objects);
    }

    public FormsAdapter(Context context, int resource, int textViewResourceId, List<FormObject> objects) {
        super(context, resource, textViewResourceId, objects);
    }


    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getItem(position).getName());

        return view;
    }


}
