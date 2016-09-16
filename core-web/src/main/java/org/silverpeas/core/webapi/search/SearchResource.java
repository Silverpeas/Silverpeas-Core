package org.silverpeas.core.webapi.search;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.index.search.PlainSearchResult;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.webapi.pdc.AxisValueCriterion;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.silverpeas.core.contribution.contentcontainer.content
    .IGlobalSilverContentProcessor.PROCESSOR_NAME_SUFFIX;

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
    List<ResultEntity> resultsOnIndexes = new ArrayList<>();
    List<ResultEntity> resultsOnTaxonomy = new ArrayList<>();
    QueryDescription queryDescription = new QueryDescription(query);

    if (StringUtil.isDefined(startDate)) {
      try {
        startDate = Instant.ofEpochMilli(Long.valueOf(startDate)).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter);
        queryDescription.setRequestedCreatedAfter(startDate);
      } catch (Exception e) {
        // ignore date
      }
    }

    if (StringUtil.isDefined(endDate)) {
      try {
        endDate =
            Instant.ofEpochMilli(Long.valueOf(endDate)).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter);
        queryDescription.setRequestedCreatedBefore(endDate);
      } catch (Exception e) {
        // ignore date
      }
    }

    // determine where to search
    setComponents(queryDescription, spaceId, appId);

    if (!queryDescription.isEmpty()) {
      resultsOnIndexes = searchOnIndexes(queryDescription);
    }

    if (StringUtil.isDefined(position)) {
      resultsOnTaxonomy = searchOnTaxonomy(position, queryDescription);
    }

    if (!queryDescription.isEmpty() && StringUtil.isDefined(position)) {
      // mixed search : retains only common results
      resultsOnTaxonomy.retainAll(resultsOnIndexes);
      return resultsOnTaxonomy;
    } else if (!queryDescription.isEmpty()) {
      return resultsOnIndexes;
    } else {
      return resultsOnTaxonomy;
    }

  }

  private List<ResultEntity> searchOnTaxonomy(String position, QueryDescription query) {
    SearchContext pdcContext = new SearchContext(getUserDetail().getId());
    // Filters by the axis' values on the PdC the content to seek should be positioned.
    List<AxisValueCriterion> axisValueCriteria = AxisValueCriterion.fromFlattenedAxisValues(position);
    axisValueCriteria.forEach(anAxisValueCriterion->pdcContext.addCriteria(anAxisValueCriterion));

    // We get silvercontentids according to the search context, author, components and dates
    try {
      List<Integer> contentIds = new GlobalPdcManager().findSilverContentIdByPosition(pdcContext,
          new ArrayList<>(query.getWhereToSearch()), null, query.getRequestedCreatedAfter(),
          query.getRequestedCreatedBefore());

      List<GlobalSilverContent> contents = getGlobalSilverContent(contentIds);
      Collections.sort(contents, cDateDesc);
      return resultsFromTaxonomy(contents);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  private List<ResultEntity> resultsFromTaxonomy(List<GlobalSilverContent> contents) {
    List<ResultEntity> results = new ArrayList<>();
    for (GlobalSilverContent gsc : contents) {
      results.add(ResultEntity.fromGlobalSilverResult(new GlobalSilverResult(gsc)));
    }
    return results;
  }

  private List<ResultEntity> resultsFromIndexes(List<MatchingIndexEntry> indexEntries) {
    List<ResultEntity> results = new ArrayList<>();
    for (MatchingIndexEntry mie : indexEntries) {
      results.add(ResultEntity.fromMatchingindexEntry(mie));
    }
    return results;
  }

  private List<GlobalSilverContent> getGlobalSilverContent(List<Integer> silverContentIds)
      throws Exception {

    List<GlobalSilverContent> silverContents = new ArrayList<>();
    if (silverContentIds == null || silverContentIds.isEmpty()) {
      return silverContents;
    }

    ContentManager contentManager = new ContentManager();
    List<String> instanceIds = contentManager.getInstanceId(silverContentIds);

   for (String instanceId : instanceIds) {
      // On récupère tous les silverContentId d'un instanceId
      List<Integer> allSilverContentIds = contentManager.getSilverContentIdByInstanceId(instanceId);

      // une fois les SilverContentId de l'instanceId récupérés, on ne garde que ceux qui sont
      // dans la liste résultat (alSilverContentIds).
      allSilverContentIds.retainAll(silverContentIds);

     ContentPeas contentP = contentManager.getContentPeas(instanceId);
     if (contentP != null) {
        // we are going to search only SilverContent of this instanceId
        ContentInterface contentInterface = contentP.getContentInterface();
        List<SilverContentInterface> silverContentTempo = contentInterface.getSilverContentById(
            allSilverContentIds, instanceId, getUserDetail().getId());

        if (silverContentTempo != null) {
          silverContents.addAll(transformSilverContentsToGlobalSilverContents(silverContentTempo,
              instanceId));
        }
      }
    }
    return silverContents;
  }

  private List<GlobalSilverContent> transformSilverContentsToGlobalSilverContents(
      List<SilverContentInterface> silverContentTempo, String instanceId) throws Exception {
    List<GlobalSilverContent> alSilverContents = new ArrayList<>(silverContentTempo.size());
    String contentProcessorPrefixId = "default";
    if (instanceId.startsWith("gallery")) {
      contentProcessorPrefixId = "gallery";
    } else if (instanceId.startsWith("kmelia")) {
      contentProcessorPrefixId = "kmelia";
    }
    IGlobalSilverContentProcessor processor =
        ServiceProvider.getService(contentProcessorPrefixId + PROCESSOR_NAME_SUFFIX);

    for (SilverContentInterface sci : silverContentTempo) {
      GlobalSilverContent gsc =
          processor.getGlobalSilverContent(sci, UserDetail.getById(sci.getCreatorId()), null);
      alSilverContents.add(gsc);
    }
    return alSilverContents;
  }

  private List<ResultEntity> searchOnIndexes(QueryDescription queryDescription) {
    try {
      PlainSearchResult searchResult = SearchEngineProvider.getSearchEngine().search(queryDescription);
      return resultsFromIndexes(searchResult.getEntries());
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
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

  Comparator<GlobalSilverContent> cDateDesc = (o1, o2) -> {
    String string1 = o1.getCreationDate();
    String string2 = o2.getCreationDate();

    if (string1 != null && string2 != null) {
      int result = string2.compareTo(string1);
      // Add comparison on title if we have the same creation date
      return (result != 0) ? result : o2.getId().compareTo(o1.getId());
    }
    return 1;
  };

  @Override
  public String getComponentId() {
    return "";
  }

}