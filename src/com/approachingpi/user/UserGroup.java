package com.approachingpi.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Enumeration;

public class UserGroup {
    private int id = 0;
    private String name = "";
    private String desc = "";

    public UserGroup() {
        init();
    }

    public UserGroup(int id, String name, String desc) {
        init();
        setId(id);
        setName(name);
        setDesc(desc);
    }

    private void init() {
    }

    public int getId() { return this.id; }
    public String getDesc() { return this.desc; }
    public String getName() { return this.name; }

    public void setId(int in) { this.id = in; }
    public void setDesc(String in) { this.desc = (in == null)?"":in; }
    public void setName(String in) { this.name = (in == null)?"":in; }
}
