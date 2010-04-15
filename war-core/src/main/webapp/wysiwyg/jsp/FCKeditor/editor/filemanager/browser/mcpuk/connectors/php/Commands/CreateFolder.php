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

class CreateFolder {
	var $fckphp_config;
	var $type;
	var $cwd;
	var $actual_cwd;
	var $newfolder;
	
	function CreateFolder($fckphp_config,$type,$cwd) {
		$this->fckphp_config=$fckphp_config;
		$this->type=$type;
		$this->raw_cwd=$cwd;
		$this->actual_cwd=str_replace("//","/",($this->fckphp_config['UserFilesPath']."/$type/".$this->raw_cwd));
		$this->real_cwd=str_replace("//","/",($this->fckphp_config['basedir']."/".$this->actual_cwd));
		$this->newfolder=str_replace(array("..","/"),"",$_GET['NewFolderName']);
	}
	
	function checkFolderName($folderName) {
		
		//Check the name is not too long
		if (strlen($folderName)>$this->fckphp_config['MaxDirNameLength']) return false;
		
		//Check that it only contains valid characters
		for($i=0;$i<strlen($folderName);$i++) if (!in_array(substr($folderName,$i,1),$this->fckphp_config['DirNameAllowedChars'])) return false;
		
		//If it got this far all is ok
		return true;
	}
	
	function run() {
		header ("content-type: text/xml");
		echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		?>
<Connector command="CreateFolder" resourceType="<?php echo $this->type; ?>">
	<CurrentFolder path="<?php echo $this->raw_cwd; ?>" url="<?php echo $this->actual_cwd; ?>" />
	<?php
		$newdir=str_replace("//","/",($this->real_cwd."/".$this->newfolder));
		
		//Check the new name
		if ($this->checkFolderName($this->newfolder)) {
			
			//Check if it already exists
			if (is_dir($newdir)) {
				$err_no=101; //Folder already exists
			} else {
				
				//Check if we can create the directory here
				if (is_writeable($this->real_cwd)) {
					
					//Make the directory
					if (mkdir($newdir,0777)) {
						$err_no=0; //Success
					} else {
					
						$err_no=110; //Unknown error
					}	
				} else {
					$err_no=103; //No permissions to create
				}
			}
		} else {
			$err_no=102; //Invalid Folder Name
		}
		
	?>
	<Error number="<?php echo "".$err_no; ?>" />
</Connector>
		<?php
	}
}

?>