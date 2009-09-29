/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.silverpeas.jobDomainPeas.control;

public class SynchroThread extends Thread {
  protected JobDomainPeasSessionController m_toAwake = null;
  protected boolean m_isEncours = false;
  protected Exception m_ErrorOccured = null;
  protected String m_SynchroReport = "";

  public SynchroThread(JobDomainPeasSessionController toAwake) {
    m_toAwake = toAwake;
  }

  public boolean isEnCours() {
    return m_isEncours;
  }

  public Exception getErrorOccured() {
    return m_ErrorOccured;
  }

  public String getSynchroReport() {
    return m_SynchroReport;
  }

  public void startTheThread() {
    m_isEncours = true;
    m_ErrorOccured = null;
    m_SynchroReport = "";
    start();
  }
}
