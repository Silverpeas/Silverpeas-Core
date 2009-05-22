
// synchronization between Google Calendar and various iCalendar (RFC 2445)
// compatible calendar applications (Sunbird, Rainlendar, iCal, Calcium, Lightning, etc).
//
package com.silverpeas.ical;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.sun.syndication.feed.synd.SyndFeed;

public final class SyncEngine {

	public final static String REMOTE_CONNECT_SUCCEEDED = "0";
	public final static String AUTHENT_LOGIN_FAILED = "1";
	public final static String AUTHENT_PWD_FAILED = "2";
	public final static String REMOTE_CONNECT_FAILED = "3";
	
	public SyncEngine()  {

	}

	/**
	 * Import a remote calendar
	 * 
	 * @param localCalendar
	 *            local calendar file
	 * @param remoteCalendar
	 *            Google Calendar's private ICAL URL
	 *            ("https://www.google.com/calendar/ical/.../basic.ics"), or the
	 *            RSS/ATOM feed's URL (= feed converter mode)
	 * @param username
	 *            full name of the user (eg. "username@gmail.com" or
	 *            "username@mydomain.org"), this value is optional in feed
	 *            converter mode
	 * @param password
	 *            Gmail password (in unencrypted, plain text format), this value
	 *            is optional in feed converter mode
	 * 
	 * @throws Exception
	 *             any exception (eg. i/o, invalid param, invalid calendar
	 *             syntax, etc)
	 * 
	 */
	public final String synchronize(File localCalendar, URL remoteCalendar,
			String username, String password) throws Exception {
		SilverTrace.info("agenda","SyncEngine.synchronize()","root.MSG_GEN_ENTER_METHOD");

		if (username == null || username.length() == 0) {
			return AUTHENT_LOGIN_FAILED;
		}
		if (password == null || password.length() == 0) {
			return AUTHENT_PWD_FAILED;
		}
		String returnCode = REMOTE_CONNECT_FAILED;
		byte[] feedBytes = FeedUtilities.loadFeed(remoteCalendar.toString(), username, password);
		
		// Save content into the calendar file
		FileOutputStream fileOutputStream = new FileOutputStream(localCalendar);
		OutputStreamWriter fileOutput = new OutputStreamWriter(fileOutputStream, ImportIcalManager.charset);

		//Write File
		try {
			//test if rss feed of file feed
			if (feedBytes != null)
			{
				if (isRssFeed(feedBytes, remoteCalendar))
				{
					SilverTrace.info("agenda","SyncEngine.synchronize()","root.MSG_GEN_PARAM_VALUE","Feed RSS");
					//convert rss feed in Calendar File
					CalendarOutputter outputter = new CalendarOutputter();
					SyndFeed feed = FeedUtilities.parseFeed(feedBytes);
					Calendar feedCalendar = FeedUtilities.convertFeedToCalendar(feed, new Long(100000000).longValue());
					outputter.output(feedCalendar, fileOutput);
				}
				else	//File feed
				{
					SilverTrace.info("agenda","SyncEngine.synchronize()","root.MSG_GEN_PARAM_VALUE","Feed File");
					fileOutput.write(StringUtils.decodeToString(feedBytes, ImportIcalManager.charset));
					fileOutput.flush();
				}
				returnCode = REMOTE_CONNECT_SUCCEEDED;
			}
			else
				returnCode = REMOTE_CONNECT_FAILED;
		}
		catch (Exception e) {
			returnCode = REMOTE_CONNECT_FAILED;
		}
		finally {
			if (fileOutput != null) {
				fileOutput.close();
			}
		}
		SilverTrace.info("agenda","SyncEngine.synchronize()","root.MSG_GEN_EXIT_METHOD");
		return returnCode;
	}

	private boolean isRssFeed(byte[] bytes, URL feedUrl) throws Exception
	{
		boolean isRssFeed = false;
		SyndFeed syndFeed = FeedUtilities.parseFeed(bytes); 
		if (syndFeed != null)
		{
			if (syndFeed.getFeedType() != null)
				isRssFeed = true;
		}
		return isRssFeed;
	}
}
