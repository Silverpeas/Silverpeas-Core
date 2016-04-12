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

package org.silverpeas.core.web.calendar.ical;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.silverpeas.core.util.Charsets;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * RSS / ATOM feed utilities. Created: Jan 03, 2007 12:50:56 PM
 * @author Andras Berkes
 */
public final class FeedUtilities {

  // --- CONSTANTS ---

  private static final String USER_AGENT = "Mozilla/5.0 (Windows; U;" +
      " Windows NT 5.1; hu; rv:1.8.0.8) Gecko/20061025 Thunderbird/1.5.0.8";

  private static final long REMOTE_CALENDAR_RETURNCODE_OK = 200;
  private static final long FEED_RETRY_MILLIS = 1000L;

  // --- HTTP CONNECTION HANDLER ---
  private static final MultiThreadedHttpConnectionManager connectionManager =
      new MultiThreadedHttpConnectionManager();
  private static final HttpClient httpClient = new HttpClient(connectionManager);

  private FeedUtilities() {
  }

  public static final byte[] loadFeed(String feedURL) throws Exception {
    return loadFeed(feedURL, null, null);
  }

  public static final byte[] loadFeed(String feedURL, String username, String password)
      throws Exception {
    // Load feed
    GetMethod get = new GetMethod(feedURL);
    get.addRequestHeader("User-Agent", USER_AGENT);
    get.setFollowRedirects(true);

    if (StringUtil.isDefined(username)) {
      // Set username/password
      byte[] auth = StringUtils
          .encodeString(username + ':' + StringUtils.decodePassword(password), Charsets.UTF_8);
      get.addRequestHeader("Authorization", "Basic " + StringUtils.encodeBASE64(auth));

    }
    byte[] bytes = null;
    for (int tries = 0; tries < 5; tries++) {
      try {
        int status = httpClient.executeMethod(get);
        SilverTrace
            .warn("agenda", "FeedUtilities.loadFeed()", "Http connection status : " + status);
        if (status == REMOTE_CALENDAR_RETURNCODE_OK) {
          bytes = get.getResponseBody();

        } else {
          SilverTrace.warn("agenda", "FeedUtilities.loadFeed()", "agenda.FEED_LOADING_FAILED",
              "Status Http Client=" + status + " Content=" + Arrays.toString(bytes));
          bytes = null;
        }
      } catch (Exception loadError) {
        SilverTrace.warn("agenda", "FeedUtilities.loadFeed()", "Attempt #" + tries + " Status=",
            loadError.getMessage());
        if (tries == 5) {
          bytes = null;
          SilverTrace.warn("agenda", "FeedUtilities.loadFeed()", "agenda.CONNECTIONS_REFUSED",
              loadError.getMessage());
        }
        Thread.sleep(FEED_RETRY_MILLIS);
      } finally {
        get.releaseConnection();
      }
    }
    return bytes;
  }

  public static Calendar convertFeedToCalendar(SyndFeed feed, long eventLength) throws Exception {

    // Create new calendar
    Calendar calendar = new Calendar();
    PropertyList props = calendar.getProperties();
    props.add(new ProdId("-//Silverpeas//iCal4j 1.0//FR"));
    props.add(Version.VERSION_2_0);
    props.add(CalScale.GREGORIAN);

    // Convert events
    SyndEntry[] entries = getFeedEntries(feed);
    ComponentList events = calendar.getComponents();
    java.util.Date now = new java.util.Date();
    SyndEntry entry;
    for (SyndEntry entry1 : entries) {
      entry = entry1;

      // Convert feed link to iCal URL
      String url = entry.getLink();
      if (url == null || url.length() == 0) {
        continue;
      }

      // Convert feed published date to iCal dates
      Date date = entry.getPublishedDate();
      if (date == null) {
        date = now;
      }
      DateTime startDate = new DateTime(date);

      // Calculate iCal end date
      DateTime endDate = new DateTime(date.getTime() + eventLength);

      // Convert feed title to iCal summary
      String title = entry.getTitle();
      if (title == null || title.length() == 0) {
        title = url;
      } else {
        title = title.trim();
      }
      VEvent event = new VEvent(startDate, endDate, title);

      // Set event URL
      PropertyList args = event.getProperties();
      URI uri = new URI(url);
      args.add(new Url(uri));

      // Generate location by URL
      Location location = new Location(uri.getHost());
      args.add(location);

      // Generate UID by URL
      Uid uid = new Uid(url);
      args.add(uid);

      // Convert feed description to iCal description
      SyndContent syndContent = entry.getDescription();
      String content = null;
      if (syndContent != null) {
        content = syndContent.getValue();
      }
      if (content == null) {
        content = url;
      }
      Description desc = new Description(content);
      args.add(desc);

      // Add converted event
      events.add(event);
    }

    return calendar;
  }

  private static final SyndEntry[] getFeedEntries(SyndFeed feed) throws Exception {
    List list = feed.getEntries();
    SyndEntry[] entries = new SyndEntry[list.size()];
    list.toArray(entries);
    return entries;
  }

  public static final SyndFeed parseFeed(byte[] feedBytes) throws Exception {
    SyndFeed synFeed = null;
    try {
      SyndFeedInput input = new SyndFeedInput();
      XmlReader xmlReader = new XmlReader(new ByteArrayInputStream(feedBytes));
      synFeed = input.build(xmlReader);
    } catch (Exception e) {
      SilverTrace.warn("agenda", "FeedUtilities.parseFeed()", "agenda.EX_CANT_PARSE_FEED", e);
    }
    return synFeed;
  }

}
