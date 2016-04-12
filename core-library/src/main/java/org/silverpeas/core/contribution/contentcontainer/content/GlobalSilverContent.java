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

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;

import java.util.Iterator;
import java.util.Map;

/**
 * This class allows the result jsp page of the global search to show all features (name,
 * description, location)
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
  private String icon_url = "";

  private String thumbnailURL = "";

  private String userId = "";
  private String creatorFirstName = "";
  private String creatorLastName = "";
  /**
   * list of XML form fields used to sort results
   */
  private Map<String, String> sortableXMLFormFields = null;

  /* following attributes are exclusively used by taglibs */
  private String spaceId = "";
  private float score = 0;
  private String type = "";

  public void init(String name, String desc, String url, String location, String id,
      String instanceId, String date, String icon, String userId) {
    setName(name);
    setDescription(desc);
    this.url = url;
    this.location = location;
    this.id = id;
    this.instanceId = instanceId;
    this.date = date;
    this.icon_url = icon;
    this.userId = userId;

    GlobalSilverContentI18N gscI18N =
        new GlobalSilverContentI18N(I18NHelper.defaultLanguage, name, desc);
    addTranslation(gscI18N);
  }

  // constructor
  public GlobalSilverContent(String name, String desc, String id, String spaceId, String instanceId,
      String date, String userId) {
    init(name, desc, null, null, id, instanceId, date, null, userId);
    this.spaceId = spaceId;
  }

  public GlobalSilverContent(MatchingIndexEntry mie) {
    init(mie.getTitle(), mie.getPreView(), null, null, mie.getObjectId(), mie.getComponent(),
        mie.getLastModificationDate(), null, mie.getCreationUser());
    setCreationDate(mie.getCreationDate());

    // add the sortable feld from XML form
    sortableXMLFormFields = mie.getSortableXMLFormFields();

    Iterator<String> languages = mie.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      GlobalSilverContentI18N gscI18N =
          new GlobalSilverContentI18N(language, mie.getTitle(language), mie.getPreview(language));
      addTranslation(gscI18N);
    }
  }

  // constructor
  public GlobalSilverContent(SilverContentInterface sci, String location) {
    init(sci.getName(), sci.getDescription(), sci.getURL(), location, sci.getId(),
        sci.getInstanceId(), sci.getDate(), sci.getIconUrl(), sci.getCreatorId());
    this.creationDate = sci.getSilverCreationDate();

    processLanguages(sci);
  }

  // constructor
  public GlobalSilverContent(SilverContentInterface sci, String location, String creatorFirstName,
      String creatorLastName) {
    init(sci.getName(), sci.getDescription(), sci.getURL(), location, sci.getId(),
        sci.getInstanceId(), sci.getDate(), sci.getIconUrl(), sci.getCreatorId());
    this.creationDate = sci.getSilverCreationDate();
    this.creatorFirstName = creatorFirstName;
    this.creatorLastName = creatorLastName;

    processLanguages(sci);
  }

  private void processLanguages(SilverContentInterface sci) {
    Iterator<String> languages = sci.getLanguages();
    while (languages != null && languages.hasNext()) {
      String language = languages.next();
      GlobalSilverContentI18N gscI18N = new GlobalSilverContentI18N(language, sci.getName(language),
          sci.getDescription(language));
      addTranslation(gscI18N);
    }
  }

  //
  // public methods
  //

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

  public String getIconUrl() {
    return this.icon_url;
  }

  public void setIconUrl(String iconURL) {
    this.icon_url = iconURL;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getCreatorFirstName() {
    return this.creatorFirstName;
  }

  public String getCreatorLastName() {
    return this.creatorLastName;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public float getRawScore() {
    return this.score;
  }

  public String getScore() {
    return Float.toString(this.score * 100);
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

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setTitle(String title) {
    setName(title);
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

  /**
   * Sets the Sortable fields if the content is a form XML
   * @param sortableXMLFormFields the sortableXMLFormFields to set
   */
  public void setSortableXMLFormFields(Map<String, String> sortableXMLFormFields) {
    this.sortableXMLFormFields = sortableXMLFormFields;
  }

}