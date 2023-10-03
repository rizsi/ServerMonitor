package com.rizsi.smsforwarder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    public static final String keySourceToForward="source.txt";
    public static final String keySourceToForward2="source2.txt";
    public static final String keySourceToForward3="source3.txt";
    public static final String keySourceToForward4="source4.txt";
    public static final String keySourceToForward5="source5.txt";
    public static final String keyTargetHttps="targetHttps.txt";
    public static final String keyTargetPhone="target.txt";
    public static final String keyTestMessage="testMessage.txt";
    public static final String keyShowAll="showall.txt";


    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("text/plain; charset=%s", PROTOCOL_CHARSET);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckBox checkBoxShowAllSMSNumber=(CheckBox)findViewById(R.id.checkBoxShowAllSMSNumber);
        boolean savedValue="true".equals(loadKey(this, keyShowAll));
        checkBoxShowAllSMSNumber.setChecked(savedValue);
        checkBoxShowAllSMSNumber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                saveKey(MainActivity.this, keyShowAll, ""+isChecked);
            }
        });
        initEditText(R.id.sourceToForward, keySourceToForward);
        initEditText(R.id.sourceToForward2, keySourceToForward2);
        initEditText(R.id.sourceToForward3, keySourceToForward3);
        initEditText(R.id.sourceToForward4, keySourceToForward4);
        initEditText(R.id.sourceToForward5, keySourceToForward5);
        initEditText(R.id.targetHttps, keyTargetHttps);
        initEditText(R.id.testMessage, keyTestMessage);
        EditText targetPhone = (EditText)findViewById(R.id.targetPhone);
        targetPhone.setText(loadKey(this, keyTargetPhone));
        targetPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                saveKey(MainActivity.this, keyTargetPhone, s.toString());
//                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initEditText(int id, final String key) {
        EditText sourceToForward = (EditText)findViewById(id);
        sourceToForward.setText(loadKey(this, key));
        sourceToForward.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                saveKey(MainActivity.this, key, s.toString());
//                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onRequirePermissions(View v) {
//        Toast.makeText(this, "HELLO!", Toast.LENGTH_SHORT).show();
        askPermission(Manifest.permission.RECEIVE_SMS);
        askPermission(Manifest.permission.SEND_SMS);
        askPermission(Manifest.permission.READ_PHONE_STATE);
        askPermission(Manifest.permission.INTERNET);
        Toast.makeText( this, "All permission checked!", Toast.LENGTH_SHORT).show();
    }
    private void askPermission(String perm)
    {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            Log.w("", "No permission for: "+perm);
            ActivityCompat.requestPermissions(this, new String[]{perm}, 0);
        }
    }
    public void onSendSMS(View v) {
        String testMessage=loadKey(this, keyTestMessage);
        sendSms(this, testMessage);
    }

    public static void sendSms(Context c, String message)
    {
        String target=loadKey(c, keyTargetPhone);
        boolean sentSms=false;
        boolean sentHttp=false;
        if(target.length()>0) {
            sentSms=true;
            try {
                SmsManager.getDefault().sendTextMessage(target, null, message, null, null);
                Toast.makeText(c, "SMS forwarded to: " + target, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("", "Error sending SMS", e);
                Toast.makeText(c, "Cannot send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        String targetHttps=loadKey(c, keyTargetHttps);
        if(targetHttps.length()>0)
        {
            sentHttp=true;
            sendSmsToHttps(c, message, targetHttps);
        }
        if(!sentHttp && ! sentSms)
        {
            Toast.makeText(c, "Cannot send SMS: target number ot https is not set", Toast.LENGTH_SHORT).show();
        }
    }

    private static void configureCookie()
    {
    }

    private static boolean initialized=false;

    private static void sendSmsToHttps(final Context context, final String message, final String targetHttps) {
        if(!initialized)
        {
            initialized=true;

            CookieManager manager = new CookieManager();
            CookieHandler.setDefault( manager  );

            // TODO cookie setup is hard coded
            HttpCookie cookie = new HttpCookie("cookie-consent", "accepted");
            cookie.setPath("/");
            cookie.setVersion(0);
            cookie.setDomain("rizsi.com");
            try {
                ((CookieManager)CookieHandler.getDefault()).getCookieStore().add(new URI("https://rizsi.com"), cookie);
            } catch (URISyntaxException e) {
                Toast.makeText(context, "Can not create cookie: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        final RequestQueue queue= Volley.newRequestQueue(context);
        long t0=System.currentTimeMillis();
        RetryPolicy retryPolicy=new DefaultRetryPolicy(
                5000,
                0,
                2);
        int index=0;
        // JsonRequest
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, targetHttps+"?msg="+ Uri.encode(message),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                            Toast.makeText(context, "onResponse", Toast.LENGTH_SHORT).show();
                            queue.stop();
                            // Display the first 500 characters of the response string.
                            String head="SMS forwarder onResponse: "+response.substring(0, Math.min(response.length(), 48));
                            Log.d("WEB", ""+targetHttps+" Response is: "+ head);
                            Toast.makeText(context, head, Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            queue.stop();
                            String response="SMS forwarder onErrorResponse: "+error.getMessage();
                            String head=response.substring(0, Math.min(response.length(), 48));
                            Log.d("WEB", ""+targetHttps+"ERROR Response is: "+ head);
                            Toast.makeText(context, head, Toast.LENGTH_SHORT).show();
                        }
                    }){
/*                @Override
                public byte[] getBody() throws AuthFailureError {
                    return message.getBytes(Charset.forName(PROTOCOL_CHARSET));
                }
                @Override
                public String getBodyContentType() {
                    return PROTOCOL_CONTENT_TYPE;
                }*/
            };
            stringRequest.setRetryPolicy(retryPolicy);
            Log.d("WEB", "Send query: "+targetHttps);
            queue.add(stringRequest);
        Toast.makeText(context, "SMS forwarder Send https...", Toast.LENGTH_SHORT).show();
    }

    public static void smsReceived(Context c,String sender, String message) {
        String[] sources=new String[]{keySourceToForward, keySourceToForward2, keySourceToForward3, keySourceToForward4, keySourceToForward5};
        String sourceToForwardFound=null;
        for(String s: sources)
        {
            String sourceToForward=loadKey(c, s);
            if(sourceToForward.length()>0 && sender.equals(sourceToForward))
            {
                sourceToForwardFound=sourceToForward;
            }
        }
        if("true".equals(loadKey(c, keyShowAll)))
        {
            Toast.makeText(c, "SMS from: "+sender+"\n"+(sourceToForwardFound==null?"no match":"MATCHES"), Toast.LENGTH_SHORT).show();
        }
        if(sourceToForwardFound!=null)
        {
            sendSms(c, message);
        }
    }

    public static void saveKey(Context c, String key, String value)
    {
        try {
            Log.d("DATA",  "Save key: "+key+" "+value);
            FileOutputStream fOut=c.openFileOutput(key, Context.MODE_PRIVATE);
            try {
                Writer wr=new OutputStreamWriter(fOut, "UTF-8");
                wr.write(value);
                wr.close();
            }finally
            {
                fOut.close();
            }
        }catch(Exception e)
        {
            Log.e("DATA",  "Save key: "+key+" "+value, e);
        }
    }
    public static String loadKey(Context c, String key)
    {
        try {
            Log.d("DATA",  "Load key: "+key);
            InputStream is=c.openFileInput(key);
            try {
                Reader r = new InputStreamReader(is, "UTF-8");
                StringBuilder sb=new StringBuilder();
                int ch;
                while((ch=r.read())!=-1)
                {
                    sb.append((char)ch);
                }
                return sb.toString();
            } finally {
                is.close();
            }
        }catch(Exception e)
        {
            Log.e("DATA",  "Load key: "+key, e);
        }
        return "";
    }
}
