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
package org.silverpeas.core.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.test.TestBeanContainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.*;

/**
 * By default, an event is built to be private, without participant and linked to a not accessible
 * calendar.
 * @author silveryocha
 */
@Execution(ExecutionMode.SAME_THREAD)
public class CalendarEventAccessTest {

  private static final String USER_TEST_ID = "26";
  private User userTest;
  private Calendar calendar;
  private CalendarEventStubBuilder eventBuilder;
  private SilverpeasComponentInstanceProvider componentInstanceProvider;

  @BeforeEach
  public void setup() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    userTest = aUser();
    when(userTest.getId()).thenReturn(USER_TEST_ID);
    calendar = mock(Calendar.class);
    when(calendar.getComponentInstanceId()).thenReturn("calendarApp32");
    eventBuilder = CalendarEventStubBuilder
        .from(Period.between(LocalDate.of(2017, 8, 25), LocalDate.of(2017, 8, 26)));
    eventBuilder.plannedOn(calendar);
    eventBuilder.withVisibilityLevel(VisibilityLevel.PRIVATE);

    componentInstanceProvider = mock(SilverpeasComponentInstanceProvider.class);
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(SilverpeasComponentInstanceProvider.class)).thenReturn(
        componentInstanceProvider);
    when(componentInstanceProvider.getById(anyString())).thenReturn(Optional.empty());
  }

  /*
  CAN BE ACCESSED
   */

  @Test
  public void notAccessibleWithDefaultTestEvent() {
    assertThatEventIsNotAccessible();
  }

  @Test
  public void accessibleWhenCalendarIsAccessible() {
    calendarCanBeAccessed();
    assertThatEventIsAccessible();
  }

  @Test
  public void accessibleWhenUserIsParticipant() {
    eventBuilder.withAttendee(userTest, attendee -> {
    });
    assertThatEventIsAccessible();
  }

  @Test
  public void notAccessibleWhenVisibilityIsPublicButNoComponentInstance() {
    eventBuilder.withVisibilityLevel(VisibilityLevel.PUBLIC);
    assertThatEventIsNotAccessible();
  }

  @Test
  public void notAccessibleWhenVisibilityIsPublicButComponentInstanceIsNotPersonalOrPublic() {
    onPrivateComponentInstance();
    eventBuilder.withVisibilityLevel(VisibilityLevel.PUBLIC);
    assertThatEventIsNotAccessible();
  }

  @Test
  public void accessibleWhenVisibilityIsPublicAndComponentInstanceIsPersonal() {
    onPersonalComponentInstance();
    eventBuilder.withVisibilityLevel(VisibilityLevel.PUBLIC);
    assertThatEventIsAccessible();
  }

  @Test
  public void accessibleWhenVisibilityIsPublicAndComponentInstanceIsPublic() {
    onPublicComponentInstance();
    eventBuilder.withVisibilityLevel(VisibilityLevel.PUBLIC);
    assertThatEventIsAccessible();
  }

  @Test
  public void notAccessibleWhenVisibilityIsPrivateAndComponentInstanceIsPublic() {
    onPublicComponentInstance();
    assertThatEventIsNotAccessible();
  }

  /*
  CAN BE MODIFIED
   */

  @Test
  public void notModifiableWithDefaultTestEvent() {
    assertThatEventIsNotModifiable();
  }

  @Test
  public void notModifiableWhenCalendarIsAccessibleButNoUserRole() {
    calendarCanBeAccessed();
    onPrivateComponentInstance();

    assertThatEventIsNotModifiable();
  }

  @Test
  public void notModifiableWhenCalendarIsAccessibleAndUserRoleIsReader() {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(USER);

    assertThatEventIsNotModifiable();
  }

  @Test
  public void modifiableWhenCalendarIsAccessibleAndUserRoleIsAdmin() {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(ADMIN);

    assertThatEventIsModifiable();
  }

  @Test
  public void notModifiableWhenCalendarIsAccessibleButSynchronizedAndUserRoleIsAdmin()
      throws MalformedURLException {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(ADMIN);

    when(calendar.getExternalCalendarUrl()).thenReturn(new URL("http://silverpeas.org"));
    assertThatEventIsNotModifiable();
  }

  @Test
  public void modifiableWhenCalendarIsAccessibleAndUserRoleIsPublisher() {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(PUBLISHER);

    assertThatEventIsModifiable();
  }

  @Test
  public void notModifiableWhenCalendarIsAccessibleAndUserRoleIsWriterAndUserIsNotEventCreator() {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(WRITER);

    eventBuilder.withCreator(aUser());
    assertThatEventIsNotModifiable();
  }

  @Test
  public void modifiableWhenCalendarIsAccessibleAndUserRoleIsWriterAndUserIsEventCreator() {
    calendarCanBeAccessed();
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(WRITER);

    eventBuilder.withCreator(userTest);
    assertThatEventIsModifiable();
  }

  @Test
  public void notModifiableWhenCalendarIsNotAccessibleAndUserRoleIsWriterAndUserIsEventCreator() {
    SilverpeasComponentInstance componentInstance = onPrivateComponentInstance();
    when(componentInstance.getHighestSilverpeasRolesFor(userTest)).thenReturn(WRITER);

    eventBuilder.withCreator(userTest);
    assertThatEventIsNotModifiable();
  }

  /*
  COMMON UNIT TEST TOOLS
   */

  private void assertThatEventIsAccessible() {
    CalendarEvent event = eventBuilder.build();
    assertThat(event.canBeAccessedBy(userTest), is(true));
  }

  private void assertThatEventIsNotAccessible() {
    CalendarEvent event = eventBuilder.build();
    assertThat(event.canBeAccessedBy(userTest), is(false));
  }

  private void assertThatEventIsNotModifiable() {
    CalendarEvent event = eventBuilder.build();
    assertThat(event.canBeModifiedBy(userTest), is(false));
  }

  private void assertThatEventIsModifiable() {
    CalendarEvent event = eventBuilder.build();
    assertThat(event.canBeModifiedBy(userTest), is(true));
  }

  private SilverpeasComponentInstance onPrivateComponentInstance() {
    SilverpeasComponentInstance componentInstance = mock(SilverpeasComponentInstance.class);
    when(componentInstance.isPublic()).thenReturn(false);
    when(componentInstance.isPersonal()).thenReturn(false);
    when(componentInstanceProvider.getById(anyString())).thenReturn(of(componentInstance));
    return componentInstance;
  }

  private void onPersonalComponentInstance() {
    SilverpeasComponentInstance componentInstance = mock(SilverpeasComponentInstance.class);
    when(componentInstance.isPublic()).thenReturn(false);
    when(componentInstance.isPersonal()).thenReturn(true);
    when(componentInstanceProvider.getById(anyString())).thenReturn(of(componentInstance));
  }

  private void onPublicComponentInstance() {
    SilverpeasComponentInstance componentInstance = mock(SilverpeasComponentInstance.class);
    when(componentInstance.isPublic()).thenReturn(true);
    when(componentInstance.isPersonal()).thenReturn(false);
    when(componentInstanceProvider.getById(anyString())).thenReturn(of(componentInstance));
  }

  private void calendarCanBeAccessed() {
    when(calendar.canBeAccessedBy(any(User.class))).thenReturn(true);
  }

  private User aUser() {
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    User aUser = mock(User.class);
    when(aUser.getId()).thenReturn(String.valueOf(System.currentTimeMillis()));
    return aUser;
  }
}
