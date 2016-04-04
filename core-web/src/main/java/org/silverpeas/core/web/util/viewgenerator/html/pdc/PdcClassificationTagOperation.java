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

package org.silverpeas.core.web.util.viewgenerator.html.pdc;

/**
 * The operations the tags on the PdC classification supports.
 */
public enum PdcClassificationTagOperation {

  /**
   * Previews the classification on the PdC of a given content.
   */
  PREVIEW_CLASSIFICATION("preview"),
  /**
   * Reads the classification on the PdC of a given content.
   */
  READ_CLASSIFICATION("open"),
  /**
   * Opens the classification on the PdC of a given content. By opening this classification, the
   * user can change it.
   */
  OPEN_CLASSIFICATION("open"),
  /**
   * Creates a new classification on the PdC for a new or an existing content.
   */
  CREATE_CLASSIFICATION("create"),
  /**
   * Predefines the classification on the PdC of contents that will published in a given node of a
   * given component instance.
   */
  PREDEFINE_CLASSIFICATION("predefine");

  private String pluginFunction;

  private PdcClassificationTagOperation(String pluginFunction) {
    this.pluginFunction = pluginFunction;
  }

  public String getPluginFunction() {
    return this.pluginFunction;
  }
}
