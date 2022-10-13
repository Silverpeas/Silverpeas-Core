/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author ehugonnet
 */
@UnitTest
class PaginationSPTest {

  /**
   * Test of init method, of class PaginationSP.
   */
  @Test
  void testInit() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSP instance = new PaginationSP();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(nbItems, instance.getNbItems());
    assertEquals(nbItemsPerPage, instance.getNbItemsPerPage());
    assertEquals(firstItemIndex, instance.getFirstItemIndex());
  }

  /**
   * Test of printCounter method, of class PaginationSP.
   */
  @Test
  void testPrintCounterWithTwoPages() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    Pagination instance = new PaginationSP();
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
   * Test of printCounter method, of class PaginationSP.
   */
  @Test
  void testPrintCounterWithOnePages() {
    int nbItems = 9;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    Pagination instance = new PaginationSP();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    String result = instance.printCounter();
    assertNotNull(result);
    assertEquals("9 ", result);
  }

  @Test
  void testGetNbPage() {
    int nbItems = 14;
    int nbItemsPerPage = 10;
    int firstItemIndex = 0;
    PaginationSP instance = new PaginationSP();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getNbPage());

    nbItemsPerPage = 5;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(3, instance.getNbPage());
  }

  @Test
  void testGetCurrentPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSP instance = new PaginationSP();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(1, instance.getCurrentPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(2, instance.getCurrentPage());

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
  void testGetIndexForPreviousPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    Pagination instance = new PaginationSP();
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(-5, instance.getIndexForPreviousPage());

    firstItemIndex = 14;
    instance.init(nbItems, nbItemsPerPage, firstItemIndex);
    assertEquals(0, instance.getIndexForPreviousPage());

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
  void testGetIndexForNextPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    Pagination instance = new PaginationSP();
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
  void testGetIndexForLastPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    PaginationSP instance = new PaginationSP();
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
  void testGetIndexForDirectPage() {
    int nbItems = 14;
    int nbItemsPerPage = 5;
    int firstItemIndex = 0;
    Pagination instance = new PaginationSP();
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

}