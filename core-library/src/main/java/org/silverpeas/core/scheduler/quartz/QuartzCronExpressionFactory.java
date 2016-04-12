/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.scheduler.quartz;

import org.silverpeas.core.scheduler.trigger.CronExpression;
import org.silverpeas.core.scheduler.trigger.CronExpressionFactory;

import java.text.ParseException;

/**
 * Implementation of the CronExpressionFactory for the Quartz engine.
 */
public class QuartzCronExpressionFactory implements CronExpressionFactory {
  /**
   * Creates a CronExpression instance from a String representation of a cron expression.
   * @param expression a cron expression.
   * @return a CronExpression instance.
   * @throws ParseException if the specified cron expression isn't well formatted.
   */
  @Override
  public CronExpression create(final String expression) throws ParseException {
    return new QuartzCronExpression("0 " + expression);
  }
}
