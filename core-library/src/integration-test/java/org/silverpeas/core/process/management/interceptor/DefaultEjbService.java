/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.process.management.interceptor;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.process.annotation.SimulationActionProcess;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.annotation.Action;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.TargetPK;

import javax.ejb.Stateless;
import javax.transaction.Transactional;

/**
 * @author Yohann Chastagnier
 */
@Stateless(name = "SimulationActionInterceptorEJB",
    description = "Stateless session bean to manage simulation action interceptor tests.")
@Transactional
public class DefaultEjbService implements EjbService {

  @SimulationActionProcess(elementLister = InterceptorTestFileElementLister.class)
  @Action(ActionType.CREATE)
  @Override
  public InterceptorTestFile create(@SourceObject final InterceptorTestFile file,
      @TargetPK final ForeignPK destination) {
    SilverTrace.info("InterceptorTest", "DefaultEjbService", "create called");
    return null;
  }

  /**
   * Missing for tests the {@link Action} annotation.
   * @param file
   * @param destination
   */
  @SimulationActionProcess(elementLister = InterceptorTestFileElementLister.class)
  // @Action(ActionType.DELETE)
  @Override
  public void delete(@SourceObject final InterceptorTestFile file,
      @TargetPK final ForeignPK destination) {
    SilverTrace.info("InterceptorTest", "DefaultEjbService", "delete called");
  }

  @SimulationActionProcess(elementLister = InterceptorTestFileElementLister.class)
  @Action(ActionType.MOVE)
  @Override
  public void move(final ForeignPK from, @TargetPK final ForeignPK destination) {
    SilverTrace.info("InterceptorTest", "DefaultEjbService", "move called");
  }
}
