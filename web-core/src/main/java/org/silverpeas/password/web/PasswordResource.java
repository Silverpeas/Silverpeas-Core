/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.password.web;

import com.silverpeas.annotation.Authenticated;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.UserPriviledgeValidation;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.password.rule.PasswordRule;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.password.service.PasswordServiceFactory.getPasswordService;
import static org.silverpeas.password.web.PasswordResourceURIs.*;

/**
 * A REST Web resource giving gallery data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(PASSWORD_BASE_URI)
@Authenticated
public class PasswordResource extends AbstractPasswordResource {
  protected static ResourceLocator settings =
      new ResourceLocator("org.silverpeas.password.settings.password", "");
  protected static int nbMatchingCombinedRules =
      settings.getInteger("password.combination.nbMatchingRules", 0);

  /**
   * User authentication is not necessary for this WEB Service. The authentication processing is
   * used here to identify the user behind the call if possible.
   * @param validation the validation instance to use.
   * @throws WebApplicationException
   */
  @Override
  public void validateUserAuthentication(final UserPriviledgeValidation validation)
      throws WebApplicationException {
    try {
      super.validateUserAuthentication(validation);
    } catch (WebApplicationException wae) {
      if (Response.Status.UNAUTHORIZED.getStatusCode() != wae.getResponse().getStatus()) {
        throw wae;
      }
    }
  }

  /**
   * Gets the JSON representation of password policy.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         photo.
   */
  @GET
  @Path(PASSWORD_POLICY_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public PasswordPolicyEntity getPolicy() {
    try {
      final PasswordPolicyEntity passwordPolicy = PasswordPolicyEntity
          .createFrom(nbMatchingCombinedRules,
              getPasswordService().getExtraRuleMessage(getLanguage()));
      for (final PasswordRule rule : getPasswordService().getRequiredRules()) {
        passwordPolicy.addRule(asWebEntity(rule));
      }
      for (final PasswordRule rule : getPasswordService().getCombinedRules()) {
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
   * Gets the JSON representation of a list of errors catched by a password checking. The returned
   * list contains names of rules which are not verified.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         photo.
   */
  @POST
  @Path(PASSWORD_POLICY_URI_PART + "/" + PASSWORD_CHECKING_URI_PART)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PasswordCheckEntity checking(final PasswordEntity password) {
    return asWebEntity(getPasswordService().check(password.getValue()));
  }
}
