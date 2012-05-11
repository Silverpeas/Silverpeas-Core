/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.searchEngine.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * 
 *
 */
public class SearchCompletion {

  private static int pdcMaxRow = 100;

  private static int thesaurusMaxRow = 100;

  private static int keywordMaxRow = 100;

  private final String pdcQuery = "SELECT DISTINCT name FROM sb_tree_tree where lower(name) like ?";

  private final String thesaurusQuery =
      "SELECT DISTINCT name FROM  sb_thesaurus_synonym where lower(name) like ?";

  private final String keywordsQuery =
      "SELECT DISTINCT label FROM sb_tagcloud_tagcloud  where lower(label) like ?";

  /**
   * 
   */
  public SearchCompletion() {
  }

  /**
   * gets a list of keyword which start with the query parameter
   * @param query the String used to get the keywords list
   * @return a list of keywords sorted by natural order
   */
  public Set<String> getSuggestions(String query) {
    query = query.toLowerCase();
    SilverTrace.debug("searchEngine", "SearchCompletion.getSuggestions()",
        "root.MSG_GEN_PARAM_VALUE", "query = " + query);
    // local variable instantiation
    TreeSet<String> set = new TreeSet<String>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    ResourceLocator resource = new ResourceLocator(
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "");

    int autocompletionMaxResults = Integer.parseInt(resource.getString("autocompletionMaxResults"));
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
      // request pdc
      ArrayList<String> pdcList = executeQuery(con, pdcMaxRow,
          query, pdcQuery);
      // request thesaurus
      ArrayList<String> thesauruslist = executeQuery(con, thesaurusMaxRow,
          query, thesaurusQuery);
      // request keywords
      ArrayList<String> keywordsList = executeQuery(con, keywordMaxRow,
          query, keywordsQuery);

      // results consolidation
      int numberOfPdcSuggest = getSize(autocompletionMaxResults, pdcList);
      int numberOfThesaurusSuggest = getSize(autocompletionMaxResults, thesauruslist);
      int numberOfKeywordsSuggest = getSize(autocompletionMaxResults, keywordsList);

      set.addAll(pdcList.subList(0, numberOfPdcSuggest));
      set.addAll(thesauruslist.subList(0, numberOfThesaurusSuggest));
      set.addAll(keywordsList.subList(0, numberOfKeywordsSuggest));

    } catch (Exception e) {
      SilverTrace.error("searchEngine", "SearchCompletion.getSuggestions()",
          "root.EX_SQL_QUERY_FAILED", e);
    } finally {
      DBUtil.close(rs, ps);
      try {
        if (con != null && !con.isClosed()) {
          con.close();
        }
      } catch (SQLException e) {
        SilverTrace.error("searchEngine", "SearchCompletion.getSuggestions()()",
            "root.EX_CONNECTION_CLOSE_FAILED", e);
      }
    }

    return set;
  }

  /**
   * @param autocompletionMaxResults
   * @param pdcList
   */
  private int getSize(int autocompletionMaxResults, ArrayList<String> list) {
    int numberOfSuggest = 0;
    int pdcSize = list.size();
    if (pdcSize > 0) {
      if (autocompletionMaxResults / 3 > pdcSize) {
        numberOfSuggest = pdcSize;
      } else {
        numberOfSuggest = pdcSize - (autocompletionMaxResults / 3);
      }
    }
    return numberOfSuggest;
  }

  /**
   * @param con
   * @param maxRow
   * @param query
   * @param sqlQuery
   * @return
   * @throws SQLException
   */
  private ArrayList<String> executeQuery(Connection con, int maxRow,
      String query, String sqlQuery) throws SQLException {
    ArrayList<String> list = new ArrayList<String>();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(sqlQuery);
      ps.setString(1, query + "%");
      ps.setMaxRows(maxRow);

      rs = ps.executeQuery();

      while (rs.next()) {
        // TODO is the result can be null ?
        list.add(rs.getString(1).toLowerCase());
      }
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }

    }
    return list;
  }

}
