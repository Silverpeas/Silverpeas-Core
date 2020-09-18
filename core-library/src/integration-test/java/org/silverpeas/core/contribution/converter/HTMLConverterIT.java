/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.contribution.converter.DocumentFormat.*;

/**
 * Test the conversion of documents with an OpenOffice server.
 */
@RunWith(Arquillian.class)
public class HTMLConverterIT extends AbstractConverterIntegrationTest {

  private static final String DOCUMENT_NAME = "wysiwyg2.html";
  private static final String WRONG_DOCUMENT_NAME = "API_REST_Silverpeas.doc";

  @Inject
  private HTMLConverter converter;

  public HTMLConverterIT() {
  }

  @Before
  public void setUp() throws Exception {
    document = getDocumentNamed(DOCUMENT_NAME);
    assertThat(document.exists(), is(true));
  }

  @Test
  public void convertAnHTMLDocumentToODT() throws Exception {
    File convertedDocument = converter.convert(document, inFormat(odt));
    assertThat(convertedDocument.exists(), is(true));
    //assertThat((Long)convertedDocument.length(), greaterThanOrEqualTo(12378l));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertANonHTMLDocument() throws Exception {
    File wrongDocument = getDocumentNamed(WRONG_DOCUMENT_NAME);
    assertThat(wrongDocument.exists(), is(true));
    converter.convert(wrongDocument, inFormat(odt));
  }

  @Test(expected=DocumentFormatException.class)
  public void convertAnHTMLDocumentIntoANonSupportedFormat() throws Exception {
    converter.convert(document, inFormat(pdf));
  }

}
