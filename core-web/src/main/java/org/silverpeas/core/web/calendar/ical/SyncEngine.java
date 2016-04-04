/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// synchronization between Google SilverpeasCalendar and various iCalendar (RFC 2445)
// compatible calendar applications (Sunbird, Rainlendar, iCal, Calcium, Lightning, etc).
//
package org.silverpeas.core.web.calendar.ical;

import com.sun.syndication.feed.synd.SyndFeed;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

public final class SyncEngine {

  public final static String REMOTE_CONNECT_SUCCEEDED = "0";
  public final static String AUTHENT_LOGIN_FAILED = "1";
  public final static String AUTHENT_PWD_FAILED = "2";
  public final static String REMOTE_CONNECT_FAILED = "3";

  public SyncEngine() {

  }

  /**
   * Import a remote calendar
   * @param localCalendar localResourceLocator calendar file
   * @param remoteCalendar Google SilverpeasCalendar's private ICAL URL
   * ("https://www.google.com/calendar/ical/.../basic.ics"), or the RSS/ATOM feed's URL (= feed
   * converter mode)
   * @param username full name of the user (eg. "username@gmail.com" or "username@mydomain.org"),
   * this value is optional in feed converter mode
   * @param password Gmail password (in unencrypted, plain text format), this value is optional in
   * feed converter mode
   * @throws Exception any exception (eg. i/o, invalid param, invalid calendar syntax, etc)
   */
  public final String synchronize(File localCalendar, URL remoteCalendar,
      String username, String password) throws Exception {


    if (username == null || username.length() == 0) {
      return AUTHENT_LOGIN_FAILED;
    }
    if (password == null || password.length() == 0) {
      return AUTHENT_PWD_FAILED;
    }
    String returnCode = REMOTE_CONNECT_FAILED;
    byte[] feedBytes = FeedUtilities.loadFeed(remoteCalendar.toString(),
        username, password);

    // Save content into the calendar file
    FileOutputStream fileOutputStream = new FileOutputStream(localCalendar);
    OutputStreamWriter fileOutput = new OutputStreamWriter(fileOutputStream,
        ImportIcalManager.charset);

    // Write File
    try {
      // test if rss feed of file feed
      if (feedBytes != null) {
        if (isRssFeed(feedBytes, remoteCalendar)) {

          // convert rss feed in SilverpeasCalendar File
          CalendarOutputter outputter = new CalendarOutputter();
          SyndFeed feed = FeedUtilities.parseFeed(feedBytes);
          Calendar feedCalendar = FeedUtilities.convertFeedToCalendar(feed, 100000000L);
          outputter.output(feedCalendar, fileOutput);
        } else // File feed
        {

          fileOutput.write(StringUtils.decodeToString(feedBytes,
              ImportIcalManager.charset));
          fileOutput.flush();
        }
        returnCode = REMOTE_CONNECT_SUCCEEDED;
      } else
        returnCode = REMOTE_CONNECT_FAILED;
    } catch (Exception e) {
      returnCode = REMOTE_CONNECT_FAILED;
    } finally {
      if (fileOutput != null) {
        fileOutput.close();
      }
    }

    return returnCode;
  }

  private boolean isRssFeed(byte[] bytes, URL feedUrl) throws Exception {
    boolean isRssFeed = false;
    SyndFeed syndFeed = FeedUtilities.parseFeed(bytes);
    if (syndFeed != null) {
      if (syndFeed.getFeedType() != null)
        isRssFeed = true;
    }
    return isRssFeed;
  }
}
