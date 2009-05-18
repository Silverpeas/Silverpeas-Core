package com.stratelia.webactiv.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class MainRequestRouter extends HttpServlet 
{
    private String m_sContext = ""; 
    private String m_sAbsolute = ""; 
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // Get the session
        HttpSession m_Session = request.getSession(true);
        // Get the context
        String sURI = request.getRequestURI();
        String sServletPath = request.getServletPath();
        String sPathInfo = request.getPathInfo();
        String sRequestURL = request.getRequestURL().toString();

        m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
        if(sPathInfo != null)
            sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
        m_sContext = sURI.substring(0,sURI.lastIndexOf(sServletPath));
        if(m_sContext.charAt(m_sContext.length()-1) == '/')
        {
            m_sContext = m_sContext.substring(0, m_sContext.length()-1);
        }

        //Get the favorite frameset to the current user
        GraphicElementFactory gef = (GraphicElementFactory) m_Session.getAttribute("SessionGraphicElementFactory");
      
      	if ( gef.getLookFrame().startsWith("/") )
			response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext+gef.getLookFrame()));
		else
        response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext+"/admin/jsp/"+gef.getLookFrame()));
        return;
    } 

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost (request, response);
    }
}