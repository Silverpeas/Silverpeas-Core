package com.silverpeas.peasUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public abstract class GoTo extends HttpServlet
{
    HttpSession session;
    PrintWriter out;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        doPost(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
    	SilverTrace.info("peasUtil", "GoTo.doPost", "root.MSG_GEN_ENTER_METHOD");
        String id = getObjectId(req);
        
        try
        {
        	SilverTrace.info("peasUtil", "GoTo.doPost", "root.MSG_GEN_PARAM_VALUE", "id = "+id);
        	
	        String redirect = getDestination(id, req, res);
	        if (redirect == null || "".equals(redirect))
	        	objectNotFound(req, res);
	        else
	        {
		        if (res.isCommitted())
		        {
		        	//La réponse a déjà été envoyée
		        }
		        else
		        	res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+"/autoRedirect.jsp?"+redirect);
	        }
        }
        catch (AccessForbiddenException afe)
        {
        	accessForbidden(req, res);
        }
        catch (Exception e)
        {
        	objectNotFound(req, res);
        }
    }
    
    public abstract String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception;
    
    private void objectNotFound(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
    	boolean isLoggedIn = isUserLogin(req);
    	if (!isLoggedIn)
    		res.sendRedirect("/weblib/notFound.html");
    	else
    		res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+"/admin/jsp/documentNotFound.jsp");
    }
    
    private void accessForbidden(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
    	res.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+"/admin/jsp/accessForbidden.jsp");
    }
    
    public String getObjectId(HttpServletRequest request)
    {
    	String pathInfo = request.getPathInfo();
    	if (pathInfo != null)
    		return pathInfo.substring(1);
    	return null;
    }
    
    public boolean isUserLogin(HttpServletRequest req)
    {
    	return (getMainSessionController(req) != null);
    }
    
    //check if the user is allowed to access the required component
    public boolean isUserAllowed(HttpServletRequest req, String componentId) {
    	
        MainSessionController mainSessionCtrl = getMainSessionController(req);
    	
        boolean isAllowed = false;

        if(componentId == null)
        {   // Personal space
            isAllowed = true;
        }
        else
        {
        	return mainSessionCtrl.getOrganizationController().isComponentAvailable(componentId, mainSessionCtrl.getUserId());
        }

        return isAllowed;
    }
    
    private MainSessionController getMainSessionController(HttpServletRequest req)
    {
    	HttpSession session = req.getSession(true);
        MainSessionController mainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
        
        return mainSessionCtrl;
    }
    
    public String getUserId(HttpServletRequest req)
    {
    	return getMainSessionController(req).getUserId();
    }
    
    public void displayError(HttpServletResponse res)
	{
		SilverTrace.info("peasUtil", "GoToFile.displayError()", "root.MSG_GEN_ENTER_METHOD");
		
		res.setContentType("text/html");
		OutputStream        out2 = null;
		int                 read;

		StringBuffer message = new StringBuffer(255);
		message.append("<HTML>");
		message.append("<BODY>");
		message.append("</BODY>");
		message.append("</HTML>");
		
		StringReader reader = new StringReader(message.toString());
		
		try
		{
			out2 = res.getOutputStream();
			read = reader.read();
			while (read != -1){
				out2.write(read); // writes bytes into the response
				read = reader.read();
			}
		}
		catch (Exception e)
		{
			SilverTrace.warn("peasUtil", "GoToFile.displayError", "root.EX_CANT_READ_FILE");
		}
		finally
		{
			// we must close the in and out streams
			try
			{
				out2.close();
			}
			catch (Exception e)
			{
				SilverTrace.warn("peasUtil", "GoToFile.displayError", "root.EX_CANT_READ_FILE", "close failed");
			}
		}
	}
}