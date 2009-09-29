package com.stratelia.webactiv.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class GoToPublication extends GoTo {
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    PublicationPK pubPK = new PublicationPK(objectId);
    PublicationDetail pub = getPublicationBm().getDetail(pubPK);

    String componentId = req.getParameter("ComponentId"); // in case of an
    // alias, componentId
    // is given
    if (!StringUtil.isDefined(componentId)) {
      componentId = pub.getPK().getInstanceId();
    }

    SilverTrace.info("peasUtil", "GoToPublication.doPost",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);

    String gotoURL = URLManager.getURL(null, componentId) + pub.getURL();

    return "goto=" + URLEncoder.encode(gotoURL);
  }

  private PublicationBm getPublicationBm() {
    PublicationBm currentPublicationBm = null;
    try {
      PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
              PublicationBmHome.class);
      currentPublicationBm = publicationBmHome.create();
    } catch (Exception e) {
      displayError(null);
    }
    return currentPublicationBm;
  }
}