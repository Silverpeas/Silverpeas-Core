/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.beans.admin.indexation;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.ParseException;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.search.searchEngine.model.WAIndexSearcher;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Indexation of the users in Silverpeas. It uses the indexer and searcher API to provide a way to
 * index and to search in a transparently way the users.
 */
public class UserIndexation {

  private WAIndexSearcher searcher = new WAIndexSearcher();
  static final String COMPONENT_ID = "users";
  public static final String OBJECT_TYPE = "UserFull";

  /**
   * Indexes the specified user. If no user exist with the specified unique identifier, nothing is
   * done.
   *
   * @param userId the unique identifier of the user to index.
   */
  public void indexUser(String userId) {
    try {
      UserFull user = UserFull.getById(userId);
      if (user != null) {
        FullIndexEntry indexEntry = new FullIndexEntry(COMPONENT_ID, OBJECT_TYPE, userId);
        indexEntry.setLastModificationDate(new Date());
        indexEntry.setTitle(user.getDisplayedName());
        indexEntry.setPreView(user.geteMail());

        // index some usefull informations
        indexEntry.addField("FirstName", user.getFirstName());
        indexEntry.addField("LastName", user.getLastName());
        indexEntry.addField("DomainId", user.getDomainId());
        indexEntry.addField("AccessLevel", user.getAccessLevel());

        // index extra informations
        String[] propertyNames = user.getPropertiesNames();
        StringBuilder extraValues = new StringBuilder(50);
        for (String propertyName : propertyNames) {
          String extraValue = user.getValue(propertyName);
          indexEntry.addField(propertyName, extraValue);
          extraValues.append(extraValue);
          extraValues.append(" ");
        }
        indexEntry.addTextContent(extraValues.toString());

        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception ex) {
      Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);
    }
  }

  public void unindexUser(String userId) {
    FullIndexEntry indexEntry = new FullIndexEntry(COMPONENT_ID, OBJECT_TYPE, userId);
    IndexEngineProxy.removeIndexEntry(indexEntry.getPK());
  }

  /**
   * Finds the users that match the specified query.
   *
   * The query is ran by the index searcher in order to find the indexes matching the request. Then
   * the details about the found users are fetched from the data source.
   *
   * @param queryDescription a description of the query to pass to the index and search engine.
   * @return a list of users matching the query.
   */
  public List<UserDetail> findUserFromQuery(final QueryDescription queryDescription) {
    try {
      List<UserDetail> foundUsers = new ArrayList<UserDetail>();
      MatchingIndexEntry[] results = searcher.search(queryDescription);
      for (MatchingIndexEntry aResult : results) {
        if (OBJECT_TYPE.equals(aResult.getObjectType())) {
          foundUsers.add(toUserDetail(aResult));
        }
      }
      return foundUsers;
    } catch (ParseException ex) {
      Logger.getLogger(UserIndexation.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private UserDetail toUserDetail(MatchingIndexEntry entry) {
    return UserDetail.getById(entry.getObjectId());
  }
}
