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

package org.silverpeas.core.webapi.admin.scim;

import org.apache.directory.scim.protocol.Constants;
import org.apache.directory.scim.protocol.adapter.FilterWrapper;
import org.apache.directory.scim.protocol.data.PatchRequest;
import org.apache.directory.scim.protocol.data.SearchRequest;
import org.apache.directory.scim.protocol.exception.ScimException;
import org.apache.directory.scim.server.rest.GroupResourceImpl;
import org.apache.directory.scim.spec.exception.ResourceException;
import org.apache.directory.scim.spec.filter.SortOrder;
import org.apache.directory.scim.spec.filter.attribute.AttributeReference;
import org.apache.directory.scim.spec.filter.attribute.AttributeReferenceListWrapper;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;

import jakarta.ws.rs.core.Response;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.core.webapi.admin.scim.ScimResourceURIs.SCIM_2_BASE_URI;

/**
 * References :
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7643</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7644</a></li>
 * </ul>
 * Implementation of a Client using SCIM 2.0 protocol.
 *
 * @author silveryocha
 */
@WebService
@Path(SCIM_2_BASE_URI + "/Groups")
@Authorized
@Alternative
@Priority(APPLICATION + 10)
public class ScimV2GroupResource extends GroupResourceImpl implements ScimProtectedWebResource {

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

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.1">Scim spec,
   * retrieving known resources</a>
   */
  @GET
  @Path("{id}")
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response getById(@PathParam("id") final String id,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes)
      throws ScimException, ResourceException {
    return super.getById(id, attributes, excludedAttributes);
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.2">Scim spec,
   * query resources</a>
   */
  @GET
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response query(@QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes,
      @QueryParam("filter") final FilterWrapper filter,
      @QueryParam("sortBy") final AttributeReference sortBy,
      @QueryParam("sortOrder") final SortOrder sortOrder,
      @QueryParam("startIndex") final Integer startIndex,
      @QueryParam("count") final Integer count) throws ScimException, ResourceException {
    return super
        .query(attributes, excludedAttributes, filter, sortBy, sortOrder, startIndex, count);
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.3">Scim spec,
   * query resources</a>
   */
  @POST
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response create(final ScimGroup resource,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes)
      throws ScimException, ResourceException {
    return super.create(resource, attributes, excludedAttributes);
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.4.3">Scim spec,
   * query with post</a>
   */
  @POST
  @Path("/.search")
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response find(final SearchRequest request) throws ScimException, ResourceException {
    return super.find(request);
  }

  /**
   * @see <a href="https://tools.ietf.org/html/rfc7644#section-3.5.1">Scim spec,
   * update</a>
   */
  @PUT
  @Path("{id}")
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response update(final ScimGroup resource, @PathParam("id") final String id,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes)
      throws ScimException, ResourceException {
    return super.update(resource, id, attributes, excludedAttributes);
  }

  @PATCH
  @Path("{id}")
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  @Override
  public Response patch(final PatchRequest patchRequest, @PathParam("id") final String id,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes)
      throws ScimException, ResourceException {
    return super.patch(patchRequest, id, attributes, excludedAttributes);
  }

  @DELETE
  @Path("{id}")
  @Override
  public Response delete(@PathParam("id") final String id) throws ScimException, ResourceException {
    return super.delete(id);
  }

  @Override
  public ScimRequestContext getSilverpeasContext() {
    return scimRequestContext;
  }
}
