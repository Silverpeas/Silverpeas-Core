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
