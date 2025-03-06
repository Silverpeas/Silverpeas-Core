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
package org.silverpeas.core.security.authentication.password;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.SimpleUserNotification;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.util.Pair;
import org.silverpeas.kernel.bundle.ResourceLocator;

import static java.util.stream.Stream.of;

@Service
public class ForgottenPasswordMailManager {

  private static final String PREFIX_RESET_PASSWORD_REQUEST = "resetPasswordRequest";
  private static final String PREFIX_NEW_PASSWORD = "newPassword";
  private static final String PREFIX_ERROR = "error";
  private static final String PREFIX_ADMIN = "admin";
  private static final String SUBJECT = "subject";

  public void sendResetPasswordRequestMail(ForgottenPasswordMailParameters parameters) {
    sendMail(parameters, PREFIX_RESET_PASSWORD_REQUEST);
  }

  public void sendNewPasswordMail(ForgottenPasswordMailParameters parameters) {
    sendMail(parameters, PREFIX_NEW_PASSWORD);
  }

  public void sendErrorMail(ForgottenPasswordMailParameters parameters) {
    User admin = User.getMainAdministrator();
    parameters.setToAddress(admin.getEmailAddress());
    sendMail(parameters, PREFIX_ERROR);
  }

  public void sendAdminMail(ForgottenPasswordMailParameters parameters) {
    User admin = User.getMainAdministrator();
    parameters.setToAddress(admin.getEmailAddress());
    sendMail(parameters, PREFIX_ADMIN);
  }

  private void sendMail(ForgottenPasswordMailParameters parameters, String resourcePrefix) {
    final LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.forgottenPasswordMail",
        parameters.getUserLanguage());
    final Pair<String, String> path = Pair.of("admin/password", "forgottenPasswordMail_" + resourcePrefix);
    final SimpleUserNotification notification = SimpleUserNotification.fromSystem()
        .toEMails(of(parameters.getToAddress()), parameters.getUserLanguage())
        .withTitle(l -> resource.getString(resourcePrefix + "." + SUBJECT))
        .fillTemplate(path, (t, l) -> parameters.applyTemplateData(t));
    parameters.getMessage().ifPresent(notification::withExtraMessage);
    notification.send();
  }
}
