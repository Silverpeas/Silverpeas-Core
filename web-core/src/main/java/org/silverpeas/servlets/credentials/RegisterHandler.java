/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.servlets.credentials;

import com.silverpeas.admin.service.UserService;
import com.silverpeas.admin.service.UserServiceProvider;
import com.stratelia.webactiv.beans.admin.AdminException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user has not an account yet and submits registration form.
 */
public class RegisterHandler extends FunctionHandler {

  /**
   * The request attribute key for the registration token.
   */
  public static final String REGISTRATION_TOKEN = "registrationToken";
  private RegistrationSettings settings = RegistrationSettings.getSettings();

  @Override
  public String doAction(HttpServletRequest request) {
    String firstName = request.getParameter("firstName");
    String lastName = request.getParameter("lastName");
    String email = request.getParameter("email");
    String token = request.getParameter(REGISTRATION_TOKEN);
    if (settings.isUserSelfRegistrationEnabled()) {
      try {
        UserService service = UserServiceProvider.getInstance().getService();
        service.registerUser(firstName, lastName, email, "0");
      } catch (AdminException e) {
        return "/admin/jsp/registrationFailed.jsp";
      }
      return "/admin/jsp/registrationSuccess.jsp";
    } else {
      Logger.getLogger(getClass().getSimpleName()).log(Level.WARNING,
          "A user is trying to register himself although this capability is deactived! Registration information: [firstname: {0}, lastname: {1}, email: {2}]",
          new Object[]{firstName, lastName, email});
      return "";
    }
  }
}
