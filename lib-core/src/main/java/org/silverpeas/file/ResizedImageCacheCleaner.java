package org.silverpeas.file;

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import com.silverpeas.scheduler.trigger.TimeUnit;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.ResourceLocator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A scheduled job to clean up all the resized images for non existent original images.
 * @author mmoquillon
 */
@Named
public class ResizedImageCacheCleaner extends Job {

  private static final String CRON_PROPERTY = "image.cleaner.cron";

  @Inject
  private Scheduler scheduler;

  @PostConstruct
  public void scheduleFrequently() throws SchedulerException, ParseException {
    ResourceLocator settings = new ResourceLocator("org.silverpeas.lookAndFeel.generalLook", "");
    if (scheduler.isJobScheduled(getName())) {
      scheduler.unscheduleJob(getName());
    }
    String cron = settings.getString(CRON_PROPERTY);
    if (StringUtil.isDefined(cron)) {
        scheduler.scheduleJob(this, JobTrigger.triggerAt(cron));
    }
  }


  /**
   * Creates a new job
   */
  public ResizedImageCacheCleaner() {
    super(ResizedImageCacheCleaner.class.getSimpleName());
  }

  @Override
  public void execute(final JobExecutionContext context) throws Exception {
    List<String> originalImagePaths = ImageCache.getAllImageEntries();
    for (String originalImagePath: originalImagePaths) {
      File originalImage = new File(originalImagePath);
      if (!originalImage.exists()) {
        ImageCache.removeImages(originalImagePath);
      }
    }
  }
}
