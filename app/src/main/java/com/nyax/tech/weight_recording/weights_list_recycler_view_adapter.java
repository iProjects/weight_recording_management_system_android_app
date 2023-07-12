package com.nyax.tech.weight_recording;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;

// Create a RecyclerView adapter and ViewHolder
// Next, you have to inherit the RecyclerView.Adapter and the RecyclerView.ViewHolder. A usual class structure would be:
public class weights_list_recycler_view_adapter extends RecyclerView.Adapter<weights_list_recycler_view_adapter.ViewHolder> {

    //dataset
    private ArrayList<weight_ui_dto> _lstdtos;
    private static final String TAG = weights_list_recycler_view_adapter.class.getSimpleName();
    Context _context;
    String IPADRESS = "";

    //abstract method in Recycleview adapter for implementing endless scrolling
    //public abstract void load();

    // The adapter's constructor sets the used dataset:
    public weights_list_recycler_view_adapter(Context context, ArrayList<weight_ui_dto> _lst_dtos, String ipaddress) {
        _context = context;
        _lstdtos = _lst_dtos;
        IPADRESS = ipaddress;
    }

    // First, we implement the ViewHolder. It only inherits the default constructor and saves the needed views into some fields:
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txt_weight_id;
        private final TextView txt_weight_weight;
        private final TextView txt_weight_date;
        private final TextView txt_weight_app;
        private final TextView txt_weight_created_date;
        private final TextView txt_weight_status;

        private final ImageButton btn_edit_weight;
        private final ImageButton btn_delete_weight;

        public ViewHolder(View view) {
            super(view);

            txt_weight_id = view.findViewById(R.id.txt_weight_id);
            txt_weight_weight = view.findViewById(R.id.txt_weight_weight);
            txt_weight_date = view.findViewById(R.id.txt_weight_date);
            txt_weight_app = view.findViewById(R.id.txt_weight_app);
            txt_weight_created_date = view.findViewById(R.id.txt_weight_created_date);
            txt_weight_status = view.findViewById(R.id.txt_weight_status);

            btn_edit_weight = view.findViewById(R.id.btn_update_weight);
            btn_delete_weight = view.findViewById(R.id.btn_delete_weight);

            btn_edit_weight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int _pos = getAbsoluteAdapterPosition();

                    Log.e(TAG, "Element " + _pos + " clicked.");

                    final weight_ui_dto _dto = _lstdtos.get(_pos);

                    Log.e(TAG, "id: " + _dto.weight_id);
                    Log.e(TAG, "date: " + _dto.weight_date);
                    Log.e(TAG, "weight: " + _dto.weight_weight);
                    Log.e(TAG, "app: " + _dto.weight_app);
                    Log.e(TAG, "status: " + _dto.weight_status);
                    Log.e(TAG, "created date: " + _dto.created_date);

                    Bundle dataBundle = new Bundle();

                    dataBundle.putString("id", _dto.weight_id);
                    dataBundle.putString("ipaddress", IPADRESS);

                    // Starting new intent
                    Intent intent = new Intent(_context, edit_weight_activity.class);

                    // sending data to next activity
                    intent.putExtras(dataBundle);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                    // starting new activity
                    _context.startActivity(intent);

                }
            });

            btn_delete_weight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int _pos = getAbsoluteAdapterPosition();

                    Log.e(TAG, "Element " + _pos + " clicked.");

                    final weight_ui_dto _dto = _lstdtos.get(_pos);

                    Log.e(TAG, "id: " + _dto.weight_id);
                    Log.e(TAG, "date: " + _dto.weight_date);
                    Log.e(TAG, "weight: " + _dto.weight_weight);
                    Log.e(TAG, "app: " + _dto.weight_app);
                    Log.e(TAG, "status: " + _dto.weight_status);
                    Log.e(TAG, "created date: " + _dto.created_date);

                    Bundle dataBundle = new Bundle();

                    dataBundle.putString("id", _dto.weight_id);
                    dataBundle.putString("ipaddress", IPADRESS);

                    // Starting new intent
                    Intent intent = new Intent(_context, delete_weight_activity.class);

                    // sending data to next activity
                    intent.putExtras(dataBundle);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                    // starting new activity
                    _context.startActivity(intent);

                }
            });

        }
    }

    // To use our custom list item layout, we override the method onCreateViewHolder(...). //In this example, the layout file is called crops_list_recycler_view_layout.xml.
    // Create new views (invoked by the layout manager).
    //inflate rows.
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weight_list_recycler_view_row_layout, parent, false);
        return new ViewHolder(view);
    }

    // In the onBindViewHolder(...), we actually set the views' contents. We get the used model by finding it in the List at the given position and then set image and name on the ViewHolder's views.
    //populate row with data.
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if ((position >= getItemCount() - 1)) {
            //call Load() method
            //load();
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final weight_ui_dto _dto = _lstdtos.get(position);

        viewHolder.txt_weight_id.setText("Id: " + _dto.weight_id);
        viewHolder.txt_weight_weight.setText("Weight: " + _dto.weight_weight);
        viewHolder.txt_weight_date.setText("Date: " + _dto.weight_date);
        viewHolder.txt_weight_app.setText("App: " + _dto.weight_app);
        viewHolder.txt_weight_status.setText("Status: " + _dto.weight_status);
        viewHolder.txt_weight_created_date.setText("Created Date: " + _dto.created_date);

    }

    // We also need to implement getItemCount(), which simply return the List's size.
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _lstdtos.size();
    }


}
