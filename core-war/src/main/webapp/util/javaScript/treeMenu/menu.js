var tree;
function loadNodeData(node, fnLoadComplete)  {

            var messages = [];
            //prepare URL for XHR request:

			var compoIdParameter ="";
			if(node.data.componentId != "undefined"){
				compoIdParameter="&compoId="+node.data.componentId
			}

            var sUrl = context+"/TreeMenuAjaxServlet?mtype="+menuType+"&ntype="+node.data.nodeType+"&key="+node.data.id+"&leaf="+node.data.isLeaf+compoIdParameter;
            //prepare our callback object
            var callback = {

                success: function(oResponse) {
                    YAHOO.log("XHR transaction was successful.", "info", "example");
					try {
						messages = YAHOO.lang.JSON.parse(oResponse.responseText);
                    }
					catch (x) {
						alert("JSON Parse failed!"+x);
						return;
					}

					for (var i = 0, len = messages.length; i < len; ++i) {
						var m = messages[i];
						var	tempNode = new YAHOO.widget.TextNode(m, node, false);
							tempNode.labelElId = m.id;
						if(m.isLeaf!="undefined"){
							tempNode.isLeaf=m.isLeaf;
						}
						if(m.labelStyle!="undefined"){
							tempNode.labelStyle = m.labelStyle;
						}
						if (m.nbObjects != -1){
							tempNode.label = m.label + " ("+m.nbObjects+")";
						}
						else {
							tempNode.label = m.label;
						}
					}

                    oResponse.argument.fnLoadComplete();
                },

                failure: function(oResponse) {
                    YAHOO.log("Failed to process XHR transaction.", "info", "example");
                    oResponse.argument.fnLoadComplete();
                },

                argument: {
                    "node": node,
                    "fnLoadComplete": fnLoadComplete
                },

                //timeout -- if more than 7 seconds go by, we'll abort
                //the transaction and assume there are no children:
                timeout: 70000
            };

            YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
        }


(function() {
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;


	//Add an onDOMReady handler to build the tree and the resize bar when the document is ready
    Event.onDOMReady(function() {

        //build the tree
	return buildTree();

    });
})();
