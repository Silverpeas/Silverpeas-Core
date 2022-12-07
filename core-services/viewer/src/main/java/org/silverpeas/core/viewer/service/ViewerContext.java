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
package org.silverpeas.core.viewer.service;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;
import org.silverpeas.core.util.StringUtil;

import java.io.File;

import static org.silverpeas.core.viewer.model.ViewerSettings.isCacheEnabled;

/**
 * This class permits to handled a context during the conversion processes.
 * @author Yohann Chastagnier
 */
public class ViewerContext {

  private final String documentId;
  private final String documentType;
  private final String originalFileName;
  private final File originalSourceFile;
  private final String language;
  private String initializerProcessName;
  private String uniqueDocumentId = String.valueOf(System.nanoTime());
  private boolean cacheRequired = false;
  private boolean processingCache = false;

  /**
   * Initializes a context from an instance of {@link SimpleDocument}.
   * @param document the {@link SimpleDocument} for which the file must be converted.
   * @return an instance of {@link ViewerContext} initialized from the given {@link SimpleDocument}.
   */
  public static ViewerContext from(SimpleDocument document) {
    final String contentLanguage = document.getLanguage();
    return new ViewerContext(document.getId(), "attachment", document.getFilename(),
        new File(document.getAttachmentPath()), contentLanguage).withUniqueDocumentId(
        contentLanguage + "-" + document.getId());
  }

  public ViewerContext(final String documentId, final String documentType,
      final String originalFileName, final File originalSourceFile, final String language) {
    this.documentId = documentId;
    this.documentType = documentType;
    this.originalFileName = originalFileName;
    this.originalSourceFile = originalSourceFile;
    this.language = language;
  }

  private ViewerContext(final ViewerContext other) {
    this.documentId = other.documentId;
    this.documentType = other.documentType;
    this.originalFileName = other.originalFileName;
    this.originalSourceFile = other.originalSourceFile;
    this.language = other.language;
    this.initializerProcessName = other.initializerProcessName;
    this.uniqueDocumentId = other.uniqueDocumentId;
    this.cacheRequired = other.cacheRequired;
    this.processingCache = other.processingCache;
  }

  /**
   * Gets the identifier of the document.
   * @return a string.
   */
  public String getDocumentId() {
    return documentId;
  }

  /**
   * Gets the type of the document.
   * @return a string.
   */
  public String getDocumentType() {
    return documentType;
  }

  /**
   * Gets the name of the original file.
   * @return the name as string.
   */
  public String getOriginalFileName() {
    return originalFileName;
  }

  /**
   * Gets the original file.
   * @return the {@link File} that represents the original file.
   */
  public File getOriginalSourceFile() {
    return originalSourceFile;
  }

  /**
   * Sets the name of the process which initializes the conversion. This identifier is used
   * by {@link #getViewId()}.
   * @param initializerProcessName the name of the process which initializes the conversion.
   * @return itself.
   */
  ViewerContext fromInitializerProcessName(final String initializerProcessName) {
    this.initializerProcessName = initializerProcessName;
    return this;
  }

  /**
   * Gets the workspace into which the processes will write the results.
   * @return the {@link File}  that represents the workspace.
   */
  public TemporaryWorkspaceTranslation getWorkspace() {
    return TemporaryWorkspaceTranslation.from(getViewId());
  }

  /**
   * Gets the identifier associated to the current conversion processes for viewing.<br>
   * If an id has been explicitly set, then it is returned.<br>
   * Otherwise, a unique id is computed.
   * @return a unique identifier as string.
   */
  public String getViewId() {
    return initializerProcessName + "-" + uniqueDocumentId;
  }

  /**
   * Sets a unique identifier associated to the current document to convert.<br>
   * This id is used by {@link #getViewId()} in order to compute the final unique identifier.
   * @param uniqueDocumentId a unique document identifier (please be careful about 'unique' word).
   * @return the current context instance.
   */
  public ViewerContext withUniqueDocumentId(final String uniqueDocumentId) {
    if (isCacheEnabled() && StringUtil.isDefined(uniqueDocumentId)) {
      this.uniqueDocumentId = uniqueDocumentId;
      cacheRequired = true;
    }
    return this;
  }

  /**
   * Indicates if cache is required.
   * @return true if required, false otherwise.
   */
  public boolean isCacheRequired() {
    return cacheRequired;
  }

  /**
   * Indicates if the current process is the one which is in charge of performing the conversion
   * tasks.
   * @return true if the current process is in charge, false otherwise.
   */
  public boolean isProcessingCache() {
    return processingCache;
  }

  /**
   * Sets into context that the current process is in charge of the conversion tasks.
   */
  public void processingCache() {
    this.processingCache = true;
  }

  /**
   * Gets the language of the content
   * @return a string.
   */
  public String getLanguage() {
    return language;
  }

  protected ViewerContext copy() {
    ViewerContext newViewerContext = new ViewerContext(this);
    newViewerContext.initializerProcessName = "";
    newViewerContext.processingCache = false;
    return newViewerContext;
  }
}
