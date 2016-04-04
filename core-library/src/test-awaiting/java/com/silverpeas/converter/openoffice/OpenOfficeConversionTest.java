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

package com.silverpeas.converter.openoffice;

import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.silverpeas.converter.DocumentFormatConversionException;
import com.silverpeas.converter.DocumentFormatConverterProvider;
import com.silverpeas.converter.DocumentFormatException;
import com.silverpeas.converter.ODTConverter;
import java.io.File;
import javax.inject.Inject;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.silverpeas.converter.DocumentsProvider.*;
import static com.silverpeas.converter.DocumentFormat.*;

/**
 * Tests about the conversion of documents with the implementation based on OpenOffice .
 * As the conversion process itself is mocked, the tests assert just it is ran as expected.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-converter.xml")
public class OpenOfficeConversionTest {

  @Inject
  private MyDocumentConverter converter;

  public OpenOfficeConversionTest() {
  }

  @Before
  public void setUp() {
    assertNotNull(converter);
  }

  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  @Test(expected = DocumentFormatException.class)
  public void convertingANonODTDocumentShouldThrowADocumentFormatException() throws Exception {
    File document = aDocumentsProvider().getMSWordDocument();
    converter.convert(document, inFormat(pdf));
  }

  @Test(expected = DocumentFormatException.class)
  public void convertingADocumentIntoANonSupportedFormatShouldThrowADocumentFormatException() throws
    Exception {
    File document = aDocumentsProvider().getMSWordDocument();
    converter.convert(document, inFormat(odt));
  }

  @Test(expected = DocumentFormatConversionException.class)
  public void conversionWithoutAnyOpenOfficeServiceShouldThrowADocumentFormatConversionException()
    throws Exception {
    ODTConverter odtConverter = DocumentFormatConverterProvider.getODTConverter();
    File document = aDocumentsProvider().getODTDocument();
    odtConverter.convert(document, inFormat(pdf));
  }

  @Test
  public void conversionWithAnOpenOfficeServiceUpShouldSucceed() throws Exception {
    File document = aDocumentsProvider().getODTDocument();
    File convertedDocument = converter.convert(document, inFormat(pdf));
    assertNotNull(convertedDocument);

    OpenOfficeDocumentConverter ooconverter = converter.getMockedOpenOfficeDocumentConverter();
    verify(ooconverter).convert(document, convertedDocument);
  }
}
