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

import static com.silverpeas.converter.DocumentFormat.doc;
import static com.silverpeas.converter.DocumentFormat.inFormat;
import static com.silverpeas.converter.DocumentFormat.odt;
import static com.silverpeas.converter.DocumentFormat.pdf;
import static com.silverpeas.converter.DocumentFormat.rtf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * Test the conversion of documents with an OpenOffice server.
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-converter.xml")
public class ToPDFConverterTest {

  private static final String ODT_DOCUMENT_NAME = "API_REST_Silverpeas.odt";
  private static final String DOC_DOCUMENT_NAME = "API_REST_Silverpeas.doc";

  @Inject
  private ToPDFConverter converter;
  private File document;

  public ToPDFConverterTest() {
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
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.cleanDirectory(new File(FileRepositoryManager.getTemporaryPath()));
  }

  @Test
  public void convertAnODTDocumentToPDF() throws Exception {
    final File convertedDocument = converter.convert(document, inFormat(pdf));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test
  public void convertAnDocDocumentToPDF() throws Exception {
    final File convertedDocument =
        converter.convert(getDocumentNamed(DOC_DOCUMENT_NAME), inFormat(pdf));
    assertThat(convertedDocument.exists(), is(true));
  }

  @Test
  public void convertAnDocumentToPDF() throws Exception {
    assertConvertAnDocumentToPDF("ppt");
    assertConvertAnDocumentToPDF("odp");
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnTxtDocumentToPDF() throws Exception {
    assertConvertAnDocumentToPDF("xml");
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnXMLDocumentToPDF() throws Exception {
    assertConvertAnDocumentToPDF("xml");
  }

  @Test(expected = DocumentFormatException.class)
  public void convertAnPDFDocumentToPDF() throws Exception {
    assertConvertAnDocumentToPDF("pdf");
  }

  public void assertConvertAnDocumentToPDF(final String extension) throws Exception {
    final File convertedDocument =
        converter.convert(getDocumentNamed("file." + extension), inFormat(pdf));
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
}
