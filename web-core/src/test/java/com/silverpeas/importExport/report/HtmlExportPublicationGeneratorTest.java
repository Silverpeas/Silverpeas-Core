/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.util.Calendar;
import com.silverpeas.importExport.model.PublicationType;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
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
        null, null, "http://www.test.fr");
    String expResult = "&#149;&nbsp;<a href='http://www.test.fr' target='_blank'><b>"
        + "Ma publication de test</b></a> - Bart Simpson<br/><i>Publication pour les tests</i><br/><br/>";
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
        null, null, "http://www.test.fr");
    String expResult = "<table cellspacing='1' width='100%' border='0' cellpadding='0' "
        + "bgcolor='#B3BFD1'><tr><td><table cellspacing='0' width='100%' border='0' cellpadding='3' "
        + "bgcolor='#EFEFEF'><tr><td><strong>Ma publication de test</strong></td>"
        + "<td><div align='right'>Bart Simpson</div></td></tr><tr><td>Publication pour les "
        + "tests</td><td><div align='right'></div></td></tr></table></td></tr></table>";
    String result = instance.toHtmlEnTetePublication();
    assertEquals(expResult, result);
    
  }
}