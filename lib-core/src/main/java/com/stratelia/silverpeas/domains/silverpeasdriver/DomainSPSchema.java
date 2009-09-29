package com.stratelia.silverpeas.domains.silverpeasdriver;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class DomainSPSchema extends Schema {
  public DomainSPSchema(int cl) throws UtilException {
    super(cl);
    init();
    SilverTrace.info("admin", "DomainSPSchema.DomainSPSchema",
        "admin.MSG_INFO_DOMAINSSP_INSTANCE_CREATION");
  }

  protected String getJNDIName() {
    return JNDINames.ADMIN_DATASOURCE;
  }

  public void init() {
    user = new SPUserTable(this);
    group = new SPGroupTable(this);
  }

  public SPUserTable user = null;
  public SPGroupTable group = null;
}
