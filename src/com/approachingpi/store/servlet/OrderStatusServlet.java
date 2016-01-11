/**
 * User: Tea Curran
 * Date: Dec 23, 2004
 * Time: 9:08:31 PM
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.servlet.Session;
import com.approachingpi.util.MessageBean;
import com.approachingpi.user.User;
import com.approachingpi.store.order.Order;
import com.approachingpi.store.order.OrderSearch;
import com.approachingpi.search.ResultPage;
import com.approachingpi.store.order.Payment;
import com.approachingpi.store.order.OrderComment;
import com.approachingpi.store.Store;
import com.approachingpi.store.Defines;
import com.approachingpi.util.MessageBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OrderStatusServlet extends PiServlet {
    public static final int ACTION_LIST     = 0;
    public static final int ACTION_VIEW     = 1;
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

        Session session = this.getSession(req,res,con);
        User user = session.getUser();
        int action = PiServlet.getReqInt(req, "action");
        //bfitch commented out the line below for the line above because OrderServlet only passes 2 params to the method.
        //int action = PiServlet.getReqInt(req, "action", ACTION_LIST);

        // Check to see if the user is logged in
        if (user.getId()==0 || user.getType() < User.TYPE_PUBLIC) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            errorBean.addMessage(getDefines().getProperty("ERROR_NOT_LOGGED_IN"));
            req.setAttribute("loginReturn","/orderstatus?action=" + action);
            req.setAttribute("loginForm","/jsp/login.jsp");
            forwardRequest(req,res,"/login");

            return;
        }

        if (action == ACTION_VIEW) {
            Order order = new Order(PiServlet.getReqString(req,"id",""));
            try {
                order.loadFromDb(con);
                //bfitch adding order.loadExtendedFromDb to get comments, order detail, & sku
                order.loadExtendedFromDb(con);
            } catch (Exception e) {
                order = new Order();
            }

            if (order.getUser() == null || order.getUser().getId() != user.getId()) {
                order = new Order();
            }

            req.setAttribute("order",order);
        }

        if (action == ACTION_LIST) {
            OrderSearch se = new OrderSearch();
            se.setUser(user);
            se.addSort(OrderSearch.SORT_DATE);
            se.setSortOrder(OrderSearch.SORT_DESC);

            ResultPage resultPage = new ResultPage();
            resultPage.setPageSize(10);

            se.setResultPage(resultPage);

            resultPage = se.executeReturnResultPage(con);
            req.setAttribute("resultPage",resultPage);
        }

        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (action == ACTION_VIEW) {
            req.getRequestDispatcher("/jsp/cart/order_status_view.jsp").forward(req, res);
        } else {
            req.getRequestDispatcher("/jsp/cart/order_status_list.jsp").forward(req, res);
        }
    }
}
