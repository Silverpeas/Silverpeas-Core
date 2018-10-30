package org.silverpeas.core.calendar.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.calendar.TestCalendarEventOccurrenceBuilder;
import org.silverpeas.core.persistence.datasource.PersistOperation;
import org.silverpeas.core.persistence.datasource.UpdateOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.TestUserProvider;

import javax.enterprise.util.AnnotationLiteral;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;

/**
 * Unit test on the view applying on a list of calendar event occurrences.
 * @author mmoquillon
 */
public class CalendarEventInternalParticipationViewTest {

  private CalendarEventInternalParticipationView view = new CalendarEventInternalParticipationView();

  @BeforeEach
  public void setUpUsers() {
    UserProvider userProvider = TestUserProvider.withoutCurrentRequester();
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(UserProvider.class)).thenReturn(
        userProvider);
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(JpaPersistOperation.class, new AnnotationLiteral<PersistOperation>() {
        })).thenReturn(new JpaPersistOperation());
    when(TestBeanContainer.getMockedBeanContainer()
        .getBeanByType(JpaUpdateOperation.class, new AnnotationLiteral<UpdateOperation>() {
        })).thenReturn(new JpaUpdateOperation());
  }

  @Test
  public void applyOnAnEmptyListShouldDoesNothing() {
    Map<String, List<CalendarEventOccurrence>> byUser = view.apply(new ArrayList<>());
    assertThat(byUser.isEmpty(), is(true));
  }

  @Test
  public void applyOnAListWithOccurrences() {
    List<CalendarEventOccurrence> occurrences = TestCalendarEventOccurrenceBuilder.build();
    Map<String, List<CalendarEventOccurrence>> byUser = view.apply(occurrences);

    assertThat(byUser.isEmpty(), is(false));
    for (Map.Entry<String, List<CalendarEventOccurrence>> entry : byUser.entrySet()) {
      List<CalendarEventOccurrence> ocs;
      switch (entry.getKey()) {
        case "0":
          ocs = entry.getValue();
          assertThat(ocs.size(), is(5));
          assertThat(isSorted(ocs), is(true));
          break;
        case "1":
          ocs = entry.getValue();
          assertThat(ocs.size(), is(4));
          assertThat(isSorted(ocs), is(true));
          break;
        case "2":
          ocs = entry.getValue();
          assertThat(ocs.size(), is(3));
          assertThat(isSorted(ocs), is(true));
          break;
        default:
          fail("Unknown user with id " + entry.getKey());
      }
    }
  }

  private boolean isSorted(final List<CalendarEventOccurrence> occurrences) {
    for (int i = 0; i < occurrences.size() - 1; i++) {
      int result = occurrences.get(i)
          .getCalendarEvent()
          .getCalendar()
          .getId()
          .compareTo(occurrences.get(i + 1).getCalendarEvent().getCalendar().getId());
      if (result == 0) {
         if (asOffsetDateTime(occurrences.get(i).getStartDate())
            .isAfter(asOffsetDateTime(occurrences.get(i + 1).getStartDate()))) {
          return false;
        }
      } else if (result > 0) {
        return false;
      }
    }
    return true;
  }

}