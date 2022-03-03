/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * JobDomainPeasException.java
 */

package org.silverpeas.web.jobdomain;


import org.silverpeas.core.SilverpeasException;

/**
 * Exception when an error is raised from a Silverpeas domain-related job.
 */
public class JobDomainPeasException extends SilverpeasException {

  private static final long serialVersionUID = 3322314537755637519L;


  public JobDomainPeasException(final String message, String ... parameters) {
    super(message, parameters);
  }

  public JobDomainPeasException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public JobDomainPeasException(final Throwable cause) {
    super(cause);
  }
}
