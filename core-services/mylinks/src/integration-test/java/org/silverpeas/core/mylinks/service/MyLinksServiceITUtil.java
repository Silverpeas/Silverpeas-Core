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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.mylinks.service;

import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;

import java.sql.SQLException;
import java.util.List;

import static java.lang.String.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

/**
 * @author silveryocha
 */
public class MyLinksServiceITUtil {

  private MyLinksServiceITUtil() {
  }

  static List<String> getLinks() throws Exception {
    return JdbcSqlQuery.createSelect("linkid, url, instanceid, userid from sb_mylinks_link")
        .addSqlPart("order by linkId")
        .execute(row -> row.getInt(1) + " | " + row.getString(2) + " | " +
                        row.getString(3) + " | " + row.getString(4));
  }

  static List<String> getCategoryIds() throws Exception {
    return JdbcSqlQuery.createSelect("catid, userid")
        .from("SB_MyLinks_Cat")
        .orderBy("catid")
        .execute(r -> r.getInt(1) + " | " + r.getString(2) );
  }

  static List<String> getLinkCategoryCouples() throws Exception {
    return JdbcSqlQuery.createSelect("*")
        .from("SB_MyLinks_LinkCat")
        .orderBy("catid, linkid")
        .execute(row -> row.getInt("catid") + " | " + row.getString("linkid"));
  }
}
