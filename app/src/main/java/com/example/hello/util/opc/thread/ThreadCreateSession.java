package com.example.hello.util.opc.thread;

import android.os.Handler;
import android.os.Message;

import com.example.hello.util.opc.ManagerOPC;

import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.EndpointDescription;

public class ThreadCreateSession extends Thread {

    private Handler             handler;
    private ManagerOPC          manager;
    private EndpointDescription endpointDescription;
    private String              url;
    private int                 position = -1;
    private boolean             sent     = false;

    public ThreadCreateSession(ManagerOPC manager, String url, EndpointDescription endpointDescription) {
        this.manager = manager;
        this.endpointDescription = endpointDescription;
        this.url = url;
    }

    private synchronized void send(Message msg) {
        if (!sent) {
            msg.sendToTarget();
            sent = true;
        }
    }

    public void start(Handler handler) {
        super.start();
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        position = manager.CreateSession(url, endpointDescription);
                        send(handler.obtainMessage(0, position));
                    } catch (ServiceResultException e) {
                        send(handler.obtainMessage(-1, e.getStatusCode()));
                    }
                }
            });
            t.start();
            t.join(8000);
            send(handler.obtainMessage(-2));
        } catch (InterruptedException e) {
            send(handler.obtainMessage(-2));
        }

    }
}
