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
package org.silverpeas.core.contribution.rating.service;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ComponentInstanceRatingDeletionIT {

  private static final Operation NOTATION_SETUP = Operations.insertInto("SB_Notation_Notation")
      .columns("id", "instanceId", "externalId", "externalType", "author", "note")
      .values(0, "kmelia382", "1155", "Publication", "42", 8)
      .values(1, "kmelia382", "1157", "Publication", "8", 5)
      .values(2, "suggestionBox26", "45a554b55", "Suggestion", "2", 50)
      .values(3, "kmelia383", "1160", "Publication", "42", 10)
      .values(4, "kmelia384", "1161", "Publication", "8", 8).build();

  @Inject
  private RatingService ratingServiceByInjection;

  private ComponentInstanceDeletion ratingService;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(
      "/org/silverpeas/core/contribution/rating/model/create_table.sql")
          .loadInitialDataSetFrom(NOTATION_SETUP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore
        .onWarForTestClass(ComponentInstanceRatingDeletionIT.class)
        .addJpaPersistenceFeatures().addComponentInstanceDeletionFeatures()
        .testFocusedOn(war -> war.addPackages(true, "org.silverpeas.core.contribution.rating"))
        .addAsResource("org/silverpeas/core/contribution/rating/model")
        .build();
  }

  @Before
  public void setup() {
    ratingService = ServiceProvider.getService(DefaultRatingService.class);
    assertThat(ratingService, sameInstance(ratingServiceByInjection));
  }

  @Test
  public void verifyingSqlTestData() throws Exception {
    assertThat("Ratings", getRatings(),
        contains("0-kmelia382-1155-Publication", "1-kmelia382-1157-Publication",
            "3-kmelia383-1160-Publication", "4-kmelia384-1161-Publication",
            "2-suggestionBox26-45a554b55-Suggestion"));
  }

  @Test
  public void deleteKmelia382() throws Exception {
    ratingService.delete("kmelia382");

    assertThat("Ratings", getRatings(),
        contains("3-kmelia383-1160-Publication", "4-kmelia384-1161-Publication",
            "2-suggestionBox26-45a554b55-Suggestion"));
  }

  @Test
  public void deleteKmelia383() throws Exception {
    ratingService.delete("kmelia383");

    assertThat("Ratings", getRatings(),
        contains("0-kmelia382-1155-Publication", "1-kmelia382-1157-Publication",
            "4-kmelia384-1161-Publication", "2-suggestionBox26-45a554b55-Suggestion"));
  }

  @Test
  public void deleteKmelia384() throws Exception {
    ratingService.delete("kmelia384");

    assertThat("Ratings", getRatings(),
        contains("0-kmelia382-1155-Publication", "1-kmelia382-1157-Publication",
            "3-kmelia383-1160-Publication", "2-suggestionBox26-45a554b55-Suggestion"));
  }

  /**
   * Returns the list of ratings (SB_Notation_Notation table).
   * @return list of strings which the schema is: [id]-[instanceid]-[externalId]-[externalType]
   * @throws Exception
   */
  private List<String> getRatings() throws Exception {
    return JdbcSqlQuery
        .select("id, instanceid, externalId, externalType from SB_Notation_Notation")
        .addSqlPart("order by externalId")
        .execute(row -> row.getInt(1) + "-" + row.getString(2) + "-" + row.getString(3) + "-" +
            row.getString(4));
  }
}