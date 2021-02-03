/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.contribution.service;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.WysiwygContent;

/**
 * A repository of WYSIWYG contents.
 * @author mmoquillon
 */
public interface WysiwygContentRepository {

  /**
   * Saves the specified content into the repository so that it can be get later.
   * @param content the content to save.
   */
  void save(final WysiwygContent content);

  /**
   * Gets the content related to the specified contribution.
   * @param contribution the contribution for which the content is asked.
   * @return the WYSIWYG content of the specified contribution or null if either the contribution
   * has not yet a content or its content is not a WYSIWYG one.
   */
  WysiwygContent getByContribution(LocalizedContribution contribution);

  /**
   * Deletes the specified content in the repository.
   * @param content the content to remove.
   */
  void delete(WysiwygContent content);

  /**
   * Deletes all the contents of the specified contribution in the repository. If the contribution
   * has several WYSIWYG contents, that is one per localization for example, then all of those
   * contents will be deleted. Otherwise only the single WYSIWYG content of the contribution,
   * whatever its localization will be deleted.
   * @param contribution the contribution for which all the WYSIWYG contents will be deleted.
   */
  void deleteByContribution(Contribution contribution);
}
