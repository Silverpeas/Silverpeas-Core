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

package org.silverpeas.core.contribution.converter;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.silverpeas.core.contribution.converter.DocumentFormat.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the conversion of documents with an OpenOffice server.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ToHTMLConverterIntegrationTest extends AbstractConverterIntegrationTest {

  private static final String ODT_DOCUMENT_NAME = "API_REST_Silverpeas.odt";
  private static final String DOC_DOCUMENT_NAME = "API_REST_Silverpeas.doc";
  private static final String RTF_DOCUMENT_NAME = "file.rtf";

  private ToHTMLConverter converter;

  public ToHTMLConverterIntegrationTest() {
  }

  @Before
  public void setUp() throws Exception {
    document = getDocumentNamed(ODT_DOCUMENT_NAME);
    assertThat(document.exists(), is(true));
    converter = DocumentFormatConverterProvider.getToHTMLConverter();
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
    try(InputStream rtfDocument = openDocumentNamedInputStream(RTF_DOCUMENT_NAME)) {
      ByteArrayOutputStream htmlDocument = new ByteArrayOutputStream();
      converter.convert(rtfDocument, inFormat(rtf), htmlDocument, inFormat(html));
      assertThat(htmlDocument.size(), greaterThan(0));
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

  private InputStream openDocumentNamedInputStream(final String name) throws Exception {
    File document = getDocumentNamed(name);
    return new FileInputStream(document);
  }
}
