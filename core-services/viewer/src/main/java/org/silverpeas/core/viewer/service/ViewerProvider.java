/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.viewer.service;

import org.silverpeas.core.util.ServiceProvider;

import java.io.File;

/**
 * Viewer factory which provides preview and view services.
 * @author Yohann Chastagnier
 */
public class ViewerProvider {

  private ViewerProvider() {
  }

  /**
   * Preview services accessor
   * @return a preview service
   * @see DefaultPreviewService
   */
  public static PreviewService getPreviewService() {
    return ServiceProvider.getService(PreviewService.class);
  }

  /**
   * Indicates if file is previewable.
   * @param file
   * @return true if a preview can be produced  - false otherwise.
   */
  public static boolean isPreviewable(File file) {
    return getPreviewService().isPreviewable(file);
  }

   /**
   * Indicates if file is previewable.
   * @param path
   * @return true if a preview can be produced  - false otherwise.
   */
  public static boolean isPreviewable(String path) {
    return getPreviewService().isPreviewable(new File(path));
  }


  /**
   * Indicates if file is displayable with FlexPaper.
   * @param file
   * @return true if a preview can be produced  - false otherwise.
   */
  public static boolean isViewable(File file) {
    return getViewService().isViewable(file);
  }

   /**
   * Indicates if file is displayable with FlexPaper.
   * @param path
   * @return true if a preview can be produced  - false otherwise.
   */
  public static boolean isViewable(String path) {
    return getViewService().isViewable(new File(path));
  }

  /**
   * View service accessor
   * @return a view service
   * @see DefaultViewService
   */
  public static ViewService getViewService() {
    return ServiceProvider.getService(ViewService.class);
  }
}
