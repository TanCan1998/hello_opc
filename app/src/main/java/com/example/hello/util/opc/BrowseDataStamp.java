package com.example.hello.util.opc;

public class BrowseDataStamp {
    public String name;
    public String namespace;
    public String nodeindex;
    public String nodeclass;
    public int    position;

    public BrowseDataStamp(String name, String namespace, String nodeindex, String nodeclass, int position) {
        this.name = name;
        this.namespace = namespace;
        this.nodeindex = nodeindex;
        this.nodeclass = nodeclass;
        this.position = position;
    }

}
