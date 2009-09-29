/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.jobDomainPeas.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;

public class SynchroLdapThread extends SynchroThread {
  protected AdminController m_AdminCtrl = null;
  protected String m_TargetDomainId = "";

  public SynchroLdapThread(JobDomainPeasSessionController toAwake,
      AdminController adminCtrl, String targetDomainId) {
    super(toAwake);
    m_AdminCtrl = adminCtrl;
    m_TargetDomainId = targetDomainId;
  }

  public void run() {
    SilverTrace.info("jobDomainPeas", "SynchroLdapThread.run",
        "root.MSG_GEN_PARAM_VALUE", "------------DEBUT DU THREAD-----------");
    try {
      m_SynchroReport = m_AdminCtrl
          .synchronizeSilverpeasWithDomain(m_TargetDomainId);
      SilverTrace.info("jobDomainPeas", "SynchroLdapThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------TOUT EST OK-----------");
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("jobDomainPeas", "SynchroLdapThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------AFTER NOTIFY-----------");
    } catch (Exception e) {
      m_ErrorOccured = e;
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("jobDomainPeas", "SynchroLdapThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------ERREUR-----------", e);
    }
  }
}
