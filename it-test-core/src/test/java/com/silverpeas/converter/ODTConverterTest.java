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
package com.silverpeas.converter;

import java.io.File;
import java.net.URL;
import javax.inject.Inject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.silverpeas.converter.DocumentFormat.*;

/**
 * Test the conversion of documents with an OpenOffice server.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/spring-converter.xml")
public class ODTConverterTest {

  private static final String DOCUMENT_NAME = "API_REST_Silverpeas.odt";
  private static final String WRONG_DOCUMENT_NAME = "API_REST_Silverpeas.doc";

  @Inject
  private ODTConverter converter;
  private File document;

  public ODTConverterTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    document = getDocumentNamed(DOCUMENT_NAME);
    assertThat(document.exists(), is(true));
  }

  @After
  public void tearDown() {
  }

  @Test
  public void convertAnODTDocumentToPDF() throws Exception {
    File convertedDocument = converter.convert(document, in(pdf));
    assertThat(convertedDocument.exists(), is(true));
    assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(143312l));
  }

  @Test
  public void convertAnODTDocumentToDoc() throws Exception {
    File convertedDocument = converter.convert(document, in(doc));
    assertThat(convertedDocument.exists(), is(true));
    assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(157184l));
  }

  @Test
  public void convertAnODTDocumentToRTF() throws Exception {
    File convertedDocument = converter.convert(document, in(rtf));
    assertThat(convertedDocument.exists(), is(true));
    assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(1333739l));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertANonODTDocument() throws Exception {
    File wrongDocument = getDocumentNamed(WRONG_DOCUMENT_NAME);
    assertThat(wrongDocument.exists(), is(true));
    converter.convert(wrongDocument, in(pdf));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertAnODTDocumentIntoANonSupportedFormat() throws Exception {
    converter.convert(document, in(odt));
  }

  private File getDocumentNamed(String name) throws Exception {
    URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }
}
