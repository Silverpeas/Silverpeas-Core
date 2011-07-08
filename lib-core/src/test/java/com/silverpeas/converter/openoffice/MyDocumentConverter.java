/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/legal/licensing"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.converter.openoffice;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.silverpeas.converter.DocumentFormat;
import java.io.File;
import java.net.ConnectException;
import javax.inject.Inject;
import javax.inject.Named;
import static org.mockito.Mockito.*;

/**
 * A converter of documents that is based upon the OpenOffice service.
 * This converter is just for testing purpose.
 */
@Named
public class MyDocumentConverter extends OpenOfficeConverter {

  @Inject
  private OpenOfficeODTConverter converter;
  private OpenOfficeDocumentConverter ooconverter = mock(OpenOfficeDocumentConverter.class);

  @Override
  public DocumentFormat[] getSupportedFormats() {
    return converter.getSupportedFormats();
  }

  @Override
  public boolean isDocumentSupported(File document) {
    return converter.isDocumentSupported(document);
  }

  @Override
  protected void closeConnection(OpenOfficeConnection connection) {
  }

  @Override
  protected OpenOfficeDocumentConverter getOpenOfficeDocumentConverterFrom(OpenOfficeConnection connection) {
    return ooconverter;
  }

  @Override
  protected OpenOfficeConnection openConnection() throws ConnectException {
    return mock(OpenOfficeConnection.class);
  }

  public OpenOfficeDocumentConverter getMockedOpenOfficeDocumentConverter() {
    return ooconverter;
  }
}
