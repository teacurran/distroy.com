package com.approachingpi.store.servlet.admin;

/**
 * Date: Aug 8, 2004
 * Time: 10:46:11 PM
 * @author Terrence Curran
 */

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.store.catalog.Product;
import com.approachingpi.store.catalog.Image;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ImageAssociateServlet extends PiServlet {
	public static final int ACTION_MAIN             = 0;
	public static final int ACTION_SHOW_AVAILABLE   = 1;
	public static final int ACTION_ADD              = 2;
	public static final int ACTION_DELETE           = 3;
	public static final int ACTION_MOVE_UP          = 4;
	public static final int ACTION_MOVE_DOWN        = 5;

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

		int action              = PiServlet.getReqInt(req,"action",ACTION_MAIN);
        int productVariationId  = PiServlet.getReqInt(req,"productVariationId");
        int productId           = PiServlet.getReqInt(req,"productId");
		int imageId             = PiServlet.getReqInt(req,"imageId");

		if (action == ACTION_ADD) {
			try {
				ps = con.prepareStatement("IF ((SELECT COUNT(*) FROM tbLinkProductVariationImage WHERE inImageId=? AND inProductVariationId=?) = 0) "+
				        "BEGIN "+
				        "INSERT INTO tbLinkProductVariationImage (inImageId, inProductVariationId, inRank) VALUES(?,?,0) "+
				        "END");
            	ps.setInt(1,imageId);
				ps.setInt(2,productVariationId);
				ps.setInt(3,imageId);
				ps.setInt(4,productVariationId);
				ps.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			action = ACTION_MAIN;
		}
		if (action == ACTION_DELETE) {
			try {
				ps = con.prepareStatement("DELETE FROM tbLinkProductVariationImage WHERE inImageId=? AND inProductVariationId=?");
            	ps.setInt(1,imageId);
				ps.setInt(2,productVariationId);
				ps.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			action = ACTION_MAIN;
		}
        if (action == ACTION_MOVE_DOWN) {
            try {
                ps = con.prepareStatement("UPDATE tbLinkProductVariationImage SET inRank=inRank+3 WHERE inImageId=? AND inProductVariationId=?");
                ps.setInt(1,imageId);
                ps.setInt(2,productVariationId);
                ps.execute();

                ps = con.prepareStatement("SELECT * FROM tbLinkProductVariationImage WHERE inProductVariationId = ? ORDER BY inRank");
                ps.setInt(1,productVariationId);
                rs = ps.executeQuery();
            
                int rank = -1;
                while (rs.next()) {
                    rank++;
                    int thisImageId = rs.getInt("inImageId");
                    PreparedStatement ps2 = con.prepareStatement("UPDATE tbLinkProductVariationImage SET inRank=? WHERE inImageId=? AND inProductVariationId=?");
                    ps2.setInt(1,rank*2);
                    ps2.setInt(2,thisImageId);
                    ps2.setInt(3,productVariationId);
                    ps2.execute();
                }
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            action = ACTION_MAIN;
        }
        if (action == ACTION_MOVE_UP) {
            try {
                ps = con.prepareStatement("UPDATE tbLinkProductVariationImage SET inRank=inRank-3 WHERE inImageId=? AND inProductVariationId=?");
                ps.setInt(1,imageId);
                ps.setInt(2,productVariationId);
                ps.execute();

                ps = con.prepareStatement("SELECT * FROM tbLinkProductVariationImage WHERE inProductVariationId = ? ORDER BY inRank");
                ps.setInt(1,productVariationId);
                rs = ps.executeQuery();
            
                int rank = -1;
                while (rs.next()) {
                    rank++;
                    int thisImageId = rs.getInt("inImageId");
                    PreparedStatement ps2 = con.prepareStatement("UPDATE tbLinkProductVariationImage SET inRank=? WHERE inImageId=? AND inProductVariationId=?");
                    ps2.setInt(1,rank*2);
                    ps2.setInt(2,thisImageId);
                    ps2.setInt(3,productVariationId);
                    ps2.execute();
                }
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            action = ACTION_MAIN;
        }


		ArrayList images = new ArrayList();
		if (action == ACTION_MAIN) {
			try {
				ps = con.prepareStatement("SELECT I.* FROM tbImage I, tbLinkProductVariationImage L WHERE I.inId = L.inImageId AND L.inProductVariationId = ? ORDER BY L.inRank");
				ps.setInt(1,productVariationId);
				rs = ps.executeQuery();
				while (rs.next()) {
					Image image = new Image(rs.getInt("inId"));
					image.loadFromRs(rs);
					images.add(image);
				}
				rs.close();
			} catch (Exception e) {
                e.printStackTrace();
			}
		}
		if (action == ACTION_SHOW_AVAILABLE) {
            try {
                ps = con.prepareStatement("SELECT I.* FROM tbImage I WHERE I.inProductId = ? ORDER BY I.inRank");
                ps.setInt(1,productId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Image image = new Image(rs.getInt("inId"));
                    image.loadFromRs(rs);
                    images.add(image);
                }
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
		}

		req.setAttribute("images",images);

		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (action == ACTION_MAIN) {
			req.getRequestDispatcher("/jsp/admin/image_associate_main.jsp").forward(req, res);
		} else if (action == ACTION_SHOW_AVAILABLE) {
			req.getRequestDispatcher("/jsp/admin/image_associate_avail.jsp").forward(req, res);
		}
	}
}
