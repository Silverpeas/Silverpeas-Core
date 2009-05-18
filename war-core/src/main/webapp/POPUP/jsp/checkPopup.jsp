<%@ page import="com.stratelia.silverpeas.notificationserver.channel.popup.POPUPSessionController"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%
    POPUPSessionController popupScc = (POPUPSessionController) request.getAttribute("POPUP");

    if( popupScc == null )
    {
      // No session controller in the request -> security exception
      String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
      getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
      return;
    }

				ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
				String m_context        = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

				GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
				Window window = gef.getWindow();
				Frame frame = gef.getFrame();
				Board board = gef.getBoard();

%>