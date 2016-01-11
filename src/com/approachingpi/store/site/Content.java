/*
 * Content.java
 *
 * Created on July 31, 2004, 2:10 PM
 *
 * @author Terrence Curran
 */

package com.approachingpi.store.site;

import com.approachingpi.user.User;

import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class Content {
    int     accessRequired;
    String  blurb           = "";
    String  bodyHtml        = "";
    String  bodyText        = "";
    Date    dateCreated;
    Date    dateModified;
    Date    dateRelease;
    int     id;
    String  title           = "";
    String  url             = "";

    /** Creates a new instance of Content */
    public Content() {
    }

    public Content(int id) {
        setId(id);
    }

    /**
     * Getter for property accessRequired.
     * @return Value of property accessRequired.
     */
    public int getAccessRequired() {
        return accessRequired;
    }
    /**
     * Getter for property blurb.
     * @return Value of property blurb.
     */
    public java.lang.String getBlurb() {
        return blurb;
    }
    public java.lang.String getBodyHtml() {
        return bodyHtml;
    }
    /**
     * Getter for property bodyHtml.
     * @return Value of property bodyHtml.
     */
    public java.lang.String getBodyHtmlFormatted() {
        String retVal = new String(bodyHtml);
        //retVal = retVal.replaceAll("\\f\\r","\n");
        retVal = retVal.replaceAll("\\r","");
        retVal = retVal.replaceAll("\\f","");
        retVal = retVal.replaceAll("\\n\\n","</p><p>");
        retVal = "<p>" + retVal + "</p>";
        return retVal;
    }
    /**
    * Getter for property bodyText.
    * @return Value of property bodyText.
    */
    public java.lang.String getBodyText() {
        return bodyText;
    }
    /**
     * Getter for property dateCreated.
     * @return Value of property dateCreated.
     */
    public java.util.Date getDateCreated() {
        return dateCreated;
    }
    /**
     * Getter for property dateModified.
     * @return Value of property dateModified.
     */
    public java.util.Date getDateModified() {
        return dateModified;
    }
    /**
     * Getter for property dateRelease.
     * @return Value of property dateRelease.
     */
    public java.util.Date getDateRelease() {
        return dateRelease;
    }
    /**
     * Setter for property dateRelease.
     * @param dateRelease New value of property dateRelease.
     */
    public void setDateRelease(java.util.Date dateRelease) {
        this.dateRelease = dateRelease;
    }
    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return id;
    }
    /**
     * Getter for property title.
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }
    /**
     * Getter for property url.
     * @return Value of property url.
     */
    public java.lang.String getUrl() {
        return url;
    }

    public static ArrayList getAllContent(Connection con) {
        return getAllContent(con, User.TYPE_PUBLIC, 0);
    }
    public static ArrayList getAllContent(Connection con, int access, int max) {
        ArrayList allContent = new ArrayList();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbContent WHERE inAccessRequired <= ? ORDER BY inAccessRequired DESC, vcUrl");
            ps.setInt(1,access);

            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
                Content content = new Content(rs.getInt("inId"));
                content.loadFromRs(rs);
                allContent.add(content);
                if (count == max) {
                    break;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allContent;
    }

    public void loadFromDb(Connection con) throws Exception {
        if (getId() == 0) { return; }

        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbContent WHERE inId = ? and inAccessRequired<=? ORDER BY inAccessRequired DESC");
        ps.setInt(1,getId());
	    ps.setInt(2,this.getAccessRequired());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            loadFromRs(rs);
        }
        rs.close();
    }

    public void loadFromDbByUrl(Connection con) throws Exception {
        System.out.println("DISTROY URL:|" + getUrl() + "|");
        if (getUrl().length()==0) { return; }

        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbContent WHERE vcUrl = ? and inAccessRequired<=? ORDER BY inAccessRequired DESC, vcUrl");
        ps.setString(1,getUrl());
	    ps.setInt(2,this.getAccessRequired());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            setId(rs.getInt("inId"));
            loadFromRs(rs);
        }
        rs.close();
    }

    public void loadFromRs(ResultSet rs) throws Exception {
        setTitle(rs.getString("vcTitle"));
        setUrl(rs.getString("vcUrl"));
        setAccessRequired(rs.getInt("inAccessRequired"));
        try {
            setDateCreated(new Date(rs.getTimestamp("dtCreated").getTime()));
        } catch (Exception e) {}
        try {
            setDateModified(new Date(rs.getTimestamp("dtModified").getTime()));
        } catch (Exception e) {}
        setDateRelease(rs.getDate("dtRelease"));
        setBlurb(rs.getString("txBlurb"));
        setBodyHtml(rs.getString("txBodyHtml"));
        setBodyText(rs.getString("txBodyText"));
    }

    public void saveToDb(Connection con) throws Exception {

        PreparedStatement ps;
        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbContent SET vcTitle=?, vcUrl=?, inAccessRequired=?, dtModified=CURRENT_TIMESTAMP, dtRelease=?, txBlurb=?, txBodyHtml=?, txBodyText=? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbContent (vcTitle, vcUrl, inAccessRequired, dtCreated, dtModified, dtRelease, txBlurb, txBodyHtml, txBodyText) VALUES(?,?,?,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,?,?,?,?)");
        }
        int i=0;
        ps.setString(++i,getTitle());
        ps.setString(++i,getUrl());
        ps.setInt(++i,getAccessRequired());
        if (getDateRelease() == null) {
            ps.setDate(++i,null);
        } else {
            ps.setDate(++i,new java.sql.Date(getDateRelease().getTime()));
        }
        ps.setString(++i,getBlurb());
        ps.setString(++i,getBodyHtml());
        ps.setString(++i,getBodyText());
        if (getId() > 0) {
            ps.setInt(++i,getId());
        }
        ps.execute();

        if (getId() == 0) {
            ps = con.prepareStatement("SELECT Max(inId) as inMaxId FROM tbContent WHERE vcTitle=?");
            ps.setString(1,getTitle());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                setId(rs.getInt("inMaxId"));
            }
            rs.close();
        }

    }

    /**
     * Setter for property url.
     * @param url New value of property url.
     */
    public void setUrl(java.lang.String url) {
        this.url = (url==null) ? "" : url;
    }
    /**
     * Setter for property accessRequired.
     * @param accessRequired New value of property accessRequired.
     */
    public void setAccessRequired(int accessRequired) {
        this.accessRequired = accessRequired;
    }
    /**
     * Setter for property blurb.
     * @param blurb New value of property blurb.
     */
    public void setBlurb(java.lang.String blurb) {
        this.blurb = (blurb==null)? "" : blurb;
    }
    /**
     * Setter for property bodyHtml.
     * @param bodyHtml New value of property bodyHtml.
     */
    public void setBodyHtml(java.lang.String bodyHtml) {
        this.bodyHtml = (bodyText==null) ? "" : bodyHtml;
    }
    /**
     * Setter for property bodyText.
     * @param bodyText New value of property bodyText.
     */
    public void setBodyText(java.lang.String bodyText) {
        this.bodyText = (bodyText==null) ? "" : bodyText;
    }
    /**
     * Setter for property dateCreated.
     * @param dateCreated New value of property dateCreated.
     */
    public void setDateCreated(java.util.Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    /**
     * Setter for property dateModified.
     * @param dateModified New value of property dateModified.
     */
    public void setDateModified(java.util.Date dateModified) {
        this.dateModified = dateModified;
    }
    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * Setter for property title.
     * @param title New value of property title.
     */
    public void setTitle(java.lang.String title) {
        this.title = (title == null) ? "" : title;
    }












}
