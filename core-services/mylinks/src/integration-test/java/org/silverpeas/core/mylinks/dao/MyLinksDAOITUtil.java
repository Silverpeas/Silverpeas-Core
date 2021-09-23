/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

/**
 * @author silveryocha
 */
public class MyLinksDAOITUtil {

  private MyLinksDAOITUtil() {
  }

  static void assertLinkIds(final List<Integer> actualIds, final Integer... expectedIds) {
    if (expectedIds.length == 0) {
      assertThat(actualIds, empty());
    } else {
      assertThat(actualIds, contains(expectedIds));
    }
  }

  static List<Integer> getAllLinkIds() throws SQLException {
    return JdbcSqlQuery.createSelect("linkid")
        .from("SB_MyLinks_Link")
        .orderBy("linkid")
        .execute(r -> r.getInt(1));
  }

  static void assertCategoryIds(final List<Integer> actualIds, final Integer... expectedIds) {
    if (expectedIds.length == 0) {
      assertThat(actualIds, empty());
    } else {
      assertThat(actualIds, contains(expectedIds));
    }
  }

  static List<Integer> getAllCategoryIds() throws SQLException {
    return JdbcSqlQuery.createSelect("catid")
        .from("SB_MyLinks_Cat")
        .orderBy("catid")
        .execute(r -> r.getInt(1));
  }

  static void assertOfCouples(final List<String> actualCouples, final String... expectedCouples) {
    if (expectedCouples.length == 0) {
      assertThat(actualCouples, empty());
    } else {
      assertThat(actualCouples, contains(expectedCouples));
    }
  }

  static List<String> getAllOfCouples() throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from("SB_MyLinks_LinkCat")
        .orderBy("catid, linkid")
        .execute(r -> r.getInt("catid") + "/" + r.getInt("linkid"));
  }
}
