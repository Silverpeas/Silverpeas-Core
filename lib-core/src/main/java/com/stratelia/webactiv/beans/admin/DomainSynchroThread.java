package com.stratelia.webactiv.beans.admin;

import java.util.Vector;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class DomainSynchroThread extends Thread {
  private Admin m_theAdmin = null;
  private boolean m_mustStop = false;
  private boolean m_mustBeRunning = false;
  private boolean m_isEncours = false;
  private Exception m_ErrorOccured = null;
  private Vector m_Domains = new Vector();
  private long m_nbSleepSec = 900; // 15 mn

  public DomainSynchroThread(Admin theAdmin, long nbSleepSec) {
    m_theAdmin = theAdmin;
    m_nbSleepSec = nbSleepSec;
  }

  public boolean isEnCours() {
    return m_isEncours;
  }

  public Exception getErrorOccured() {
    return m_ErrorOccured;
  }

  public void startTheThread() {
    m_isEncours = true;
    m_mustStop = false;
    m_ErrorOccured = null;
    m_mustBeRunning = true;
    start();
  }

  public void stopTheThread() {
    m_mustBeRunning = false;
    m_mustStop = true;
  }

  public void addDomain(String domainId) {
    m_Domains.add(domainId);
    if (m_mustBeRunning && !m_isEncours && !m_mustStop) {
      startTheThread();
    }
  }

  public void removeDomain(String domainId) {
    m_Domains.remove(domainId);
  }

  public void run() {
    int i;

    SilverTrace.info("admin", "DomainSynchroThread.run",
        "root.MSG_GEN_PARAM_VALUE",
        "------------DEBUT DU THREAD DE SYNCHRO-----------");
    while (!m_mustStop && (m_Domains.size() > 0)) {
      for (i = 0; (i < m_Domains.size()) && (!m_mustStop); i++) {
        SilverTrace.info("admin", "DomainSynchroThread.run",
            "root.MSG_GEN_PARAM_VALUE", "------------DEBUT SYNCHRO DOMAINE #"
                + (String) m_Domains.get(i) + "-----------");
        try {
          m_theAdmin.difSynchro((String) m_Domains.get(i));
        } catch (Exception e) {
          m_ErrorOccured = e;
          SilverTrace.error("admin", "DomainSynchroThread.run",
              "root.MSG_GEN_PARAM_VALUE",
              "------------ERREUR DANS LE THREAD DE SYNCHRO-----------", e);
        }
        SilverTrace.info("admin", "DomainSynchroThread.run",
            "root.MSG_GEN_PARAM_VALUE", "------------FIN SYNCHRO DOMAINE #"
                + (String) m_Domains.get(i) + "-----------");
      }
      if (!m_mustStop && (m_Domains.size() > 0)) {
        try {
          sleep(m_nbSleepSec * 1000);
        } catch (Exception e) {
          SilverTrace.info("admin", "DomainSynchroThread.run",
              "root.MSG_GEN_PARAM_VALUE",
              "------------ERREUR DANS LE SLEEP DE SYNCHRO-----------", e);
        }
      }
    }
    m_isEncours = false;
    SilverTrace.info("admin", "DomainSynchroThread.run",
        "root.MSG_GEN_PARAM_VALUE",
        "------------FIN DU THREAD DE SYNCHRO-----------");
  }

  public boolean isMustBeRunning() {
    return m_mustBeRunning;
  }

  public boolean isMustStop() {
    return m_mustStop;
  }

  public long getNbSleepSec() {
    return m_nbSleepSec;
  }

  public Admin getTheAdmin() {
    return m_theAdmin;
  }

  public void setEncours(boolean encours) {
    m_isEncours = encours;
  }

  public void setErrorOccured(Exception errorOccured) {
    m_ErrorOccured = errorOccured;
  }
}
