package org.silverpeas.file;

import com.silverpeas.scheduler.Job;
import com.silverpeas.scheduler.JobExecutionContext;
import com.silverpeas.scheduler.Scheduler;
import com.silverpeas.scheduler.SchedulerException;
import com.silverpeas.scheduler.trigger.JobTrigger;
import org.silverpeas.initialization.Initialization;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.text.ParseException;
import java.util.List;

/**
 * A scheduled job to clean up all the resized images for non existent original images.
 * @author mmoquillon
 */
public class ResizedImageCacheCleaner extends Job implements Initialization {

  private static final String CRON_PROPERTY = "image.cleaner.cron";

  @Inject
  private Scheduler scheduler;

  @Override
  public void init() throws SchedulerException, ParseException {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
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
