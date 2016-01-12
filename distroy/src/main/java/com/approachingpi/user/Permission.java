package com.approachingpi.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Enumeration;

public class Permission {
    private int id = 0;
    private String key = "";
    private String desc = "";

    public Permission() {
        init();
    }

    public Permission(int id, String key, String desc) {
        init();
        setId(id);
        setKey(key);
        setDesc(desc);
    }

    private void init() {
    }

    public int getId() { return this.id; }
    public String getDesc() { return this.desc; }
    public String getKey() { return this.key; }

    public void setId(int in) { this.id = in; }
    public void setDesc(String in) { this.desc = (in == null)?"":in; }
    public void setKey(String in) { this.key = (in == null)?"":in; }
}
