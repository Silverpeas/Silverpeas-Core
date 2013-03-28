/**
 * jQuery plugin for posting form including file inputs.
 * 
 * Copyright (c) 2010 - 2011 Ewen Elder
 *
 * Licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 * @author: Ewen Elder <ewen at jainaewen dot com> <glomainn at yahoo dot co dot uk>
 * @version: 1.1.1 (2011-07-29) updated by Silverpeas : 
 * if the response is of type json we are no longer expecting it to be written in an HTML body (like it was done with the php)
 * but we are expecting it to be the content of the iframe. 
 * Thus instead of  response = iframe.contents().find('body'); we are using  response = iframe.contents(); in case on json, and the content
 * is retrieved using .text() instead of .html().
 *  
**/
(function ($)
{
	$.fn.iframePostForm = function (options)
	{
		var response,
			returnReponse,
			element,
			status = true,
			iframe;
		
		options = $.extend({}, $.fn.iframePostForm.defaults, options);
		
		
		// Add the iframe.
		if (!$('#' + options.iframeID).length)
		{
			$('body').append('<iframe id="' + options.iframeID + '" name="' + options.iframeID + '" style="display:none" />');
		}
		
		
		return $(this).each(function ()
		{
			element = $(this);
			
			
			// Target the iframe.
			element.attr('target', options.iframeID);
			
			
			// Submit listener.
			element.submit(function ()
			{
				// If status is false then abort.
				status = options.post.apply(this);
				
				if (status === false)
				{
					return status;
				}
				
				
				iframe = $('#' + options.iframeID).load(function ()
				{
						
					if (options.json)
					{
            response = iframe.contents();
						returnReponse = $.parseJSON(response.text());
					}
					
					else
					{
            response = iframe.contents().find('body');
						returnReponse = response.html();
					}
					
					
					options.complete.apply(this, [returnReponse]);
					
					iframe.unbind('load');
					
					
					setTimeout(function ()
					{
						response.html('');
					}, 1);
				});
			});
		});
	};
	
	
	$.fn.iframePostForm.defaults =
	{
		iframeID : 'iframe-post-form',       // Iframe ID.
		json : false,                        // Parse server response as a json object.
		post : function () {},               // Form onsubmit.
		complete : function (response) {}    // After response from the server has been received.
	};
})(jQuery);