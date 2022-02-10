/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;
import java.io.File;
import java.text.ParseException;
import java.util.List;

/**
 * A scheduled job to clean up all the resized images for non existent original images.
 * @author mmoquillon
 */
@Service
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
  public void execute(final JobExecutionContext context) {
    List<String> originalImagePaths = ImageCache.getAllImageEntries();
    for (String originalImagePath: originalImagePaths) {
      File originalImage = new File(originalImagePath);
      if (!originalImage.exists()) {
        ImageCache.removeImages(originalImagePath);
      }
    }
  }
}
