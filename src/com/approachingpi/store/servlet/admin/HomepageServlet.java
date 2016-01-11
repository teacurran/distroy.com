package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.user.User;
import com.approachingpi.store.site.HomepageItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * User: terrence
 * Date: Aug 23, 2004
 * Time: 11:48:57 PM
 */
public class HomepageServlet extends PiServlet {
	public final int ACTION_LIST    = 0;
	public final int ACTION_EDIT    = 1;
	public final int ACTION_SAVE    = 2;

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
		int accessLevel = PiServlet.getReqInt(req,"accessLevel", User.TYPE_PUBLIC_ANON);
        int id = PiServlet.getReqInt(req,"id");

		if (action == ACTION_SAVE) {
			HomepageItem item = new HomepageItem(id);
			try {
				item.loadFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
			item.setTitle(PiServlet.getReqString(req,"title"));
			item.setBlurb(PiServlet.getReqString(req,"blurb"));
			item.setLink(PiServlet.getReqString(req,"link"));
			item.setAccessRequired(PiServlet.getReqInt(req,"accessRequired"));
			try {
				item.saveToDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
			action = ACTION_LIST;
		}

		if (action == ACTION_EDIT) {
			HomepageItem item = new HomepageItem(id);
			try {
				item.loadFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
			req.setAttribute("item",item);
		}

		if (action == ACTION_LIST) {
			ArrayList items = HomepageItem.getAllHomepageItems(con,accessLevel,-1);
			req.setAttribute("items",items);
		}

		try {
		    con.close();
		} catch (Exception e) { e.printStackTrace(); }

		this.getAltAttribute(req).put("accessLevel",accessLevel);

		if (action == ACTION_LIST) {
			req.getRequestDispatcher("/jsp/admin/homepage_list.jsp").forward(req, res);
		} else if (action == ACTION_EDIT) {
			req.getRequestDispatcher("/jsp/admin/homepage_edit.jsp").forward(req, res);
		}
	}
}
