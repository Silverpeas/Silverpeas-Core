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

class GetUploadProgress {
	var $fckphp_config;
	var $type;
	var $cwd;
	var $actual_cwd;
	var $uploadID;
	
	function GetUploadProgress($fckphp_config,$type,$cwd) {
		$this->fckphp_config=$fckphp_config;
		$this->type=$type;
		$this->raw_cwd=$cwd;
		$this->actual_cwd=str_replace("//","/",($fckphp_config['UserFilesPath']."/$type/".$this->raw_cwd));
		$this->real_cwd=str_replace("//","/",($this->fckphp_config['basedir']."/".$this->actual_cwd));
		$this->uploadID=$_GET['uploadID'];
		$this->refreshURL=$_GET['refreshURL'];
		
	}
	
	function run() {
		if (isset($this->refreshURL)&&($this->refreshURL!="")) {
			//Continue monitoring
			$uploadProgress=file($this->refreshURL);
			$url=$this->refreshURL;
		} else {
			//New download
			$uploadProgressHandler=$this->fckphp_config['uploadProgressHandler'];
			if ($uploadProgressHandler=='') {
				//Progresshandler not specified, return generic response
		?>
<Connector command="GetUploadProgress" resourceType="<?php echo $this->type; ?>">
	<CurrentFolder path="<?php echo $this->raw_cwd; ?>" url="<?php echo $this->actual_cwd; ?>" />
	<Progress max="2" value="1" />
	<RefreshURL url="" />
</Connector>
		<?php
				exit(0);
			}
			
			$url=$uploadProgressHandler."?iTotal=0&iRead=0&iStatus=1&sessionid=".$this->uploadID."&dtnow=".time()."&dtstart=".time();
			
			$_SESSION[$this->uploadID]=$url;
			$uploadProgress=file($url);
			
		}
		
		$uploadProgress2=implode("\n",$uploadProgress);
		
		$parser = xml_parser_create();
		xml_parse_into_struct($parser, $uploadProgress2, $vals, $index);
		
		$refreshURL=isset($vals[$index['REFRESHURL'][0]]['value'])?$vals[$index['REFRESHURL'][0]]['value']:"";
		$totalBytes=isset($vals[$index['TOTALBYTES'][0]]['value'])?$vals[$index['TOTALBYTES'][0]]['value']:0;
		$readBytes=isset($vals[$index['READBYTES'][0]]['value'])?$vals[$index['READBYTES'][0]]['value']:0;
		$status=isset($vals[$index['STATUS'][0]]['value'])?$vals[$index['STATUS'][0]]['value']:1;
		
		header ("content-type: text/xml");
		echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		?>
<Connector command="GetUploadProgress" resourceType="<?php echo $this->type; ?>">
	<CurrentFolder path="<?php echo $this->raw_cwd; ?>" url="<?php echo $this->actual_cwd; ?>" />
	<Progress max="<?php echo $totalBytes; ?>" value="<?php echo $readBytes; ?>" />
	<RefreshURL url="<?php echo htmlentities($refreshURL); ?>" />
</Connector>
		<?php
		xml_parser_free($parser);
	}
}

?>