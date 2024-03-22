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

package org.silverpeas.core.documenttemplate;

import org.silverpeas.core.ui.UserI18NTranslationMap;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneId.systemDefault;
import static java.util.stream.Collectors.toList;
import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;

/**
 * This class handles the json data persisted into repository of document template.
 * @author silveryocha
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class JsonDocumentTemplate implements Serializable {
  private static final long serialVersionUID = 725822202636699367L;

  @XmlElement
  private String id;
  @XmlElement
  private UserI18NTranslationMap nameTranslations = new UserI18NTranslationMap();
  @XmlElement
  private UserI18NTranslationMap descriptionTranslations = new UserI18NTranslationMap();
  @XmlElement
  private int position = -1;
  @XmlElement
  private JsonDocumentTemplateRestrictions restrictions = new JsonDocumentTemplateRestrictions();
  @XmlElement
  private String creatorId;
  @XmlElement
  private String creationInstant;
  @XmlElement
  private String lastUpdaterId;
  @XmlElement
  private String lastUpdateInstant;

  JsonDocumentTemplate() {
    // constructor only visible into package
  }

  JsonDocumentTemplate(final JsonDocumentTemplate other) {
    this.id = other.id;
    this.nameTranslations = new UserI18NTranslationMap(other.nameTranslations);
    this.descriptionTranslations = new UserI18NTranslationMap(other.descriptionTranslations);
    this.position = other.position;
    this.restrictions = new JsonDocumentTemplateRestrictions(other.restrictions);
    this.creatorId = other.creatorId;
    this.creationInstant = other.creationInstant;
    this.lastUpdaterId = other.lastUpdaterId;
    this.lastUpdateInstant = other.lastUpdateInstant;
  }

  String getId() {
    return id;
  }

  void setId(final String id) {
    this.id = id;
  }

  UserI18NTranslationMap getNameTranslations() {
    return nameTranslations;
  }

  UserI18NTranslationMap getDescriptionTranslations() {
    return descriptionTranslations;
  }

  int getPosition() {
    return position;
  }

  void setPosition(final int position) {
    this.position = position;
  }

  public JsonDocumentTemplateRestrictions getRestrictions() {
    return restrictions;
  }

  String getCreatorId() {
    return creatorId;
  }

  void setCreatorId(final String creatorId) {
    this.creatorId = creatorId;
    setLastUpdaterId(creatorId);
  }

  Instant getCreationInstant() {
    return creationInstant != null ? OffsetDateTime.parse(creationInstant).toInstant() : null;
  }

  void setCreationInstant(final Instant creationInstant) {
    this.creationInstant = OffsetDateTime.ofInstant(creationInstant, systemDefault()).toString();
    setLastUpdateInstant(creationInstant);
  }

  String getLastUpdaterId() {
    return lastUpdaterId;
  }

  void setLastUpdaterId(final String lastUpdaterId) {
    this.lastUpdaterId = lastUpdaterId;
  }

  Instant getLastUpdateInstant() {
    return lastUpdateInstant != null ? OffsetDateTime.parse(lastUpdateInstant).toInstant() : null;
  }

  void setLastUpdateInstant(final Instant lastUpdateInstant) {
    this.lastUpdateInstant = OffsetDateTime.ofInstant(lastUpdateInstant, systemDefault()).toString();
  }

  @Override
  public String toString() {
    final JsonDocumentTemplateRestrictions savedRestrictions = this.restrictions;
    if (savedRestrictions.isEmpty()) {
      this.restrictions = null;
    }
    try {
      return JSONCodec.encode(this);
    } finally {
      if (savedRestrictions.isEmpty()) {
        this.restrictions = savedRestrictions;
      }
    }
  }

  static JsonDocumentTemplate decode(final Path jsonPath) {
    try {
      return decode(Files.readString(jsonPath, Charsets.UTF_8));
    } catch (IOException e) {
      throw new DocumentTemplateRuntimeException(e);
    }
  }

  static JsonDocumentTemplate decode(final String jsonContent) {
    final String safeJson = defaultStringIfNotDefined(jsonContent, EMPTY);
    return Optional.of(safeJson)
        .filter(StringUtil::isDefined)
        .stream()
        .map(j -> JSONCodec.decode(j, JsonDocumentTemplate.class))
        .findFirst()
        .orElseGet(JsonDocumentTemplate::new);
  }

  /**
   * This class handles the json data part dedicated to restrictions.
   * @author silveryocha
   */
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  static class JsonDocumentTemplateRestrictions implements Serializable {
    private static final long serialVersionUID = 136978536660882286L;

    @XmlElement
    private List<String> spaceIds;

    JsonDocumentTemplateRestrictions() {
      // constructor only visible into package
    }

    JsonDocumentTemplateRestrictions(final JsonDocumentTemplateRestrictions other) {
      this.spaceIds = other.spaceIds != null ? new ArrayList<>(other.spaceIds) : null;
    }

    boolean isEmpty() {
      return spaceIds == null || spaceIds.isEmpty();
    }

    /**
     * Gets the list of space identifiers the document template is restricted to.
     * <p>
     *   Children space of indicated ones are allowed to provide the document template.
     * </p>
     * @return a list of space identifier as string. Empty list if it does not exist restriction.
     */
    public List<String> getSpaceIds() {
      return spaceIds != null ? spaceIds : new ArrayList<>(0);
    }

    /**
     * Gets the list of space identifiers the document template is restricted to.
     * <p>
     *   Children space of indicated ones are allowed to provide the document template, so there
     *   is no need to specified explicitly them.
     * </p>
     * <p>
     *   If an empty list (or a null one) is given, no data about restriction is saved into JSON
     *   model.
     * </p>
     * @param spaceIds a list of space identifier as string. Null or empty list means no space
     * restriction.
     */
    public void setSpaceIds(final List<String> spaceIds) {
      this.spaceIds = isNotEmpty(spaceIds) ?
          spaceIds.stream().map(String::toUpperCase).collect(toList()) :
          null;
    }
  }
}
