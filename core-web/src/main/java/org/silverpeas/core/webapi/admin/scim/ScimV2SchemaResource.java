/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.admin.scim;

import edu.psu.swe.scim.server.rest.SchemaResourceImpl;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.core.webapi.admin.scim.ScimResourceURIs.SCIM_2_BASE_URI;

/**
 * References :
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7643</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7644</a></li>
 * </ul>
 * Implementation of a Client using SCIM 2.0 protocol.
 * @author silveryocha
 */
@WebService
@Path(SCIM_2_BASE_URI + "/Schemas")
@Authorized
@Alternative
@Priority(APPLICATION + 10)
public class ScimV2SchemaResource extends SchemaResourceImpl implements ScimProtectedWebResource {

  @PathParam("domainId")
  private String domainId;

  @Inject
  private ScimRequestContext scimRequestContext;

  @Context
  private HttpServletRequest httpRequest;

  @Context
  private HttpServletResponse httpResponse;

  @PostConstruct
  protected void initContext() {
    scimRequestContext.init(httpRequest, httpResponse, domainId);
  }

  @Override
  public ScimRequestContext getSilverpeasContext() {
    return scimRequestContext;
  }
}
