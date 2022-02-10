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

package org.silverpeas.core.contribution.converter.openoffice;

import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.core.office.OfficeManager;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.stream.Stream;

import static org.silverpeas.core.util.ResourceLocator.getSettingBundle;

/**
 * The OpenOffice service gives access to an open office process.
 * @author mmoquillon
 */
@Service
@Singleton
public class OpenOfficeService implements Initialization {

  private static final SettingBundle settings = getSettingBundle("org.silverpeas.converter.openoffice");
  private static final String OPENOFFICE_PORT = "openoffice.port";
  private static final String OPENOFFICE_HOME = "openoffice.home";
  private static final String OPENOFFICE_TASK_TIMEOUT = "openoffice.taskTimeout";

  private OfficeManager officeManager;

  @Override
  public void init() throws Exception {
    final String home = settings.getString(OPENOFFICE_HOME);
    final String ports = settings.getString(OPENOFFICE_PORT, "8100");
    final long taskTimeout = settings.getLong(OPENOFFICE_TASK_TIMEOUT, 30000);
    final int[] portNumbers = Stream.of(ports.split(","))
        .map(String::trim)
        .mapToInt(Integer::parseInt)
        .toArray();
    LocalOfficeManager.Builder builder = LocalOfficeManager.builder()
        .portNumbers(portNumbers)
        .taskQueueTimeout(taskTimeout);
    if (StringUtil.isDefined(home)) {
      builder = builder.officeHome(home);
    }
    officeManager = builder.install().build();
    officeManager.start();
  }

  @Override
  public void release() throws Exception {
    if (officeManager != null && officeManager.isRunning()) {
      officeManager.stop();
    }
  }

  /**
   * Gets the {@link OfficeManager} instance to use to manages the different processes of the
   * OpenOffice/LibreOffice program.
   * @return the {@link OfficeManager} instance preconfigured according to the OpenOffice settings
   * in Silverpeas.
   */
  public OfficeManager getOfficeManager() {
    return officeManager;
  }
}
  