package org.silverpeas.core.scheduler.trigger;

import java.text.ParseException;

/**
 * A factory to build {@code CronExpression} instances whose
 * implementation is provided by the scheduling engine used as backend of the Silverpeas
 * Scheduler API.
 */
public interface CronExpressionFactory {

  /**
   * Creates a CronExpression instance from a String representation of a cron expression.
   * @param expression a cron expression.
   * @return a CronExpression instance.
   * @throws ParseException if the specified cron expression isn't well formatted.
   */
  CronExpression create(String expression) throws ParseException;
}
