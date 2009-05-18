package com.stratelia.webactiv.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class LogoutServlet extends HttpServlet
{
	private String m_sContext = "";
	private String m_sAbsolute = "";
	
	private String errorCode = "";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		// Get the session
		HttpSession session = request.getSession(true);

		// Get the context
		String sURI = request.getRequestURI();
		String sServletPath = request.getServletPath();
		String sPathInfo = request.getPathInfo();
		String sRequestURL = request.getRequestURL().toString(); 
		//String sRequestURL = HttpUtils.getRequestURL(request).toString();

		m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

		SilverTrace.info("peasCore", "LogoutServlet.doPost()", "root.MSG_GEN_PARAM_VALUE", "sURI=" + sURI);
		SilverTrace.info("peasCore", "LogoutServlet.doPost()", "root.MSG_GEN_PARAM_VALUE", "sServletPath=" + sServletPath);
		SilverTrace.info("peasCore", "LogoutServlet.doPost()", "root.MSG_GEN_PARAM_VALUE", "sPathInfo=" + sPathInfo);
		SilverTrace.info("peasCore", "LogoutServlet.doPost()", "root.MSG_GEN_PARAM_VALUE", "sRequestURL=" + sRequestURL);
		SilverTrace.info("peasCore", "LogoutServlet.doPost()", "root.MSG_GEN_PARAM_VALUE", "sAbsolute=" + m_sAbsolute);

		if (sPathInfo != null)
			sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));

		m_sContext = sURI.substring(0, sURI.lastIndexOf(sServletPath));
		if (m_sContext.charAt(m_sContext.length() - 1) == '/')
		{
			m_sContext = m_sContext.substring(0, m_sContext.length() - 1);
		}

		// Empty the MainSessionController and all SilverPeas variables
		// already done by SessionManager cleanSession(session);

		// Notify session manager : invalidate unbinds any objects bound.
		SessionManager.getInstance().closeHttpSession(session);
		
		// Route on login page
		errorCode = "4";
		response.sendRedirect(response.encodeRedirectURL(m_sAbsolute + m_sContext + "/Login.jsp?ErrorCode="+errorCode+"&logout=true"));
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	/*protected void cleanSession(HttpSession session)
	{
		Enumeration spSessionAttNames = session.getAttributeNames();
		String spName;

		while (spSessionAttNames.hasMoreElements())
		{
			spName = (String) spSessionAttNames.nextElement();
			//            SilverTrace.info("peasCore","LogoutServlet.doPost()","root.MSG_GEN_PARAM_VALUE","spName=" + spName);
			if ((spName != null) && ((spName.startsWith("Silverpeas_")) || (spName.startsWith("WYSIWYG_"))))
			{
				//                SilverTrace.info("peasCore","LogoutServlet.doPost()","root.MSG_GEN_PARAM_VALUE","Remove=" + spName);
				session.removeAttribute(spName);
			}
		}
		session.removeAttribute("SilverSessionController");
		session.removeAttribute("SessionGraphicElementFactory");
		session.removeAttribute("spaceModel"); // For Portlets
		session.removeAttribute("quizzUnderConstruction"); // For Quizz
		session.removeAttribute("questionsVector"); // For Quizz
		session.removeAttribute("currentQuizzId"); // For Quizz
		session.removeAttribute("questionsResponses"); // For Quizz
		session.removeAttribute("currentParticipationId"); // For Quizz
	}*/
}
