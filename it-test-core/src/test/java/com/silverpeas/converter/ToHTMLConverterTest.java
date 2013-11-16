/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.silverpeas.converter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static com.silverpeas.converter.DocumentFormat.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the conversion of documents with an OpenOffice server.
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-converter.xml")
public class ToHTMLConverterTest {

  private static final String ODT_DOCUMENT_NAME = "API_REST_Silverpeas.odt";
  private static final String DOC_DOCUMENT_NAME = "API_REST_Silverpeas.doc";
  private static final String RTF_DOCUMENT_NAME = "file.rtf";

  private ToHTMLConverter converter;
  private File document;

  public ToHTMLConverterTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    document = getDocumentNamed(ODT_DOCUMENT_NAME);
    assertThat(document.exists(), is(true));
    converter = DocumentFormatConverterFactory.getFactory().getToHTMLConverter();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void convertAnODTDocumentToHTML() throws Exception {
    final File convertedDocument = converter.convert(document, inFormat(html));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test
  public void convertAnDocDocumentToHTML() throws Exception {
    final File convertedDocument =
        converter.convert(getDocumentNamed(DOC_DOCUMENT_NAME), inFormat(html));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test
  public void convertAnRtfDocumentToHTML() throws Exception {
    final File convertedDocument =
        converter.convert(getDocumentNamed(RTF_DOCUMENT_NAME), inFormat(html));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test
  public void convertAnRtfInputStreamToHTMLOutputStream() throws Exception {
    InputStream rtfDocument = openDocumentNamedInputStream(RTF_DOCUMENT_NAME);
    try {
      ByteArrayOutputStream htmlDocument = new ByteArrayOutputStream();
      converter.convert(rtfDocument, inFormat(rtf), htmlDocument, inFormat(html));
      assertThat(htmlDocument.size(), greaterThan(0));
    } finally {
      if (rtfDocument != null) {
        rtfDocument.close();
      }
    }
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnTxtDocumentToHTML() throws Exception {
    assertConvertAnDocumentToHTML("xml");
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnXMLDocumentToHTML() throws Exception {
    assertConvertAnDocumentToHTML("xml");
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnPDFDocumentToHTML() throws Exception {
    assertConvertAnDocumentToHTML("pdf");
  }

  public void assertConvertAnDocumentToHTML(final String extension) throws Exception {
    final File convertedDocument =
        converter.convert(getDocumentNamed("file." + extension), inFormat(html));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnODTDocumentToDoc() throws Exception {
    final File convertedDocument = converter.convert(document, inFormat(doc));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnODTDocumentToRTF() throws Exception {
    final File convertedDocument = converter.convert(document, inFormat(rtf));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnODTDocumentIntoANonSupportedFormat() throws Exception {
    converter.convert(document, inFormat(odt));
  }

  private File getDocumentNamed(final String name) throws Exception {
    final URL documentLocation = getClass().getResource(name);
    return new File(documentLocation.toURI());
  }

  private InputStream openDocumentNamedInputStream(final String name) throws Exception {
    return getClass().getResourceAsStream(name);
  }
}
