/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordException;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailManager;
import org.silverpeas.core.security.authentication.password.ForgottenPasswordMailParameters;
import org.silverpeas.core.web.token.SynchronizerTokenService;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Handler of an incoming request within an authentication process related to the credentials of the
 * requester.
 *
 * @author ehugonnet
 */
public abstract class CredentialsFunctionHandler implements HttpFunctionHandler {

  private final SettingBundle resources =
      ResourceLocator.getSettingBundle("org.silverpeas.peasCore.SessionManager");
  private LocalizationBundle multilang;
  private final SettingBundle general
      = ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
  private final SettingBundle authenticationSettings = ResourceLocator.getSettingBundle(
      "org.silverpeas.authentication.settings.authenticationSettings");

  @Inject
  private Administration admin;
  @Inject
  private ForgottenPasswordMailManager forgottenPasswordMailManager;

  @PostConstruct
  protected void loadSettings() {
    String language = resources.getString("language", "");
    if (!StringUtil.isDefined(language)) {
      language = "fr";
    }
    multilang =
        ResourceLocator.getLocalizationBundle("org.silverpeas.peasCore.multilang.peasCoreBundle",
            language);
  }

  protected ForgottenPasswordMailParameters getMailParameters(String userId) throws AdminException {
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    UserDetail userDetail = getAdminService().getUserDetail(userId);
    parameters.setUserName(userDetail.getDisplayedName());
    parameters.setLogin(userDetail.getLogin());
    parameters.setDomainId(userDetail.getDomainId());
    parameters.setToAddress(userDetail.getEmailAddress());
    parameters.setUserLanguage(userDetail.getUserPreferences().getLanguage());
    return parameters;
  }

  protected String getContextPath(HttpServletRequest request) {
    String requestUrl = request.getRequestURL().toString();
    String servletPath = request.getServletPath();
    return requestUrl.substring(
        0, requestUrl.indexOf(servletPath) + servletPath.length());
  }

  protected String forgottenPasswordError(HttpServletRequest request,
      ForgottenPasswordException fpe) {
    String error = fpe.getMessage() + " - " + fpe.getExtraInfos();
    ForgottenPasswordMailParameters parameters = new ForgottenPasswordMailParameters();
    parameters.setError(error);
    getForgottenPasswordMailManager().sendErrorMail(parameters);
    request.setAttribute("error", error);
    return getGeneral().getString("forgottenPasswordError");
  }

  /**
   * @return the resources
   */
  protected SettingBundle getResources() {
    return resources;
  }

  /**
   * @return the multilang
   */
  protected LocalizationBundle getMultilang() {
    return multilang;
  }

  /**
   * @return the forgottenPasswordMailManager
   */
  protected ForgottenPasswordMailManager getForgottenPasswordMailManager() {
    return forgottenPasswordMailManager;
  }

  /**
   * @return the admin
   */
  protected Administration getAdminService() {
    return admin;
  }

  /**
   * @return the general
   */
  protected SettingBundle getGeneral() {
    return general;
  }

  public SettingBundle getAuthenticationSettings() {
    return authenticationSettings;
  }

  protected void renewSecurityToken(HttpServletRequest request) {
    SynchronizerTokenService tokenService = SynchronizerTokenService.getInstance();
    tokenService.setUpNavigationTokens(request);
  }

  /**
   * Register this handler by not bypassing the handler pre-processing tasks. Override this method
   * if the pre-processing tasks have to be bypassed for the handler.
   *
   * @param registering the registering service among which this handler has to be register itself.
   */
  @Override
  public void registerWith(HttpFunctionHandlerRegistering registering) {
    registering.register(this, false);
  }
}
