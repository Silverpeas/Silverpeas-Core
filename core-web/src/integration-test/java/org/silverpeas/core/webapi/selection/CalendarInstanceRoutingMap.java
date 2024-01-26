package org.silverpeas.core.webapi.selection;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.web.calendar.AbstractCalendarInstanceRoutingMap;

import javax.inject.Named;

/**
 * Required by the tests as they use {@link org.silverpeas.core.calendar.CalendarEvent} to compute
 * their permalinks (and avoid stack overflow with the use of the default implementation of the
 * {@link org.silverpeas.core.web.mvc.route.AbstractComponentInstanceRoutingMap} which invokes the
 * {@link WithPermanentLink#getPermalink()} of the objects themselves).
 */
@Technical
@Bean
@Named
public class CalendarInstanceRoutingMap extends AbstractCalendarInstanceRoutingMap {
}
