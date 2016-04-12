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

package org.silverpeas.core.contribution.converter.openoffice;

import static org.silverpeas.core.contribution.converter.DocumentFormat.pdf;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import org.silverpeas.core.contribution.converter.DocumentFormat;
import org.silverpeas.core.contribution.converter.ToPDFConverter;
import org.silverpeas.core.util.file.FileUtil;

/**
 * Implementation of the ToPDFConverter interface by using the OpenOffice API to perform its job.
 * @author Yohann Chastagnier
 */
@Singleton
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
}
