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
package com.silverpeas.jcrutil.servlets;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.data.GarbageCollector;
import org.apache.jackrabbit.core.state.ItemStateException;

import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class PeriodicJcrCleaner implements Runnable {

  private GarbageCollector gc;
  private boolean running;

  public PeriodicJcrCleaner(Repository repository) throws ItemStateException,
      RepositoryException {
    SessionImpl session = (SessionImpl) repository
        .login(new SilverpeasSystemCredentials());
    gc = session.createDataStoreGarbageCollector();
    this.running = true;
  }

  public void run() {
    try {
      while (running) {
        synchronized (this) {
          SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
              "RepositoryAccessServlet initialized",
              "Cleaning the repository ...........");
          System.gc();
          if (gc != null && gc.getDataStore() != null) {
            gc.scan();
            gc.stopScan();
            gc.deleteUnused();
          }
        }
        Thread.sleep(300000);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      running = false;
    }
  }

  public synchronized void stop() {
    this.running = false;
  }
}
