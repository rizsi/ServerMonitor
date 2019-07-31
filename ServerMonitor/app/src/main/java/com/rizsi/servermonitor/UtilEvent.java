package com.rizsi.servermonitor;

import java.util.ArrayList;
import java.util.List;

public class UtilEvent {
    public interface Listener
    {
        void eventHappened();
    }
    private List<Listener> listeners=new ArrayList<>();
    public void addListener(Listener l)
    {
        synchronized (this)
        {
            listeners.add(l);
        }
    }
    public void removeListener(Listener l)
    {
        synchronized (this)
        {
            listeners.remove(l);
        }
    }
    public void eventHappened()
    {
        List<Listener> listeners;
        synchronized (this)
        {
            listeners=new ArrayList<>(this.listeners);
        }
        for(Listener l:listeners)
        {
            l.eventHappened();
        }
    }
}
