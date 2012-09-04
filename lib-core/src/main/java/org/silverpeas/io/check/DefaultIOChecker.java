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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.io.check;

import java.util.ArrayList;
import java.util.Collection;

import org.silverpeas.io.file.FileHandler;
import org.silverpeas.io.session.IOSession;

import com.silverpeas.annotation.Service;

/**
 * @author Yohann Chastagnier
 */
@Service
public class DefaultIOChecker implements IOChecker {

  /** Check container */
  private final Collection<IOCheck> checks = new ArrayList<IOCheck>();

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.check.IOChecker#register(org.silverpeas.io.check.IOCheck)
   */
  @Override
  public void register(final IOCheck check) {
    checks.add(check);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.check.IOChecker#unregister(org.silverpeas.io.check.IOCheck)
   */
  @Override
  public void unregister(final IOCheck check) {
    checks.remove(check);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.check.IOChecker#check(org.silverpeas.io.session.IOSession,
   * org.silverpeas.io.FileHandler, java.util.Set)
   */
  @Override
  public void checks(final IOSession session, final FileHandler fileHandler) throws Exception {
    for (final IOCheck check : checks) {
      check.check(session, fileHandler);
    }
  }
}
