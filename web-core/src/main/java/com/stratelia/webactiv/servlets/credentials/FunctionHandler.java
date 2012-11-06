/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.servlets.credentials;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordException;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailManager;
import com.stratelia.silverpeas.authentication.password.ForgottenPasswordMailParameters;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ehugonnet
 */
public abstract class FunctionHandler {

  private ResourceBundle resources;
  private ResourceLocator m_Multilang;
  private ForgottenPasswordMailManager forgottenPasswordMailManager;
  private ResourceLocator general =
      new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");

  public FunctionHandler() {
    resources = FileUtil.loadBundle("com.stratelia.silverpeas.peasCore.SessionManager",
        Locale.ROOT);
    String language = resources.getString("language");
    if (!StringUtil.isDefined(language)) {
      language = "fr";
    }
    m_Multilang = new ResourceLocator("com.stratelia.silverpeas.peasCore.multilang.peasCoreBundle",
        language);
    forgottenPasswordMailManager = new ForgottenPasswordMailManager();
  }

  public abstract String doAction(HttpServletRequest request);

  protected ForgottenPasswordMailParameters getMailParameters(String userId) throws AdminException {
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    UserDetail userDetail = getAdmin().getUserDetail(userId);
    parameters.setUserName(userDetail.getDisplayedName());
    parameters.setLogin(userDetail.getLogin());
    parameters.setDomainId(userDetail.getDomainId());
    parameters.setToAddress(userDetail.geteMail());
    return parameters;
  }

  protected String getContextPath(HttpServletRequest request) {
    String requestUrl = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    String contextPath = requestUrl.substring(
        0, requestUrl.indexOf(servletPath) + servletPath.length());
    return contextPath;
  }

  protected String forgottenPasswordError(HttpServletRequest request, ForgottenPasswordException fpe) {
    String error = SilverTrace.getTraceMessage(fpe.getMessage()) + " - " + fpe.getExtraInfos();
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    parameters.setError(error);
    try {
      getForgottenPasswordMailManager().sendErrorMail(parameters);
    } catch (MessagingException e) {
      SilverTrace.error("peasCore",
          "CredentialsServlet.forgottenPasswordError()",
          "forgottenPassword.EX_SEND_MAIL", e);
    }
    request.setAttribute("error", error);
    return getGeneral().getString("forgottenPasswordError");
  }

  /**
   * @return the resources
   */
  protected ResourceBundle getResources() {
    return resources;
  }

  /**
   * @param resources the resources to set
   */
  protected void setResources(ResourceBundle resources) {
    this.resources = resources;
  }

  /**
   * @return the m_Multilang
   */
  protected ResourceLocator getM_Multilang() {
    return m_Multilang;
  }

  /**
   * @param m_Multilang the m_Multilang to set
   */
  protected void setM_Multilang(ResourceLocator m_Multilang) {
    this.m_Multilang = m_Multilang;
  }

  /**
   * @return the forgottenPasswordMailManager
   */
  protected ForgottenPasswordMailManager getForgottenPasswordMailManager() {
    return forgottenPasswordMailManager;
  }

  /**
   * @param forgottenPasswordMailManager the forgottenPasswordMailManager to set
   */
  protected void setForgottenPasswordMailManager(
      ForgottenPasswordMailManager forgottenPasswordMailManager) {
    this.forgottenPasswordMailManager = forgottenPasswordMailManager;
  }

  /**
   * @return the admin
   */
  protected Admin getAdmin() {
    return AdminReference.getAdminService();
  }

  /**
   * @return the general
   */
  protected ResourceLocator getGeneral() {
    return general;
  }

  /**
   * @param general the general to set
   */
  protected void setGeneral(ResourceLocator general) {
    this.general = general;
  }
}
