package com.rizsi.servermonitor.data;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DataModel {
    private static DataModel instance;
    private static final String settingsFile="servers.xml";

    public static DataModel getInstance(Context c) {
        if(instance==null)
        {
            instance=new DataModel();
            instance.reload(c);
        }
        return instance;
    }

    public void reload(Context c) {
        servers.clear();
        try {
            Log.d("DATA",  "Loading data model!!");
            InputStream is=c.openFileInput(settingsFile);
            try {
                Reader r = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(r);
                String line;
                int index=0;
                while((line=br.readLine())!=null)
                {
                    String status="";
                    int separator=line.indexOf(' ');
                    if(separator>0)
                    {
                        status=line.substring(separator+1);
                        line=line.substring(0, separator);
                    }
                    ServerEntry se=new ServerEntry(line, status);
                    se.index=index;
                    Log.d("DATA",  "Loading data model: "+se.url);
                    instance.add(se);
                    index++;
                }
            } finally {
                is.close();
            }
        }catch(Exception e)
        {
            Log.e("DATA",  "Loading data model: "+e.getMessage());
        }
    }

    private List<ServerEntry> servers=new ArrayList<>();
    public void add(ServerEntry serverEntry)
    {
        servers.add(serverEntry);
        serverEntry.index=servers.size()-1;
    }
    public void deleteByIndex(int index){servers.remove(index);}
    public void duplicateByIndex(int index){
        ServerEntry toDuplicate=servers.get(index);
        ServerEntry toadd=toDuplicate.cloneForDuplicate();
        servers.add(toadd);
        toadd.index=servers.size()-1;
    }
    public void updateEntry(ServerEntry updatedServerEntry){
        Log.d("DATA", "Update entry: "+updatedServerEntry.index+" "+updatedServerEntry.url+" "+updatedServerEntry.status);
        servers.set(updatedServerEntry.index, updatedServerEntry);
    }
    public int getSize()
    {
        return servers.size();
    }

    public ServerEntry get(int position) {
        return servers.get(position);
    }

    public void saveSettings(Context context)
    {
        try {
            FileOutputStream fOut = context.openFileOutput(settingsFile, Context.MODE_PRIVATE);
            try {
                Writer wr=new OutputStreamWriter(fOut, "UTF-8");
                for(ServerEntry se: servers)
                {
                    wr.write(se.url);
                    wr.write(" ");
                    wr.write(se.status);
                    wr.write("\n");
                    Log.d("DATA",  "Saving data model: "+se.url+" "+se.status);
                }
                wr.close();
            }finally
            {
                fOut.close();
            }
        }catch (Exception e)
        {
            Log.e("DATA",  "Saving data model: "+e.getMessage());
        }
    }
    public List<ServerEntry> list()
    {
        return new ArrayList<>(servers);
    }
}
