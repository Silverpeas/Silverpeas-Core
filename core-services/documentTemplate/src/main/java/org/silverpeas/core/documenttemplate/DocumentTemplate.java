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

import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.service.ViewerContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.silverpeas.core.documenttemplate.DefaultDocumentTemplateRepository.getDocumentTemplateRepositoryPath;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * This class is the representation of a document template.
 * @author silveryocha
 */
public class DocumentTemplate implements Serializable {
  private static final long serialVersionUID = -1458951974587553231L;
  private static final String DOCUMENT_TEMPLATE = "documentTemplate";

  private final JsonDocumentTemplate json;
  private String extension;

  /**
   * Initializing a new instance of {@link DocumentTemplate}.
   */
  public DocumentTemplate() {
    this(new JsonDocumentTemplate(), null);
  }

  DocumentTemplate(final DocumentTemplate other) {
    this.json = new JsonDocumentTemplate(other.json);
    this.extension = other.extension;
  }

  DocumentTemplate(final JsonDocumentTemplate json, final String extension) {
    this.json = json;
    this.extension = extension;
  }

  /**
   * Gets the identifier of the document template.
   * <p>
   *   The identifier is the file base name part of the file registered into repository.
   * </p>
   * @return a string representing an id (UUID).
   */
  public String getId() {
    return json.getId();
  }

  void setId(final String id) {
    this.json.setId(id);
  }

  /**
   * Indicates if the document template is persisted into Silverpeas's context.
   * @return true is persisted, false otherwise.
   */
  public boolean isPersisted() {
    return isDefined(getId());
  }

  /**
   * Gets the position for collection sort processing.
   * @return an integer representing the position into a sorted collection.
   */
  public int getPosition() {
    return json.getPosition();
  }

  /**
   * Sets the position for collection sort processing.
   * @param position an integer representing the position into a sorted collection.
   */
  public void setPosition(final int position) {
    this.json.setPosition(position);
  }

  /**
   * Indicates if a name translation exists into the given language.
   * @param language the locale into which the name should be provided.
   * @return true of a translation exists, false otherwise.
   */
  public boolean existNameTranslationIn(final String language) {
    return json.getNameTranslations().containsKey(language);
  }

  /**
   * Gets the name of the document template.
   * @param language the locale into which the name should be provided. A default one is
   * provided if none retrieved with given language.
   * @return a string representing a name.
   */
  public String getName(final String language) {
    return json.getNameTranslations().get(language);
  }

  /**
   * Sets the name of the document template.
   * @param name a name.
   * @param language the locale of the given name.
   */
  public void setName(final String name, final String language) {
    json.getNameTranslations().put(language, name);
  }

  /**
   * Indicates if a description translation exists for the given language.
   * @param language the locale into which the description should be provided.
   * @return true of a translation exists, false otherwise.
   */
  public boolean existDescriptionTranslationIn(final String language) {
    return json.getDescriptionTranslations().containsKey(language);
  }

  /**
   * Gets the description of the document template.
   * @param language the locale into which the description should be provided. A default one is
   * provided if none retrieved with given language.
   * @return a string representing a description.
   */
  public String getDescription(final String language) {
    return json.getDescriptionTranslations().get(language);
  }

  /**
   * Sets the description of the document template.
   * @param description a description.
   * @param language the locale of the given description.
   */
  public void setDescription(final String description, final String language) {
    json.getDescriptionTranslations().put(language, description);
  }

  /**
   * Gets the file extension of the document template.
   * @return a string (without the file extension separator).
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Sets the file extension of the document template.
   * @param extension a string (without the file extension separator).
   */
  public void setExtension(final String extension) {
    this.extension = extension;
  }

  /**
   * Sets the space identifiers the document template is restricted to.
   * @param spacesIds list of space identifier.
   */
  public void setRestrictedToSpaceIds(final List<String> spacesIds) {
    json.getRestrictions().setSpaceIds(spacesIds);
  }

  /**
   * Gets the space identifiers the document template is restricted to.
   * @return list of space identifier.
   */
  public List<String> getRestrictedToSpaceIds() {
    return json.getRestrictions().getSpaceIds();
  }

  /**
   * Gets the viewer context of the document template.
   * <p>
   *   Useful for {@link DocumentView} and {@link Preview} services.
   * </p>
   * @param language the language into which the {@link ViewerContext} is expected.
   * @return a {@link ViewerContext} instance.
   */
  public ViewerContext getViewerContext(final String language) {
    return new ViewerContext(getId(), DOCUMENT_TEMPLATE, getName(language),
        getContentFilePath().toFile(), I18n.get().getDefaultLanguage())
        .withUniqueDocumentId(language + "-" + getId());
  }

  /**
   * Gets the content type of the physical file.
   * @return a string representing a mime type.
   */
  public String getContentType() {
    return FileUtil.getMimeType(getContentFilePath().toString());
  }

  /**
   * Opens an {@link InputStream} over the content of the document template in order to copy it.
   * @return an {@link InputStream} instance.
   * @throws IOException See FileNotFoundException above, FileNotFoundException is a subclass of IOException.
   */
  public InputStream openInputStream() throws IOException {
    return Files.newInputStream(getContentFilePath());
  }

  Path getContentFilePath() {
    return Paths.get(getDocumentTemplateRepositoryPath().toString(), getId() + "." + getExtension());
  }

  Path getDescriptorFilePath() {
    return Paths.get(getDocumentTemplateRepositoryPath().toString(), getId() + ".json");
  }

  JsonDocumentTemplate getJson() {
    return json;
  }
}
