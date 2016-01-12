/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 18, 2004
 * Time: 4:03:49 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.catalog.Artist;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.io.IOException;
import java.util.ArrayList;
import java.math.BigDecimal;

public class ArtistServlet extends PiServlet {
    public static final int ACTION_LIST             = 0;
    public static final int ACTION_EDIT             = 1;
    public static final int ACTION_DELETE           = 2;
    

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        Connection con = openConnection();
        PreparedStatement ps;
        ResultSet rs;

        MessageBean errorBean = this.getErrorBean(req);
        MessageBean messageBean = this.getMessageBean(req);

        int action = PiServlet.getReqInt(req,"action", ACTION_LIST);


        if (action == ACTION_LIST) {
            ArrayList artists = Artist.getAllArtists(con);
            req.setAttribute("artists",artists);
        }

        if (action == ACTION_EDIT) {
            Artist artist = new Artist(PiServlet.getReqInt(req,"artistId"));

            if (PiServlet.getReqBoolean(req,"formSubmitted")) {
                artist.setActive(PiServlet.getReqBoolean(req,"active"));
                artist.setDesc(PiServlet.getReqString(req,"desc"));
                artist.setNameDisplay(PiServlet.getReqString(req,"nameDisplay"));
                artist.setNameFirst(PiServlet.getReqString(req,"nameFirst"));
                artist.setNameLast(PiServlet.getReqString(req,"nameLast"));
                try {
                    artist.setRoyaltyDollarRetail(new BigDecimal(PiServlet.getReqString(req,"royaltyDollarRetail","0.00")));
                } catch (Exception e) {}
                try {
                    artist.setRoyaltyDollarWholesale(new BigDecimal(PiServlet.getReqString(req,"royaltyDollarWholesale","0.00")));
                } catch (Exception e) {}
                try {
                    artist.setRoyaltyPercentRetail(new Float(PiServlet.getReqString(req,"royaltyPercentRetail","0")).floatValue());
                } catch (Exception e) {}
                try {
                    artist.setRoyaltyPercentWholesale(new Float(PiServlet.getReqString(req,"royaltyPercentWholesale","0")).floatValue());
                } catch (Exception e) {}

                if (errorBean.getMessageCount() == 0) {
                    try {
                        artist.saveToDb(con);
                        messageBean.addMessage(getDefines().getProperty("message.save.done").replaceAll("#OBJECT#","Artist"));
                    } catch (Exception e) {
                        errorBean.addMessage("Unable to save artist: " + e.toString());
                        e.printStackTrace();
                    }
                }
            }

            try {
                artist.loadFromDb(con);
            } catch (Exception e) {
                errorBean.addMessage("Error Loading artist: " + e.toString());
                e.printStackTrace();
            }
            req.setAttribute("artist",artist);
        }


        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (action == ACTION_LIST) {
            req.getRequestDispatcher("/jsp/admin/artist_list.jsp").forward(req, res);
        } else if (action == ACTION_EDIT) {
            req.getRequestDispatcher("/jsp/admin/artist_edit.jsp").forward(req, res);
        }
    }
}
