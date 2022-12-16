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

package org.silverpeas.core.documenttemplate;

import org.silverpeas.core.ui.UserI18NTranslationMap;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;

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
import java.util.Optional;

import static java.time.ZoneId.systemDefault;
import static org.silverpeas.core.util.StringUtil.EMPTY;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

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
    return JSONCodec.encode(this);
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
}
