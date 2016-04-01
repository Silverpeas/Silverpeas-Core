package org.silverpeas.web.socialnetwork.myprofil.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.servlet.GoTo;

public class GoToMyProfile extends GoTo {

  private static final long serialVersionUID = -6462472953010182009L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {

    StringBuilder gotoURL = new StringBuilder();
    gotoURL.append(URLUtil.getURL(URLUtil.CMP_MYPROFILE, null, null));
    gotoURL.append(objectId);

    return "goto=" + URLEncoder.encode(gotoURL.toString(), "UTF-8");
  }

}
