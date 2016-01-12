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
import com.approachingpi.store.catalog.Royalties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.io.IOException;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.Date;

public class RoyaltiesServlet extends PiServlet {
    public static final int ACTION_LIST             = 0;
    public static final int ACTION_EDIT             = 1;
    public static final int ACTION_DELETE           = 2;
    public static final int ACTION_AUDIT            = 3;
    

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
            ArrayList artists = Royalties.getAllArtists(con);
            req.setAttribute("artists",artists);  
        }
        
        if (action == ACTION_AUDIT) {
            Royalties artist = new Royalties(PiServlet.getReqInt(req,"artistId"));
            try{
      artist.loadFromDb(con);
            ArrayList audit = Royalties.loadAudit(con, PiServlet.getReqInt(req,"artistId"));
            req.setAttribute("audit",audit);     
            }
            catch (Exception e) {}
            
            try {
                artist.loadFromDb(con);
            } catch (Exception e) {
                errorBean.addMessage("Error Loading artist: " + e.toString());
                e.printStackTrace();
            }
            req.setAttribute("artist",artist);
        }

        if (action == ACTION_EDIT) {
            Royalties artist = new Royalties(PiServlet.getReqInt(req,"artistId"));
            try{
      artist.loadFromDb(con);
            ArrayList history = Royalties.getHistory(con, PiServlet.getReqInt(req,"artistId"));
            req.setAttribute("history",history);
            }
            catch (Exception e) {}
         
            if (PiServlet.getReqBoolean(req,"formSubmitted")) {
                //artist.setDateCheck(new Date(PiServlet.getString(req, "dateCheck")));
                artist.setCheckNumber(PiServlet.getReqString(req, "CheckNumber","0"));
                artist.setCheckAmount(new BigDecimal(PiServlet.getReqString(req, "CheckAmount","0.00")));

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
                artist.getAllArtists(con);
                artist.updateRoyalties(con);
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
            req.getRequestDispatcher("/jsp/admin/royalties_list.jsp").forward(req, res);
        } else if (action == ACTION_EDIT) {
            req.getRequestDispatcher("/jsp/admin/royalties_edit.jsp").forward(req, res);
        } else if (action == ACTION_AUDIT) {
            req.getRequestDispatcher("/jsp/admin/royalties_audit.jsp").forward(req, res);
        }
    }
}
