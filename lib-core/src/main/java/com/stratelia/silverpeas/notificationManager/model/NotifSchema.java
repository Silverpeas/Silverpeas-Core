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
