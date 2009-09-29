package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * IndexEntry is the base class for all the entries which are indexed in the
 * web'activ index.
 * 
 * A IndexEntry is create by a web'activ's component when it creates a new
 * element or document. This IndexEntry will be return later when the document
 * matchs a query.
 */

public class IndexEntry implements Serializable {
  /**
   * This constructor set the key part of the IndexEntry but leave empty the
   * object type.
   * 
   * This constructor can be used by any component which indexes only one kind
   * of entities and then doesn't need to tag them with a type.
   */
  public IndexEntry(WAPrimaryKey key) {
    // this(new IndexEntryPK(key.space, key.componentName, "", key.id));
    this(new IndexEntryPK(key.componentName, "", key.id));
  }

  /**
   * This constructor set the key part of the IndexEntry from WAPrimaryKey
   * completed with a type given as a String.
   * 
   * When the indexed document will be retrieved, this type will be used to
   * distinguish the real type of the document between all the indexed document
   * kind.
   */
  public IndexEntry(WAPrimaryKey key, String type) {
    // this(new IndexEntryPK(key.space, key.componentName, type, key.id));
    this(new IndexEntryPK(key.componentName, type, key.id));
  }

  /**
   * The constructor only set the key part of the IndexEntry.
   * 
   * @deprecated - parameter space is no more used
   */
  public IndexEntry(String space, String component, String objectType,
      String objectId) {
    this(new IndexEntryPK(space, component, objectType, objectId));
  }

  public IndexEntry(String component, String objectType, String objectId) {
    this(new IndexEntryPK(component, objectType, objectId));
  }

  /**
   * The constructor only set the key part of the IndexEntry.
   */
  public IndexEntry(IndexEntryPK pk) {
    this.pk = pk;
  }

  /**
   * Returns as a string the key part of the indexEntry. the key part of the
   * IndexEntry
   */
  public String toString() {
    return (pk == null) ? "" : pk.toString();
  }

  /**
   * Return the key part of the IndexEntry
   */
  public IndexEntryPK getPK() {
    return pk;
  }

  /**
   * Return the space of the indexed document or the userId if the space is a
   * private working space.
   * 
   * @deprecated - to use this method is forbidden
   */
  public String getSpace() {
    return pk.getSpace();
  }

  /**
   * Return the name of the component's instance which handles the object.
   */
  public String getComponent() {
    return pk.getComponent();
  }

  /**
   * Return the type of the indexed document. The meaning of this type is
   * uniquely determined by the component handling the object.
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
   * Set the title of the index entry as it will be displayed to the user if the
   * indexed document match his query.
   */
  public void setTitle(String title) {
    /*
     * if (title != null) this.title = title.toLowerCase(); else this.title =
     * title;
     */
    setTitle(title, null);
  }

  public void setTitle(String title, String lang) {
    /*
     * if (lang == null || I18NHelper.isDefaultLanguage(lang)) this.title =
     * title; else {
     */
    if (title != null)
      getTitles().put(I18NHelper.checkLanguage(lang), title);
    // }
  }

  /**
   * Return the title of the index entry as it will be displayed to the user if
   * the indexed document match his query.
   */
  public String getTitle() {
    /*
     * if (title != null) return title; else return "";
     */
    if (getTitle(null) != null)
      return getTitle(null);
    else
      return "";
  }

  public String getTitle(String lang) {
    return (String) getTitles().get(I18NHelper.checkLanguage(lang));
  }

  /**
   * Set key words for the index entry.
   */
  public void setKeyWords(String keywords) {
    /*
     * if (keyWords != null) this.keyWords = keyWords.toLowerCase(); else
     * this.keyWords = keyWords;
     */
    setKeywords(keywords, null);
  }

  public void setKeywords(String keywords, String lang) {
    /*
     * if (lang == null || I18NHelper.isDefaultLanguage(lang)) this.keyWords =
     * keywords; else {
     */
    if (keywords != null)
      getKeywords().put(I18NHelper.checkLanguage(lang), keywords);
    // }
  }

  /**
   * Return the key words of the index entry.
   */
  public String getKeyWords() {
    /*
     * if (keyWords != null) return keyWords; else return "";
     */
    if (getKeywords(null) != null)
      return getKeywords(null);
    else
      return "";
  }

  public String getKeywords(String lang) {
    return (String) getKeywords().get(I18NHelper.checkLanguage(lang));
  }

  /**
   * Set a pre-view text for the index entry as it will be displayed at the user
   * when the document will be retrieved.
   */
  public void setPreView(String preview) {
    /*
     * if (preView != null) this.preView = preView.toLowerCase(); else
     * this.preView = preView;
     */
    setPreview(preview, null);
  }

  public void setPreview(String preview, String lang) {
    /*
     * if (lang == null || I18NHelper.isDefaultLanguage(lang)) this.preView =
     * preview; else {
     */
    if (preview != null)
      getPreviews().put(I18NHelper.checkLanguage(lang), preview);
    // }
  }

  /**
   * Return the pre-view text for the index entry as it will be displayed at the
   * user when the document will be retrieved.
   */
  public String getPreView() {
    /*
     * if (preView != null) return preView; else return "";
     */
    if (getPreview(null) != null)
      return getPreview(null);
    else
      return "";
  }

  public String getPreview(String lang) {
    return (String) getPreviews().get(I18NHelper.checkLanguage(lang));
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
    if (lang != null)
      return lang;
    else
      return "";
  }

  /**
   * Set the creation time of the indexed document.
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Set the creation time of the indexed document.
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = new SimpleDateFormat("yyyy/MM/dd").format(creationDate);
  }

  /**
   * Return the creation time of the indexed document.
   */
  public String getCreationDate() {
    if (creationDate != null)
      return creationDate;
    else
      return new SimpleDateFormat("yyyy/MM/dd").format(new Date());
  }

  /**
   * Set the author of the indexed document.
   * 
   * This is the full name of the user (and not his login or user_id).
   */
  public void setCreationUser(String creationUser) {
    // this.creationUser = creationUser.toLowerCase();
    this.creationUser = creationUser;
  }

  /**
   * Return the author of the indexed document.
   * 
   * This is the full name of the user (and not his login or user_id).
   */
  public String getCreationUser() {
    if (creationUser != null)
      return creationUser;
    else
      return "";
  }

  /**
   * Set the start date from which the document will be displayed.
   */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /**
   * The start date defaults to 0000/00/00 so the document is visible as soon as
   * published.
   */
  static public final String STARTDATE_DEFAULT = "0000/00/00";

  /**
   * Get the start date from which the document will be displayed.
   * 
   * Returns 0000/00/00 if the start date is not set.
   */
  public String getStartDate() {
    if (startDate != null)
      return startDate;
    else
      return STARTDATE_DEFAULT;
  }

  /**
   * The end date defaults to 9999/99/99 so the document will be visible for
   * ever.
   */
  static public final String ENDDATE_DEFAULT = "9999/99/99";

  /**
   * Set the end date until which the document will be displayed.
   */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /**
   * Get the end date until which the document will be displayed.
   * 
   * Returns 9999/99/99 if the end date is not set.
   */
  public String getEndDate() {
    if (endDate != null)
      return endDate;
    else
      return ENDDATE_DEFAULT;
  }

  /**
   * To be equal two IndexEntry must have the same PK.
   * 
   * The equals method is redefined so IndexEntry objects can be put in a Set or
   * used as Map key.
   */
  public boolean equals(Object o) {
    if (o instanceof IndexEntry) {
      IndexEntry e = (IndexEntry) o;
      return this.getPK().equals(e.getPK());
    } else
      return false;
  }

  /**
   * Returns the hash code of the indexEntry.
   * 
   * The hashCode method is redefined so IndexEntry objects can be put in a Set
   * or used as Map key.
   * 
   * Only the primary key is used to compute the hash code, as only the primary
   * key is used to compare two entries.
   */
  public int hashCode() {
    return getPK().hashCode();
  }

  /**
   * The primary key is fixed at construction time.
   */
  private final IndexEntryPK pk;

  /**
   * The IndexEntry attributes are null by default.
   * 
   * The title should been set in order to display the entry to the user when
   * the document match his query.
   * 
   * The index engine may set with a default value any of this attributes if
   * null.
   */
  private String lang = null;
  private String creationDate = null;
  private String creationUser = null;
  private String startDate = null;
  private String endDate = null;
  private boolean indexId = false;

  private String thumbnail = null;
  private String thumbnailMimeType = null;
  private String thumbnailDirectory = null;

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

  private Hashtable getTitles() {
    if (titles == null)
      titles = new Hashtable();

    return titles;
  }

  private Hashtable getPreviews() {
    if (previews == null)
      previews = new Hashtable();

    return previews;
  }

  private Hashtable getKeywords() {
    if (keywordsI18N == null)
      keywordsI18N = new Hashtable();

    return keywordsI18N;
  }

  private Hashtable titles = null;
  private Hashtable previews = null;
  private Hashtable keywordsI18N = null;

  public Iterator getLanguages() {
    return getTitles().keySet().iterator();
  }

  public boolean isIndexId() {
    return indexId;
  }

  public void setIndexId(boolean indexId) {
    this.indexId = indexId;
  }
}