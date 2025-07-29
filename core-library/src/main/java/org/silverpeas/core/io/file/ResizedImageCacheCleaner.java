/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulingInitializer;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

/**
 * A scheduled job to clean up all the resized images for non existent original images.
 * @author mmoquillon
 */
@Service
public class ResizedImageCacheCleaner extends SchedulingInitializer {

  private static final String CRON_PROPERTY = "image.cleaner.cron";

  @Inject
  private Scheduler scheduler;
  private final Job job = new ResizedImageCacheCleanerJob();

  @NonNull
  @Override
  protected String getCron() {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.lookAndFeel.generalLook");
    return settings.getString(CRON_PROPERTY, "");
  }

  @NonNull
  @Override
  protected Job getJob() {
    return job;
  }

  @Override
  protected boolean isSchedulingEnabled() {
    return true;
  }


  private static class ResizedImageCacheCleanerJob extends Job {

    /**
     * Creates a new job
     */
    public ResizedImageCacheCleanerJob() {
      super(ResizedImageCacheCleanerJob.class.getSimpleName());
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
}
