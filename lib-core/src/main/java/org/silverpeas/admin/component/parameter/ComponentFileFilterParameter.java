/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.admin.component.parameter;

import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.silverpeas.admin.component.constant.ComponentInstanceParameterName;
import org.silverpeas.admin.component.exception.ComponentFileFilterException;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * This class handles component file filters (authorized or forbidden files).
 * User: Yohann Chastagnier
 * Date: 17/12/12
 */
public class ComponentFileFilterParameter {
  /* Global settings (authorized or forbidden files) */
  protected static String defaultAuthorizedFiles =
      ComponentInstanceParameterName.authorizedFileExtension.getDefaultValue();
  protected static String defaultForbiddenFiles =
      ComponentInstanceParameterName.forbiddenFileExtension.getDefaultValue();

  /* Source component */
  private ComponentInst component;
  /* By default, the authorizations are given priority over forbiddens */
  private boolean isAuthorization = true;
  /* Defined file filters */
  private String fileFilters = null;
  /* By default, the authorizations are given priority over forbiddens */
  private boolean isFileFilterGloballySet = false;
  /* Collection of MIME-TYPE authorized or forbidden */
  private Collection<String> mimeTypes = null;

  /**
   * Default hidden constructor.
   * @param component
   */
  private ComponentFileFilterParameter(final ComponentInst component) {
    this.component = component;
  }

  /**
   * Getting component file filter from component instance.
   * @param component
   * @return
   */
  public static ComponentFileFilterParameter from(final ComponentInst component) {
    return new ComponentFileFilterParameter(component).initialize();
  }

  /**
   * Gets MIME-TYPES authorized or not.
   * @return
   */
  private Collection<String> getMimeTypes() {
    return mimeTypes;
  }

  /**
   * Initialize component file filter variables. By default, the authorizations are given priority
   * over forbiddens.
   */
  private ComponentFileFilterParameter initialize() {
    if (mimeTypes == null) {
      mimeTypes = new HashSet<String>();

      /* Excluding or including files ? */

      // Authorized file parameter of component has the priority
      fileFilters =
          component.getParameterValue(ComponentInstanceParameterName.authorizedFileExtension);
      if (isDefined(fileFilters)) {
        // Authorization and parameterized on component instance
        parseFileFilters(fileFilters);
        return this;
      }

      // If no filters previously defined, forbidden file parameter of component becomes the
      // priority
      fileFilters =
          component.getParameterValue(ComponentInstanceParameterName.forbiddenFileExtension);
      if (isDefined(fileFilters)) {
        // forbidden and parameterized on component instance
        isAuthorization = false;
        parseFileFilters(fileFilters);
        return this;
      }

      // Global settings
      isFileFilterGloballySet = true;

      // If no filters previously defined, authorized file parameter from server settings becomes
      // the priority
      fileFilters = defaultAuthorizedFiles;
      if (isDefined(fileFilters)) {
        // Authorization and parameterized globally
        parseFileFilters(fileFilters);
        return this;
      }

      // If no filters previously defined, forbidden file parameter from server settings becomes
      // the priority
      fileFilters = defaultForbiddenFiles;
      if (isDefined(fileFilters)) {
        // forbidden and parameterized globally
        isAuthorization = false;
        parseFileFilters(fileFilters);
      }
    }
    return this;
  }

  /**
   * Parse file filters.
   * @param definedFileFilters file filters separated by semicolons
   */
  private void parseFileFilters(final String definedFileFilters) {
    fileFilters = "";
    if (isDefined(definedFileFilters)) {
      fileFilters =
          definedFileFilters.trim().replaceAll("[\\* ;,]+[\\.]", ",").replaceAll("[, ]+", ", ")
              .replaceAll("^, ", "");
      for (String fileFilter : fileFilters.split("[,]")) {
        mimeTypes.add(FileUtil.getMimeType("file." + fileFilter.trim()));
      }
    }
  }

  /**
   * Gets the component instance.
   * @return
   */
  public ComponentInst getComponent() {
    return component;
  }

  /**
   * Indicates if getMimeTypes returns authorized or forbidden files.
   * @return
   */
  public boolean isAuthorization() {
    return isAuthorization;
  }

  /**
   * Gets the current file filter.
   * @return
   */
  public String getFileFilters() {
    return fileFilters;
  }

  /**
   * Indicates if the file filter is set globally.
   * @return
   */
  public boolean isFileFilterGloballySet() {
    return isFileFilterGloballySet;
  }

  /**
   * Checks if the given file is authorized. If parameter is null, it is considered as forbidden.
   * @param file
   * @return
   */
  public boolean isFileAuthorized(final File file) {
    if (file != null) {
      if (!getMimeTypes().isEmpty()) {
        final String fileMimeType = FileUtil.getMimeType(file.getPath());

        // File mime-type has to be found
        if (!isDefined(fileMimeType)) {
          return false;
        }

        // On authorized check, fileMimeType has to be contained in authorized files defined.
        // On forbidden check, fileMimeType has not to be contained in forbidden files defined.
        return isAuthorization() ? getMimeTypes().contains(fileMimeType) :
            !getMimeTypes().contains(fileMimeType);
      }
      return true;
    }
    return false;
  }

  /**
   * Throwing the component file filter exception (RuntimeException) if file is forbidden.
   * @param file
   */
  public void verifyFileAuthorized(final File file) {
    if (!isFileAuthorized(file)) {
      throw new ComponentFileFilterException(this, file);
    }
  }
}
