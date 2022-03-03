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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NHelper;

import java.util.Collection;
import java.util.Map;

/**
 * This class represents contribution classified on taxonomy
 */
public class GlobalSilverContent extends AbstractI18NBean<GlobalSilverContentI18N>
    implements java.io.Serializable {

  private static final long serialVersionUID = 1L;
  private String url = "";
  private String location = "";
  private String id = "";
  private String instanceId = "";
  private String date = ""; // this is the updateDate
  private String creationDate = "";
  private String thumbnailURL = "";
  private String userId = "";
  private String type = "";

  /**
   * list of XML form fields used to sort results
   */
  private Map<String, String> sortableXMLFormFields = null;

  /* following attributes are exclusively used by taglibs */
  private String spaceId = "";

  private void init(String name, String desc, String id,
      String instanceId, String date, String userId) {
    setName(name);
    setDescription(desc);
    this.id = id;
    this.instanceId = instanceId;
    this.date = date;
    this.userId = userId;

    GlobalSilverContentI18N gscI18N =
        new GlobalSilverContentI18N(I18NHelper.defaultLanguage, name, desc);
    addTranslation(gscI18N);
  }

  // constructor
  public GlobalSilverContent(SilverContentInterface sci) {
    init(sci.getName(), sci.getDescription(), sci.getId(),
        sci.getInstanceId(), sci.getDate(), sci.getCreatorId());
    this.creationDate = sci.getSilverCreationDate();
    this.type = sci.getContributionId().getType();

    processLanguages(sci);
  }

  private void processLanguages(SilverContentInterface sci) {
    Collection<String> languages = sci.getLanguages();
    languages.forEach(l -> {
      GlobalSilverContentI18N gscI18N = new GlobalSilverContentI18N(l, sci.getName(l),
          sci.getDescription(l));
      addTranslation(gscI18N);
    });
  }

  public String getURL() {
    return url;
  }

  public String getLocation() {
    return location;
  }

  public String getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getTitle() {
    return getName();
  }

  public String getDate() {
    return this.date;
  }

  public String getUserId() {
    return this.userId;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public void setURL(String url) {
    this.url = url;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public String getThumbnailURL() {
    return thumbnailURL;
  }

  public void setThumbnailURL(String thumbnailURL) {
    this.thumbnailURL = thumbnailURL;
  }

  /**
   * gets the list of Sortable fields if the content is a form XML
   * @return the sortableXMLFormFields
   */
  public Map<String, String> getSortableXMLFormFields() {
    return sortableXMLFormFields;
  }

}