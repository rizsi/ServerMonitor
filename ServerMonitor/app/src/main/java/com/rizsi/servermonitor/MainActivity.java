package com.rizsi.servermonitor;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.rizsi.servermonitor.data.DataModel;
import com.rizsi.servermonitor.data.ServerEntry;

import java.io.FileOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static com.rizsi.servermonitor.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    public RequestQueue queue;
//    private Button toggleServiceButton;

    volatile public static MainActivity current;

    public static final String intentId="ServerMonitorUpdateUI";
    DataModel dataModel;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MAIN","Event received!");
            dataModel.reload(context);
            recyclerView.getAdapter().notifyDataSetChanged();
            // Extract data included in the Intent
            // String message = intent.getStringExtra("message");
            //update the TextView
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.list);
//        toggleServiceButton =(Button) findViewById(R.id.toggleService);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        dataModel=DataModel.getInstance(this);
        MyListAdapter mAdapter = new MyListAdapter(this, dataModel);
        recyclerView.setAdapter(mAdapter);
        AlarmReceiver.setAlarm(getApplicationContext());
//        updateToggleServiceButtonLabel();
        BootReceiver.deleteNotification(getApplicationContext());
        current=this;
        this.registerReceiver(mMessageReceiver, new IntentFilter(intentId));
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mMessageReceiver, new IntentFilter(intentId));
    }

    public void toggleService(View view)
    {
//        MonitorService.startPeriodic(getApplicationContext());
 //       MonitorService2.reschedule(getApplicationContext());
        AlarmReceiver.setAlarm(getApplicationContext());
//        updateToggleServiceButtonLabel();
    }

/*    private void updateToggleServiceButtonLabel() {
        JobScheduler jobScheduler = (JobScheduler)getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);
        if(jobScheduler.getAllPendingJobs().size()==0)
        {
            toggleServiceButton.setText("Start background service");
        }else
        {
            toggleServiceButton.setText("Stop background service");
        }
    }
    */

    public void addServerButton(View view) {
        startActivityForResult(new Intent(this, AddServerActivity.class), 1);
        // intent.putExtra(EXTRA_MESSAGE, message);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                ServerEntry newServerEntry = (ServerEntry)data.getSerializableExtra(AddServerActivity.resultKey);
                DataModel dm=DataModel.getInstance(getApplicationContext());
                dm.add(newServerEntry);
                dm.saveSettings(getApplicationContext());
            }
        }else if(requestCode==2)
        {
            if(resultCode == RESULT_OK) {
                int deleteIndex = data.getIntExtra(AddServerActivity.resultDeleteIndex, -1);
                if(deleteIndex>=0)
                {
                    DataModel dm=DataModel.getInstance(getApplicationContext());
                    dm.deleteByIndex(deleteIndex);
                    dm.saveSettings(getApplicationContext());
                    Log.d("MAIN", "Edit returned delete "+deleteIndex);
                }
                int duplicateIndex = data.getIntExtra(AddServerActivity.resultDuplicateIndex, -1);
                if(duplicateIndex>=0)
                {
                    DataModel dm=DataModel.getInstance(getApplicationContext());
                    dm.duplicateByIndex(duplicateIndex);
                    dm.saveSettings(getApplicationContext());
                    Log.d("MAIN", "Edit returned duplicate "+deleteIndex);
                }
                ServerEntry updatedServerEntry= (ServerEntry)data.getSerializableExtra(AddServerActivity.resultKey);
                if(updatedServerEntry!=null)
                {
                    DataModel dm=DataModel.getInstance(getApplicationContext());
                    dm.updateEntry(updatedServerEntry);
                    dm.saveSettings(getApplicationContext());
                    Log.d("MAIN", "Edit returned update "+updatedServerEntry.index);
                }
                Log.d("MAIN", "Edit returned");
//                ServerEntry newServerEntry = (ServerEntry)data.getSerializableExtra(AddServerActivity.resultKey);
//                dataModel.add(newServerEntry);
            }
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }
    public void editServer(int position)
    {
        Intent i=new Intent(this, AddServerActivity.class);
        i.putExtra(AddServerActivity.resultKey, position+1);
        startActivityForResult(i, 2);
    }

    public void updateEntryStatus(int index, String newStatus) {
        Log.d("MAIN", "Updated status query");
        if(!isDestroyed()&&!isFinishing())
        {
            Log.d("MAIN", "Updated status query inside");
//            dataModel.updateEntry(newentry);
            DataModel dm=DataModel.getInstance(getApplicationContext());
            ServerEntry se=dm.list().get(index);
            ServerEntry newse=new ServerEntry(se.url, newStatus);
            newse.index=index;
            dm.updateEntry(newse);
            dm.saveSettings(getApplicationContext());
            recyclerView.getAdapter().notifyDataSetChanged();
            Log.d("MAIN", "Updated status: "+index);
        }
    }
}
