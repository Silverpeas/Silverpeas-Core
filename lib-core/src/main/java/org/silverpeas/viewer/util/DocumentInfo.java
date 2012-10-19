/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.viewer.util;

import java.util.Collection;

/**
 * Some document info
 * @author Yohann Chastagnier
 */
public class DocumentInfo {
  private int nbPages = 0;
  private int maxWidh = 0;
  private int maxHeight = 0;

  /**
   * Initializing data from output SwfTools query
   * @param swfOutput
   * @return
   */
  protected DocumentInfo addFromSwfToolsOutput(final Collection<String> swfOutput) {
    boolean widthOk;
    boolean heightOk;
    try {
      for (final String outputLine : swfOutput) {
        try {
          widthOk = false;
          heightOk = false;
          for (final String info : outputLine.split(" ")) {
            if (info.indexOf("width") >= 0) {
              maxWidh =
                  Math.max(maxWidh,
                      Integer.valueOf(info.replaceAll("width=", "").replaceAll("\\.[0-9]{1,}", "")));
              widthOk = true;
            } else if (info.indexOf("height") >= 0) {
              maxHeight =
                  Math.max(maxHeight, Integer.valueOf(info.replaceAll("height=", "").replaceAll(
                      "\\.[0-9]{1,}", "")));
              heightOk = true;
            }
          }
          if (widthOk && heightOk) {
            nbPages++;
          }
        } catch (final Exception e) {
          // Nothing to do, just pass to the next line
        }
      }
    } catch (final Exception e) {
      // Nothing to do
    }
    return this;
  }

  /**
   * @return the nbPage
   */
  public int getNbPages() {
    return nbPages;
  }

  /**
   * @return the maxWidh
   */
  public int getMaxWidh() {
    return maxWidh;
  }

  /**
   * @return the maxHeight
   */
  public int getMaxHeight() {
    return maxHeight;
  }
}
