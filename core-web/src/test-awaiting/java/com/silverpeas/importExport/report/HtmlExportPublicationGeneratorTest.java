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

package com.silverpeas.importExport.report;

import org.hamcrest.Matchers;
import java.util.Calendar;
import com.silverpeas.importExport.model.PublicationType;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class HtmlExportPublicationGeneratorTest {

  public HtmlExportPublicationGeneratorTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of toHtmlSommairePublication method, of class HtmlExportPublicationGenerator.
   */
  @Test
  public void testToHtmlSommairePublicationTarget() {
    String target = "_blank";
    PublicationDetail publication = new PublicationDetail();
    publication.setName("Ma publication de test");
    publication.setDescription("Publication pour les tests");
    publication.setCreatorName("Bart Simpson");
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 15);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 2011);
    PublicationType publicationType = Mockito.mock(PublicationType.class);
    Mockito.when(publicationType.getPublicationDetail()).thenReturn(publication);
    HtmlExportPublicationGenerator instance = new HtmlExportPublicationGenerator(publicationType,
        null, "http://www.test.fr", 0);
    String expResult = "<li><a href='http://www.test.fr' target='_blank'><b>Ma publication de test</b></a> - Bart Simpson<br/><i>Publication pour les tests</i></li>";
    String result = instance.toHtmlSommairePublication(target);
    assertEquals(expResult, result);
  }

  @Test
  public void testToHtmlEnTetePublication() {
    PublicationDetail publication = new PublicationDetail();
    publication.setName("Ma publication de test");
    publication.setDescription("Publication pour les tests");
    publication.setCreatorName("Bart Simpson");
    Calendar calend = Calendar.getInstance();
    calend.set(Calendar.DAY_OF_MONTH, 15);
    calend.set(Calendar.MONTH, Calendar.FEBRUARY);
    calend.set(Calendar.YEAR, 2011);
    PublicationType publicationType = Mockito.mock(PublicationType.class);
    Mockito.when(publicationType.getPublicationDetail()).thenReturn(publication);
    HtmlExportPublicationGenerator instance = new HtmlExportPublicationGenerator(publicationType,
        null, "http://www.test.fr", 0);
    String expResult = "<h1>Ma publication de test</h1><div class='creationDetail'>Bart Simpson</div>";
    String result = instance.toHtmlEnTetePublication();
    assertEquals(expResult, result);

  }

  @Test
  public void testReplaceFilesPathForExport() {
    String html = "<a href=\"/attached_file/test/01/image.jpg\">Mon image</a>";
    String result = HtmlExportPublicationGenerator.replaceFilesPathForExport(html);
    assertThat(result, Matchers.is("<a href=\"image.jpg\">Mon image</a>"));

    html = "<p><a href=\"/attached_file/test/01/image.png\">Mon image</a> est plus belle que "
        + "<href=\"/attached_file/test/02/image.jpg\">Son image</a></p>";
    result = HtmlExportPublicationGenerator.replaceFilesPathForExport(html);
    assertThat(result, Matchers.is("<p><a href=\"image.png\">Mon image</a> est plus belle que "
        + "<href=\"image.jpg\">Son image</a></p>"));

    html = "<a href=\"/silverpeas/test/01/image.jpg\">Mon image</a>";
    result = HtmlExportPublicationGenerator.replaceFilesPathForExport(html);
    assertThat(result, Matchers.is("<a href=\"/silverpeas/test/01/image.jpg\">Mon image</a>"));
  }

  @Test
  public void testReplaceImagesPathForExport() {
    String html = "<img src=\"/attached_file/test/01/image.jpg\" />";
    String result = HtmlExportPublicationGenerator.replaceImagesPathForExport(html);
    assertThat(result, Matchers.is("<img src=\"image.jpg\" />"));

    html = "<p><img src=\"/attached_file/test/01/image.png\"/> Mon image est plus belle que "
        + "<img src=\"/attached_file/test/02/image.jpg\"/> Son image</p>";
    result = HtmlExportPublicationGenerator.replaceImagesPathForExport(html);
    assertThat(result, Matchers.is("<p><img src=\"image.png\"/> Mon image est plus belle que "
        + "<img src=\"image.jpg\"/> Son image</p>"));

    html = "<img src=\"/silverpeas/test/01/image.jpg\" />";
    result = HtmlExportPublicationGenerator.replaceImagesPathForExport(html);
    assertThat(result, Matchers.is("<img src=\"/silverpeas/test/01/image.jpg\" />"));
  }
}