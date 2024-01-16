/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.test.UnitTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * @author silveryocha
 */
@UnitTest
class PaginationTest {

  private List<Integer> dataSource = new ArrayList<>();
  private Function<PaginationPage, SilverpeasList<Integer>> simplePaginatedDataSource = p -> {
    int start = (p.getPageNumber() - 1) * p.getPageSize();
    if (start >= this.dataSource.size()) {
      return new SilverpeasArrayList<>(0);
    }
    int end = start + p.getPageSize();
    if (end > this.dataSource.size()) {
      end = this.dataSource.size();
    }
    return new SilverpeasArrayList<>(this.dataSource.subList(start, end));
  };

  @BeforeEach
  void setup() {
    for (int i = 0; i < 1000; i++) {
      dataSource.add(i);
    }
  }

  @Test
  void firstPageWithoutFilteringShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(1, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .filter(r -> r)
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(5));
    assertThat(result, contains(0, 1 ,2 ,3 ,4));
  }

  @Test
  void secondPageWithoutFilteringShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(2, 3));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .filter(r -> r)
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(3));
    assertThat(result, contains(3 ,4, 5));
  }

  @Test
  void firstPageButFilteringAlmostAllDataShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(1, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .filter(r -> r.stream().filter(i -> i > 996).collect(SilverpeasList.collector(r)))
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(3));
    assertThat(result, contains(997, 998, 999));
  }

  @Test
  void firstPageButFilteringAlmostAllDataAndLimitingDataSourceUseShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(1, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .limitDataSourceCallsTo(50)
        .filter(r -> r.stream().filter(i -> i > 996).collect(SilverpeasList.collector(r)))
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(true));
    assertThat(result.size(), is(0));
  }

  @Test
  void firstPageButFilteringAlmostAllDataAndLimitingDataSourceUseCanReturnPartialResult() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(1, 3));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .limitDataSourceCallsTo(333)
        .filter(r -> r.stream().filter(i -> i > 996).collect(SilverpeasList.collector(r)))
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(true));
    assertThat(result.size(), is(2));
    assertThat(result, contains(997, 998));
  }

  @Test
  void middlePageWithFilteringAlmostAllDataShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(100, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .filter(r -> r.stream().filter(i -> i > 996).collect(SilverpeasList.collector(r)))
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(3));
    assertThat(result, contains(997, 998, 999));
  }

  @Test
  void middlePageWithFilteringShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(100, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(simplePaginatedDataSource)
        .filter(r -> r.stream().filter(i -> i > 496 && i != 499).collect(SilverpeasList.collector(r)))
        .factor(1)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(5));
    assertThat(result, contains(497, 498, 500, 501, 502));
  }

  @Test
  void justLittleDataShouldWork() {
    final Pagination<Integer> pagination = new Pagination<>(new PaginationPage(1, 5));
    final List<Integer> result = pagination
        .paginatedDataSource(p -> new SilverpeasArrayList<>(Arrays.asList(3, 7, 8)))
        .filter(r -> r)
        .execute();
    assertThat(pagination.isNbMaxDataSourceCallLimitReached(), is(false));
    assertThat(result.size(), is(3));
    assertThat(result, contains(3, 7, 8));
  }
}