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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Navigation case : user has not an account yet and registers itself to silverpeas.
 */
@Service
public class NewRegistrationHandler extends CredentialsFunctionHandler {

  private final RegistrationSettings settings = RegistrationSettings.getSettings();

  @Inject
  private PublicationTemplateManager publicationTemplateManager;

  @Override
  public String getFunction() {
    return "NewRegistration";
  }

  @Override
  public String doAction(HttpServletRequest request) {
    String destination = "";
    renewSecurityToken(request);
    if (settings.isUserSelfRegistrationEnabled()) {
      PagesContext context = new PagesContext();
      context.setDomainId(settings.userSelfRegistrationDomainId());
      PublicationTemplate template = publicationTemplateManager.getDirectoryTemplate(context);
      request.setAttribute("ExtraTemplate", template);

      destination = "/admin/jsp/newRegistration.jsp";
    }
    return destination;
  }

  @Override
  public void registerWith(HttpFunctionHandlerRegistering registering) {
    registering.register(this, true);
  }
}
