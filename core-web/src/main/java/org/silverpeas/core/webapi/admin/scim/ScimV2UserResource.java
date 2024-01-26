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

import edu.psu.swe.scim.server.rest.UserResourceImpl;
import edu.psu.swe.scim.spec.adapter.FilterWrapper;
import edu.psu.swe.scim.spec.protocol.Constants;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReferenceListWrapper;
import edu.psu.swe.scim.spec.protocol.data.PatchRequest;
import edu.psu.swe.scim.spec.protocol.data.SearchRequest;
import edu.psu.swe.scim.spec.protocol.search.SortOrder;
import edu.psu.swe.scim.spec.resources.ScimUser;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.webapi.admin.scim.adaptation.SilverpeasPatchRequest;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;

import static java.text.MessageFormat.format;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;
import static org.silverpeas.core.webapi.admin.scim.ScimResourceURIs.SCIM_2_BASE_URI;

/**
 * References :
 * <ul>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7643</a></li>
 * <li><a href="https://tools.ietf.org/html/rfc7643">https://tools.ietf.org/html/rfc7644</a></li>
 * </ul>
 * Implementation of a user provisioning server using SCIM 2.0 protocol.
 * @author silveryocha
 */
@WebService
@Path(SCIM_2_BASE_URI + "/Users")
@Authorized
@Alternative
@Priority(APPLICATION + 10)
public class ScimV2UserResource extends UserResourceImpl implements ScimProtectedWebResource {

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
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes) {
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
      @QueryParam("count") final Integer count) {
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
  public Response create(final ScimUser resource,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes) {
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
  public Response find(final SearchRequest request) {
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
  public Response update(final ScimUser resource, @PathParam("id") final String id,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes) {
    return super.update(resource, id, attributes, excludedAttributes);
  }

  @PATCH
  @Path("{id}")
  @Consumes(Constants.SCIM_CONTENT_TYPE)
  @Produces(Constants.SCIM_CONTENT_TYPE)
  public Response patch(@PathParam("id") final String id,
      @QueryParam("attributes") final AttributeReferenceListWrapper attributes,
      @QueryParam("excludedAttributes") final AttributeReferenceListWrapper excludedAttributes)
      throws IOException {
    final StringWriter writer = new StringWriter();
    final String charset = isDefined(httpRequest.getCharacterEncoding())
        ? httpRequest.getCharacterEncoding()
        : Charsets.UTF_8.name();
    IOUtils.copy(httpRequest.getInputStream(), writer, charset);
    // Adapting the JSON structure in order to use the work done by edu.psu.swe
    final String scimJson = writer.toString();
    final String adaptedScimJson = scimJson
        .replaceAll("(?i)\"patchOperationList\":\\[", "\"adaptedPatchOperationList\":[")
        .replaceAll("(?i)\"Operations\":\\[", "\"adaptedPatchOperationList\":[")
        .replaceAll("(?i)\"path\":\"([^\"]+)\"", "\"adaptedPath\":\"$1\"")
        .replaceAll("(?i)\"op\":\"Add\",", "\"op\":\"add\",")
        .replaceAll("(?i)\"op\":\"Replace\",", "\"op\":\"replace\",")
        .replaceAll("(?i)\"op\":\"Remove\",", "\"op\":\"remove\",");
    logger().debug(() -> format("patching data: {0}", scimJson));
    final SilverpeasPatchRequest silverpeasPatchRequest = JSONCodec
        .decode(adaptedScimJson, SilverpeasPatchRequest.class);
    silverpeasPatchRequest.afterDecode();
    return super.patch(silverpeasPatchRequest, id, attributes, excludedAttributes);
  }

  @PATCH
  @Path("{id}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  @Override
  public Response patch(final PatchRequest patchRequest, final String id,
      final AttributeReferenceListWrapper attributes,
      final AttributeReferenceListWrapper excludedAttributes) {
    throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
  }

  @DELETE
  @Path("{id}")
  @Override
  public Response delete(@PathParam("id") final String id) {
    return super.delete(id);
  }

  @Override
  public ScimRequestContext getSilverpeasContext() {
    return scimRequestContext;
  }
}
