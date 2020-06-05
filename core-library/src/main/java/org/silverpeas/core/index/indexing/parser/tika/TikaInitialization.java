/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.index.indexing.parser.tika;

import org.apache.tika.exception.TikaException;
import org.apache.tika.utils.XMLReaderUtils;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Initialization of some of the Tika properties.
 *
 * @author mmoquillon
 */
public class TikaInitialization implements Initialization {
  @Override
  public void init() throws Exception {
    final SettingBundle settingBundle =
        ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");
    final int poolSize =
        settingBundle.getInteger("tika.saxParserPoolSize", XMLReaderUtils.DEFAULT_POOL_SIZE);
    try {
      XMLReaderUtils.setPoolSize(poolSize);
    } catch (TikaException e) {
      SilverLogger.getLogger(this)
          .error("Failure while setting the size of the SAX parsers pool to " + poolSize +
              ". Rollback to the default pool size (" + XMLReaderUtils.DEFAULT_POOL_SIZE + ")", e);
      XMLReaderUtils.setPoolSize(XMLReaderUtils.DEFAULT_POOL_SIZE);
    }
  }
}
  