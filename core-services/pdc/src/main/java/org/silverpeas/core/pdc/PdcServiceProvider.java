/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.pdc;

import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.util.ServiceProvider;

/**
 * The provider of services working on the PdC. It defines an entry point for no-managed object
 * to get the service instances that are managed by the IoC container.
 */
public class PdcServiceProvider {

  /**
   * Gets an instance of a manager of the classification plan (PdC).
   * @return a PdcBm object.
   */
  public static PdcManager getPdcManager() {
    return PdcManager.get();
  }

  /**
   * Gets an instance of a manager of the thesaurus used with the PdC.
   * @return a ThesaurusManager object.
   */
  public static ThesaurusManager getThesaurusManager() {
    return ServiceProvider.getService(ThesaurusManager.class);
  }

  /**
   * Gets an instance of the service dedicated to the classification of contents on the PdC.
   * @return a PdcClassificationService object.
   */
  public static PdcClassificationService getPdcClassificationService() {
    return ServiceProvider.getService(PdcClassificationService.class);
  }

  private PdcServiceProvider() {

  }
}
