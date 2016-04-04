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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class PaginationSilverpeasV5Test {

  public PaginationSilverpeasV5Test() {
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
   * Test of init method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testInit() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(nbItems, instance.getNbItems());
    assertEquals(nbItemsPerPage, instance.getNbItemsPerPage());
    assertEquals(firstItemIndex, instance.getFirstItemIndex());
  }

  /**
   * Test of printCounter method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintCounterWithTwoPages() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printCounter();
    assertNotNull(result);
    assertEquals("1 - 10 / 14 ", result);

    firstItemIndex = 12;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    result = instance.printCounter();
    assertNotNull(result);
    assertEquals("13 - 14 / 14 ", result);
  }

  /**
   * Test of printCounter method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintCounterWithOnePages() {
    int nbItems = 9;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printCounter();
    assertNotNull(result);
    assertEquals("9 ", result);
  }

  /**
   * Test of printIndex method, of class PaginationSilverpeasV5.
   */
  @Test
  public void testPrintIndex() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.setActionSuffix("");
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printIndex();
    assertNotNull(result);
    assertEquals("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" "
        + "align=\"center\"><tr valign=\"middle\" class=\"intfdcolor\"><td align=\"center\">"
        + "&#160;&#160;&#160; 1  <a href=\"Pagination?Index=10\">2</a>  <a href=\"Pagination?Index=10\"><img src=\""
        + "/silverpeas//util/viewGenerator/icons/arrows/arrowRight.gif\" border=\"0\" "
        + "align=\"absmiddle\" alt=\"\"/></a></td></tr></table>", result);

    instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, 10);
    result = instance.printIndex();
    assertNotNull(result);
    assertEquals("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" "
        + "align=\"center\"><tr valign=\"middle\" class=\"intfdcolor\"><td align=\"center\"> "
        + "<a href=\"Pagination?Index=0\"><img src=\"/silverpeas//util/viewGenerator/icons/arrows/"
        + "arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"\"/></a>  <a href="
        + "\"Pagination?Index=0\">1</a>  2 &#160;&#160;&#160;</td></tr></table>", result);
  }

  @Test
  public void testGetNbPage() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getNbPage());

    nbItemsPerPage = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(3, instance.getNbPage());
  }

  @Test
  public void testGetCurrentPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(1, instance.getCurrentPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(3, instance.getCurrentPage());

    firstItemIndex = 8;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getCurrentPage());


    firstItemIndex = 6;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getCurrentPage());

    firstItemIndex = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getCurrentPage());

     firstItemIndex = 10;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(3, instance.getCurrentPage());
  }


  @Test
  public void testGetIndexForPreviousPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(-5, instance.getIndexForPreviousPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(5, instance.getIndexForPreviousPage());

    firstItemIndex = 8;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(0, instance.getIndexForPreviousPage());


    firstItemIndex = 6;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(0, instance.getIndexForPreviousPage());

    firstItemIndex = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(0, instance.getIndexForPreviousPage());

     firstItemIndex = 10;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(5, instance.getIndexForPreviousPage());
  }


  @Test
  public void testGetIndexForNextPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(5, instance.getIndexForNextPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForNextPage());

    firstItemIndex = 8;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForNextPage());


    firstItemIndex = 6;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForNextPage());

    firstItemIndex = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForNextPage());

    firstItemIndex = 10;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForNextPage());
  }


  @Test
  public void testGetIndexForLastPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());

    firstItemIndex = 8;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());


    firstItemIndex = 6;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());

    firstItemIndex = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());

    firstItemIndex = 10;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForLastPage());
  }


  @Test
  public void testGetIndexForDirectPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(0, instance.getIndexForDirectPage(1));

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(5, instance.getIndexForDirectPage(2));

    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForDirectPage(3));

    firstItemIndex = 8;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(10, instance.getIndexForDirectPage(5));
  }
  /**
   * Test of printIndex method, of class PaginationSilverpeasV5.
   */
  /*@Test
  public void testPrintIndex_String() {
  System.out.println("printIndex");
  String javascriptFunc = "";
  PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
  String expResult = "";
  String result = instance.printIndex(javascriptFunc);
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
  /**
   * Test of print method, of class PaginationSilverpeasV5.
   */
  /*@Test
  public void testPrint() {
  System.out.println("print");
  PaginationSilverpeasV5 instance = new PaginationSilverpeasV5();
  String expResult = "";
  String result = instance.print();
  assertEquals(expResult, result);
  // TODO review the generated test code and remove the default call to fail.
  fail("The test case is a prototype.");
  }*/
}