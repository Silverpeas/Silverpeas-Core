<?php 
/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function iconLookup($mime,$ext) {

	$mimeIcons=array(
			"image"=>"image.jpg",
			"audio"=>"sound.jpg",
			"video"=>"video.jpg",
			"text"=>"document2.jpg",
			"text/html"=>"html.jpg",
			"application"=>"binary.jpg",
			"application/pdf"=>"pdf.jpg",
			"application/msword"=>"document2.jpg",
			"application/postscript"=>"postscript.jpg",
			"application/rtf"=>"document2.jpg",
			"application/vnd.ms-excel"=>"document2.jpg",
			"application/vnd.ms-powerpoint"=>"document2.jpg",
			"application/x-tar"=>"tar.jpg",
			"application/zip"=>"tar.jpg",
			"message"=>"email.jpg",
			"message/html"=>"html.jpg",
			"model"=>"kmplot.jpg",
			"multipart"=>"kmultiple.jpg"
			);
	
	$extIcons=array(
			"pdf"=>"pdf.jpg",
			"ps"=>"postscript.jpg",
			"eps"=>"postscript.jpg",
			"ai"=>"postscript.jpg",
			"ra"=>"real_doc.jpg",
			"rm"=>"real_doc.jpg",
			"ram"=>"real_doc.jpg",
			"wav"=>"sound.jpg",
			"mp3"=>"sound.jpg",
			"ogg"=>"sound.jpg",
			"eml"=>"email.jpg",
			"tar"=>"tar.jpg",
			"zip"=>"tar.jpg",
			"bz2"=>"tar.jpg",
			"tgz"=>"tar.jpg",
			"gz"=>"tar.jpg",
			"rar"=>"tar.jpg",
			"avi"=>"video.jpg",
			"mpg"=>"video.jpg",
			"mpeg"=>"video.jpg",
			"jpg"=>"image.jpg",
			"gif"=>"image.jpg",
			"png"=>"image.jpg",
			"jpeg"=>"image.jpg",
			"nfo"=>"info.jpg",
			"xls"=>"spreadsheet.jpg",
			"csv"=>"spreadsheet.jpg",
			"html"=>"html.jpg",
			"doc"=>"document2.jpg",
			"rtf"=>"document2.jpg",
			"txt"=>"document2.jpg",
			"xla"=>"document2.jpg",
			"xlc"=>"document2.jpg",
			"xlt"=>"document2.jpg",
			"xlw"=>"document2.jpg",
			"txt"=>"document2.jpg"
			);

	if ($mime!="text/plain") {
		//Check specific cases
		$mimes=array_keys($mimeIcons);
		if (in_array($mime,$mimes)) {
			return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/".$mimeIcons[$mime];
		} else {
			//Check for the generic mime type
			$mimePrefix="text";
			$firstSlash=strpos($mime,"/"); 
			if ($firstSlash!==false) $mimePrefix=substr($mime,0,$firstSlash);
			
			if (in_array($mimePrefix,$mimes)) {
				return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/".$mimeIcons[$mimePrefix];
			} else {
				return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/empty.jpg";	
			}
		}
	} else {
		$extensions=array_keys($extIcons);
		if (in_array($ext,$extensions)) {
			return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/".$extIcons[$ext];
		} else {
			return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/empty.jpg";
		}
	}

	return $_SERVER['DOCUMENT_ROOT'].dirname($_SERVER['PHP_SELF'])."/images/empty.jpg";
}

?>