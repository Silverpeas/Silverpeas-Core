/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function($){
	var updateUpDown = function(sortable, save){
		$('dl:not(.ui-sortable-helper)', sortable)
			.removeClass('first').removeClass('last')
			.find('.up, .down').removeClass('disabled').end()
			.filter(':first').addClass('first').find('.up').addClass('disabled').end().end()
			.filter(':last').addClass('last').find('.down').addClass('disabled').end().end();

		//alert(sortable.id); //thin or thick

		if (save == '1')
			savePositions(sortable.id);
	};

	var moveUpDown = function(){
		var link = $(this),
			dl = link.parents('dl'),
			prev = dl.prev('dl'),
			next = dl.next('dl');

		if(link.is('.up') && prev.length > 0)
			dl.insertBefore(prev);

		if(link.is('.down') && next.length > 0)
			dl.insertAfter(next);

		updateUpDown(dl.parent(), '1');
	};

	var openDialog = function(){
		$('#dialog').css('display', 'block');
		$("#dialog").dialog({ modal: true, overlay: { opacity: 0.5, background: "black" } });
	};

	var addItem = function(){
		var portletId 	= $(this).attr("id");
		var portletName = $(this).text();

		var sortable = $("#column0");
		var options = '<span class="options"><a class="up">up</a><a class="down">down</a><a class="remove" title="Remove">X</a></span>';
		var tpl = '<dl id="{id}" class="sort"><dt><span class="portletName">{name}</span>' + options + '</dt><dd>Chargement en cours...</dd></dl>';
		var html = tpl.replace(/{id}/g, portletId).replace(/{name}/g, portletName);

		sortable.prepend(html).sortable('refresh').find('a.up, a.down').bind('click', moveUpDown).end()
			.find('a.remove').bind('click', removeItem);

		updateUpDown(sortable, '1');

		//alert($("#column0 > dl:first").attr("id"));
		//$("#column0 > dl:first > dd").text("Voil� la nouvelle portlet !");
		//$("#column0 > dl:first > dd").load("/admin/jsp/lastPublis.jsp");

		$("#dialog").dialog('close');

		var url = portletId.substring("portlet_SPBus".length, portletId.length)+".jsp";

		//alert(url);

		//TODO : enlever le hard coding
		$.post("http://localhost:8000/silverpeas/admin/jsp/"+url, function(data){
			$("#column0 > dl:first > dd").html(data);
		});

		//remove portlet from dialog
		$(this).parent().remove();
	};

	var removeItem = function(){
		//alert("removeItem");
		var sortable = $(this).parents('.ui-sortable');

		//add portlet to dialog
		var dialog = $("#dialog");
		var portletId 	= $(this).parents('.sort').attr("id");

		//get portlet's name
		var tab = $(this).parents('.sort').contents();
		var dt = $(tab[0]).contents();
		var portletName = ($(dt[0]).text());

		var html = '<li><a href="#" class="choose" id="'+portletId+'">'+portletName+'</a></li>';

		//alert(html);

		//add new line to dialog
		$("#dialog > ul").prepend(html);

		//rebind event
		$('a.choose').bind('click', addItem);

		//remove item from layout
		$(this).parents('.sort').remove();

		//refresh column where it was
		updateUpDown(sortable, '1');
	};

	var sortableChange = function(e, ui){
		//alert('sortableChange');
		//alert(ui.item[0].id);
		if(ui.sender){
			var w = $(this).width();
			ui.placeholder.width(w);
			ui.helper.css("width",$(this).children().width());
		}
	};

	var sortableUpdate = function(e, ui){
		//alert('sortableUpdate');
		if($(this)[0].id == 'trashcan'){
			emptyTrashCan(ui.item);
		} else {
			updateUpDown($(this)[0], '1');
			if(ui.sender)
				updateUpDown(ui.sender[0], '0');
		}
	};

	//Send ajax request to save portlets positions
	var savePositions = function (columnId)
	{
		//alert("savePositions");

		var reg=new RegExp("portlet", "g");

		var column1Data = $('#thick').sortable('serialize');
		column1Data += "#";
		var tableau=column1Data.split(reg);
		var column1Param = "";
		for (var i=0; i<tableau.length; i++)
		{
			//alert(tableau[i].substring(3, tableau[i].length-1));
			if (i != 0)
				column1Param += ","

			column1Param += tableau[i].substring(3, tableau[i].length-1);
		}

		var column2Data = $('#thin').sortable('serialize');
		column2Data += "#";
		var tableau=column2Data.split(reg);
		var column2Param = "";
		for (var i=0; i<tableau.length; i++)
		{
			//alert(tableau[i].substring(3, tableau[i].length-1));
			if (i != 0)
				column2Param += ",";

			column2Param += tableau[i].substring(3, tableau[i].length-1);
		}

		//alert("column1 = "+column1Param);
		//alert("column2 = "+column2Param);

		$.ajax({
			   type: "POST",
			   url: getSilverpeasContext()+"/portletAdmin",
			   data: "movePortletWindow=true&column1="+column1Param+"&column2="+column2Param+"&dt.SpaceId="+getSpaceId()
			 });
	}

	$(document).ready(function(){
		//alert('ready');
		var els = ['#thick', '#thin'];
		var $els = $(els.toString());

		//$('h2', $els.slice(0,-1)).append('<span class="options"><a class="add">add</a></span>');
		//$('dt', $els).append('<span class="options"><a class="up" title="Up">up</a><a class="down" title="Down">down</a><a class="remove" title="Remove">X</a></span>');

		//$('a.open').bind('click', openDialog);
		//$('a.up, a.down').bind('click', moveUpDown);
		//$('a.remove').bind('click', removeItem);
		//$('a.choose').bind('click', addItem);

		//$els.each(function(){
		//	updateUpDown(this, '0');
		//});

		$els.sortable({
			items: '> dl',
			handle: 'dt',
			cursor: 'move',
			//cursorAt: { top: 2, left: 2 },
			opacity: 0.40,
			//helper: 'clone',
			appendTo: 'body',
			//placeholder: 'clone',
			placeholder: 'placeholder',
			connectWith: els,
			start: function(e,ui) {
				ui.helper.css("width", ui.item.width());
			},
			change: sortableChange,
			update: sortableUpdate
		});

	});

	$(window).bind('load',function(){
		//alert('load');
		setTimeout(function(){
			$('#overlay').fadeOut(function(){
				$('body').css('overflow', 'auto');
			});
		}, 750);
	});
})(jQuery);