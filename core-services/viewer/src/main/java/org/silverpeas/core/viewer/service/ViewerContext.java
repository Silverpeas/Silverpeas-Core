/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.viewer.service;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;

import java.io.File;

import static org.silverpeas.core.viewer.model.ViewerSettings.isCacheEnabled;

/**
 * This class permits to handled a context during the conversion processes.
 * @author Yohann Chastagnier
 */
public class ViewerContext implements Cloneable {

  private final String originalFileName;
  private final File originalSourceFile;
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
    return new ViewerContext(document.getFilename(), new File(document.
        getAttachmentPath())).withUniqueDocumentId(document.getLanguage() + "-" + document.getId());
  }

  protected ViewerContext(final String originalFileName, final File originalSourceFile) {
    this.originalFileName = originalFileName;
    this.originalSourceFile = originalSourceFile;
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
   * @return
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
   * Gets the identifier associated to the current conversion processes for viewing.<br/>
   * If an id has been explicitly set, then it is returned.<br/>
   * Otherwise, a unique id is computed.
   * @return a unique identifier as string.
   */
  public String getViewId() {
    return initializerProcessName + "-" + uniqueDocumentId;
  }

  /**
   * Sets a unique identifier associated to the current document to convert.<br/>
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

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  @Override
  protected ViewerContext clone() {
    try {
      ViewerContext clonedViewerContext = (ViewerContext) super.clone();
      clonedViewerContext.initializerProcessName = "";
      clonedViewerContext.processingCache = false;
      return clonedViewerContext;
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
