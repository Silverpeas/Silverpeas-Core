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
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.core.webapi.pdc.PdcEntity.*;

import javax.ws.rs.*;

import static org.silverpeas.core.webapi.pdc.PdcServiceProvider.inComponentOfId;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * A REST Web resource that represents the classification plan (named PdC).
 *
 * The PdC is defined by a set of semantic axis that vehicle the business concepts and the
 * structures on which is based a given organization that uses the Silverpeas collaborative portal.
 * The values of an axis is thus made either of single terms (of the inherent concept or structure)
 * or of hierarchic semantic trees in which each branch carries an exactness about the value of a
 * concept. It exists two kinds of PdC: the model (or the referent) in which all the axis to be used
 * in the whole portal are defined, and the instances of the model that represent a PdC configured
 * for a given Silverpeas component instance. The instances of the model can be just a clone of the
 * model or a modified version by taking only some of the model's axis and by setting a different
 * origin value (among the possible values of the axis) for each chosen axis. Such PdCs are
 * identified by an unique URI in which the identifier of the Silverpeas component instance is
 * referenced.
 */
@Service
@RequestScoped
@Path("pdc")
@Authenticated
public class PdcResource extends RESTWebService {

  @Inject
  private PdcServiceProvider pdcServiceProvider;
  private String componentId;

  /**
   * Gets the PdC configured for the Silverpeas component instance identified by the requested URI.
   * The PdC that is sent back is adapted for classifying or updating the classification of the
   * resource content referred by the specified request query parameter. In that case, all the
   * invariant axis of the PdC will have an invariant value set with the one coming from the
   * classification of the resource. In effect, an invariant axis means that no other values are
   * possible when one was already set in a position of the content on the axis. The PdC is sent
   * back in JSON. If the user isn't authenticated, a 401 HTTP code is returned. If the user isn't
   * authorized to access the requested component instance, a 403 is returned. If the resource
   * content isn't indicated as query parameter, a 400 HTTP code is returned. If a problem occurs
   * when processing the request, a 503 HTTP code is returned.
   *
   * @param component the unique identifier of the component instance for which the PdC is
   * instanciated.
   * @param content the unique identifier of the content to classify with the PdC.
   *
   * @return a web entity representing the PdC ready to be used to classify a content. The entity is
   * serialized in JSON.
   */
  @GET
  @Path("{componentId:[a-zA-Z]+[0-9]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public PdcEntity getPdcForClassification(@PathParam("componentId") String component, @QueryParam(
      "contentId") String content) {
    setComponentId(component);
    try {
      List<UsedAxis> axis;
      if (isDefined(content)) {
        axis = pdcServiceProvider().getAxisUsedInPdcToClassify(
            content,
            inComponentOfId(getComponentId()));

      } else {
        axis = pdcServiceProvider().getAxisUsedInPdcFor(getComponentId());
      }

      UserPreferences userPreferences = getUserPreferences();
      return aPdcEntityWithUsedAxis(
          axis,
          inLanguage(userPreferences.getLanguage()),
          atURI(getUriInfo().getRequestUri()),
          withThesaurusAccordingTo(userPreferences));
    } catch (ContentManagerException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the PdC. The PdC is sent back in JSON. If the user isn't authenticated, a 401 HTTP code is
   * returned. If the user isn't authorized to access the requested component instance, a 403 is
   * returned. If the resource content isn't indicated as query parameter, a 400 HTTP code is
   * returned. If a problem occurs when processing the request, a 503 HTTP code is returned.
   *
   * @return a web entity representing the PdC. The entity is serialized in JSON.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PdcEntity getPdc() {
    try {
      List<Axis> axis = pdcServiceProvider().getAllAxis();

      UserPreferences userPreferences = getUserPreferences();
      return aPdcEntityWithAxis(
          axis,
          inLanguage(userPreferences.getLanguage()),
          atURI(getUriInfo().getRequestUri()),
          withThesaurusAccordingTo(userPreferences));
    } catch (WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Sets the specified identifier of the component instance to which the resource is referred. The
   * access right against the component is checked in the setting.
   *
   * @param componentId the unique identifier a component instance.
   */
  private void setComponentId(String componentId) {
    int index = componentId.indexOf("?contentId");
    if (index > 0) {
      this.componentId = componentId.substring(0, index);
    } else {
      this.componentId = componentId;
    }
    UserPrivilegeValidation validation = UserPrivilegeValidation.get();
    validateUserAuthorization(validation);
  }

  private PdcServiceProvider pdcServiceProvider() {
    return pdcServiceProvider;
  }

  private UserThesaurusHolder withThesaurusAccordingTo(UserPreferences userPreferences) {
    UserThesaurusHolder thesaurus = NoThesaurus;
    if (userPreferences.isThesaurusEnabled()) {
      thesaurus = pdcServiceProvider().getThesaurusOfUser(getUserDetail());
    }
    return thesaurus;
  }
}
