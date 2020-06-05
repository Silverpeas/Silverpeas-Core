/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.calendar.ical;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
  private static final int MAX_ATTEMPTS = 5;

  // --- HTTP CONNECTION HANDLER ---
  private static final CloseableHttpClient httpClient = HttpClients.createDefault();

  private FeedUtilities() {
  }

  public static byte[] loadFeed(String feedURL) throws SilverpeasException {
    return loadFeed(feedURL, null, null);
  }

  static byte[] loadFeed(String feedURL, String username, String password)
      throws SilverpeasException {
    try {
      // Load feed
      HttpGet get = new HttpGet(feedURL);
      get.setHeader("User-Agent", USER_AGENT);
      if (StringUtil.isDefined(username)) {
        // Set username/password
        byte[] auth =
            StringUtils.encodeString(username + ':' + StringUtils.decodePassword(password), Charsets.UTF_8);
        get.setHeader("Authorization", "Basic " + StringUtils.encodeBASE64(auth));

      }
      return loadFeedBody(get);
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  private static byte[] loadFeedBody(final HttpGet get) throws InterruptedException {
    byte[] bytes = null;
    for (int tries = 0; tries < MAX_ATTEMPTS; tries++) {
      try(CloseableHttpResponse response = httpClient.execute(get)) {
        int status = response.getStatusLine().getStatusCode();
        if (status == REMOTE_CALENDAR_RETURNCODE_OK) {
          ByteArrayOutputStream output = new ByteArrayOutputStream(
              (int) response.getEntity().getContentLength());
          response.getEntity().writeTo(output);
          bytes = output.toByteArray();
        } else {
          SilverLogger.getLogger(FeedUtilities.class)
              .warn("Feed loading failed with Http status code {0}. Actual content fetched: {1}",
                  status, Arrays.toString(bytes));
          bytes = null;
        }
      } catch (Exception loadError) {
        SilverLogger.getLogger(FeedUtilities.class)
            .warn("Feed loading failure after {0} attempts. {1}", tries, loadError.getMessage());
        if (tries == MAX_ATTEMPTS - 1) {
          bytes = null;
          SilverLogger.getLogger(FeedUtilities.class).error(loadError);
        }
        Thread.sleep(FEED_RETRY_MILLIS);
      } finally {
        get.reset();
      }
    }
    return bytes;
  }

  @SuppressWarnings("unchecked")
  static Calendar convertFeedToCalendar(SyndFeed feed, long eventLength) {

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
      URI uri = URI.create(url);
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

  private static final SyndEntry[] getFeedEntries(SyndFeed feed) {
    List list = feed.getEntries();
    SyndEntry[] entries = new SyndEntry[list.size()];
    list.toArray(entries);
    return entries;
  }

  public static final SyndFeed parseFeed(byte[] feedBytes) {
    SyndFeed synFeed = null;
    try {
      SyndFeedInput input = new SyndFeedInput();
      XmlReader xmlReader = new XmlReader(new ByteArrayInputStream(feedBytes));
      synFeed = input.build(xmlReader);
    } catch (Exception e) {
      SilverLogger.getLogger(FeedUtilities.class).error(e);
    }
    return synFeed;
  }

}
