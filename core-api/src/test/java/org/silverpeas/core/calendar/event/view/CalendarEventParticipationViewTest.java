package org.silverpeas.core.calendar.event.view;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.event.CalendarEventOccurrence;
import org.silverpeas.core.calendar.event.TestCalendarEventOccurrenceBuilder;
import org.silverpeas.core.test.TestUserProvider;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit test on the view applying on a list of calendar event occurrences.
 * @author mmoquillon
 */
public class CalendarEventParticipationViewTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();
  private CalendarEventParticipationView view = new CalendarEventParticipationView();

  @Before
  public void setUpUsers() {
    UserProvider userProvider = TestUserProvider.withoutCurrentRequester();
    commonAPI4Test.injectIntoMockedBeanContainer(userProvider);
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
        if (occurrences.get(i)
            .getStartDateTime()
            .isAfter(occurrences.get(i + 1).getStartDateTime())) {
          return false;
        }
      } else if (result > 0) {
        return false;
      }
    }
    return true;
  }

}