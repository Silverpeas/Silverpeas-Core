/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.persistence.datasource.PersistOperation;
import org.silverpeas.core.persistence.datasource.UpdateOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.TestBeanContainer;

import javax.enterprise.util.AnnotationLiteral;

import static java.time.LocalDate.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.ACCEPTED;
import static org.silverpeas.core.calendar.Attendee.ParticipationStatus.AWAITING;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.INFORMATIVE;
import static org.silverpeas.core.calendar.Attendee.PresenceStatus.REQUIRED;
import static org.silverpeas.core.calendar.CalendarComponentDiffDescriptor.diffBetween;

/**
 * @author Yohann Chastagnier
 */
public class CalendarComponentDiffDescriptorTest {

  private static final String TITLE_BEFORE = "TITLE BEFORE";

  @BeforeEach
  public void prepareInjection() {
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(JpaPersistOperation.class, new AnnotationLiteral<PersistOperation>() {
        })).thenReturn(new JpaPersistOperation());
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(JpaUpdateOperation.class, new AnnotationLiteral<UpdateOperation>() {
        })).thenReturn(new JpaUpdateOperation());
  }

  @Test
  public void nullOrEmptyValuesShouldNotThrowError() {
    CalendarComponent componentBase = createComponentToMergeDiffInto();
    componentBase.setPeriod(Period.between(parse("2017-04-12"), parse("2017-04-13")));
    CalendarComponent modifiedComponent = clone(componentBase);
    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(false));
  }

  @Test
  public void noChangeShouldMergeNothing() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("DESCRIPTION BEFORE");
    componentBase.setLocation("LOCATION BEFORE");
    componentBase.setPriority(Priority.NORMAL);
    CalendarComponent modifiedComponent = clone(componentBase);
    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(false));
  }

  @Test
  public void titleChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("DESCRIPTION BEFORE");
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void descriptionChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void descriptionEmptyChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setDescription("A DESCRIPTION");
    CalendarComponent modifiedComponent = clone(componentBase);
    modifiedComponent.setDescription(null);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.setDescription("A DESCRIPTION");
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getTitle(), nullValue());
    assertThat(mergedComponent.getDescription(), isEmptyString());
  }

  @Test
  public void locationChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setLocation("LOCATION BEFORE");
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void priorityChangeShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.setPriority(Priority.NORMAL);
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void oneAttributeAddShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void oneAttributeUpdateShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_A", "VALUE A BEFORE");
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void oneAttributeRemoveShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void severalAttributeChangesShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttributes().set("ATTR_NOT_CHANGED", "VALUE NOT CHANGED");
    componentBase.getAttributes().set("ATTR_UPDATE", "VALUE TO UPDATE");
    componentBase.getAttributes().set("ATTR_REMOVE", "VALUE TO REMOVE");
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void oneAttendeeAddShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    CalendarComponent modifiedComponent = clone(componentBase);
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
    assertThat(attendeeAdded.getId(), is("ATTENDEE_ID"));
  }

  @Test
  public void oneAttendeeUpdateShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = clone(componentBase);
    modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null).setPresenceStatus(INFORMATIVE);

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    assertThat(mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null).getPresenceStatus(),
        is(REQUIRED));
    boolean dataMerged = diff.mergeInto(mergedComponent);

    assertThat(dataMerged, is(true));
    assertThat(mergedComponent.getDescription(), nullValue());
    assertThat(mergedComponent.getAttributes().getData().size(), is(0));
    assertThat(mergedComponent.getAttendees().size(), is(1));
    final Attendee attendeeUpdated = mergedComponent.getAttendees().get("ATTENDEE_ID").orElse(null);
    assertThat(attendeeUpdated.getPresenceStatus(), is(INFORMATIVE));
  }

  @Test
  public void oneAttendeeRemoveShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    CalendarComponent modifiedComponent = clone(componentBase);
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
  public void severalAttendeeChangesShouldBeMerged() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_NOT_CHANGED"));
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_UPDATE"));
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_REMOVE"));
    CalendarComponent modifiedComponent = clone(componentBase);
    modifiedComponent.getAttendees().get("ATTENDEE_ID_TO_UPDATE").orElse(null)
        .setPresenceStatus(INFORMATIVE);
    modifiedComponent.getAttendees().removeIf(a -> "ATTENDEE_ID_TO_REMOVE".equals(a.getId()));
    modifiedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_ADD"));

    CalendarComponentDiffDescriptor diff = diffBetween(modifiedComponent, componentBase);
    assertThat(diff.existsDiff(), is(true));

    CalendarComponent mergedComponent = createComponentToMergeDiffInto();
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_OTHER"));
    mergedComponent.getAttendees().add(new AttendeeTest("ATTENDEE_ID_TO_UPDATE"));
    assertThat(mergedComponent.getAttendees().get("ATTENDEE_ID_TO_UPDATE").orElse(null)
        .getPresenceStatus(), is(REQUIRED));
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
    assertThat(attendeeOther.getId(), is("ATTENDEE_ID_OTHER"));
    assertThat(attendeeAdded.getId(), is("ATTENDEE_ID_TO_ADD"));
    assertThat(attendeeUpdated.getId(), is("ATTENDEE_ID_TO_UPDATE"));
    assertThat(attendeeUpdated.getPresenceStatus(), is(INFORMATIVE));
  }

  @Test
  public void attendeeStatusChangeShouldBeMergedIfAttendeeExists() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = clone(componentBase);
    modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null)
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
    assertThat(attendee.getParticipationStatus(), is(ACCEPTED));
  }

  @Test
  public void attendeeStatusChangeShouldNotBeMergedIfAttendeeDoesNotExist() {
    CalendarComponent componentBase = initComponent();
    componentBase.getAttendees().add(new AttendeeTest("ATTENDEE_ID"));
    CalendarComponent modifiedComponent = clone(componentBase);
    modifiedComponent.getAttendees().get("ATTENDEE_ID").orElse(null)
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

  private CalendarComponent clone(CalendarComponent component) {
    return component.clone();
  }

  private static class AttendeeTest extends Attendee {

    AttendeeTest(String id) {
      super(id, null);
    }

    @Override
    public String getFullName() {
      return null;
    }
  }
}