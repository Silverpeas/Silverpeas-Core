/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.rest.RESTWebService;
import com.stratelia.silverpeas.pdc.model.PdcException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import static com.silverpeas.pdc.web.PdcClassificationEntity.*;

/**
 * A REST Web resource that represents the default classification set for a node within a component
 * instance.
 * 
 * With a default classification, any new contents added to a node could be classified onto the
 * classification plan (named PdC) with this default classification.
 * 
 * A classification on the PdC is defined by a set of different positions on the axis of the PdC.
 * A position is a set of one or more values in the axis of the PdC.
 * 
 * The positions of a given classification can be accessed with this Web resource by the URI of the
 * position; classifications and positions are exposed in the Web by Silverpeas and are thus
 * uniquely identified by an URI in the Web.
 */
@Service
@Scope("request")
@Path("pdc/{componentId}/default")
public class PdcDefaultClassificationResource extends RESTWebService {
  
  @Inject
  private PdcServiceProvider pdcServiceProvider;
  @PathParam("componentId")
  private String componentId;
  
  @Override
  protected String getComponentId() {
    return componentId;
  }

  /**
   * Gets the default classification on the PdC that is set for the contents in the node identified
   * by the query part of the request URI. If no node identifier is provided in the URI, the default
   * classification set for the whole component instance is seeked.
   * 
   * A node in a component instance is a generic way in Silverpeas to categorize hierarchically the
   * contents of the component instance. If no default classification onto the PdC is defined
   * for the requested node, a default classification is then looking backward among the parent
   * nodes up to the component instance itself.
   * 
   * The PdC classification is sent back in JSON.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If the user isn't authorized to access the requested resource, a 403 is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return a web entity representing the requested default PdC classification. If no default 
   * classification is defined along the path of the nodes up to the component instance, then an
   * empty classification is sent back.
   */
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public PdcClassificationEntity getDefaultPdCClassificationForContentsInNode(
          @QueryParam("nodeId") String nodeId) {
    checkUserPriviledges();
    try {
      return theDefaultClassificationOfNode(nodeId);
    } catch (PdcException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  private PdcClassificationEntity theDefaultClassificationOfNode(String nodeId) throws Exception {
    PdcClassification classification = pdcServiceProvider().getPreDefinedClassificationForContentsIn(
            nodeId, getComponentId());
    if (classification == PdcClassification.NO_DEFINED_CLASSIFICATION) {
      return undefinedClassification();
    } else {
      UserPreferences userPreferences = getUserPreferences();
      PdcClassificationEntity theClassificationEntity = aPdcClassificationEntity(
              fromPdcClassification(classification),
              inLanguage(userPreferences.getLanguage()),
              atURI(getUriInfo().getRequestUri()));
      if (userPreferences.isThesaurusEnabled()) {
        UserThesaurusHolder theUserThesaurus =
                pdcServiceProvider().getThesaurusOfUser(getUserDetail());
        theClassificationEntity.withSynonymsFrom(theUserThesaurus);
      }
      return theClassificationEntity;
    }
  }
  
  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }
}
