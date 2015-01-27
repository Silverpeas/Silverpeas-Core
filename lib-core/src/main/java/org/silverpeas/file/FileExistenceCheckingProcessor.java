package org.silverpeas.file;

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Named;

/**
 * A processor to check the existence of a {@link org.silverpeas.file.SilverpeasFile}. It the
 * file doesn't exist, then {@¢ode NO_FILE} is returned.
 * @author mmoquillon
 */
@Named("fileExistenceCheckingProcessor")
public class FileExistenceCheckingProcessor extends AbstractSilverpeasFileProcessor{

  @Override
  public String processBefore(final String path, ProcessingContext context) {
    return path;
  }

  @Override
  public SilverpeasFile processAfter(final SilverpeasFile file, ProcessingContext context) {
    SilverpeasFile validatedFile = file;
    switch (context) {
      case GETTING:
      case MOVING:
        if (!validatedFile.exists() && !validatedFile.isFile()) {
          validatedFile = SilverpeasFile.NO_FILE;
        }
        break;
    }
    return validatedFile;
  }

}
