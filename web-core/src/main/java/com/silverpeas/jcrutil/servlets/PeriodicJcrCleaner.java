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
    SessionImpl session = (SessionImpl) repository.login(new SilverpeasSystemCredentials());
    gc = session.createDataStoreGarbageCollector();
    this.running = true;
  }

  public void run() {
    try {
      while (running) {
        synchronized (this) {
          SilverTrace.info("RepositoryAccessServlet", "jackrabbit.init",
              "RepositoryAccessServlet initialized", "Cleaning the repository ...........");
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
