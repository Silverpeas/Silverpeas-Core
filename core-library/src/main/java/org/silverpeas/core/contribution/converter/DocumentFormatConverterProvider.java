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

package org.silverpeas.core.contribution.converter;

import org.silverpeas.core.util.ServiceProvider;

/**
 * A factory of document format converters available in Silverpeas. This factory is dedicated to
 * objects whose the life-cycle isn't managed by the IoC container.
 */
public class DocumentFormatConverterProvider {

  /**
   * Gets an instance of the ODTConverter interface.
   * @return an ODTConverter instance.
   */
  public static ODTConverter getODTConverter() {
    return ServiceProvider.getService(ODTConverter.class);
  }

  /**
   * Gets an instance of the HTMLConverter interface.
   * @return a HTMLConverter instance.
   */
  public static HTMLConverter getHTMLConverter() {
    return ServiceProvider.getService(HTMLConverter.class);
  }

  /**
   * Gets an instance of the ToPDFConverter interface.
   * @return a ToPDFConverter instance.
   */
  public static ToPDFConverter getToPDFConverter() {
    return ServiceProvider.getService(ToPDFConverter.class);
  }

  /**
   * Gets an instance of the ToHTMLConverter interface.
   * @return a ToHTMLConverter instance.
   */
  public static ToHTMLConverter getToHTMLConverter() {
    return ServiceProvider.getService(ToHTMLConverter.class);
  }

  private DocumentFormatConverterProvider() {
  }
}
