<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<%!
  ResourceLocator messageBundle ;

public String getMessage(String messageName) {
  String message = null ;

  message = messageBundle.getString(messageName);

  if (message == null) {
    return message = messageName ;
  }
  return message ;
}
%>
<%
  MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
  messageBundle = new ResourceLocator("com.stratelia.silverpeas.portlet.multilang.portletBundle", m_MainSessionCtrl.getFavoriteLanguage());
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
%>
