/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PaginationUtilTest {

  @Test
  public void formatFromFirstIndexOfItem() {
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(-1, -1, -1), is("-1 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(-1, -1, 0), is("-1 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(-1, 0, 0), is("0 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(1, 1, 0), is("1 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(15, 1, 0), is("1 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(15, 16, 0), is("1 - 15 / 16 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(15, 50, 0), is("1 - 15 / 50 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(15, 50, 1), is("2 - 16 / 50 "));
    assertThat(PaginationUtil.formatFromFirstIndexOfItem(15, 50, 15), is("16 - 30 / 50 "));
  }

  @Test
  public void formatFromCurrentPageIndex() {
    assertThat(PaginationUtil.formatFromCurrentPageIndex(-1, -1, -1), is("-1 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(-1, -1, 0), is("-1 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(-1, 0, 0), is("0 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(1, 1, 0), is("1 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 1, 0), is("1 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 16, 0), is("1 - 15 / 16 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 50, 0), is("1 - 15 / 50 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 50, 1), is("16 - 30 / 50 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 50, 2), is("31 - 45 / 50 "));
    assertThat(PaginationUtil.formatFromCurrentPageIndex(15, 50, 3), is("46 - 50 / 50 "));
  }

  @Test
  public void countTotalNumberOfPages() {
    assertThat(PaginationUtil.countTotalNumberOfPages(-10, -10), is(1));
    assertThat(PaginationUtil.countTotalNumberOfPages(0, 0), is(1));
    assertThat(PaginationUtil.countTotalNumberOfPages(10, 0), is(1));
    assertThat(PaginationUtil.countTotalNumberOfPages(0, 10), is(1));
    for (int i = 1; i < 16; i++) {
      assertThat(PaginationUtil.countTotalNumberOfPages(15, i), is(1));
    }
    for (int i = 16; i < 31; i++) {
      assertThat(PaginationUtil.countTotalNumberOfPages(15, i), is(2));
    }
    for (int i = 31; i < 46; i++) {
      assertThat(PaginationUtil.countTotalNumberOfPages(15, i), is(3));
    }
  }

  @Test
  public void getPageIndexFromFirstDisplayedItemIndex() {
    assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(-10, -10), is(0));
    assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(0, 0), is(0));
    assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(10, 0), is(0));
    assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(0, 10), is(0));
    for (int i = 0; i < 15; i++) {
      assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(15, i), is(0));
    }
    for (int i = 15; i < 30; i++) {
      assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(15, i), is(1));
    }
    for (int i = 30; i < 45; i++) {
      assertThat(PaginationUtil.getPageIndexFromFirstDisplayedItemIndex(15, i), is(2));
    }
  }
}