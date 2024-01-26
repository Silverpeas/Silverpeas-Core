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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.util;

import org.silverpeas.core.util.ServiceProvider;

/**
 * The indexation mechanism to use for indexing the content of Silverpeas in the JCR. The indexation can be included in
 * the JCR implementation used by Silverpeas; otherwise it has to be implemented by the Silverpeas wrapper of the JCR
 * implementation.
 * <p>
 * The implementation of this interface has to be a singleton.
 * </p>
 * <p>
 * The indexation of the Silverpeas content in the JCR is required in order to be retrieved fastly. This is why in the
 * case the underlying implementation of the JCR doesn't take it in charge, it has to be implemented by the Silverpeas
 * wrapper of this JCR implementation. Whatever, an implementation of this interface has to be defined and, in the case
 * the indexation is already taken in charge, it can does nothing.
 * </p>
 * <p>
 * Usually, when the indexation isn't taken in charge by the JCR implementation, its initialization or setting is done
 * in fact by the Silverpeas installer itself (as it does for the SQL database). Nevertheless, beside that, the
 * indexation should be also set in tests in which the JCR is used in order to speed up them. This is why an
 * implementation of this interface has to be provided with the indexation initialization or setting.
 * </p>
 *
 * @author mmoquillon
 */
public interface SilverpeasJCRIndexation {

  /**
   * Gets the single instance of this class.
   *
   * @return the single instance of SilverpeasJCRIndexation.
   */
  static SilverpeasJCRIndexation get() {
    return ServiceProvider.getService(SilverpeasJCRIndexation.class);
  }

  /**
   * Initializes the indexation mechanism for the JCR in order to index the content of Silverpeas in the JCR.
   */
  void initialize();
}
