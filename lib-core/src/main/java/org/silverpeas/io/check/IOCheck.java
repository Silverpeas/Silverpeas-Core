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

import org.silverpeas.io.file.FileHandler;
import org.silverpeas.io.session.IOSession;

/**
 * @author Yohann Chastagnier
 */
public interface IOCheck {

  /**
   * This method have to be annoted by @PostConstruct.
   * Just after Silverpeas server start, this method is called.
   * The content of this method consists to register the class instance into IOChecker.register().
   */
  void register();

  /**
   * This method have to be annoted by @PreDestroy.
   * Just before Silverpeas server stop, this method is called.
   * The content of this method consists to unregister the class instance from
   * IOChecker.unregister().
   */
  void unregister();

  /**
   * Check processing
   * @param session
   * @param fileHandler
   * @throws Exception
   */
  void check(IOSession session, FileHandler fileHandler) throws Exception;
}
