/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.index.search.model;

import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.kernel.logging.SilverLogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.contribution.indicator.NewContributionIndicator.isNewContribution;

/**
 * @author Nicolas Eysseric
 */
public class SearchResult extends AbstractBean {

  private String keywords;
  private final LocalDate creationDate;
  private final String creatorId;
  private final LocalDate lastUpdateDate;
  private String lastUpdaterId;

  private final ContributionIdentifier cId;

  private String linkedResId;

  private float score;
  private String serverName;
  private String thumbnailURL;
  private String attachmentFilename;
  private boolean externalResult = false;
  private boolean alias = false;

  /**
   * list of XML form fields used to sort results
   */
  private Map<String, String> sortableXMLFormFields = null;
  private Map<String, String> formFieldsForFacets;

  private SearchResult(MatchingIndexEntry mie) {
    setName(mie.getTitle());
    setDescription(mie.getPreview());
    this.keywords = mie.getKeywords(null);

    this.creationDate = getLocalDate(mie.getCreationDate());
    this.lastUpdateDate = getLocalDate(mie.getLastModificationDate());

    this.creatorId = mie.getCreationUser();
    this.lastUpdaterId = mie.getLastModificationUser();

    this.cId = ContributionIdentifier.from(mie.getComponent(), mie.getObjectId(),
        mie.getObjectType());
    this.linkedResId = mie.getLinkedObjectId();

    this.score = mie.getScore();
    this.serverName = mie.getServerName();
    attachmentFilename = mie.getFilename();
    externalResult = mie.isExternalResult();
    alias = mie.isAlias();

    // add sortable fields from XML form
    sortableXMLFormFields = mie.getSortableXMLFormFields();
    formFieldsForFacets = mie.getXMLFormFieldsForFacets();

    Iterator<String> languages = mie.getLanguages();
    while (languages.hasNext()) {
      String language = languages.next();
      SearchResultTranslation translation =
          new SearchResultTranslation(language, mie.getTitle(language), mie.getPreview(language));
      addTranslation(translation);
    }

    if (mie.getThumbnail() != null) {
      if (mie.getThumbnail().startsWith("/")) {
        // case of a thumbnail picked up in a gallery
        thumbnailURL = mie.getThumbnail();
      } else {
        // case of an uploaded image
        thumbnailURL = FileServerUtils.getUrl(mie.getComponent(),
            mie.getThumbnail(), mie.getThumbnailMimeType(), mie.getThumbnailDirectory());
      }
    }
  }

  private SearchResult(GlobalSilverContent gsc) {
    setName(gsc.getName());
    setDescription(gsc.getDescription());

    this.creationDate = DateUtil.toLocalDate(gsc.getCreationDate());
    this.creatorId = gsc.getUserId();

    this.lastUpdateDate = DateUtil.toLocalDate(gsc.getLastUpdateDate());

    this.cId = ContributionIdentifier.from(gsc.getInstanceId(), gsc.getId(), gsc.getType());

    for (String language : gsc.getTranslations().keySet()) {
      SearchResultTranslation translation =
          new SearchResultTranslation(language, gsc.getName(language),
              gsc.getDescription(language));
      addTranslation(translation);
    }

    thumbnailURL = gsc.getThumbnailURL();
  }

  public static SearchResult fromIndexEntry(MatchingIndexEntry mie) {
    return new SearchResult(mie);
  }

  public static SearchResult fromGlobalSilverContent(GlobalSilverContent gsc) {
    return new SearchResult(gsc);
  }

  /**
   * Is the contribution referred by this search result linked to another contribution? For example,
   * a comment or an attachment is a contribution that is linked usually to a publication.
   * @return true if the resource referred by this result is linked to another resource.
   */
  public boolean isLinkedToAnotherContribution() {
    return StringUtil.isDefined(this.linkedResId);
  }

  private LocalDate getLocalDate(String date) {
    if (StringUtil.isDefined(date)) {
      try {
        if (date.contains("/")) {
          // case of taxonomy result (date stored in database)
          return DateUtil.toLocalDate(date);
        } else {
          return DateUtil.parseFromLucene(date);
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  public String getKeywords() {
    return keywords;
  }

  public LocalDate getCreationLocalDate() {
    return creationDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public LocalDate getLastUpdateLocalDate() {
    return lastUpdateDate;
  }

  public String getLastUpdaterId() {
    return lastUpdaterId;
  }

  public String getId() {
    return cId.getLocalId();
  }

  public String getInstanceId() {
    return cId.getComponentInstanceId();
  }

  public String getType() {
    return cId.getType();
  }

  public String getLinkedResourceId() {
    return linkedResId;
  }

  public float getScore() {
    return score;
  }

  public String getServerName() {
    return serverName;
  }

  public String getThumbnailURL() {
    return thumbnailURL;
  }

  public void setThumbnailURL(String url) {
    thumbnailURL = url;
  }

  public String getAttachmentFilename() {
    return attachmentFilename;
  }

  /**
   * gets the list of Sortable fields if the content is a form XML
   * @return the sortableXMLFormFields
   */
  @SuppressWarnings("unused")
  public Map<String, String> getSortableXMLFormFields() {
    return sortableXMLFormFields;
  }

  public Map<String, String> getFormFieldsForFacets() {
    return formFieldsForFacets;
  }

  public boolean isExternalResult() {
    return externalResult;
  }

  public boolean isAlias() {
    return alias;
  }

  public boolean isNew() {
    return ofNullable(lastUpdateDate != null ? lastUpdateDate : creationDate)
        .map(d -> d.atStartOfDay(ZoneId.systemDefault()).toInstant())
        .map(i -> isNewContribution(cId, i))
        .orElse(false);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SearchResult that = (SearchResult) o;
    return Objects.equals(cId, that.cId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cId);
  }
}
