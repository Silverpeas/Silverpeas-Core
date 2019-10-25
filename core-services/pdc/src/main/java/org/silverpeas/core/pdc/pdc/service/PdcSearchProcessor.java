/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.index.search.SearchQueryProcessor;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.index.search.qualifiers.TaxonomySearch;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.silverpeas.core.contribution.contentcontainer.content
    .IGlobalSilverContentProcessor.PROCESSOR_NAME_SUFFIX;

/**
 * @author Nicolas Eysseric
 */
@Singleton
@TaxonomySearch
public class PdcSearchProcessor implements SearchQueryProcessor {

  @Inject
  private PdcManager pdcManager;

  private Comparator<GlobalSilverContent> cDateDesc = (o1, o2) -> {
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
  public List<SearchResult> process(final QueryDescription query,
      final List<SearchResult> results) {

    SearchContext pdcContext = new SearchContext(query.getSearchingUser());

    List<AxisValueCriterion> axisValueCriteria =
        AxisValueCriterion.fromFlattenedAxisValues(query.getTaxonomyPosition());
    axisValueCriteria.forEach(anAxisValueCriterion->pdcContext.addCriteria(anAxisValueCriterion));

    if (!pdcContext.isEmpty()) {
      // We get silvercontentids according to the search context, author, components and dates
      try {
        List<Integer> contentIds = pdcManager
            .findSilverContentIdByPosition(pdcContext, new ArrayList<>(query.getWhereToSearch()),
                null, query.getRequestedCreatedAfter(), query.getRequestedCreatedBefore());

        List<GlobalSilverContent> contents =
            getGlobalSilverContents(contentIds, pdcContext.getUserId());
        Collections.sort(contents, cDateDesc);

        return toSearchResults(contents);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Error during taxonomy search by user {0}",
            new String[] {User.getById(pdcContext.getUserId()).getDisplayedName()}, e);
      }
    }
    return new ArrayList<>();
  }

  private List<GlobalSilverContent> getGlobalSilverContents(List<Integer> silverContentIds,
      String userId) throws ContentManagerException {

    List<GlobalSilverContent> silverContents = new ArrayList<>();
    if (silverContentIds == null || silverContentIds.isEmpty()) {
      return silverContents;
    }

    ContentManager contentManager = ContentManagerProvider.getContentManager();
    List<String> instanceIds = contentManager.getInstanceId(silverContentIds);

    for (String instanceId : instanceIds) {
      try {
        // On récupère tous les silverContentId d'un instanceId
        List<Integer> allSilverContentIds =
            contentManager.getSilverContentIdByInstanceId(instanceId);

        // une fois les SilverContentId de l'instanceId récupérés, on ne garde que ceux qui sont
        // dans la liste résultat (alSilverContentIds).
        allSilverContentIds.retainAll(silverContentIds);

        ContentPeas contentP = contentManager.getContentPeas(instanceId);
        if (contentP != null) {
          // we are going to search only SilverContent of this instanceId
          ContentInterface contentInterface = contentP.getContentInterface();
          List<SilverContentInterface> silverContentTempo =
              contentInterface.getSilverContentById(allSilverContentIds, instanceId, userId);

          if (silverContentTempo != null) {
            silverContents.addAll(
                transformSilverContentsToGlobalSilverContents(silverContentTempo, instanceId));
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Can't retrieve content from taxonomy for component {0}",
            new String[] {instanceId}, e);
      }
    }
    return silverContents;
  }

  private List<GlobalSilverContent> transformSilverContentsToGlobalSilverContents(
      List<SilverContentInterface> silverContentTempo, String instanceId) {
    List<GlobalSilverContent> alSilverContents = new ArrayList<>(silverContentTempo.size());
    String contentProcessorPrefixId = "default";
    if (instanceId.startsWith("gallery")) {
      contentProcessorPrefixId = "gallery";
    } else if (instanceId.startsWith("kmelia")) {
      contentProcessorPrefixId = "kmelia";
    }
    IGlobalSilverContentProcessor processor =
        ServiceProvider.getSingleton(contentProcessorPrefixId + PROCESSOR_NAME_SUFFIX);

    for (SilverContentInterface sci : silverContentTempo) {
      GlobalSilverContent gsc = processor.getGlobalSilverContent(sci);
      alSilverContents.add(gsc);
    }
    return alSilverContents;
  }

  private List<SearchResult> toSearchResults(List<GlobalSilverContent> contents) {
    List<SearchResult> results = new ArrayList<>();
    for (GlobalSilverContent gsc : contents) {
      results.add(SearchResult.fromGlobalSilverContent(gsc));
    }
    return results;
  }
}