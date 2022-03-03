/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.index.search.SearchQueryProcessor;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.index.search.qualifiers.TaxonomySearch;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nicolas Eysseric
 */
@Service
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
    final SearchContext pdcContext = new SearchContext(query.getSearchingUser());
    AxisValueCriterion.fromFlattenedAxisValues(query.getTaxonomyPosition()).forEach(pdcContext::addCriteria);
    if (!pdcContext.isEmpty()) {
      try {
        // getting silver content ids according to the search context, author, components and dates
        final List<Integer> contentIds = pdcManager
            .findSilverContentIdByPosition(pdcContext, new ArrayList<>(query.getWhereToSearch()),
                null, query.getRequestedCreatedAfter(), query.getRequestedCreatedBefore());
        // getting sorted instance local contents by their ids
        return pdcManager.getSilverContentsByIds(contentIds, pdcContext.getUserId()).stream()
            .sorted(cDateDesc)
            .map(SearchResult::fromGlobalSilverContent)
            .collect(Collectors.toList());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("Error during taxonomy search by user {0}",
            new String[] {User.getById(pdcContext.getUserId()).getDisplayedName()}, e);
      }
    }
    return new ArrayList<>();
  }
}