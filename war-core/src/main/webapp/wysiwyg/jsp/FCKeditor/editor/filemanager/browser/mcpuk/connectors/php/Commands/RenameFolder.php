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

class RenameFolder {
	var $fckphp_config;
	var $type;
	var $cwd;
	var $actual_cwd;
	var $newfolder;
	
	function RenameFolder($fckphp_config,$type,$cwd) {
		$this->fckphp_config=$fckphp_config;
		$this->type=$type;
		$this->raw_cwd=$cwd;
		$this->actual_cwd=str_replace("//","/",($fckphp_config['UserFilesPath']."/$type/".$this->raw_cwd));
		$this->real_cwd=str_replace("//","/",($this->fckphp_config['basedir']."/".$this->actual_cwd));
		$this->foldername=str_replace(array("..","/"),"",$_GET['FolderName']);
		$this->newname=str_replace(array("..","/"),"",$this->checkName($_GET['NewName']));
	}
	
	function checkName($name) {
		$newName="";
		for ($i=0;$i<strlen($name);$i++) {
			if (in_array($name[$i],$this->fckphp_config['DirNameAllowedChars'])) $newName.=$name[$i];
		}
		return $newName;
	}
	
	function run() {
		$result1=false;

		
		if ($this->newname!='') {
			$result1=rename($this->real_cwd.'/'.$this->foldername,$this->real_cwd.'/'.$this->newname);
		}
		
		header ("content-type: text/xml");
		echo "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		?>
<Connector command="RenameFolder" resourceType="<?php echo $this->type; ?>">
	<CurrentFolder path="<?php echo $this->raw_cwd; ?>" url="<?php echo $this->actual_cwd; ?>" />
	<?php
		if ($result1) {
			$err_no=0;
		} else {
			$err_no=602;
		}
		
	?>
	<Error number="<?php echo "".$err_no; ?>" />
</Connector>
		<?php
	}
}

?>