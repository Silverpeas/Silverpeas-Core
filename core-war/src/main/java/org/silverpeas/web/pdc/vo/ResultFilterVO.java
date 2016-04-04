/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.pdc.vo;

import java.util.HashMap;
import java.util.Map;

import org.silverpeas.core.util.StringUtil;

public class ResultFilterVO {
  private String authorId = null;
  private String componentId = null;
  private String datatype = null;
  private String filetype = null;
  private String year = null;
  private Map<String, String> formFieldFacets;

  /**
   * Default constructor
   */
  public ResultFilterVO() {
    super();
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public void addFormFieldSelectedFacetEntry(String facetId, String value) {
    if (formFieldFacets == null) {
      formFieldFacets = new HashMap<String, String>();
    }
    formFieldFacets.put(facetId, value);
  }

  public String getFormFieldSelectedFacetEntry(String facetId) {
    if (formFieldFacets == null) {
      return null;
    }
    return formFieldFacets.get(facetId);
  }

  public boolean isSelectedFormFieldFacetsEmpty() {
    if (formFieldFacets == null) {
      return true;
    }
    return formFieldFacets.isEmpty();
  }

  public boolean isEmpty() {
    return !StringUtil.isDefined(authorId) && !StringUtil.isDefined(componentId) &&
        !StringUtil.isDefined(datatype) && !StringUtil.isDefined(filetype) && isSelectedFormFieldFacetsEmpty();
  }

  public Map<String, String> getFormFieldSelectedFacetEntries() {
    return formFieldFacets;
  }

  public String toString() {
    StringBuilder str = new StringBuilder();
    if (StringUtil.isDefined(authorId)) {
      str.append("AuthorId=").append(authorId);
    }
    if (StringUtil.isDefined(componentId)) {
      str.append(" ComponentId=").append(componentId);
    }
    if (StringUtil.isDefined(datatype)) {
      str.append(" DataType=").append(datatype);
    }
    if (StringUtil.isDefined(filetype)) {
      str.append(" FileType=").append(filetype);
    }
    if (!isSelectedFormFieldFacetsEmpty()) {
      for (String facetId : formFieldFacets.keySet()) {
        str.append(" ").append(facetId).append("=").append(formFieldFacets.get(facetId));
      }
    }
    if (str.length() == 0) {
      return "Facets filter is empty";
    }
    return str.toString();
  }

  public void setDatatype(String datatype) {
    this.datatype = datatype;
  }

  public String getDatatype() {
    return datatype;
  }

  public void setFiletype(String filetype) {
    this.filetype = filetype;
  }

  public String getFiletype() {
    return filetype;
  }

}