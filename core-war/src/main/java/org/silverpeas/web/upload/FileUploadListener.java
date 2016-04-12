/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.upload;

/**
 * Listener to listen to the status of a file upload.
 *
 * @author ehugonnet
 */
public class FileUploadListener implements OutputStreamListener {

  private final FileUploadStats fileUploadStats = new FileUploadStats();

  public FileUploadListener(long totalSize) {
    fileUploadStats.setTotalSize(totalSize);
  }

  @Override
  public void begin() {
    fileUploadStats.setCurrentStatus("begin");
  }

  @Override
  public void readBytes(int byteCount) {
    fileUploadStats.incrementBytesRead(byteCount);
    fileUploadStats.setCurrentStatus("reading");
  }

  @Override
  public void error(String s) {
    fileUploadStats.setCurrentStatus("error");
  }

  @Override
  public void end() {
    fileUploadStats.setBytesRead(fileUploadStats.getTotalSize());
    fileUploadStats.setCurrentStatus("end");
  }

  public FileUploadStats getFileUploadStats() {
    return fileUploadStats;
  }

  public static class FileUploadStats {

    private long totalSize = 0;
    private long bytesRead = 0;
    private final long startTime = System.currentTimeMillis();
    private String currentStatus = "none";

    public long getTotalSize() {
      return totalSize;
    }

    public void setTotalSize(long totalSize) {
      this.totalSize = totalSize;
    }

    public long getBytesRead() {
      return bytesRead;
    }

    public long getElapsedTimeInSeconds() {
      return (System.currentTimeMillis() - startTime) / 1000;
    }

    public String getCurrentStatus() {
      return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
      this.currentStatus = currentStatus;
    }

    public void setBytesRead(long bytesRead) {
      this.bytesRead = bytesRead;
    }

    public void incrementBytesRead(int byteCount) {
      this.bytesRead += byteCount;
    }
  }
}
