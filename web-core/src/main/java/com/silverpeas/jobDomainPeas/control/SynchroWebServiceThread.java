/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.jobDomainPeas.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class SynchroWebServiceThread extends SynchroThread {

  public SynchroWebServiceThread(JobDomainPeasSessionController toAwake) {
    super(toAwake);
  }

  public void run() {
    SilverTrace.info("jobDomainPeas", "SynchroWebServiceThread.run",
        "root.MSG_GEN_PARAM_VALUE", "------------DEBUT DU THREAD-----------");
    try {
      m_SynchroReport = m_toAwake.synchronizeSilverpeasViaWebService();
      SilverTrace.info("jobDomainPeas", "SynchroWebServiceThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------TOUT EST OK-----------");
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("jobDomainPeas", "SynchroWebServiceThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------AFTER NOTIFY-----------");
    } catch (Exception e) {
      m_ErrorOccured = e;
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("jobDomainPeas", "SynchroWebServiceThread.run",
          "root.MSG_GEN_PARAM_VALUE", "------------ERREUR-----------", e);
    }
  }
}
