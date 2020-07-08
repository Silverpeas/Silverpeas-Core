/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.calendar;

import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.SQLException;
import java.util.List;

/**
 * @author silveryocha
 */
@Service
public class CalendarIntegrityProcessor implements Initialization {

  @Override
  public void init() throws Exception {
    BackgroundProcessTask.push(new DeletedUserCalendarCleaner());
  }

  private static class DeletedUserCalendarCleaner extends AbstractBackgroundProcessRequest {

    private static final String COMPONENT_INSTANCE_FROM_USER =
        "CONCAT('userCalendar' , CONCAT(CAST(u.id AS VARCHAR(20)), '_PCI'))";

    @Override
    protected void process() {
      try {
        final List<String> componentInstanceIdsToClear = JdbcSqlQuery
            .createSelect("distinct c.instanceid")
            .from("st_user u")
            .join("sb_cal_calendar c").on("c.instanceid = " + COMPONENT_INSTANCE_FROM_USER)
            .where("u.state = ?", UserState.DELETED.name())
            .execute(r -> r.getString(1));
        if (!componentInstanceIdsToClear.isEmpty()) {
          Transaction.performInOne(() -> {
            Calendar.getByComponentInstanceIds(componentInstanceIdsToClear).forEach(Calendar::delete);
            return null;
          });
        }
      } catch (SQLException e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }
}
