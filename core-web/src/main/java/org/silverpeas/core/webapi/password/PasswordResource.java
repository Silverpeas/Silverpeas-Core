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
package org.silverpeas.core.webapi.password;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.authentication.password.rule.PasswordRule;
import org.silverpeas.core.security.authentication.password.service.PasswordRulesService;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * A REST Web resource about the actual policy on the passwords for the running Silverpeas.
 *
 * @author Yohann Chastagnier
 */
@WebService
@Path(PasswordResourceURIs.PASSWORD_BASE_URI)
public class PasswordResource extends AbstractPasswordResource {
  protected static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");
  protected static int nbMatchingCombinedRules =
      settings.getInteger("password.combination.nbMatchingRules", 0);

  @Inject
  private PasswordRulesService service;

  @Override
  protected String getResourceBasePath() {
    return PasswordResourceURIs.PASSWORD_BASE_URI;
  }

  /**
   * Gets the JSON representation of password policy. If it doesn't exist, a 404 HTTP code is
   * returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @return the response to the HTTP GET request with the JSON representation of the asked photo.
   */
  @GET
  @Path(PasswordResourceURIs.PASSWORD_POLICY_URI_PART)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public PasswordPolicyEntity getPolicy() {
    try {
      final PasswordPolicyEntity passwordPolicy = PasswordPolicyEntity
          .createFrom(nbMatchingCombinedRules,
              service.getExtraRuleMessage(getLanguage()));
      for (final PasswordRule rule : service.getRequiredRules()) {
        passwordPolicy.addRule(asWebEntity(rule));
      }
      for (final PasswordRule rule : service.getCombinedRules()) {
        passwordPolicy.addCombinedRule(asWebEntity(rule));
      }
      return passwordPolicy;
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of a list of errors caught by a password checking. The returned
   * list contains names of rules which are not verified. If it doesn't exist, a 404 HTTP code is
   * returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @return the response to the HTTP GET request with the JSON representation of the asked photo.
   */
  @POST
  @Path(
      PasswordResourceURIs.PASSWORD_POLICY_URI_PART + "/" + PasswordResourceURIs.PASSWORD_CHECKING_URI_PART)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PasswordCheckEntity checking(final PasswordEntity password) {
    return asWebEntity(service.check(password.getValue()));
  }
}
