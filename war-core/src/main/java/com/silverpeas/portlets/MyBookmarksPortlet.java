package com.silverpeas.portlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.myLinks.ejb.MyLinksBm;
import com.silverpeas.myLinks.ejb.MyLinksBmHome;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

public class MyBookmarksPortlet extends GenericPortlet implements FormNames {

  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController",
            PortletSession.APPLICATION_SCOPE);

    List links = new ArrayList();
    try {
      links = (List) getMyLinksBm().getAllLinks(m_MainSessionCtrl.getUserId());
    } catch (Exception e) {
      SilverTrace.error("portlet", "MyBookmarksPortlet", "portlet.ERROR", e);
    }

    request.setAttribute("Links", links.iterator());

    include(request, response, "portlet.jsp");
  }

  public void doEdit(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "edit.jsp");
  }

  /** Include "help" JSP. */
  public void doHelp(RenderRequest request, RenderResponse response)
      throws PortletException {
    include(request, response, "help.jsp");
  }

  private MyLinksBm getMyLinksBm() throws Exception {
    MyLinksBm currentMyLinksBm = null;
    MyLinksBmHome myLinksBmHome = (MyLinksBmHome) EJBUtilitaire
        .getEJBObjectRef(JNDINames.MYLINKSBM_EJBHOME, MyLinksBmHome.class);
    currentMyLinksBm = myLinksBmHome.create();
    return currentMyLinksBm;
  }

  /** Include a page. */
  private void include(RenderRequest request, RenderResponse response,
      String pageName) throws PortletException {
    response.setContentType(request.getResponseContentType());
    if (!StringUtil.isDefined(pageName)) {
      // assert
      throw new NullPointerException("null or empty page name");
    }
    try {
      PortletRequestDispatcher dispatcher = getPortletContext()
          .getRequestDispatcher("/portlets/jsp/myBookmarks/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
