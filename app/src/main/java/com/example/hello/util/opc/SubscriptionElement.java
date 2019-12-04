package com.example.hello.util.opc;


import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.CreateMonitoredItemsRequest;
import org.opcfoundation.ua.core.CreateMonitoredItemsResponse;
import org.opcfoundation.ua.core.CreateSubscriptionResponse;
import org.opcfoundation.ua.core.SubscriptionAcknowledgement;

import java.util.ArrayList;
import java.util.List;


public class SubscriptionElement {
    private CreateSubscriptionResponse subscription;
    private List<MonitoredItemElement> monitoredItems;
    private SessionChannel             sessionChannel;

    private SubscriptionAcknowledgement subAck;

    public void setLastSeqNumber(UnsignedInteger lastSeqNumber) {
        subAck.setSequenceNumber(lastSeqNumber);
    }


    public SubscriptionAcknowledgement getSubAck() {
        return subAck;
    }

    public SubscriptionElement(CreateSubscriptionResponse subscription, SessionChannel sessionChannel) {
        this.subscription = subscription;
        this.monitoredItems = new ArrayList<>();
        this.sessionChannel = sessionChannel;

        subAck = new SubscriptionAcknowledgement();
        subAck.setSubscriptionId(new UnsignedInteger(subscription.getSubscriptionId()));

    }

    public CreateSubscriptionResponse getSubscription() {
        return subscription;
    }

    public List<MonitoredItemElement> getMonitoredItems() {
        return monitoredItems;
    }

    public SessionChannel getSession() {
        return sessionChannel;
    }

    public int CreateMonitoredItem(CreateMonitoredItemsRequest request)
            throws ServiceResultException, MonItemNotCreatedException {
        CreateMonitoredItemsResponse response = sessionChannel.CreateMonitoredItems(request);
        if (response.getResults()[0].getStatusCode().getValue().intValue() != StatusCode.GOOD.getValue()
                .intValue()) {
            throw new MonItemNotCreatedException(response.getResults()[0].getStatusCode()
                    .getDescription());
        }
        monitoredItems.add(new MonitoredItemElement(response, request));
        return monitoredItems.size() - 1;
    }
}