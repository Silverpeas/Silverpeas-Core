package org.silverpeas.core.webapi.search;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.search.SearchService;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
@Service
@RequestScoped
@Path("search")
@Authenticated
public class SearchResource extends RESTWebService {

  static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ResultEntity> search(@QueryParam("query") String query,
      @QueryParam("taxonomyPosition") String position,
      @QueryParam("spaceId") String spaceId, @QueryParam("appId") String appId,
      @QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.setTaxonomyPosition(position);

    if (StringUtil.isDefined(startDate)) {
      try {
        String date = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault())
            .toLocalDate().format(formatter);
        queryDescription.setRequestedCreatedAfter(date);
      } catch (Exception e) {
        SilverLogger.getLogger(this).info("Can't parse start date as Long : {0}",
            new String[] {startDate}, e);
      }
    }

    if (StringUtil.isDefined(endDate)) {
      try {
        String date =
            Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate()
                .format(formatter);
        queryDescription.setRequestedCreatedBefore(date);
      } catch (Exception e) {
        SilverLogger.getLogger(this).info("Can't parse end date as Long : {0}",
            new String[] {endDate}, e);
      }
    }

    // determine where to search
    setComponents(queryDescription, spaceId, appId);

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

  @Override
  public String getComponentId() {
    return "";
  }

}