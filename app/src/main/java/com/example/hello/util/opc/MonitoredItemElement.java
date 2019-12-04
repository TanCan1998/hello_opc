package com.example.hello.util.opc;

import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.MonitoredItemNotification;

import java.util.LinkedList;

public class MonitoredItemElement {
    private CreateMonitoredItemsResponse          monitoredItem;
    private CreateMonitoredItemsRequest           monitoredItemRequest;
    private LinkedList<MonitoredItemNotification> readings;
    public static final int                       BUFFER_SIZE = 5;

    public MonitoredItemElement(CreateMonitoredItemsResponse monitoredItem,
                                CreateMonitoredItemsRequest monitoredItemRequest) {
        this.monitoredItem = monitoredItem;
        this.monitoredItemRequest = monitoredItemRequest;
        readings = new LinkedList<>();
    }

    public CreateMonitoredItemsResponse getMonitoredItem() {
        return monitoredItem;
    }

    public LinkedList<MonitoredItemNotification> getReadings() {
        return readings;
    }

    public void insertNotification(MonitoredItemNotification notification) {
        if (readings.size() == BUFFER_SIZE)
            readings.removeLast();
        readings.addFirst(notification);
    }

    public CreateMonitoredItemsRequest getMonitoredItemRequest() {
        return monitoredItemRequest;
    }
}
