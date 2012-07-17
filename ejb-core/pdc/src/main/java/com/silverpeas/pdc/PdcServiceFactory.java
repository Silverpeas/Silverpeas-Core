/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.pdc;

import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * The factory of PdC service instances. It manages the way the services are created and backs the
 * management of their life-cycle. It defines an entry point for no-managed object to get the
 * service instances that are managed by the IoC container.
 */
public class PdcServiceFactory {

  private static PdcServiceFactory instance = new PdcServiceFactory();

  @Inject
  @Named("pdcBm")
  private PdcBm pdcBm;

  @Inject
  private ThesaurusManager thesaurusManager;

  @Inject
  private PdcClassificationService pdcClassificationService;

  /**
   * Gets an instance of this factory.
   * @return a PdcServiceFactory instance.
   */
  public static PdcServiceFactory getFactory() {
    return instance;
  }

  /**
   * Gets an instance of a manager of the classification plan (PdC).
   * @return a PdcBm object.
   */
  public PdcBm getPdcManager() {
    return pdcBm;
  }

  /**
   * Gets an instance of a manager of the thesaurus used with the PdC.
   * @return a ThesaurusManager object.
   */
  public ThesaurusManager getThesaurusManager() {
    return thesaurusManager;
  }

  /**
   * Gets an instance of the service dedicated to the classification of contents on the PdC.
   * @return a PdcClassificationService object.
   */
  public PdcClassificationService getPdcClassificationService() {
    return pdcClassificationService;
  }

  private PdcServiceFactory() {

  }
}
