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

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;

import static org.silverpeas.core.contribution.converter.DocumentFormat.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the conversion of documents with an OpenOffice server.
 */
@RunWith(Arquillian.class)
public class ODTConverterIntegrationTest extends AbstractConverterIntegrationTest {

  private static final String DOCUMENT_NAME = "API_REST_Silverpeas.odt";
  private static final String WRONG_DOCUMENT_NAME = "API_REST_Silverpeas.doc";

  @Inject
  private ODTConverter converter;

  public ODTConverterIntegrationTest() {
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
    File convertedDocument = converter.convert(document, inFormat(pdf));
    assertThat(convertedDocument.exists(), is(true));
    //assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(143312l));
  }

  @Test
  public void convertAnODTDocumentToDoc() throws Exception {
    File convertedDocument = converter.convert(document, inFormat(doc));
    assertThat(convertedDocument.exists(), is(true));
    //assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(155000l));
  }

  @Test
  public void convertAnODTDocumentToRTF() throws Exception {
    File convertedDocument = converter.convert(document, inFormat(rtf));
    assertThat(convertedDocument.exists(), is(true));
    //assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(1333739l));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertANonODTDocument() throws Exception {
    File wrongDocument = getDocumentNamed(WRONG_DOCUMENT_NAME);
    assertThat(wrongDocument.exists(), is(true));
    converter.convert(wrongDocument, inFormat(pdf));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertAnODTDocumentIntoANonSupportedFormat() throws Exception {
    converter.convert(document, inFormat(odt));
  }

}
