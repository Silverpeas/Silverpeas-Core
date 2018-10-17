/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.silverstatistics.volume.service;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.silverstatistics.volume.model.DirectoryStats;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Compute the size in terms of number of files and total size of all the components available for a
 * specified user.
 */
public class DirectoryVolumeService {

  private final File workspace;
  private boolean onlyComponentData;

  public DirectoryVolumeService() {
    this(new File(FileRepositoryManager.getUploadPath()));
    onlyComponentData = true;
  }

  DirectoryVolumeService(File workspace) {
    this.workspace = workspace;
    onlyComponentData = false;
  }

  private List<DirectorySizeComputer> buildSizeScanners(File dataDirectory, String userId) {
    File[] files = listDirectoriesToScan(dataDirectory, userId);
    List<DirectorySizeComputer> result = new ArrayList<>(files.length);
    for (File componentDir : files) {
      result.add(new DirectorySizeComputer(componentDir, onlyComponentData));
    }
    return result;
  }

  private List<FileNumberComputer> buildFileNumberScanners(File dataDirectory, String userId) {
    File[] files = listDirectoriesToScan(dataDirectory, userId);
    List<FileNumberComputer> result = new ArrayList<>(files.length);
    for (File componentDir : files) {
      result.add(new FileNumberComputer(componentDir, onlyComponentData));
    }
    return result;
  }

  long getTotalSize() throws InterruptedException, ExecutionException {
    List<DirectorySizeComputer> scanners = buildSizeScanners(workspace, null);
    long totalSize = 0L;
    ExecutorService executor = Executors.newFixedThreadPool(getNumberOfThread());
    List<Future<DirectoryStats>> result = executor.invokeAll(scanners);
    try {
      for (Future<DirectoryStats> future : result) {
        DirectoryStats stats = future.get();
        totalSize = totalSize + stats.getDirectorySize();
      }
    } finally {
      executor.shutdown();
    }
    return totalSize;
  }

  public Map<String, String[]> getSizeVentilation(String userId) throws
      InterruptedException, ExecutionException {
    List<DirectorySizeComputer> scanners = buildSizeScanners(workspace, userId);
    Map<String, String[]> volume = new HashMap<>(scanners.size());
    ExecutorService executor = Executors.newFixedThreadPool(getNumberOfThread());
    List<Future<DirectoryStats>> result = executor.invokeAll(scanners);
    try {
      for (Future<DirectoryStats> future : result) {
        DirectoryStats stats = future.get();
        volume.put(stats.getDirectoryName(), new String[]{String.valueOf(stats.getDirectorySize()),
          null, null});
      }
    } finally {
      executor.shutdown();
    }
    return volume;
  }

  public Map<String, String[]> getFileNumberVentilation(String userId) throws
      InterruptedException, ExecutionException {
    List<FileNumberComputer> scanners = buildFileNumberScanners(workspace, userId);
    Map<String, String[]> volume = new HashMap<>(scanners.size());
    ExecutorService executor = Executors.newFixedThreadPool(getNumberOfThread());
    List<Future<DirectoryStats>> result = executor.invokeAll(scanners);
    try {
      for (Future<DirectoryStats> future : result) {
        DirectoryStats stats = future.get();
        volume.put(stats.getDirectoryName(), new String[]{String.valueOf(stats.getNumberOfFiles()),
          null, null});
      }
    } finally {
      executor.shutdown();
    }
    return volume;
  }

  private File[] listDirectoriesToScan(File dataDirectory, String userId) {
    final OrganizationController controller = OrganizationController.get();
    final FileFilter filter;
    if (!StringUtil.isDefined(userId) || controller.getUserDetail(userId).isAccessAdmin()) {
      filter = DirectoryFileFilter.DIRECTORY;
    } else {
      final String[] spaceIds = controller.getAllSpaceIds(userId);
      final List<String> componentIds = Arrays.stream(spaceIds)
          .flatMap(i -> Arrays.stream(controller.getAllComponentIdsRecur(i)))
          .collect(Collectors.toList());
      filter = new AndFileFilter(DirectoryFileFilter.DIRECTORY, new NameFileFilter(componentIds));
    }
    return dataDirectory.listFiles(filter);
  }

  private int getNumberOfThread() {
    return Runtime.getRuntime().availableProcessors();
  }
}
