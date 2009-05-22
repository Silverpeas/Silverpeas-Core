package com.stratelia.webactiv.agenda.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.ical.ExportIcalManager;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class SubscribeAgenda extends HttpServlet {
    HttpSession session;
    PrintWriter out;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
    	SilverTrace.info("agenda", "SubscribeAgenda.doPost", "root.MSG_GEN_ENTER_METHOD");
        String userId = getUserId(req);
        String login = getLogin(req);
        String password = getPassword(req); 
        
        try
        {
        	SilverTrace.info("agenda", "SubscribeAgenda.doPost", "root.MSG_GEN_PARAM_VALUE", "userId = "+userId);
        	
        	//Check login/pwd must be a identified user
        	AdminController adminController = new AdminController(null);
        	UserFull user = adminController.getUserFull(userId);
        	if(user != null && login.equals(user.getLogin()) && password.equals(user.getPassword()))
        	{
	        	//Get calendar user
		        ExportIcalManager exportManager = new ExportIcalManager(userId);
		        String filePath = exportManager.exportIcalAgendaForSynchro();
				res.setContentType("text/calendar");
				res.setHeader("Content-Disposition","attachment;filename=calendar"+userId+".ics");
				OutputStream os = res.getOutputStream();
				FileInputStream fs = new FileInputStream(filePath);
				//Stream data back to the client
				int i;
				while( ((i = fs.read()) != -1) )
				{
				 os.write(i);
				}
				os.flush();
				os.close();		    	 
				res.getOutputStream();
        	} else {
        		objectNotFound(req, res);
        	}
        }
        catch (Exception e)
        {
        	objectNotFound(req, res);
        }
    	SilverTrace.info("agenda", "SubscribeAgenda.doPost", "root.MSG_GEN_EXIT_METHOD");
    }
    
    public String getServerURL(AdminController admin, String domainId)
    {
    	Domain defaultDomain = admin.getDomain(domainId);
    	return defaultDomain.getSilverpeasServerURL();
    }
        
    private String getUserId(HttpServletRequest request)
    {
    	return request.getParameter("userId");
    }
    
    private String getLogin(HttpServletRequest request)
    {
    	return request.getParameter("login");
    }
    
    private String getPassword(HttpServletRequest request)
    {
    	return request.getParameter("password");
    }
    
    private MainSessionController getMainSessionController(HttpServletRequest req)
    {
    	HttpSession session = req.getSession(true);
        MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
        return mainSessionCtrl;
    }
    
    private boolean isUserLogin(HttpServletRequest req)
    {
    	return (getMainSessionController(req) != null);
    }
    
    private void objectNotFound(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
    	boolean isLoggedIn = isUserLogin(req);
    	if (!isLoggedIn)
    		res.sendRedirect("/weblib/notFound.html");
    	else
    		res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+"/admin/jsp/documentNotFound.jsp");
    }
    
}
