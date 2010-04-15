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

class Auth {
	
	function authenticate($data,$fckphp_config) {

		//Hold relevant$fckphp_config vars locally
		$key=$fckphp_config['auth']['Handler']['SharedKey'];
		$fckphp_config['authSuccess']=false;
		
		//Decrypt the data passed to us
		$decData="";
		for ($i=0;$i<strlen($data)-1;$i+=2) $decData.=chr(hexdec($data[$i].$data[$i+1]));
		
		$decArray=explode("|^SEP^|",$decData);
		
		if (sizeof($decArray)==4) {
			//0 = Timestamp
			//1 = Client IP
			//2 = Username
			//3 = MD5
			if ($decArray[3]==md5($decArray[0]."|^SEP^|".$decArray[1]."|^SEP^|".$decArray[2].$key)) {
				if (time()-$decArray[0]<3600) { //Token valid for max of 1 hour
					if ($_SERVER['REMOTE_ADDR']==$decArray[1]) {
						
						//Set the file root to the users individual one
						$top=str_replace("//","/",$fckphp_config['basedir'].'/'.$fckphp_config['UserFilesPath']."/users");
						$fckphp_config['UserFilesPath']=$fckphp_config['UserFilesPath']."/users/".$decArray[2];
						$up=str_replace("//","/",$fckphp_config['basedir'].'/'.$fckphp_config['UserFilesPath']);
						
						if (!file_exists($top)) {
							mkdir($top,0777) or die("users folder in UserFilesPath does not exist and could not be created.");
							chmod($top,0777);
						}
						
						//Create folder if it doesnt exist
						if (!file_exists($up)) {
							mkdir($up,0777) or die("users/".$decArray[2]." folder in UserFilesPath does not exist and could not be created.");
							chmod($up,0777); //Just for good measure
						}
						
						//Create resource area subfolders if they dont exist
						foreach ($fckphp_config['ResourceTypes'] as $value) {
							if (!file_exists("$up/$value")) {
								mkdir("$up/$value",0777) or die("users/".$decArray[2]."/$value folder in UserFilesPath does not exist and could not be created.");
								chmod("$up/$value",0777); //Just for good measure
							}
						}
						$fckphp_config['authSuccess']=true;
					} else {
						//Not same client as auth token is for
					}
				} else {
					//Token more than an hour old
				}
			} else {
				//Data integrity failed
			}
		} else {
			//Not enough data (decryption failed?)
		}
		
		return $fckphp_config;
	}
}
?>