/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.webapi.search;

import org.apache.commons.collections.EnumerationUtils;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.search.SearchService;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
@Service
@RequestScoped
@Path("search")
@Authenticated
public class SearchResource extends RESTWebService {

  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ResultEntity> search(@QueryParam("query") String query,
      @QueryParam("taxonomyPosition") String position,
      @QueryParam("spaceId") String spaceId, @QueryParam("appId") String appId,
      @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate,
      @QueryParam("form") String form) {
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setTaxonomyPosition(position);

    setCreationDateInterval(queryDescription, startDate, endDate);

    // determine where to search
    setComponents(queryDescription, spaceId, appId);

    // add query parameters about form
    if (StringUtil.isDefined(form)) {
      List<String> paramNames = EnumerationUtils.toList(getHttpRequest().getParameterNames());
      setQueryFormFields(queryDescription, form, paramNames);
    }

    SearchService searchService = SearchService.get();
    List<ResultEntity> entities = new ArrayList<>();
    try {
      List<SearchResult> results = searchService.search(queryDescription);
      for (SearchResult result : results) {
        entities.add(ResultEntity.fromSearchResult(result));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Error during search...", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    return entities;
  }

  private void setComponents(QueryDescription queryDescription, String spaceId, String appId) {
    String userId = getUserDetail().getId();
    if (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(appId)) {
      String[] appIds = getOrganisationController().getAvailCompoIds(userId);
      for (String id : appIds) {
        queryDescription.addComponent(id);
      }
    } else if (StringUtil.isDefined(appId)) {
      if (getOrganisationController().isComponentAvailable(appId, userId)) {
        queryDescription.addComponent(appId);
      }
    } else {
      String[] appIds = getOrganisationController().getAvailCompoIds(spaceId, userId);
      for (String id : appIds) {
        queryDescription.addComponent(id);
      }
    }
  }

  private void setCreationDateInterval(QueryDescription queryDescription, String startDate,
      String endDate) {
    if (StringUtil.isDefined(startDate)) {
      try {
        String date = LocalDate.parse(startDate).format(formatter);
        queryDescription.setRequestedCreatedAfter(date);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Can't parse start date as Long : {0}",
            new String[] {startDate}, e);
      }
    }

    if (StringUtil.isDefined(endDate)) {
      try {
        String date = LocalDate.parse(endDate).format(formatter);
        queryDescription.setRequestedCreatedBefore(date);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Can't parse end date as Long : {0}",
            new String[] {endDate}, e);
      }
    }
  }

  private void setQueryFormFields(QueryDescription queryDescription, String form,
      List<String> paramNames) {
    List<FieldDescription> formQuery = new ArrayList<>();
    for (String paramName : paramNames) {
      if (paramName.startsWith("field_")) {
        String fieldName = paramName.replace("field_", "");
        String value = getHttpRequest().getParameter(paramName);
        formQuery.add(new FieldDescription(form+"$$"+fieldName, value, "fr"));
      } else if (paramName.startsWith("fieldDate_")) {
        String fieldName = paramName.replace("fieldDate_", "");
        String value = getHttpRequest().getParameter(paramName);
        String sDate = value.substring(0, value.indexOf(","));
        String eDate = value.substring(value.indexOf(",")+1);
        Date fromDate = null;
        Date toDate = null;
        try {
          fromDate = DateUtil.parseISO8601Date(sDate);
        } catch (ParseException e) {
          // ignore unparsable date
        }
        try {
          toDate = DateUtil.parseISO8601Date(eDate);
        } catch (ParseException e) {
          // ignore unparsable date
        }
        if (fromDate != null || toDate != null) {
          formQuery.add(new FieldDescription(form+"$$"+fieldName, fromDate, toDate, "fr"));
        }
      }
    }
    queryDescription.setFieldQueries(formQuery);
  }

  @Override
  public String getComponentId() {
    return "";
  }

}