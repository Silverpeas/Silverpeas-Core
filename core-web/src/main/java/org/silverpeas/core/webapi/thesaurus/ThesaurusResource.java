/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.thesaurus;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.pdc.thesaurus.model.Synonym;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusService;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

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

@Service
@RequestScoped
@Path(ThesaurusResource.PATH)
@Authenticated
public class ThesaurusResource extends RESTWebService {

  static final String PATH = "thesaurus";

  @POST
  @Path("/vocabulary/{vocabularyId}/axis/{axisId}/values/{valueId}/synonyms")
  @Produces(MediaType.APPLICATION_JSON)
  public void setSynonyms(@PathParam("vocabularyId") String vocabularyId,
      @PathParam("axisId") String axisId, @PathParam("valueId") String valueId,
      @FormParam("synonym") Set<String> synonyms) {

    if (!getUser().isAccessAdmin() && !getUser().isAccessPdcManager()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    List<Synonym> synonymList = synonyms.stream()
        .flatMap(s -> Arrays.stream(s.split(",")))
        .map(s -> getNewSynonym(vocabularyId, axisId, valueId, s)).collect(Collectors.toList());
    try {
      getThesaurusService().updateSynonyms(synonymList);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      throw new WebApplicationException(INTERNAL_SERVER_ERROR);
    }
  }

  private Synonym getNewSynonym(String vocabularyId, String axisId, String valueId, String name) {
    Synonym syno = new Synonym();
    syno.setName(name);
    syno.setIdVoca(new Integer(vocabularyId).longValue());
    syno.setIdTree(new Integer(axisId).longValue());
    syno.setIdTerm(new Integer(valueId).longValue());
    return syno;
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