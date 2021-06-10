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
package org.silverpeas.core.contribution.publication.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.ContributionEventProcessor;
import org.silverpeas.core.contribution.ContributionModification;
import org.silverpeas.core.contribution.ContributionMove;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.test.WarBuilder4Publication;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PublicationUpdateIT {

  private static final String TABLE_CREATION_SCRIPT = "create-table.sql";
  private static final String DATASET_SCRIPT = "test-publication-dao-dataset.sql";

  @Inject
  private PublicationService publicationService;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Publication.onWarForTestClass(PublicationUpdateIT.class)
        .addJcrFeatures()
        .addClasses(ContributionEventProcessor.class, PublicationEvents.class)
        .addPackages(true, "org.silverpeas.core.io.media.image.thumbnail")
        .build();
  }

  @Before
  public void setup() {
    DataBeforeUpdate.get().clear();
  }

  @DisplayName("Default update of a publication")
  @Test
  public void defaultUpdate() throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    new UpdateTreatment(publicationService).execute(pk);
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(1));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  @DisplayName("Simulating two updates into a same transaction and verifying that publication " +
      "data before the two update are the one before the transaction, and not those just after " +
      "the first update")
  @Test
  public void twoUpdatesIntoSameTransaction() throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    final UpdateTreatment updateTreatment = new UpdateTreatment(publicationService);
    Transaction.getTransaction().perform(() -> {
      updateTreatment.execute(pk);
      updateTreatment.execute(pk);
      return null;
    });
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(2));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  @DisplayName("Update publication by forcing last update data")
  @Test
  public void updateByForcingLastUpdateData() throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    new UpdateTreatment(publicationService).withLastUpdateDateForcedWith(
        java.sql.Date.valueOf("2050-01-01")).execute(pk);
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(1));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  @DisplayName("Update publication by forcing last update data and indicating also no update of " +
      "last update data")
  @Test
  public void updateByForcingLastUpdateDataAndIndicatingAlsoNoUpdateOfLastUpdateData()
      throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    new UpdateTreatment(publicationService).withLastUpdateDateForcedWith(
        java.sql.Date.valueOf("2050-01-01")).noUpdateOfLastUpdateData().execute(pk);
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(1));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  @DisplayName("Update publication by indicating also no update of last update data")
  @Test
  public void updateByIndicatingAlsoNoUpdateOfLastUpdateData() throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    new UpdateTreatment(publicationService).noUpdateOfLastUpdateData().execute(pk);
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(1));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  @DisplayName("Update publication by simulating a move operation")
  @Test
  public void updateBySimulatingMoveOperation() throws Exception {
    PublicationPK pk = new PublicationPK("100", "kmelia200");
    final PublicationDetail before = publicationService.getDetail(pk);
    assertBeforeUpdateData(before);
    new UpdateTreatment(publicationService).simulatingMoveOperation().execute(pk);
    assertThat(DataBeforeUpdate.get().getBeforesFromEvents(), hasSize(1));
    DataBeforeUpdate.get().getBeforesFromEvents().forEach(this::assertBeforeUpdateData);
  }

  private static class UpdateTreatment {
    private final PublicationService service;
    private boolean noUpdateDataModification = false;
    private Date lastUpdateDateForced = null;
    private ResourceEvent.Type eventType = ResourceEvent.Type.UPDATE;
    private String moveOnInstanceId = null;

    private UpdateTreatment(final PublicationService service) {
      this.service = service;
    }

    UpdateTreatment withLastUpdateDateForcedWith(Date lastUpdateDate) {
      lastUpdateDateForced = lastUpdateDate;
      return this;
    }

    UpdateTreatment noUpdateOfLastUpdateData() {
      noUpdateDataModification = true;
      return this;
    }

    UpdateTreatment simulatingMoveOperation() {
      moveOnInstanceId = "kmelia300";
      eventType = ResourceEvent.Type.MOVE;
      return this;
    }

    void execute(final PublicationPK pk) {
      Transaction.getTransaction().perform(() -> {
        perform(pk);
        return null;
      });
    }

    private void perform(final PublicationPK pk) {
      final PublicationDetail detail = service.getDetail(pk);
      Calendar now = Calendar.getInstance();
      now.set(Calendar.SECOND, 0);
      now.set(Calendar.MILLISECOND, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.HOUR_OF_DAY, 0);
      Calendar lastUpdateDate = RandomGenerator.getCalendarAfter(now);
      Calendar beginDate = RandomGenerator.getCalendarAfter(now);
      Calendar endDate = RandomGenerator.getCalendarAfter(beginDate);
      String name = RandomGenerator.getRandomString();
      String description = RandomGenerator.getRandomString();
      String creatorId = "" + RandomGenerator.getRandomInt();
      int importance = RandomGenerator.getRandomInt(5);
      String version = RandomGenerator.getRandomString();
      String contenu = RandomGenerator.getRandomString();
      StringBuilder buffer = new StringBuilder();
      int nbKeywords = RandomGenerator.getRandomInt(5) + 2;
      for (int i = 0; i < nbKeywords; i++) {
        buffer.append(RandomGenerator.getRandomString());
        if (i < (nbKeywords - 1)) {
          buffer.append(' ');
        }
      }
      String keywords = buffer.toString();
      detail.setUpdateDataMustBeSet(!noUpdateDataModification);
      detail.setName(name);
      detail.setDescription(description);
      detail.setCreationDate(now.getTime());
      detail.setUpdateDate(
          Objects.requireNonNullElseGet(lastUpdateDateForced, lastUpdateDate::getTime));
      detail.setUpdaterId("38");
      detail.setBeginDate(beginDate.getTime());
      detail.setEndDate(endDate.getTime());
      detail.setCreatorId(creatorId);
      final int oldImportance = detail.getImportance();
      detail.setImportance(importance);
      detail.setVersion(version);
      detail.setKeywords(keywords);
      detail.setContentPagePath(contenu);
      detail.setBeginHour(DateUtil.formatTime(beginDate));
      detail.setEndHour(DateUtil.formatTime(endDate));
      if (moveOnInstanceId != null) {
        detail.getPK().setComponentName(moveOnInstanceId);
      }
      service.setDetail(detail, lastUpdateDateForced != null, eventType);
      PublicationDetail result = service.getDetail(pk);
      detail.setInfoId("0");
      assertEquals("100", result.getPK().getId());
      if (moveOnInstanceId != null) {
        assertNotSame("kmelia200", moveOnInstanceId);
        assertEquals(moveOnInstanceId, result.getPK().getInstanceId());
      } else {
        assertEquals("kmelia200", result.getPK().getInstanceId());
      }
      assertEquals(detail.getAuthor(), result.getAuthor());
      assertEquals(detail.getBeginDate(), result.getBeginDate());
      assertEquals(detail.getBeginHour(), result.getBeginHour());
      assertEquals(detail.getContentPagePath(), result.getContentPagePath());
      assertEquals(detail.getCreationDate(), result.getCreationDate());
      assertEquals(detail.getCreatorId(), result.getCreatorId());
      if (noUpdateDataModification) {
        assertEquals("2009/11/18", DateUtil.formatDate(result.getLastUpdateDate()));
        assertEquals("200", result.getUpdaterId());
      } else {
        if (lastUpdateDateForced != null) {
          assertEquals(detail.getLastUpdateDate(), result.getLastUpdateDate());
          assertEquals(lastUpdateDateForced, result.getLastUpdateDate());
        } else {
          assertEquals(detail.getCreationDate(), result.getLastUpdateDate());
        }
        assertEquals("38", result.getUpdaterId());
      }
      assertEquals(detail.getDescription(), result.getDescription());
      assertEquals(detail.getEndDate(), result.getEndDate());
      assertEquals(detail.getEndHour(), result.getEndHour());
      if (detail.getImportance() == 0) {
        assertEquals(oldImportance, result.getImportance());
      } else {
        assertEquals(detail.getImportance(), result.getImportance());
      }
      assertEquals(detail.getInfoId(), result.getInfoId());
      assertEquals(detail.getKeywords(), result.getKeywords());
      assertEquals(detail.getName(), result.getName());
      assertEquals(detail.getStatus(), result.getStatus());
      assertEquals(detail.getTitle(), result.getTitle());
    }
  }

  private void assertBeforeUpdateData(final PublicationDetail data) {
    assertEquals("100", data.getPK().getId());
    assertEquals("kmelia200", data.getPK().getInstanceId());
    assertEquals("Homer Simpson", data.getAuthor());
    assertEquals("2009/10/18", DateUtil.formatDate(data.getBeginDate()));
    assertEquals("00:00", data.getBeginHour());
    assertEquals("Contenu de la publication 1", data.getContentPagePath());
    assertEquals("2008/11/18", DateUtil.formatDate(data.getCreationDate()));
    assertEquals("100", data.getCreatorId());
    assertEquals("2009/11/18", DateUtil.formatDate(data.getLastUpdateDate()));
    assertEquals("200", data.getUpdaterId());
    assertEquals("Première publication de test", data.getDescription());
    assertEquals("2120/12/18", DateUtil.formatDate(data.getEndDate()));
    assertEquals("23:59", data.getEndHour());
    assertEquals(1, data.getImportance());
    assertEquals("0", data.getInfoId());
    assertEquals("kmelia200", data.getInstanceId());
    assertEquals("test", data.getKeywords());
    assertEquals("Publication 1", data.getName());
    assertEquals("Valid", data.getStatus());
    assertEquals("300", data.getValidatorId());
    assertEquals("Publication 1", data.getTitle());
  }

  @Singleton
  @Service
  public static class DataBeforeUpdate {

    private final List<PublicationDetail> beforesFromEvents = new ArrayList<>();

    public static DataBeforeUpdate get() {
      return ServiceProvider.getService(DataBeforeUpdate.class);
    }

    public void clear() {
      beforesFromEvents.clear();
    }

    public List<PublicationDetail> getBeforesFromEvents() {
      return beforesFromEvents;
    }

    public void addBeforeFromEvent(final PublicationDetail beforeFromEvent) {
      this.beforesFromEvents.add(beforeFromEvent);
    }
  }

  @Service
  public static class PublicationEvents implements ContributionModification, ContributionMove {

    @Override
    public void update(final Contribution before, final Contribution after) {
      before(before);
    }

    @Override
    public void move(final Contribution before, final Contribution after) {
      before(before);
    }

    private void before(final Contribution before) {
      DataBeforeUpdate.get()
          .addBeforeFromEvent(Optional.of(before)
              .filter(PublicationDetail.class::isInstance)
              .map(PublicationDetail.class::cast)
              .orElse(null));
    }
  }
}