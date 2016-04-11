/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
* This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
* You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.search.model;

import org.silverpeas.core.silvertrace.SilverTrace;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.silverpeas.core.index.indexing.model.IndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;

/**
 * A MatchingIndexEntry is an IndexEntry completed with a score by the search engine.
 */
public class MatchingIndexEntry extends IndexEntry implements Serializable {

  /**
   * List of all linked attachment in wysiwyg content
   */
  private List<String> embeddedFileIds;
  /**
   * list of XML form fields used to sort results
   */
  private Map<String, String> sortableXMLFormFields = null;
  private Map<String, String> xmlFormFieldsForFacet = null;
  private static final long serialVersionUID = 5931254295396221458L;
  /**
   * UTF-8 encode definition
   */
  private static final String CHARACTER_ENCODING = "UTF-8";

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
  public MatchingIndexEntry(IndexEntryKey pk) {
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
    String type = "";
    String id = "";
    try {
      type = URLEncoder.encode(getObjectType(), CHARACTER_ENCODING);
      id = URLEncoder.encode(getObjectId(), CHARACTER_ENCODING);
    } catch (UnsupportedEncodingException e) {
      SilverTrace.error("SearchEngine", "MatchingIndexEntry.getPageAndParams()",
          "Unsupported encoding", e);
    }

    return "searchResult?Type=" + type + "&Id=" + id;
  }

  public String getParams() {
    String id = "";
    try {
      id = URLEncoder.encode(getObjectId(), CHARACTER_ENCODING);
    } catch (UnsupportedEncodingException e) {
      SilverTrace.error("SearchEngine", "MatchingIndexEntry.getParams()",
          "Unsupported encoding", e);
    }
    return "documentId%3d" + id;
  }

  /**
   * gets the list of Sortable fields if the content is a form XML
   *
   * @return the sortableXMLFormFields
   */
  public Map<String, String> getSortableXMLFormFields() {
    return sortableXMLFormFields;
  }

  /**
   * Sets the Sortable fields if the content is a form XML
   *
   * @param sortableXMLFormFields the sortableXMLFormFields to set
   */
  public void setSortableXMLFormFields(HashMap<String, String> sortableXMLFormFields) {
    this.sortableXMLFormFields = sortableXMLFormFields;
  }
  /**
   * The score defaults to 0 as if the entry wasn't a matching entry.
   */
  private float score = 0;

  /**
   * Set the list of all linked attachment in wysiwyg content
   *
* @param embeddedFileIds attachments ids separated by a blank space
   */
  public void setEmbeddedFileIds(String[] embeddedFileIds) {
    if (embeddedFileIds == null) {
      this.embeddedFileIds = new ArrayList<String>();
    } else {
      this.embeddedFileIds = Arrays.asList(embeddedFileIds.clone());
    }
  }

  /**
   * List of all linked attachment in wysiwyg content
   */
  public List<String> getEmbeddedFileIds() {
    return embeddedFileIds;
  }

  public void setXMLFormFieldsForFacets(Map<String, String> fields) {
    xmlFormFieldsForFacet = fields;
  }

  public Map<String, String> getXMLFormFieldsForFacets() {
    return xmlFormFieldsForFacet;
  }
}
