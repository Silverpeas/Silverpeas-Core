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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.security;

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ProgressControlCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.AbstractCmisServiceWrapper;
import org.silverpeas.cmis.CmisRequestContext;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.WebAuthenticationValidation;
import org.silverpeas.core.web.rs.WebAuthorizationValidation;

import javax.ws.rs.WebApplicationException;

import static org.silverpeas.core.util.StringUtil.*;

/**
 * Authenticates and identifies the user requesting a CMIS service.
 * <p>
 * It wraps the requested CMIS
 * service in the chain of responsibilities that passes the control over each CMIS service wrapper
 * in order they perform their own control on the user request before eventually invoke the asked
 * CMIS service's functionality.
 * </p>
 * @author mmoquillon
 */
public class CmisUserAuthenticator extends AbstractCmisServiceWrapper
    implements ProgressControlCmisService, WebAuthenticationValidation, WebAuthorizationValidation {

  public CmisUserAuthenticator(final CmisService service) {
    super(service);
  }

  @Override
  public void setCallContext(final CallContext callContext) {
    final CmisRequestContext context;
    if (callContext instanceof CmisRequestContext) {
      context = (CmisRequestContext) callContext;
    } else {
      context = new CmisRequestContext(callContext);
    }
    super.setCallContext(context);
  }

  @Override
  public CmisRequestContext getSilverpeasContext() {
    return (CmisRequestContext) getCallContext();
  }

  @Override
  public Progress beforeServiceCall() {
    checkUserAuthentication();
    return super.beforeServiceCall();
  }

  private void checkUserAuthentication() {
    try {
      prepareForSOAPUsernameToken();
      final UserPrivilegeValidation validation =
          ServiceProvider.getSingleton(UserPrivilegeValidation.class);
      validateUserAuthentication(validation);
      validateUserAuthorization(validation);
    } catch (WebApplicationException e) {
      throw new CmisPermissionDeniedException(e.getMessage());
    }
  }

  private void prepareForSOAPUsernameToken() {
    final CmisRequestContext context = getSilverpeasContext();
    final CmisRequest request = context.getRequest();
    if (isNotDefined(request.getHeader(UserPrivilegeValidation.HTTP_AUTHORIZATION)) &&
        isDefined(context.getUsername()) && isDefined(context.getPassword())) {
      // at this point, the UsernameToken was loaded by OpenCMIS into the CallContext instance
      // (username and password properties)
      final String credentials = context.getUsername() + ":" + context.getPassword();
      request.addHeader(UserPrivilegeValidation.HTTP_AUTHORIZATION,
          "Basic " + asBase64(credentials.getBytes(Charsets.UTF_8)));
    }
  }
}
  