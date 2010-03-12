package com.stratelia.webactiv.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

public class LoginFilter
    implements Filter {

  private boolean isUserLoginQuestionMandatory;
  private String anonymousId;

  public void init(FilterConfig filterConfig) {
    ResourceLocator general =
        new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
    isUserLoginQuestionMandatory =
        "personalQuestion".equals(general.getString("forgottenPwdActive")) &&
        "true".equals(general.getString("userLoginQuestionMandatory"));
    anonymousId = general.getString("anonymousId");
  }

  @SuppressWarnings("unchecked")
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;

    HttpSession session = req.getSession(true);
    Map<String, String[]> parameters = req.getParameterMap();
    Iterator<Entry<String, String[]>> it = parameters.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, String[]> entry = it.next();
      String paramName = (String) entry.getKey();
      String[] paramValues = (String[]) entry.getValue();
      if (paramValues.length == 1)
        session.setAttribute("svplogin_" + paramName, paramValues[0]);
      else
        session.setAttribute("svplogin_" + paramName, paramValues);
    }

    String destination = null;
    if (isUserLoginQuestionMandatory) {
      String sessionId = session.getId();
      Admin admin = new Admin();
      try {
        String key = req.getParameter("Key");
        String userId = admin.authenticate(key, sessionId, false, false);
        UserDetail userDetail = admin.getUserDetail(userId);

        if (userDetail != null
            && !userDetail.getId().equals(anonymousId)
            && (!StringUtil.isDefined(userDetail.getLoginQuestion()))) {
          request.setAttribute("userDetail", userDetail);
          destination = "/CredentialsServlet/ChangeQuestion";
        }
      } catch (AdminException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // destination = "/CredentialsServlet/ForcePasswordChange";
    if (destination != null) {
      RequestDispatcher dispatcher = request.getRequestDispatcher(destination);
      dispatcher.forward(request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  public FilterConfig getFilterConfig() {
    return null;
  }

  public void setFilterConfig(FilterConfig config) {
    //
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

}