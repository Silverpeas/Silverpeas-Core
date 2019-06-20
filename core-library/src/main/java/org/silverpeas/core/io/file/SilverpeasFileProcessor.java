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
package org.silverpeas.core.io.file;

/**
 * A processor of a {@link SilverpeasFile} instance. It performs some peculiar
 * tasks according to a file path in order to apply some additional business or technical rules
 * on the asked file.
 * @author mmoquillon
 */
public interface SilverpeasFileProcessor extends Comparable<SilverpeasFileProcessor> {

  /**
   * The value of the maximum priority
   */
  int MAX_PRIORITY = 100;

  enum ProcessingContext {
    /**
     * The processing is about the getting of a file.
     */
    GETTING,
    /**
     * The processing is about the writing of the content into a file.
     */
    WRITING,
    /**
     * The processing is about the deletion of a file.
     */
    DELETION,
    /**
     * The processing is about the copy of a file.
     */
    COPY,
    /**
     * The processing is about the moving of a file.
     */
    MOVING
  }

  /**
   * Gets the priority that permits to sort the processor list to execute.<br>
   * The more the value of the priority is high, the more the processor is executed first.<br>
   * The chained execution of processors that have the same priority could be known.<br>
   * By default, the priority is set to 50.
   * @return the priority value.
   */
  int getPriority();

  /**
   * Processes the specified path and returns the new path of the SilverpeasFile to get. This method
   * is triggered before retrieving the SilverpeasFile matching a given file path. If nothing
   * should be done with the path, then just returns the path passed as argument.
   * @param path the path of the asked file.
   * @param context the processing context.
   * @return either the specified path or a new path of the asked file.
   */
  String processBefore(String path, ProcessingContext context);

  /**
   * Processes the specified SilverpeasFile and returns the new one. This method is triggered after
   * retrieving the SilverpeasFile. If nothing should be done with the path, then just returns the
   * SilverpeasFile instance passed as argument.
   * @param file the SilverpeasFile to process.
   * @param context the processing context.
   * @return either the specified one or a new one.
   */
  SilverpeasFile processAfter(SilverpeasFile file, ProcessingContext context);
}
