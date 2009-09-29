package com.silverpeas.versioning.control;

import com.stratelia.silverpeas.silverpeasinitialize.IInitialize;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class VersioningInitialize implements IInitialize {
  public VersioningInitialize() {
  }

  public boolean Initialize() {
    try {
      // pour les réservations de fichiers
      ScheduledReservedFile rf = new ScheduledReservedFile();
      rf.initialize();
    } catch (Exception e) {
      SilverTrace.error("versioning", "VersioningInitialize.Initialize()", "",
          e);
    }
    return true;
  }
}
