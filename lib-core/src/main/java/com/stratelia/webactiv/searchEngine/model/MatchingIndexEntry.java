/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.io.Serializable;
import java.net.URLEncoder;

import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * A MatchingIndexEntry is an IndexEntry completed with a score by the search engine.
 */
public class MatchingIndexEntry extends IndexEntry implements Serializable {
  /**
   * marks an entry as read
   */
  private boolean hasRead = false;

  /**
   * The constructor set only the key part of the entry.
   */
  public MatchingIndexEntry(String space, String component, String objectType,
      String objectId) {
    super(component, objectType, objectId);
  }

  /**
   * The constructor set only the key part of the entry.
   */
  public MatchingIndexEntry(IndexEntryPK pk) {
    super(pk);
  }

  /**
   * Return the score of this entry according the request.
   */
  public float getScore() {
    return score;
  }

  /**
   * Set the score of this entry. Only the searchEngine should call this method.
   */
  public void setScore(float score) {
    this.score = score;
  }

  /**
   * Returns web'activ logic parameters for the URL used to displayed this entry.
   */
  public String getPageAndParams() {
    String type = URLEncoder.encode(getObjectType());
    String id = URLEncoder.encode(getObjectId());

    return "searchResult?Type=" + type + "&Id=" + id;
  }

  public String getParams() {
    String id = URLEncoder.encode(getObjectId());

    return "documentId%3d" + id;
  }

  /**
   * indicates if a entry has been read
   * @return the hasRead
   */
  public boolean isHasRead() {
    return hasRead;
  }

  /**
   * @param hasRead the hasRead to set
   */
  public void setHasRead(boolean hasRead) {
    this.hasRead = hasRead;
  }

  /**
   * The score defaults to 0 as if the entry wasn't a matching entry.
   */
  private float score = 0;
}
