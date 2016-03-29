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
package org.silverpeas.core.contribution.content.wysiwyg.service;

/**
 * Processes operation on WYSIWYG content and returning a typed result.
 * Must be used with {@link WysiwygContentTransformer}.
 * @param <TYPED_RESULT> the type of the result.
 * @author Yohann Chastagnier
 */
public interface WysiwygContentTransformerProcess<TYPED_RESULT> {

  /**
   * Executes the process on the given WYSIWYG content.
   * @param wysiwygContent the WYSIWYG content source.
   * @return the typed result of the WYSIWYG transformation process.
   */
  TYPED_RESULT execute(String wysiwygContent) throws Exception;
}
