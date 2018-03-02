package com.example.svenu.guldendata;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by svenu on 20-2-2018.
 */

public class GuldenDataAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<GuldenData> guldenData;

    public GuldenDataAdapter(Context context, ArrayList<GuldenData> data) {
        super(context, R.layout.row_data, data);
        this.context = context;
        this.guldenData = data;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = layoutInflater.inflate(R.layout.row_data, parent, false);

        GuldenData gulden = guldenData.get(position);

        Date d = new Date(gulden.getTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm"); // HH for 0-23
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = df.format(d);

        String factor = String.valueOf(gulden.getFactor());

        TextView timeTextView = rootView.findViewById(R.id.textViewTime);
        TextView factorTextView = rootView.findViewById(R.id.textViewFactor);

        timeTextView.setText(time);
        factorTextView.setText(factor);

        return rootView;
    }
}
