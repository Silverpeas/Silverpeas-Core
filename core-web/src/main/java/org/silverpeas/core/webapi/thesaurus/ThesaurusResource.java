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
package org.silverpeas.core.webapi.thesaurus;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusService;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@WebService
@Path(ThesaurusResource.PATH)
@Authorized
public class ThesaurusResource extends RESTWebService {

  static final String PATH = "thesaurus";

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) {
    if (!getUser().isAccessAdmin() && !getUser().isAccessPdcManager()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  @POST
  @Path("/vocabulary/{vocabularyId}/axis/{axisId}/values/{valueId}/synonyms")
  @Produces(MediaType.APPLICATION_JSON)
  public void setSynonyms(@PathParam("vocabularyId") String vocabularyId,
      @PathParam("axisId") String axisId, @PathParam("valueId") String valueId,
      @FormParam("synonym") Set<String> synonyms) {

    final List<Synonym> synonymList = synonyms.stream()
        .flatMap(s -> Arrays.stream(s.split(",")))
        .distinct()
        .filter(StringUtil::isDefined)
        .map(s -> getNewSynonym(vocabularyId, axisId, valueId, s))
        .collect(Collectors.toList());

    final int nbLimit = ResourceLocator
        .getSettingBundle("org.silverpeas.thesaurusPeas.settings.thesaurusSettings")
        .getInteger("thesaurus.synonym.nbmax", 5);
    if (synonymList.size() > nbLimit) {
      throw new WebApplicationException("only " + nbLimit + " synonyms at most",
          Response.Status.NOT_ACCEPTABLE);
    }

    try {
      getThesaurusService().updateSynonyms(synonymList);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      throw new WebApplicationException(INTERNAL_SERVER_ERROR);
    }
  }

  private Synonym getNewSynonym(String vocabularyId, String axisId, String valueId, String name) {
    Synonym synonym = new Synonym();
    synonym.setName(name);
    synonym.setIdVoca(new Integer(vocabularyId).longValue());
    synonym.setIdTree(new Integer(axisId).longValue());
    synonym.setIdTerm(new Integer(valueId).longValue());
    return synonym;
  }

  private ThesaurusService getThesaurusService() {
    return ThesaurusService.getInstance();
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  @Override
  public String getComponentId() {
    return "";
  }
}