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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.silverpeas.process.management.ProcessCheckRegistration;

/**
 * The abstract root implementation of <code>Check</code> interface.
 * Methods <code>register</code> and <code>unregister</code> are implemented at this level, and be
 * sure <code>@Named</code> class annotation is well mentionned in the final implementation in the
 * aim to be taken in charge by <code>ProcessCheckRegistration</code> (@see
 * {@link ProcessCheckRegistration}).
 * @author Yohann Chastagnier
 */
public abstract class AbstractProcessCheck implements ProcessCheck {

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.check.Check#register()
   */
  @Override
  @PostConstruct
  public void register() {
    ProcessCheckRegistration.register(this);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.check.Check#unregister()
   */
  @Override
  @PreDestroy
  public void unregister() {
    ProcessCheckRegistration.unregister(this);
  }
}
