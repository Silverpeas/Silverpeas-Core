/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.pdc;

import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.silverpeas.core.webapi.pdc.PdcEntity.*;
import static org.silverpeas.core.util.logging.SilverLogger.*;

/**
 * A REST Web resource that represents the classification plan (named PdC) filtered by some
 * criteria.
 *
 * For a description of the PdC, see the documentation on {@link PdcResource}.
 */
@Service
@RequestScoped
@Path("pdc/filter")
@Authenticated
public class FilteredPdcResource extends RESTWebService {

  @Inject
  private PdcServiceProvider pdcServiceProvider;

  /**
   * Gets a PdC containing only the axis and the axis's value that were used in the classification
   * of the contents in Silverpeas. The Pdc can be restricted by the workspace and by the component
   * to which the classified contents belong. As the filtered PdC is for a search, only the
   * component instances configured as searchable are taken into account.
   *
   * The PdC that is sent back contains only the axis and, with each of them, the values to which
   * the contents in Silverpeas are classified. The classified contents to take into account can be
   * restricted by the workspace or by the application to which they belong, and by a set of axis'
   * values with which they have to be classified. The version of the returned PdC indicates, for
   * each axis's value, the count of contents that are classified with this value. According to the
   * query parameters, it can contain also the secondary axis of the PdC. The PdC is sent back in
   * JSON. If the user isn't authenticated, a 401 HTTP code is returned. If a problem occurs when
   * processing the request, a 503 HTTP code is returned.
   *
   * @param workspaceId optionally the unique identifier of the workspace in which the classified
   * contents are published.
   * @param componentId optionally the unique identifier of the component to which the classified
   * contents belong.
   * @param withSecondaryAxis optionally a boolean flag indicating whether the secondary PdC axis
   * should be taken into account.
   * @param axisValues optionally a set of axis' values on which the contents to take into account
   * have to be classified. A value is defined by the identifier of the axis it belongs to and by
   * its path from the root value in this axis, the two fields separated by a ':' character.
   *
   * @return a web entity representing the PdC filtered by the contents that are classified on it.
   * The entity is serialized in JSON.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("used")
  public PdcEntity getPdcFilteredByClassifiedContents(@QueryParam("workspaceId") String workspaceId,
      @QueryParam("componentId") String componentId,
      @QueryParam("withSecondaryAxis") boolean withSecondaryAxis,
      @QueryParam("values") String axisValues) {
    PdcFilterCriteria criteria = new PdcFilterCriteria().
        onWorkspace(workspaceId).
        onComponentInstance(componentId).
        onSecondaryAxisInclusion(withSecondaryAxis).
        onUser(getUserDetail());
    setAxisValues(criteria, axisValues);
    try {
      List<UsedAxis> axis = pdcServiceProvider().getAxisUsedInClassificationsByCriteria(criteria);
      UserPreferences userPreferences = getUserPreferences();
      return aPdcEntityWithUsedAxis(
          withAxis(axis),
          inLanguage(userPreferences.getLanguage()),
          atURI(getUriInfo().getRequestUri()),
          withThesaurusAccordingTo(userPreferences));
    } catch (Exception ex) {
      getLogger(this).error(ex.getMessage(), ex);
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return null;
  }

  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }

  private void setAxisValues(PdcFilterCriteria criteria, String axisValues) {
    List<AxisValueCriterion> axisValuesCriteria = AxisValueCriterion.fromFlattenedAxisValues(
        axisValues);
    criteria.onAxisValues(axisValuesCriteria);
  }

  private UserThesaurusHolder withThesaurusAccordingTo(UserPreferences userPreferences) {
    UserThesaurusHolder thesaurus = NoThesaurus;
    if (userPreferences.isThesaurusEnabled()) {
      thesaurus = pdcServiceProvider().getThesaurusOfUser(getUserDetail());
    }
    return thesaurus;
  }
}
