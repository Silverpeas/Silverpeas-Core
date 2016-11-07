package org.silverpeas.core.index.search;

import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchResult;

import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public interface SearchQueryProcessor {

  List<SearchResult> process(QueryDescription query, List<SearchResult> results);

}