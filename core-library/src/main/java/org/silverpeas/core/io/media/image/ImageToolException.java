/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.io.media.image;

import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

/**
 * @author Yohann Chastagnier
 */
public class ImageToolException extends SilverpeasRuntimeException {
  private static final long serialVersionUID = -1010169458973310498L;

  /**
   * Default constructor
   * @param e
   */
  public ImageToolException(final Exception e) {
    super("ImageTool", SilverpeasException.ERROR, e.getMessage(), e);
  }

  /**
   * Default constructor
   * @param message the exception message
   */
  public ImageToolException(final String message) {
    super("ImageTool", SilverpeasException.ERROR, message);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.exception.SilverpeasRuntimeException#getModule()
   */
  @Override
  public String getModule() {
    return "image";
  }
}
