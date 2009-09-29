package com.silverpeas.external.filesharing;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class FileSharingInstanciator implements ComponentsInstanciatorIntf {

  public FileSharingInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("fileSharing", "FileSharingInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // insert your code here !

    SilverTrace.info("fileSharing", "FileSharingInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("fileSharing", "FileSharingInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // insert your code here !

    SilverTrace.info("fileSharing", "FileSharingInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }
}