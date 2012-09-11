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
package org.silverpeas.process.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.silverpeas.process.management.ProcessExecutionContext;
import org.silverpeas.process.session.Session;

import com.silverpeas.annotation.Service;

/**
 * Default implementation of <code>Checker</code> interface.
 * @author Yohann Chastagnier
 */
@Service
public class DefaultChecker implements Checker {

  /** Check container */
  private final Collection<Check> checks = new ArrayList<Check>();

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.check.IOChecker#register(org.silverpeas.io.check.IOCheck)
   */
  @Override
  public synchronized void register(final Check check) {
    checks.add(check);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.io.check.IOChecker#unregister(org.silverpeas.io.check.IOCheck)
   */
  @Override
  public synchronized void unregister(final Check check) {
    checks.remove(check);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.check.Checker#checks(org.silverpeas.process.management.
   * ProcessExecutionContext, org.silverpeas.process.session.Session, java.util.Set)
   */
  @Override
  public void checks(final ProcessExecutionContext processExecutionProcess, final Session session,
      final Set<CheckType> checkTypesToProcess) throws Exception {
    for (final Check check : checks) {
      if (checkTypesToProcess.contains(check.getType())) {
        check.check(processExecutionProcess, session);
      }
    }
  }
}
