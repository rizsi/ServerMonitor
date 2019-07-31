package com.rizsi.servermonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rizsi.servermonitor.data.DataModel;
import com.rizsi.servermonitor.data.ServerEntry;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Auto-called by the system when the set up alarm time has passed.
 * We use this service for background work in every minute.
 */
public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            Log.d("Carbon", "Alrm worked !!" + Thread.currentThread().getName());

            //Toast.makeText(context, "Status download initiated----", Toast.LENGTH_SHORT).show();

            final LinkedBlockingQueue<ServerEntry> sesfinished=new LinkedBlockingQueue<>();
            final DataModel dataModel=DataModel.getInstance(context);
            dataModel.reload(context);
            final List<ServerEntry> entries=dataModel.list();
            RequestQueue queue=Volley.newRequestQueue(context);
            long t0=System.currentTimeMillis();
            int maxTimeout=10000;
            RetryPolicy retryPolicy=new DefaultRetryPolicy(
                    maxTimeout,
                    0,
                    0);
            int index=0;
            for(final ServerEntry se: entries)
            {
                se.index=index++;
                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, se.url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                sesfinished.add(se);
                                // Display the first 500 characters of the response string.
                                String head=response.substring(0, Math.min(response.length(), 48));
                                Log.d("WEB", ""+se.url+" Response is: "+ head);
//                                Toast.makeText(context, head, Toast.LENGTH_SHORT).show();
                                String status="Uninitialized";
                                try {
                                    JSONObject reader = new JSONObject(response);
                                    if(!"OK".equals(reader.getString("status")))
                                    {
                                        status="Status is not OK";
                                    }else
                                    {
                                        status="OK";
                                    }
                                    long t=reader.getLong("timestamp");
                                    long diff=Math.abs(t-System.currentTimeMillis());
                                    if(diff>70000)
                                    {
                                        status+=" OLD AT ";
                                        status+=reader.getString("date");
                                    }
                                }catch(Exception e)
                                {
                                    status="Error parsing";
                                }
                                checkReady(dataModel, sesfinished, entries, context, se, status);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                sesfinished.add(se);
                                Log.d("WEB", "That didn't work "+se.url);
                                checkReady(dataModel, sesfinished, entries, context, se, "ERROR");
                            }
                        });
                stringRequest.setRetryPolicy(retryPolicy);
                Log.d("WEB", "Send query: "+se.url);
                queue.add(stringRequest);
            }
            /*
            for(final ServerEntry se: entries) {
                long alreadyElapsed=System.currentTimeMillis()-t0;
                if(alreadyElapsed<maxTimeout) {
                    sesfinished.poll(maxTimeout-alreadyElapsed, TimeUnit.MILLISECONDS);
                    if(se==null) {
                        Log.d("WEB", "Finished: " + se.status);
                    }else
                    {
                        Log.d("WEB", "Timeout on some queries");
                    }
                }
            }
            queue.stop();
            */
            // BootReceiver.sendNotification(context, "");
            // queue.stop();
        }catch(Exception e)
        {
            Log.e("TIMER", "Error in timer: "+ e.getMessage());
        }
        finally {
            setAlarm(context);
        }
//        Intent intent2 = new Intent(context, TripNotification.class);
 //       intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  //      context.startActivity(intent2);
    }

    private void checkReady(DataModel dataModel, LinkedBlockingQueue<ServerEntry> sesfinished, List<ServerEntry> entries,
                            Context context, ServerEntry se, String newStatus) {
        String oldStatus=se.status;
        if(!newStatus.equals(oldStatus))
        {
            MainActivity ac=MainActivity.current;
            if(ac!=null) {
                ac.updateEntryStatus(se.index,newStatus);
            }else
            {
                Log.d("ALARM", "Main activity is null");

                DataModel dm=DataModel.getInstance(context);
                dm.reload(context);
                se=dm.list().get(se.index);
                ServerEntry newEntry=new ServerEntry(se.url, newStatus);
                newEntry.index=se.index;
                dm.updateEntry(newEntry);
                dm.saveSettings(context);

                Intent intent = new Intent(MainActivity.intentId);

                // put whatever data you want to send, if any
//                intent.putExtra("index", se.index);
//                intent.putExtra("status", newStatus);

                // send broadcast
                context.sendBroadcast(intent);
            }
            BootReceiver.sendNotification(context, "Status changed of a server");
        }
        if(sesfinished.size()==entries.size())
        {
            Log.d("WEB", "All finished");
            dataModel.saveSettings(context);
        }
    }

    public static void setAlarm(Context context){

        Log.d("Carbon","Alrm SET !!");

        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 30 seconds to the calendar object
        cal.add(Calendar.SECOND, 10);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+5000, sender);
//        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 10000, 10000, sender);
    }
}
