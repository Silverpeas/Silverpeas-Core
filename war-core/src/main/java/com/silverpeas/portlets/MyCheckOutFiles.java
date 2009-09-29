package com.silverpeas.portlets;

import java.io.IOException;
import java.util.Iterator;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentBmImpl;

public class MyCheckOutFiles extends GenericPortlet implements FormNames {

  public void doView(RenderRequest request, RenderResponse response)
      throws PortletException, IOException {
    PortletSession session = request.getPortletSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController",
            PortletSession.APPLICATION_SCOPE);

    Iterator attachments = null;
    Iterator documents = null;
    try {
      attachments = (new AttachmentBmImpl()).getAttachmentsByWorkerId(
          m_MainSessionCtrl.getUserId()).iterator();
      documents = getVersioningBm().getAllFilesReserved(
          Integer.parseInt(m_MainSessionCtrl.getUserId())).iterator();
    } catch (Exception e) {
      SilverTrace.error("portlet", "MyCheckOutFiles", "portlet.ERROR", e);
    }

    request.setAttribute("Documents", documents);
    request.setAttribute("Attachments", attachments);

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

  private VersioningBm getVersioningBm() throws Exception {
    VersioningBmHome vbmHome = (VersioningBmHome) EJBUtilitaire
        .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
    VersioningBm vbm = vbmHome.create();
    return vbm;
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
          .getRequestDispatcher("/portlets/jsp/myCheckOutFiles/" + pageName);
      dispatcher.include(request, response);
    } catch (IOException ioe) {
      throw new PortletException(ioe);
    }
  }
}
