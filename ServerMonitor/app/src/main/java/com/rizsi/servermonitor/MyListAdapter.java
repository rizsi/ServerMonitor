package com.rizsi.servermonitor;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rizsi.servermonitor.data.DataModel;
import com.rizsi.servermonitor.data.ServerEntry;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.MyViewHolder> {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View textView;
        public MyViewHolder(View v) {
            super(v);
            textView = v;
        }
    }
    private DataModel mDataset;
    MainActivity mainActivity;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyListAdapter(MainActivity mainActivity, DataModel myDataset) {
        this.mainActivity=mainActivity;
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        ServerEntry se= mDataset.get(position);
        LinearLayout ll=(LinearLayout)holder.textView;
        TextView tv=(TextView) ll.findViewById(R.id.serverEntryUrl);
        tv.setText(se.url);
        Button b=(Button) ll.findViewById(R.id.buttonEdit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MY", "Edit server button pressed "+position+"!");
                mainActivity.editServer(position);
            }
        });
        final TextView tv2=(TextView) ll.findViewById(R.id.returnText);
        // holder.
        // - replace the contents of the view with that element
        // holder.textView.setText(mDataset[position]);
        // Instantiate the RequestQueue.
//        String url ="http://www.google.com";

        // Request a string response from the provided URL.


        StringRequest stringRequest = new StringRequest(Request.Method.GET, se.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        tv2.setText("Response is: "+ response.substring(0, Math.min(response.length(), 48)));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tv2.setText("That didn't work!");
            }
        });

        tv2.setText(se.status);

// Add the request to the RequestQueue.
// TODO       mainActivity.queue.add(stringRequest);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.getSize();
    }
}
