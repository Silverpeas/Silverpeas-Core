/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.jcr.impl.oak.factories;

import org.apache.jackrabbit.oak.spi.gc.GCMonitor;
import org.silverpeas.core.util.logging.SilverLogger;

import static org.silverpeas.core.util.UnitUtil.formatMemSize;
import static org.slf4j.helpers.MessageFormatter.arrayFormat;

/**
 * @author silveryocha
 */
public class GCLogger implements GCMonitor {

  private final SilverLogger logger = SilverLogger.getLogger(this);

  @Override
  public void info(final String message, final Object... arguments) {
    logger.info(arrayFormat(message, arguments).getMessage());
  }

  @Override
  public void warn(final String message, final Object... arguments) {
    logger.warn(arrayFormat(message, arguments).getMessage());
  }

  @Override
  public void error(final String message, final Exception exception) {
    logger.error(message, exception);
  }

  @Override
  public void skipped(final String reason, final Object... arguments) {
    logger.info(arrayFormat(reason, arguments).getMessage());
  }

  @Override
  public void compacted() {
    logger.info("File store compacted");
  }

  @Override
  public void cleaned(final long reclaimedSize, final long currentSize) {
    logger.info("File store cleaned with reclaimed size of {0}. New current size of {1}",
        formatMemSize(reclaimedSize), formatMemSize(currentSize));
  }

  @Override
  public void updateStatus(final String status) {
    logger.info("Current garbage collector status: {0}", status);
  }
}
