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

package com.silverpeas.converter.openoffice;

import static com.silverpeas.converter.DocumentFormat.pdf;

import java.io.File;

import javax.inject.Named;

import com.silverpeas.converter.DocumentFormat;
import com.silverpeas.converter.ToPDFConverter;
import com.silverpeas.converter.option.FilterOption;
import com.silverpeas.util.FileUtil;

/**
 * Implementation of the ToPDFConverter interface by using the OpenOffice API to perform its job.
 * @author Yohann Chastagnier
 */
@Named("toPDFConverter")
public class OpenOfficeToPDFConverter extends OpenOfficeConverter implements ToPDFConverter {

  @Override
  public DocumentFormat[] getSupportedFormats() {
    return new DocumentFormat[] { pdf };
  }

  @Override
  public boolean isDocumentSupported(final File document) {
    return FileUtil.isOpenOfficeCompatible(document.getName());
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.converter.openoffice.OpenOfficeConverter#convert(com.artofsolving.jodconverter
   * .DocumentConverter, java.io.File, java.io.File,
   * com.silverpeas.converter.option.ConverterOption[])
   */
  @Override
  protected void convert(final SilverpeasOpenOfficeDocumentConverter documentConverter,
      final File source, final File destination, final FilterOption... options) {

    // Options
    if (options != null) {
      for (final FilterOption option : options) {
        documentConverter.addFilterData(option);
      }
    }

    // Convertion
    documentConverter.convert(source, destination);
  }
}
