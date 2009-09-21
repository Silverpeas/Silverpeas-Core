package com.silverpeas.external.filesharing.model;

public class FileSharingFactory {
	private static final FileSharingInterface fileSharing = new FileSharingInterfaceImpl();
public static FileSharingInterface getFileSharing() {
	return fileSharing;
}
}
