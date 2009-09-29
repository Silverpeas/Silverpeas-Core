/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
