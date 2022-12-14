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
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.web.rs.WebEntity;

/**
 * The preview entity is a preview instance that is exposed in the web as
 * an entity (web entity).
 * @author Yohann Chastagnier
 */
public class PreviewEntity extends AbstractPreviewEntity<PreviewEntity> {
  private static final long serialVersionUID = 4270519541076741138L;

  private String url;

  /**
   * Creates a new Preview entity from the specified preview.
   * @param preview the preview to transform to {@link WebEntity}.
   * @return the entity representing the specified preview.
   */
  public static PreviewEntity createFrom(final Preview preview) {
    return new PreviewEntity(preview);
  }

  /**
   * Default constructorC
   * @param preview a {@link Preview} instance.
   */
  protected PreviewEntity(final Preview preview) {
    super(preview);
    url = preview.getURLAsString().replaceAll("[/]{2,}", "/");
  }

  protected PreviewEntity() {
  }

  /**
   * @return the url
   */
  public String getURL() {
    return url;
  }
}
