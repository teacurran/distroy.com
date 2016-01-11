package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.user.Company;
import com.approachingpi.user.User;
import com.approachingpi.user.CompanySearchEngine;
import com.approachingpi.util.MessageBean;
import com.approachingpi.search.ResultPage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;

/**
 * User: terrence
 * Date: Sep 2, 2004
 * Time: 3:43:37 PM
 */
public class CompanyServlet extends PiServlet {
	public static final int ACTION_LIST     = 0;
	public static final int ACTION_EDIT     = 1;
	public static final int ACTION_SAVE     = 2;
    

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

		Connection con = openConnection();
		MessageBean errors = this.getErrorBean(req);
		MessageBean messageBean = this.getMessageBean(req);
		User user = getSession(req,res,con).getUser();

		user.refreshPrefs(con);

		int action = PiServlet.getReqInt(req, "action", ACTION_LIST);
		Company company = new Company(PiServlet.getReqInt(req,"companyId"));

		if (action==ACTION_LIST) {
            CompanySearchEngine cse = new CompanySearchEngine();

            
            cse.addSort(PiServlet.getReqInt(req,"sortOrder", user.getPrefInt("admin.company.sortorder", CompanySearchEngine.SORT_ID)));
            cse.setStatus(PiServlet.getReqInt(req,"statusFilter",user.getPrefInt("admin.company.statusfilter", Company.STATUS_ANY)));
            cse.setTerm(PiServlet.getReqString(req,"term",user.getPref("admin.company.term")));
            
            ResultPage rp = new ResultPage();
            rp.setPageSize(20);
            rp.setPage(PiServlet.getReqInt(req,"page", user.getPrefInt("admin.company.page",1)));
            cse.setResultPage(rp);

            rp = cse.executeReturnResultPage(con);
            req.setAttribute("rp", rp);
		}

		if (action==ACTION_SAVE) {
			company.setName(PiServlet.getReqString(req,"name"));
			company.setStatus(PiServlet.getReqInt(req,"status"));
			if (company.getName().equalsIgnoreCase("")) {
				errors.addMessage("You must enter a name for this company.");
			}

			if (errors.getMessageCount()==0) {
				try {
					company.saveToDb(con);
                    messageBean.addMessage(getDefines().getProperty("message.save.done").replaceAll("#OBJECT#","Company"));
                    // load from teh db to get teh create and modified date.
                    company.loadFromDb(con);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
            action = ACTION_EDIT;
		} else if (action==ACTION_EDIT) {
			try {
				company.loadFromDb(con);
                company.loadUsersFromDb(con);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
        if (action == ACTION_SAVE || action == ACTION_EDIT) {
            try {
                company.loadUsersFromDb(con);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (action==ACTION_LIST) {
			getServletContext().getRequestDispatcher("/jsp/admin/company_list.jsp").forward(req, res);
		} else if (action==ACTION_EDIT) {
			req.setAttribute("company", company);
			getServletContext().getRequestDispatcher("/jsp/admin/company_edit.jsp").forward(req, res);
		}
	}
}
