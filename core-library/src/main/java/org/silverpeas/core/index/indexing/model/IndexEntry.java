/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * IndexEntry is the base class for all the entries which are indexed in the Silverpeas indexes. An
 * IndexEntry is created by a Silverpeas component when it creates a new element or document. This
 * IndexEntry will be returned later when the document matches a query.
 */
public class IndexEntry implements Serializable, Cloneable {

  private static final long serialVersionUID = -4817004188601716658L;

  // The start date defaults to 01/01/1900 so the document is visible as soon as published.
  public static final String STARTDATE_DEFAULT = "19000101";

  // The end date defaults to 31/12/2400 so the document will be visible for ever.
  public static final String ENDDATE_DEFAULT = "24001231";


  private IndexEntryKey pk;
  /**
   * The IndexEntry attributes are null by default. The title should been set in order to display
   * the entry to the user when the document match his query. The index engine may set with a
   * default value any of this attributes if null.
   */
  private String lang = null;
  private String creationDate = null;
  private String creationUser = null;
  private String lastModificationDate = null;
  private String lastModificationUser = null;
  private String startDate = null;
  private String endDate = null;
  private boolean indexId = false;
  private String thumbnail = null;
  private String thumbnailMimeType = null;
  private String thumbnailDirectory = null;
  private Map<String, String> titles = null;
  private Map<String, String> previews = null;
  private Map<String, String> keywordsI18N = null;
  private String serverName = null;
  private String filename = null;
  private List<String> paths = null;
  private boolean alias = false;

  public IndexEntry(String component, String objectType, String objectId) {
    this(new IndexEntryKey(component, objectType, objectId));
  }

  /**
   * The constructor only set the key part of the IndexEntry.
   */
  public IndexEntry(IndexEntryKey pk) {
    this.pk = pk;
  }

  /**
   * Returns as a string the key part of the indexEntry. the key part of the IndexEntry
   */
  @Override
  public String toString() {
    return (pk == null) ? "" : pk.toString();
  }

  /**
   * Return the key part of the IndexEntry
   */
  public IndexEntryKey getPK() {
    return pk;
  }

  /**
   * Return the name of the component's instance which handles the object.
   */
  public String getComponent() {
    return pk.getComponent();
  }

  /**
   * Return the type of the indexed document. The meaning of this type is uniquely determined by the
   * component handling the object.
   */
  public String getObjectType() {
    return pk.getObjectType();
  }

  /**
   * Return the object id.
   */
  public String getObjectId() {
    return pk.getObjectId();
  }

  /**
   * Set the title of the index entry as it will be displayed to the user if the indexed document
   * match his query.
   */
  public void setTitle(String title) {
    setTitle(title, null);
  }

  public void setTitle(String title, String lang) {
    if (title != null) {
      getTitles().put(I18NHelper.checkLanguage(lang), title);
    }
  }

  /**
   * Return the title of the index entry as it will be displayed to the user if the indexed document
   * match his query.
   */
  public String getTitle() {
    if (getTitle(null) != null) {
      return getTitle(null);
    }
    return "";
  }

  public String getTitle(String lang) {
    String title = getTitles().get(I18NHelper.checkLanguage(lang));
    if (!StringUtil.isDefined(title)) {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      for (String language : languages) {
        title = getTitles().get(language);
        if (StringUtil.isDefined(title)) {
          return title;
        }
      }
    }
    return title;
  }

  /**
   * Set key words for the index entry.
   */
  public void setKeywords(String keywords) {
    setKeywords(keywords, null);
  }

  public void setKeywords(String keywords, String lang) {
    if (keywords != null) {
      getAllKeywords().put(I18NHelper.checkLanguage(lang), keywords);
    }
  }

  /**
   * Return the key words of the index entry.
   */
  public String getKeywords() {
    if (getKeywords(null) != null) {
      return getKeywords(null);
    }
    return "";
  }

  public String getKeywords(String lang) {
    String keywords = getAllKeywords().get(I18NHelper.checkLanguage(lang));
    if (!StringUtil.isDefined(keywords)) {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      for (String language : languages) {
        keywords = getAllKeywords().get(language);
        if (StringUtil.isDefined(keywords)) {
          return keywords;
        }
      }
    }
    return keywords;
  }

  /**
   * Set a pre-view text for the index entry as it will be displayed at the user when the document
   * will be retrieved.
   */
  public void setPreview(String preview) {
    setPreview(preview, null);
  }

  public void setPreview(String preview, String lang) {
    if (preview != null) {
      getPreviews().put(I18NHelper.checkLanguage(lang), preview);
    }
  }

  /**
   * Return the pre-view text for the index entry as it will be displayed at the user when the
   * document will be retrieved.
   */
  public String getPreview() {
    if (getPreview(null) != null) {
      return getPreview(null);
    }
    return "";
  }

  public String getPreview(String lang) {
    String preview = getPreviews().get(I18NHelper.checkLanguage(lang));
    if (!StringUtil.isDefined(preview)) {
      Set<String> languages = I18NHelper.getAllSupportedLanguages();
      for (String language : languages) {
        preview = getPreviews().get(language);
        if (StringUtil.isDefined(preview)) {
          return preview;
        }
      }
    }
    return preview;
  }

  /**
   * Set the language of the indexed document.
   */
  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Return the language of the indexed document.
   */
  public String getLang() {
    if (lang != null) {
      return lang;
    }
    return "";
  }

  /**
   * Set the creation time of the indexed document.
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = getDate(creationDate);
  }

  /**
   * Set the creation time of the indexed document.
   */
  public void setCreationDate(LocalDate creationDate) {
    this.creationDate = getDate(creationDate);
  }

  /**
   * Return the creation time of the indexed document.
   */
  public String getCreationDate() {
    if (creationDate != null) {
      return creationDate;
    }
    return getDate(new Date());
  }

  /**
   * Set the author of the indexed document. This is the full name of the user (and not his login or
   * user_id).
   */
  public void setCreationUser(String creationUser) {
    this.creationUser = creationUser;
  }

  /**
   * Return the author of the indexed document. This is the full name of the user (and not his login
   * or user_id).
   */
  public String getCreationUser() {
    if (creationUser != null) {
      return creationUser;
    }
    return "";
  }

  /**
   * Set the start date from which the document will be displayed.
   */
  public void setStartDate(Date date) {
    this.startDate = getDate(date);
  }

  public void setStartDate(LocalDate date) {
    this.startDate = getDate(date);
  }

  /**
   * Get the start date from which the document will be displayed. Returns 0000/00/00 if the start
   * date is not set.
   */
  public String getStartDate() {
    if (startDate != null) {
      return startDate;
    } else {
      return STARTDATE_DEFAULT;
    }
  }

  /**
   * Set the end date until which the document will be displayed.
   */
  public void setEndDate(Date date) {
    this.endDate = getDate(date);
  }

  public void setEndDate(LocalDate date) {
    this.endDate = getDate(date);
  }

  /**
   * Get the end date until which the document will be displayed. Returns 9999/99/99 if the end date
   * is not set.
   */
  public String getEndDate() {
    if (endDate != null) {
      return endDate;
    } else {
      return ENDDATE_DEFAULT;
    }
  }

  /**
   * To be equal two IndexEntry must have the same PK. The equals method is redefined so IndexEntry
   * objects can be put in a Set or used as Map key.
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof IndexEntry) {
      IndexEntry e = (IndexEntry) o;
      return this.getPK().equals(e.getPK());
    }
    return false;
  }

  /**
   * Returns the hash code of the indexEntry. The hashCode method is redefined so IndexEntry objects
   * can be put in a Set or used as Map key. Only the primary key is used to compute the hash code,
   * as only the primary key is used to compare two entries.
   */
  @Override
  public int hashCode() {
    return getPK().hashCode();
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String getThumbnailDirectory() {
    return thumbnailDirectory;
  }

  public void setThumbnailDirectory(String thumbnailDirectory) {
    this.thumbnailDirectory = thumbnailDirectory;
  }

  public String getThumbnailMimeType() {
    return thumbnailMimeType;
  }

  public void setThumbnailMimeType(String thumbnailMimeType) {
    this.thumbnailMimeType = thumbnailMimeType;
  }

  private Map<String, String> getTitles() {
    if (titles == null) {
      titles = new HashMap<String, String>();
    }
    return titles;
  }

  private Map<String, String> getPreviews() {
    if (previews == null) {
      previews = new HashMap<String, String>();
    }
    return previews;
  }

  private Map<String, String> getAllKeywords() {
    if (keywordsI18N == null) {
      keywordsI18N = new HashMap<>();
    }
    return keywordsI18N;
  }

  public Iterator<String> getLanguages() {
    return getTitles().keySet().iterator();
  }

  public boolean isIndexId() {
    return indexId;
  }

  public void setIndexId(boolean indexId) {
    this.indexId = indexId;
  }

  public String getLastModificationDate() {
    if (!StringUtil.isDefined(lastModificationDate)) {
      return getCreationDate();
    }
    return lastModificationDate;
  }

  public void setLastModificationDate(Date lastModificationDate) {
    this.lastModificationDate = getDate(lastModificationDate);
  }

  public void setLastModificationDate(LocalDate lastModificationDate) {
    this.lastModificationDate = getDate(lastModificationDate);
  }

  public String getLastModificationUser() {
    if (!StringUtil.isDefined(lastModificationUser)) {
      return getCreationUser();
    }
    return lastModificationUser;
  }

  public void setLastModificationUser(String lastModificationUser) {
    this.lastModificationUser = lastModificationUser;
  }

  /**
   * @return the serverName in order to distinguish each server for external server research
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * @param serverName the serverName to set
   */
  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setPaths(List<String> paths) {
    this.paths = paths;
  }

  public List<String> getPaths() {
    return paths;
  }

  public void setPK(IndexEntryKey pk) {
    this.pk = pk;
  }

  public boolean isAlias() {
    return alias;
  }

  public void setAlias(boolean alias) {
    this.alias = alias;
  }

  @Override
  public IndexEntry clone() {
    IndexEntry clone;
    try {
      clone = (IndexEntry) super.clone();
    } catch (final CloneNotSupportedException e) {
      SilverLogger.getLogger(this).error(e);
      clone = null;
    }
    return clone;
  }

  private String getDate(Date date) {
    return getDate(DateUtil.toLocalDate(date));
  }

  private String getDate(LocalDate date) {
    if (date == null) {
      return null;
    }
    return DateUtil.formatAsLuceneDate(date);
  }

}