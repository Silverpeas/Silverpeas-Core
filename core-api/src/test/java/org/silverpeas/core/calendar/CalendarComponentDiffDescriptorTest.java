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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.PersistOperation;
import org.silverpeas.core.persistence.datasource.UpdateOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.kernel.TestManagedBeanFeeder;

import javax.enterprise.util.AnnotationLiteral;
import java.util.Objects;

import static java.time.LocalDate.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.ACCEPTED;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.AWAITING;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.INFORMATIVE;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.REQUIRED;
import static org.silverpeas.core.calendar.CalendarComponentDiffDescriptor.diffBetween;

/**
 * @author Yohann Chastagnier
 */
class CalendarComponentDiffDescriptorTest {

  private static final String TITLE_BEFORE = "TITLE BEFORE";

  @BeforeEach
  public void prepareInjection() {
    var feeder = new TestManagedBeanFeeder();
    feeder.manageBean(new JpaPersistOperation(), JpaPersistOperation.class,
        new AnnotationLiteral<PersistOperation>() {
        });
    feeder.manageBean(new JpaUpdateOperation(), JpaUpdateOperation.class,
        new AnnotationLiteral<UpdateOperation>() {
        });
  }

  @AfterEach
  public void cleanBeanContainer() {
    TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();
    feeder.removeAllManagedBeans();
  }

  @Test
  void nullOrEmptyValuesShouldNotThrowError() {
    CalendarComponent componentBase = createComponentToMergeDiffInto();
    componentBase.setPeriod(Period.between(parse("2017-04-12"), parse("2017-04-13")));
    CalendarComponent modifiedComponent = copy(componentBase);
    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(false));
  }

  @Test
  void noChangeShouldMergeNothing() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("DESCRIPTION BEFORE");
    componentBase.setLocation("LOCATION BEFORE");
    componentBase.setPriority(Priority.NORMAL);
    CalendarComponent modifiedComponent = copy(componentBase);
    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(false));
  }

  @Test
  void titleChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("DESCRIPTION BEFORE");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.setTitle("TITLE AFTER");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getTitle(), is("TITLE AFTER"));
    assertThat(mergedComponent.getDescription(), nullValue());
  }

  @Test
  void descriptionChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.setDescription("A DESCRIPTION");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getTitle(), nullValue());
    assertThat(mergedComponent.getDescription(), is("A DESCRIPTION"));
  }

  @Test
  void descriptionEmptyChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("A DESCRIPTION");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.setDescription(null);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.setDescription("A DESCRIPTION");
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getTitle(), nullValue());
    assertThat(mergedComponent.getDescription(), is(emptyString()));
  }

  @Test
  void locationChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setLocation("LOCATION BEFORE");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.setLocation("LOCATION AFTER");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getLocation(), is("LOCATION AFTER"));
  }

  @Test
  void priorityChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setPriority(Priority.NORMAL);
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.setPriority(Priority.HIGH);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getPriority(), is(Priority.HIGH));
  }

  @Test
  void oneAttributeAddShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttributes().set("ATTR_ADD", "ADDED VALUE");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(1));
    assertThat(mergedComponent.getAttributes().get("ATTR_ADD").orElse(null), is("ADDED VALUE"));
    assertThat(mergedComponent.getAttendees().size(), is(0));
  }

  @Test
  void oneAttributeUpdateShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_A", "VALUE A BEFORE");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttributes().set("ATTR_A", "VALUE A AFTER");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttributes().set("ATTR_A", "VALUE A BEFORE");
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(1));
    assertThat(mergedComponent.getAttributes().get("ATTR_A").orElse(null), is("VALUE A AFTER"));
    assertThat(mergedComponent.getAttendees().size(), is(0));
  }

  @Test
  void oneAttributeRemoveShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttributes().remove("ATTR_REMOVE");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    assertThat(mergedComponent.getAttributes().getData().size(), is(1));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(0));
  }

  @Test
  void severalAttributeChangesShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_NOT_CHANGED", "VALUE NOT CHANGED");
    componentBase.getAttributes().set("ATTR_UPDATE", "VALUE TO UPDATE");
    componentBase.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttributes().remove("ATTR_REMOVE");
    modifiedComponent.getAttributes().set("ATTR_UPDATE", "UPDATED VALUE");
    modifiedComponent.getAttributes().set("ATTR_ADD", "ADDED VALUE");

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    mergedComponent.getAttributes().set("ATTR_UPDATE", "VALUE TO UPDATE");
    mergedComponent.getAttributes().set("ATTR_OTHER", "OTHER VALUE");
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(3));
    assertThat(mergedComponent.getAttributes().get("ATTR_OTHER").orElse(null), is("OTHER VALUE"));
    assertThat(mergedComponent.getAttributes().get("ATTR_UPDATE").orElse(null),
        is("UPDATED VALUE"));
    assertThat(mergedComponent.getAttributes().get("ATTR_ADD").orElse(null), is("ADDED VALUE"));
    assertThat(mergedComponent.getAttendees().size(), is(0));
  }

  @Test
  void oneAttendeeAddShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    final Attendee attendeeAdded = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendeeAdded, is(notNullValue()));
    assertThat(attendeeAdded.getId(), is("ATTENDEE_ID"));
  }

  @Test
  void oneAttendeeUpdateShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = copy(componentBase);
    Objects.requireNonNull(modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null))
        .setPresenceStatus(INFORMATIVE);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    Attendee attendee = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendee, is(notNullValue()));
    assertThat(attendee.getPresenceStatus(), is(REQUIRED));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    final Attendee attendeeUpdated = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendeeUpdated, is(notNullValue()));
    assertThat(attendeeUpdated.getPresenceStatus(), is(INFORMATIVE));
  }

  @Test
  void oneAttendeeRemoveShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    CalendarComponent modifiedComponent = copy(componentBase);
    modifiedComponent.getAttendees().removeIf(a -> "ATTENDEE_ID_TO_REMOVE".equals(a.getId()));

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(0));
  }

  @Test
  void severalAttendeeChangesShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_NOT_CHANGED"));
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_UPDATE"));
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    CalendarComponent modifiedComponent = copy(componentBase);
    Objects.requireNonNull(modifiedComponent.getAttendees().get("ATTENDEE_ID_TO_UPDATE").orElse(null))
        .setPresenceStatus(INFORMATIVE);
    modifiedComponent.getAttendees().removeIf(a -> "ATTENDEE_ID_TO_REMOVE".equals(a.getId()));
    modifiedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_ADD"));

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_OTHER"));
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_UPDATE"));
    Attendee attendee = mergedComponent.getAttendees().get("ATTENDEE_ID_TO_UPDATE").orElse(null);
    assertThat(attendee, is(notNullValue()));
    assertThat(attendee.getPresenceStatus(), is(REQUIRED));
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    assertThat(mergedComponent.getAttendees().size(), is(3));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(3));
    final Attendee attendeeOther =
        mergedComponent.getAttendees().get("ATTENDEE_ID_OTHER").orElse(null);
    final Attendee attendeeAdded =
        mergedComponent.getAttendees().get("ATTENDEE_ID_TO_ADD").orElse(null);
    final Attendee attendeeUpdated =
        mergedComponent.getAttendees().get("ATTENDEE_ID_TO_UPDATE").orElse(null);
    assertThat(attendeeOther, is(notNullValue()));
    assertThat(attendeeOther.getId(), is("ATTENDEE_ID_OTHER"));
    assertThat(attendeeAdded, is(notNullValue()));
    assertThat(attendeeAdded.getId(), is("ATTENDEE_ID_TO_ADD"));
    assertThat(attendeeUpdated, is(notNullValue()));
    assertThat(attendeeUpdated.getId(), is("ATTENDEE_ID_TO_UPDATE"));
    assertThat(attendeeUpdated.getPresenceStatus(), is(INFORMATIVE));
  }

  @Test
  void attendeeStatusChangeShouldBeMergedIfAttendeeExists() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = copy(componentBase);
    Objects.requireNonNull(modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null))
        .setParticipationStatus(ACCEPTED);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    final Attendee attendee = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendee, is(notNullValue()));
    assertThat(attendee.getParticipationStatus(), is(ACCEPTED));
  }

  @Test
  void attendeeStatusChangeShouldNotBeMergedIfAttendeeDoesNotExist() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = copy(componentBase);
    Objects.requireNonNull(modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null))
        .setParticipationStatus(ACCEPTED);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_OTHER_ID"));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(false));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    final Attendee attendee = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendee, nullValue());
    final Attendee attendeeOther =
        mergedComponent.getAttendees().get("ATTENDEE_OTHER_ID").orElse(null);
    assertThat(attendeeOther, is(notNullValue()));
    assertThat(attendeeOther.getParticipationStatus(), is(AWAITING));
  }

  private CalendarComponent createComponentToMergeDiffInto() {
    return new CalendarComponent();
  }

  private CalendarComponent initComponent() {
    CalendarComponent component = createComponentToMergeDiffInto();
    component.setTitle(TITLE_BEFORE);
    component.setPeriod(Period.between(parse("2017-04-12"), parse("2017-04-13")));
    return component;
  }

  private CalendarComponent copy(CalendarComponent component) {
    return component.copy();
  }

  private static class AttendeeTest extends Attendee {

    @SuppressWarnings("unused")
    protected AttendeeTest() {
      // used in Attendee#copyFor
    }

    AttendeeTest(String id) {
      super(id, null);
    }

    @Override
    public String getFullName() {
      return null;
    }
  }
}