/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.converter;

import javax.inject.Inject;

/**
 * A factory of document format converters available in Silverpeas. This factory is dedicated to
 * objects whose the life-cycle isn't managed by the IoC container.
 */
public class DocumentFormatConverterFactory {

  private static final DocumentFormatConverterFactory instance =
      new DocumentFormatConverterFactory();

  @Inject
  private ODTConverter odtConverter;

  @Inject
  private HTMLConverter htmlConverter;

  /**
   * Gets an instance of this factory.
   * @return a DocumentFormatConverterFactory instance.
   */
  public static DocumentFormatConverterFactory getFactory() {
    return instance;
  }

  /**
   * Gets an instance of the ODTConverter interface.
   * @return an ODTConverter instance.
   */
  public ODTConverter getODTConverter() {
    return odtConverter;
  }

  /**
   * Gets an instance of the HTMLConverter interface.
   * @return a HTMLConverter instance.
   */
  public HTMLConverter getHTMLConverter() {
    return htmlConverter;
  }

  private DocumentFormatConverterFactory() {

  }
}
