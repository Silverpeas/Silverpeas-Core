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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.contribution.publication.service;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;
import org.silverpeas.core.contribution.publication.dao.PublicationDAO;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static java.util.Calendar.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.silverpeas.core.test.rule.DbSetupRule.getSafeConnection;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class LastPublicationDAOIT {

  private static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  private static final String DATASET_SQL_SCRIPT = "test-last-publication-dao-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SQL_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Publication.onWarForTestClass(LastPublicationDAOIT.class)
        .build();
  }

  @Test
  public void selectLatestPksByStatus() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .onComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .mustHaveAtLeastOneNodeFather()
          .orderByDescendingLastUpdateDate());
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(4));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("101", "kmelia100"),
              new PublicationPK("100", "kmelia100"), new PublicationPK("202", "kmelia200")));
    }
  }

  @Test
  public void selectLatestPksByStatusExcludingTrashNode() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .orderByDescendingLastUpdateDate());
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(4));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("101", "kmelia100"),
              new PublicationPK("100", "kmelia100"), new PublicationPK("202", "kmelia200")));
    }
  }

  @Test
  public void selectLatestPksByStatusWithoutCheckingNodeFathers() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .onComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .orderByDescendingLastUpdateDate());
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(5));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("103", "kmelia100"), new PublicationPK("101", "kmelia100"),
              new PublicationPK("100", "kmelia100"), new PublicationPK("202", "kmelia200")));
    }
  }

  @Test
  public void selectLatestPksByStatusWithSmallPaginationFirstPageResult() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .orderByDescendingLastUpdateDate()
          .paginateBy(new PaginationPage(1, 2)));
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(2));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("101", "kmelia100")));
    }
  }

  @Test
  public void selectLatestPksByStatusWithSmallPaginationSecondPageResult() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .orderByDescendingLastUpdateDate()
          .paginateBy(new PaginationPage(2, 3)));
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(1));
      assertThat(keys, IsIterableContainingInOrder.contains(new PublicationPK("202", "kmelia200")));
    }
  }

  @Test
  public void selectLastPublications() throws Exception {
    try (Connection con = getSafeConnection()) {
      List<String> componentIds = Arrays.asList("kmelia100", "kmelia200");
      Calendar calend = Calendar.getInstance();
      calend.set(YEAR, 2009);
      calend.set(MONTH, NOVEMBER);
      calend.set(DAY_OF_MONTH, 15);
      calend.set(HOUR_OF_DAY, 10);
      calend.set(MINUTE, 15);
      calend.set(SECOND, 0);
      calend.set(MILLISECOND, 0);
      OffsetDateTime since = OffsetDateTime.ofInstant(calend.getTime().toInstant(), ZoneId.systemDefault());
      Collection<PublicationPK> keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .lastUpdatedSince(since)
          .orderByDescendingLastUpdateDate());
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(3));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("101", "kmelia100"),
              new PublicationPK("100", "kmelia100")));
      keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .lastUpdatedSince(since)
          .orderByDescendingLastUpdateDate()
          .paginateBy(new PaginationPage(1, 2)));
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(2));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("200", "kmelia200"), new PublicationPK("101", "kmelia100")));
      keys = PublicationDAO.selectPksByCriteria(con, PublicationCriteria
          .excludingTrashNodeOnComponentInstanceIds(componentIds)
          .ofStatus(PublicationDetail.VALID_STATUS)
          .lastUpdatedSince(since)
          .orderByDescendingLastUpdateDate()
          .paginateBy(new PaginationPage(3, 1)));
      assertThat(keys, notNullValue());
      assertThat(keys.size(), is(1));
      assertThat(keys, IsIterableContainingInOrder
          .contains(new PublicationPK("100", "kmelia100")));
    }
  }
}
