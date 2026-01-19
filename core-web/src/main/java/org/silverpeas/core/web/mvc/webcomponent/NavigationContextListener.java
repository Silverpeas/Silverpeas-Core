/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import java.util.EventListener;

/**
 * A listener of events on the navigation context of a user. By implementing this interface, the
 * bean will be informed about change on the user's Web navigation so that is can be able to
 * perform some specific tasks relative to the navigation change.
 * @author Yohann Chastagnier
 */
public interface NavigationContextListener<T extends WebComponentRequestContext<?>>
    extends EventListener {

  /**
   * Method called after navigation context is cleared.
   */
  void navigationContextCleared(NavigationContext<T> navigationContext);

  /**
   * Method called after a new navigation step is created.
   */
  void navigationStepCreated(NavigationContext<T> navigationContext);

  /**
   * Method called after an existing navigation step is reset.
   */
  void navigationStepReset(NavigationContext<T> navigationContext);

  /**
   * Method called after that no navigation step has been performed (created or reset).
   */
  void noNavigationStepPerformed(
      NavigationContext<T> navigationContext);

  /**
   * Method called for each navigation state trashed after an existing navigation step is reset.
   */
  void navigationStepTrashed(
      NavigationContext<T>.NavigationStep trashedNavigationStep);

  /**
   * Method called after an identifier of the context of a navigation step is set.
   */
  void navigationStepContextIdentifierSet(
      NavigationContext<T>.NavigationStep navigationStep,
      String oldContextIdentifier);

  /**
   * Method called after a label of a navigation step is set.
   */
  void navigationStepLabelSet(
      NavigationContext<T>.NavigationStep navigationStep,
      String oldLabel);
}
