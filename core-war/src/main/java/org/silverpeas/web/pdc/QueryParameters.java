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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.util.StringUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class QueryParameters implements java.io.Serializable {

  public static final String PARAM_FOLDER = "folderSearch";

  private static final long serialVersionUID = -5191736720955151540L;
  private String keywords = null;
  private String spaceId = null;
  private String instanceId = null;
  private String creatorId = null;
  private LocalDate afterdate = null;
  private LocalDate beforedate = null;
  private LocalDate afterupdatedate = null;
  private LocalDate beforeupdatedate = null;
  private Map<String, String> xmlQuery = null;
  private String folder = null;

  // attributes below are used only to display info in the search page
  private UserDetail creatorDetail = null;

  public void clear() {
    this.keywords = null;
    this.spaceId = null;
    this.instanceId = null;
    this.creatorId = null;
    this.afterdate = null;
    this.beforedate = null;
    this.afterupdatedate = null;
    this.beforeupdatedate = null;
    this.creatorDetail = null;
    this.xmlQuery = null;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public String getSpaceId() {
    return spaceId;
  }

  /**
   * By using this method the spaceId filter is set.
   * The instanceId (if any) is cleared.
   * @param spaceId the unique identifier of a space
   */
  public void setSpaceId(String spaceId) {
    setSpaceIdAndInstanceId(spaceId, null);
  }

  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Setting an instanceId while the spaceId is not defined makes no sense here.
   * That's why a spaceId must be passed to the method, if it is null, empty or "*",
   * then no instanceId is set.
   * @param spaceId the unique identifier of a space
   * @param instanceId the unique identifier of a component instance
   */
  public void setSpaceIdAndInstanceId(String spaceId, String instanceId) {
    this.spaceId = null;
    this.instanceId = null;
    if (StringUtil.isDefined(spaceId) && !"*".equals(spaceId)) {
      this.spaceId = spaceId;
      if (StringUtil.isDefined(instanceId) && !"*".equals(instanceId)) {
        this.instanceId = instanceId;
      }
    }
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    if (!StringUtil.isDefined(creatorId) || "*".equals(creatorId)) {
      this.creatorId = null;
      this.creatorDetail = null;
    } else {
      this.creatorId = creatorId;
    }
  }

  public LocalDate getAfterDate() {
    return afterdate;
  }

  public void setAfterDate(LocalDate afterdate) {
    this.afterdate = afterdate;
  }

  public LocalDate getBeforeDate() {
    return beforedate;
  }

  public void setBeforeDate(LocalDate beforedate) {
    this.beforedate = beforedate;
  }

  public LocalDate getAfterUpdateDate() {
    return afterupdatedate;
  }

  public void setAfterUpdateDate(LocalDate afterdate) {
    this.afterupdatedate = afterdate;
  }

  public LocalDate getBeforeUpdateDate() {
    return beforeupdatedate;
  }

  public void setBeforeUpdateDate(LocalDate beforedate) {
    this.beforeupdatedate = beforedate;
  }

  public void addXmlSubQuery(String field, String query) {
    if (xmlQuery == null) {
      xmlQuery = new HashMap<>();
    }

    xmlQuery.put(field, query);
  }

  public void clearXmlQuery() {
    xmlQuery = null;
  }

  public QueryDescription getQueryDescription(String searchingUser, String searchingLanguage) {
    QueryDescription query = new QueryDescription(getKeywords());
    query.setSearchingUser(searchingUser);
    query.setRequestedLanguage(searchingLanguage);

    if (StringUtil.isDefined(getCreatorId())) {
      query.setRequestedAuthor(getCreatorId());
    } else {
      query.setRequestedAuthor(null);
    }

    query.setRequestedCreatedAfter(getAfterDate());
    query.setRequestedCreatedBefore(getBeforeDate());
    query.setRequestedUpdatedAfter(getAfterUpdateDate());
    query.setRequestedUpdatedBefore(getBeforeUpdateDate());

    if (xmlQuery != null) {
      for (var q : xmlQuery.entrySet()) {
        query.addFieldQuery(new FieldDescription(q.getKey(), q.getValue(), searchingLanguage));
      }
    }

    return query;
  }

  public void setCreatorDetail(UserDetail userDetail) {
    this.creatorDetail = userDetail;
  }

  public UserDetail getCreatorDetail() {
    return this.creatorDetail;
  }

  public boolean isDefined() {
    return StringUtil.isDefined(keywords) || afterdate != null || beforedate != null || StringUtil
        .isDefined(creatorId);
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public String getFolder() {
    return folder;
  }
}