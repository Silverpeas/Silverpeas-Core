infobulleCommand_GetState = function() {
	if ( FCK.EditMode != FCK_EDITMODE_WYSIWYG )
		return FCK_TRISTATE_DISABLED;
	return FCK_TRISTATE_OFF;
}

FCKCommands.RegisterCommand('Infobulle', new FCKDialogCommand('Infobulle','Infobulle',FCKConfig.PluginsPath+'infobulle/test.html', 400, 220,infobulleCommand_GetState ));

// Ici je crée un bouton pour la toolbar auquel j'associe l'action précédemment définie
var oTestItem = new FCKToolbarButton('Infobulle', 'Infobulle',null, null, false, true);

oTestItem.IconPath = FCKConfig.PluginsPath + 'infobulle/infobulle.gif';
FCKToolbarItems.RegisterItem('Infobulle',oTestItem);

FCK.ContextMenu.RegisterListener({
	AddItems : function( menu, tag, tagName ) {
		// under what circumstances do we display this option
		if (tagName == 'A') {
			// when the option is displayed, show a separator  the command
			menu.AddSeparator();
			// the command needs the registered command name, the title for the context menu, and the icon path
			menu.AddItem('Infobulle', 'Infobulle', oTestItem.IconPath);
		}
	}
});
