package com.rizsi.smsforwarder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class MainActivity extends AppCompatActivity {

    public static final String keySourceToForward="source.txt";
    public static final String keySourceToForward2="source2.txt";
    public static final String keySourceToForward3="source3.txt";
    public static final String keySourceToForward4="source4.txt";
    public static final String keySourceToForward5="source5.txt";
    public static final String keyTargetPhone="target.txt";
    public static final String keyShowAll="showall.txt";
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
        sendSms(this, "Test message!");
    }

    public static void sendSms(Context c, String message)
    {
        String target=loadKey(c, keyTargetPhone);
        if(target.length()>0) {
            try {
                SmsManager.getDefault().sendTextMessage(target, null, message, null, null);
                Toast.makeText(c, "SMS forwarded to: " + target, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("", "Error sending SMS", e);
                Toast.makeText(c, "Cannot send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else
        {
            Toast.makeText(c, "Cannot send SMS: target number is not set", Toast.LENGTH_SHORT).show();
        }
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
            Toast.makeText(c, "SMS from: "+sender+"\n"+(sourceToForwardFound==null?"MATCHES":"no match"), Toast.LENGTH_SHORT).show();
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
