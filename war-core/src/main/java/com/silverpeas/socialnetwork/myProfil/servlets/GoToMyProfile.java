package com.silverpeas.socialnetwork.myProfil.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToMyProfile extends GoTo {

  private static final long serialVersionUID = -6462472953010182009L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    SilverTrace.info("myProfile", getClass().getSimpleName() + ".getDestination",
        "root.MSG_GEN_PARAM_VALUE", "objectId = " + objectId);
    StringBuilder gotoURL = new StringBuilder();
    gotoURL.append(URLManager.getURL(URLManager.CMP_MYPROFILE, null, null));
    gotoURL.append(objectId);

    return "goto=" + URLEncoder.encode(gotoURL.toString(), "UTF-8");
  }

}
