insert into model_contact 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(1, 'Employ&eacute;' , NULL, NULL, 
	 '<TR><TD colspan=2><hr><br>
<table width="100%">
<tr>
<td align=left class=txtlibform>Mobile :</td>
<td align=left><input type=text size=15 readonly 
VALUE="%WATXTDATA%"></td>
<td rowspan=3 valign=top>%WAIMGDATA%</td>
</TR>
<TR>
<td  align=left class=txtlibform>N&deg; poste interne :</td>
<td align=left><input type=text size=15 readonly 
VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td  align=left class=txtlibform>T&eacute;l. perso. :</td>
<td align=left><input type=text size=15 readonly 
VALUE="%WATXTDATA%"></td>
</tr>
<tr valign=top>
<td  align=left class=txtlibform>Adresse perso. :</td>
<td align=left colspan=2><textarea rows=4 cols=50 onFocus="this.blur();" wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
<TR>
<td  align=left class=txtlibform>Fonction :</td>
<td align=left colspan=2><input type=text size=50 readonly 
VALUE="%WATXTDATA%"></td>
</tr>
<TR>
<td  align=left class=txtlibform>Service :</td>
<td align=left colspan=2><input type=text size=50 readonly 
VALUE="%WATXTDATA%"></td>
</tr>
<tr valign=top>
<td  align=left class=txtlibform>Autres :</td>
<td align=left colspan=2><textarea rows=4 cols=50 onFocus="this.blur();" wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
</table>
</TD></TR>','
<table><tr>
<td valign=bottom>
<table cellpadding=0 cellspacing=0>
<tr>
<td width=150 valign=bottom align=left class=txtlibform>Mobile :</td>
<td valign=bottom align=left><input name="%WATXTVAR%" type=text size=15 VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td width=150 valign=bottom  align=left class=txtlibform>N&deg; poste interne :</td>
<td valign=bottom align=left colspan=2><input name="%WATXTVAR%" type=text size=15 VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td width=150 valign=bottom  align=left class=txtlibform>T&eacute;l. perso. :</td>
<td valign=bottom align=left colspan=2><input name="%WATXTVAR%" type=text size=15 VALUE="%WATXTDATA%"></td>
</tr>
</table>
</td>
<td>
%WAIMGDATA%<br><span class=txtlibform>Modifier photo :</span><br>%WAIMGVAR%
</td>
</tr></table>
<table>
<tr valign=top>
<td width=150  align=left class=txtlibform>Adresse perso. :</td>
<td align=left colspan=2><textarea name="%WATXTVAR%" rows=4 cols=50 wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
<TR>
<td width=150 align=left class=txtlibform>Fonction :</td>
<td align=left colspan=2><input name="%WATXTVAR%" type=text size=50 VALUE="%WATXTDATA%"></td>
</tr>
<TR>
<td width=150 align=left class=txtlibform>Service :</td>
<td align=left colspan=2><input name="%WATXTVAR%" type=text size=50 
VALUE="%WATXTDATA%"></td>
</tr>
<tr valign=top>
<td width=150 align=left class=txtlibform>Autres :</td>
<td align=left colspan=2><textarea name="%WATXTVAR%" rows=4 cols=50 wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
</table>'
	)
GO


insert into model_contact 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(2, 'Fournisseur', NULL, NULL, 
	 '<TR><TD colspan=2><hr><br>
<table width="100%">
<tr>
<td align=left class=txtlibform>Entreprise :</td>
<td align=left><input type=text size=50 readonly 
VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td  align=left class=txtlibform>Fonction :</td>
<td align=left><input type=text size=50 readonly 
VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td  align=left class=txtlibform>Mobile :</td>
<td align=left><input type=text size=50 readonly 
VALUE="%WATXTDATA%"></td>
</tr>
<tr valign=top>
<td  align=left class=txtlibform>Adresse :</td>
<td align=left><textarea rows=4 cols=50 onFocus="this.blur();" wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
<TR>
<td  align=left class=txtlibform>Site web :</td>
<td align=left colspan=2>%WAURLDATA%</td>
</tr>
<TR>
<tr valign=top>
<td  align=left class=txtlibform>Autres :</td>
<td align=left colspan=2><textarea rows=4 cols=50 onFocus="this.blur();" wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
</table>
</TD></TR>','<table>
<TR><TD colspan=2>
<table width="100%">
<tr>
<td align=left class=txtlibform>Entreprise :</td>
<td align=left><input name="%WATXTVAR%" type=text size=50 VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td  align=left class=txtlibform>Fonction :</td>
<td align=left><input name="%WATXTVAR%" type=text size=50 VALUE="%WATXTDATA%"></td>
</TR>
<TR>
<td  align=left class=txtlibform>Mobile :</td>
<td align=left><input name="%WATXTVAR%" type=text size=50 VALUE="%WATXTDATA%"></td>
</tr>
<tr valign=top>
<td  align=left class=txtlibform>Adresse :</td>
<td align=left><textarea name="%WATXTVAR%" rows=4 cols=50 wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
<TR>
<td  align=left class=txtlibform>Site web :</td>
<td align=left colspan=2><input name="%WATXTVAR%" type=text size=40 VALUE="%WATXTDATA%"></td>
</tr>
<TR>
<tr valign=top>
<td  align=left class=txtlibform>Autres :</td>
<td align=left colspan=2><textarea name="%WATXTVAR%" rows=4 cols=50 wrap="virtual">%WATXTDATA%</textarea></td>
</tr>
</table>
</TD></TR>
</table>'
	)
GO

insert into model 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(1, 'Mod&egrave;le n&deg;1', 'Un r&eacute;sum&eacute; et un d&eacute;veloppement', 'model1.gif', 
	'<div align=left>
<table cellpadding=0 width=100%>
<tr valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr><tr r valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr>
</table>
</div>',
	 '<tr valign=top><td align=left  class=txtlibform>
Saisie de texte 1 :
</td><td>
<textarea name=%WATXTVAR% cols=130 rows=6>%WATXTDATA%</textarea>
</td></tr>
<tr><td align=left  class=txtlibform>
Saisie de texte 2 :
</td><td>
<textarea name=%WATXTVAR% cols=130  rows=16>%WATXTDATA%</textarea>
</td>
</tr>'
	)
GO

insert into model 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(2, 'Mod&egrave;le n&deg;2', 'Du texte et une image', 'model2.gif', 
	'<div align=left>
<table cellpadding=0 width=100%>
<tr valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr>
</table>
<br><center>
%WAIMGDATA%
</center></div>', 
	 '<tr valign=top><td align=left  class=txtlibform>
Saisie de texte :
</td><td>
<textarea name=%WATXTVAR% cols=130 rows=20>%WATXTDATA%</textarea>
</td></tr>
<tr><td colspan=2 align=center>
%WAIMGDATA%
</td></tr>
<tr><td class=txtlibform>Modifier :</td>
<td>
%WAIMGVAR%
</td></tr>'
	)
GO

insert into model 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(3, 'Mod&egrave;le n&deg;3', 'Du texte sur deux colonnes et une image', 'model3.gif', 
	 '<div align=left>
<table cellpadding=0 width=100%>
<tr valign=top>
<td width=50%><font size=1>%WATXTDATA%</font></td>
<td width=50%><font size=1>%WATXTDATA%</font></td>
</tr>
</table>
<br><center>
%WAIMGDATA%
</center></div>', 	
	 '<tr><td colspan=2>
<table><tr>
<td  class=txtlibform>Texte de la premi&egrave;re colonne :</td>
<td></td>
<td   class=txtlibform>
Texte de la deuxi&egrave;me colonne : </td></tr>
<tr><td>
<textarea name=%WATXTVAR% rows=20 cols=60 nowrap>%WATXTDATA%</textarea>
</td>
<td>&nbsp;&nbsp;</td>
<td>
<textarea name=%WATXTVAR% rows=20 cols=60 nowrap>%WATXTDATA%</textarea>
</td></tr></table>
</td> </tr>
<tr><td colspan=2 align=center>
%WAIMGDATA%
</td></tr>
<tr><td  class=txtlibform>Modifier :</td>
<td>  %WAIMGVAR%
</td></tr>'
	)
GO


insert into model 
	(id, name, description, imageName, htmlDisplayer, htmlEditor) values 
	(4, 'Mod&egrave;le n&deg;4', 'Simple mais efficace', 'model4.gif', 
	 '<div align=left>
<table cellpadding=0 width=100%>
<tr valign=top>
<td><font size=4><b>%WATXTDATA%</b></font></td>
</tr><tr valign=top>
<td><font size=2><b>%WATXTDATA%</b></font></td>
</tr><tr valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr>
</table><br><br>
<table cellpadding=0 width=100%>
<tr valign=top>
<td><font size=1><b>%WATXTDATA%</b></font></td>
</tr><tr valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr>
</table><br><br>
<center>%WAIMGDATA%</center>
<br><br>
<table cellpadding=0 width=100%>
<tr valign=top>
<td><font size=1><b>%WATXTDATA%</b></font></td>
</tr><tr valign=top>
<td><font size=1>%WATXTDATA%</font></td>
</tr>
</table>
</div>', 
	 '<tr>
<td class=txtlibform>Titre :</td>
<td>
<input type=text name=%WATXTVAR% value="%WATXTDATA%" size=100>
</td></tr>
<tr><td class=txtlibform>Sous titre :</td>
<td>
<input type=text name=%WATXTVAR% value="%WATXTDATA%" size=100></td></tr>
<tr><td class=txtlibform>Introduction :</td>
<td>
<textarea name=%WATXTVAR% cols=100 rows=6>%WATXTDATA%</textarea>
</td></tr>
<tr>
<td class=txtlibform>Sous titre 1 :</td>
<td>
<input type=text name=%WATXTVAR% value="%WATXTDATA%" size=100>
</td></tr>
<tr><td class=txtlibform>Partie 1 :</td>
<td>
<textarea name=%WATXTVAR% cols=100 rows=6>%WATXTDATA%</textarea>
</td></tr>
<tr><td colspan=2>
<center>%WAIMGDATA%</center>
</td></tr>
<tr><td class=txtlibform>Modifier :</td>
<td>
%WAIMGVAR%
</td>
</tr>
<tr><td class=txtlibform>Sous titre 2 :</td>
<td>
<input type=text name=%WATXTVAR% value="%WATXTDATA%" size=100>'	
	)
GO


insert into calendarCategory(categoryId, name) values (1, 'R&eacute;union')
GO
insert into calendarCategory(categoryId, name) values (2, 'D&eacute;placement')
GO
insert into calendarCategory(categoryId, name) values (3, 'Vacances')
GO
insert into calendarCategory(categoryId, name) values (4, 'Personnel')
GO
insert into calendarCategory(categoryId, name) values (5, 'Brain Storming')
GO
insert into calendarCategory(categoryId, name) values (6, 'Formation')
GO

insert into days(day) values ('01')
GO
insert into days(day) values ('02')
GO
insert into days(day) values ('03')
GO
insert into days(day) values ('04')
GO
insert into days(day) values ('05')
GO
insert into days(day) values ('06')
GO
insert into days(day) values ('07')
GO
insert into days(day) values ('08')
GO
insert into days(day) values ('09')
GO
insert into days(day) values ('10')
GO
insert into days(day) values ('11')
GO
insert into days(day) values ('12')
GO
insert into days(day) values ('13')
GO
insert into days(day) values ('14')
GO
insert into days(day) values ('15')
GO
insert into days(day) values ('16')
GO
insert into days(day) values ('17')
GO
insert into days(day) values ('18')
GO
insert into days(day) values ('19')
GO
insert into days(day) values ('20')
GO
insert into days(day) values ('21')
GO
insert into days(day) values ('22')
GO
insert into days(day) values ('23')
GO
insert into days(day) values ('24')
GO
insert into days(day) values ('25')
GO
insert into days(day) values ('26')
GO
insert into days(day) values ('27')
GO
insert into days(day) values ('28')
GO
insert into days(day) values ('29')
GO
insert into days(day) values ('30')
GO
insert into days(day) values ('31')
GO

insert into ST_FormDesigner_Connectors 
	(ID, NAME, DESCRIPTION, DRIVER, URL, LOGIN, PASSWD, SQLQUERY, TYPE) values 
	(0, '________', ' ', ' ', ' ', ' ', ' ', ' ', ' ')
GO


