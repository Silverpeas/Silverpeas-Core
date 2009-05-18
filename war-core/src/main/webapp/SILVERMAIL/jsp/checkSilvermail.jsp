<%@ page import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILSessionController"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%
    SILVERMAILSessionController silvermailScc = (SILVERMAILSessionController) request.getAttribute("SILVERMAIL");

    if( silvermailScc == null )
    {
      // No session controller in the request -> security exception
      String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
      getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
      return;
    }

	ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
	String m_Context        = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>