package com.rizsi.servermonitor.data;

import java.io.Serializable;

public class ServerEntry implements Serializable {
    public String url;
    public int index;
    public String status="";

    public ServerEntry(String url, String status) {
        this.url = url;
        this.status = status;
    }

    public ServerEntry cloneForDuplicate()
    {
        ServerEntry ret=new ServerEntry(url, "duplicate init");
        return ret;
    }
}
