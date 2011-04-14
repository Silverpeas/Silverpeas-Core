/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationManager.model;

import java.sql.Connection;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class NotifSchema extends Schema {
  public NotifSchema(int cl, Connection co) throws UtilException {
    super(cl, co);
    init();
  }

  public NotifSchema(int cl) throws UtilException {
    super(cl);
    init();
  }

  protected String getJNDIName() {
    return JNDINames.ADMIN_DATASOURCE;
  }

  private void init() {
    notifAddress = new NotifAddressTable(this);
    notifChannel = new NotifChannelTable(this);
    notifDefaultAddress = new NotifDefaultAddressTable(this);
    notifPreference = new NotifPreferenceTable(this);
  }

  public NotifAddressTable notifAddress = null;
  public NotifChannelTable notifChannel = null;
  public NotifDefaultAddressTable notifDefaultAddress = null;
  public NotifPreferenceTable notifPreference = null;
}
