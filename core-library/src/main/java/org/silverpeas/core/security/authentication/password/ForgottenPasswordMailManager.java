/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.security.authentication.password;

import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.mail.MessagingException;

import static org.silverpeas.core.mail.MailAddress.eMail;

public class ForgottenPasswordMailManager {

  private static final String PREFIX_RESET_PASSWORD_REQUEST = "resetPasswordRequest";
  private static final String PREFIX_NEW_PASSWORD = "newPassword";
  private static final String PREFIX_ERROR = "error";
  private static final String PREFIX_ADMIN = "admin";
  private static final String CONTENT = "content";
  private static final String SUBJECT = "subject";

  private LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
      "org.silverpeas.authentication.multilang.forgottenPasswordMail");

  // SMTP parameters
  private String fromAddress;
  private String fromName;
  private String adminEmail;

  public ForgottenPasswordMailManager() {
    initFromAddress();
  }

  private void initFromAddress() {
    SettingBundle mailSettings = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.forgottenPasswordMail");
    adminEmail = mailSettings.getString("admin.mail", Administration.get().getAdministratorEmail());
    fromAddress = mailSettings.getString("fromAddress", adminEmail);
    fromName = mailSettings.getString("fromName", "Silverpeas");
  }

  public void sendResetPasswordRequestMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    sendMail(parameters, PREFIX_RESET_PASSWORD_REQUEST);
  }

  public void sendNewPasswordMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    sendMail(parameters, PREFIX_NEW_PASSWORD);
  }

  public void sendErrorMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    parameters.setToAddress(adminEmail);
    sendMail(parameters, PREFIX_ERROR);
  }

  public void sendAdminMail(ForgottenPasswordMailParameters parameters)
      throws MessagingException {
    parameters.setToAddress(adminEmail);
    sendMail(parameters, PREFIX_ADMIN);
  }

  private void sendMail(ForgottenPasswordMailParameters parameters, String resourcePrefix)
      throws MessagingException {
    parameters.setSubject(resource.getString(resourcePrefix + "." + SUBJECT));
    parameters.setContent(resource.getString(resourcePrefix + "." + CONTENT));
    MailSending.from(eMail(fromAddress).withName(fromName)).to(eMail(parameters.getToAddress()))
        .withSubject(parameters.getSubject()).withContent(parameters.getFilledContent()).send();
  }
}
