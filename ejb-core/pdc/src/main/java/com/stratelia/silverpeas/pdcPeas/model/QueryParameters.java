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

package com.stratelia.silverpeas.pdcPeas.model;

import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;

public class QueryParameters implements java.io.Serializable {
  // class version identifier
  private static final long serialVersionUID = -5191736720955151540L;
  private String keywords = null;
  private String spaceId = null;
  private String instanceId = null;
  private String creatorId = null;
  private Date afterdate = null;
  private Date beforedate = null;
  private Date afterupdatedate = null;
  private Date beforeupdatedate = null;

  private Hashtable<String, String> xmlQuery = null;
  private String xmlTitle = null;

  // attributes below are used only to display info in the search page
  private UserDetail creatorDetail = null;

  public QueryParameters() {
  }

  public QueryParameters(String keywords, String spaceId, String instanceId,
      String creatorId, Date afterDate, Date beforeDate) {
    this.keywords = keywords;
    if (spaceId != null && spaceId.length() > 0) {
      this.spaceId = spaceId;
    }
    if (instanceId != null && instanceId.length() > 0) {
      this.instanceId = instanceId;
    }
    if (creatorId != null && creatorId.length() > 0) {
      this.creatorId = creatorId;
    }
    if (afterDate != null) {
      this.afterdate = afterDate;
    }
    if (beforeDate != null) {
      this.beforedate = beforeDate;
    }
  }

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

  public void setSpaceId(String spaceId) {
    if (!StringUtil.isDefined(spaceId) || spaceId.equals("*")) {
      this.spaceId = null;
    } else {
      this.spaceId = spaceId;
    }
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    if (!StringUtil.isDefined(instanceId) || instanceId.equals("*")) {
      this.instanceId = null;
    } else {
      this.instanceId = instanceId;
    }
  }

  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    if (creatorId == null || creatorId.length() == 0 || creatorId.equals("*")) {
      this.creatorId = null;
      this.creatorDetail = null;
    } else {
      this.creatorId = creatorId;
    }
  }

  public Date getAfterDate() {
    return afterdate;
  }

  public void setAfterDate(Date afterdate) {
    this.afterdate = afterdate;
  }

  public Date getBeforeDate() {
    return beforedate;
  }

  public void setBeforeDate(Date beforedate) {
    this.beforedate = beforedate;
  }

  public Date getAfterUpdateDate() {
    return afterupdatedate;
  }

  public void setAfterUpdateDate(Date afterdate) {
    this.afterupdatedate = afterdate;
  }

  public Date getBeforeUpdateDate() {
    return beforeupdatedate;
  }

  public void setBeforeUpdateDate(Date beforedate) {
    this.beforeupdatedate = beforedate;
  }

  public void addXmlSubQuery(String field, String query) {
    if (xmlQuery == null) {
      xmlQuery = new Hashtable<String, String>();
    }

    xmlQuery.put(field, query);
  }

  public Hashtable<String, String> getXmlQuery() {
    return xmlQuery;
  }

  public void clearXmlQuery() {
    xmlQuery = null;
  }

  public QueryDescription getQueryDescription(String searchingUser,
      String searchingLanguage) throws ParseException {
    QueryDescription query = new QueryDescription(getKeywords());

    query.setSearchingUser(searchingUser);
    query.setRequestedLanguage(searchingLanguage);

    if (getCreatorId() != null && !getCreatorId().equals("")) {
      query.setRequestedAuthor(getCreatorId());
    } else {
      query.setRequestedAuthor(null);
    }

    if (getAfterDate() != null) {
      query.setRequestedCreatedAfter(DateUtil.date2SQLDate(getAfterDate()));
    } else {
      query.setRequestedCreatedAfter(null);
    }

    if (getBeforeDate() != null) {
      query.setRequestedCreatedBefore(DateUtil.date2SQLDate(getBeforeDate()));
    } else {
      query.setRequestedCreatedBefore(null);
    }

    if (getAfterUpdateDate() != null) {
      query.setRequestedUpdatedAfter(DateUtil.date2SQLDate(getAfterUpdateDate()));
    } else {
      query.setRequestedUpdatedAfter(null);
    }

    if (getBeforeUpdateDate() != null) {
      query.setRequestedUpdatedBefore(DateUtil.date2SQLDate(getBeforeUpdateDate()));
    } else {
      query.setRequestedUpdatedBefore(null);
    }

    if (xmlQuery != null) {
      query.setXmlQuery(xmlQuery);
    }

    if (xmlTitle != null) {
      query.setXmlTitle(xmlTitle);
    }

    return query;
  }

  public void setCreatorDetail(UserDetail userDetail) {
    this.creatorDetail = userDetail;
  }

  public UserDetail getCreatorDetail() {
    return this.creatorDetail;
  }

  public String getXmlTitle() {
    return xmlTitle;
  }

  public void setXmlTitle(String xmlTitle) {
    this.xmlTitle = xmlTitle;
  }

  public boolean isDefined() {
    return StringUtil.isDefined(keywords) || afterdate != null ||
        beforedate != null || StringUtil.isDefined(creatorId);
  }
}