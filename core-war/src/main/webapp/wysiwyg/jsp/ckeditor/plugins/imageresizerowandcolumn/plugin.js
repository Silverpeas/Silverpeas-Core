/**
 * @license Copyright (c) 2003-2017, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

( function() {
    var pxUnit = CKEDITOR.tools.cssLength,
        needsIEHacks = CKEDITOR.env.ie && ( CKEDITOR.env.ie7Compat || CKEDITOR.env.quirks );

    function getWidth( el ) {

        return CKEDITOR.env.ie ? el.$.clientWidth : parseInt( el.$.width, 10 );
    }

    function getHeight( el ) {

        return CKEDITOR.env.ie ? el.$.clientHeight : parseInt( el.$.height, 10 );
    }

    function buildImgRowPillars(img){
        var $imgX = img.$.x;
        var $imgY = img.$.y;
        var $pillarWidth = img.$.width;
        var $pillarHeight = img.$.height;
        var pillars = [];

        // The pillar should reflects exactly the shape of the hovered
        // column border line.
        pillars.push( {
            img: img,
            index: 0,
            x: $imgX,
            y: $imgY + img.$.offsetHeight - 5,
            width: $pillarWidth,
            height: 5
        } );

        return pillars;
    }

    function buildImgColumnPillars( img ) {

        var $imgX = img.$.x;
        var $imgY = img.$.y;
        var $pillarWidth = img.$.width;
        var $pillarHeight = img.$.height;
        var pillars = [];

        // The pillar should reflects exactly the shape of the hovered
        // column border line.
        pillars.push( {
            img: img,
            index: 0,
            x: $imgX + img.$.offsetWidth - 5,
            y: $imgY,
            width: 5,
            height: $pillarHeight
        } );



        return pillars;
    }

    function getPillarAtPositionY( pillars, positionX ) {

        for ( var i = 0, len = pillars.length; i < len; i++ ) {
            var pillar = pillars[ i ];

            if ( positionX >= pillar.y && positionX <= ( pillar.y + pillar.height ) )
                return pillar;
        }

        return null;
    }

    function getPillarAtPosition( pillars, positionX ) {
        for ( var i = 0, len = pillars.length; i < len; i++ ) {
            var pillar = pillars[ i ];

            if ( positionX >= pillar.x && positionX <= ( pillar.x + pillar.width ) )
                return pillar;
        }

        return null;
    }

    function cancel( evt ) {
        ( evt.data || evt ).preventDefault();
    }

    function columnResizer( editor ) {
        this.dim = 'col';
        var pillar, document, resizer, isResizing, startOffset, currentShift, move, offsetX;

        var leftSideCells, leftShiftBoundary, rightShiftBoundary;

        function detach() {
            pillar = null;
            currentShift = 0;
            isResizing = 0;

            document.removeListener( 'mouseup', onMouseUp );
            resizer.removeListener( 'mousedown', onMouseDown );
            resizer.removeListener( 'mousemove', onMouseMove );

            document.getBody().setStyle( 'cursor', 'auto' );

            // Hide the resizer (remove it on IE7 - http://dev.ckeditor.com/ticket/5890).
            needsIEHacks ? resizer.remove() : resizer.hide();
        }

        function resizeStart() {
            // Before starting to resize, figure out which cells to change
            // and the boundaries of this resizing shift.

            var columnIndex = pillar.index,
                leftColumnCells = [];

                leftColumnCells.push( pillar.img);


            // Cache the list of cells to be resized.
            leftSideCells = leftColumnCells;


            resizer.setOpacity( 0.5 );
            currentShift = 0;
            isResizing = 1;

            resizer.on( 'mousemove', onMouseMove );

            // Prevent the native drag behavior otherwise 'mousemove' won't fire.
            document.on( 'dragstart', cancel );
        }

        function resizeEnd(direction) {
            isResizing = 0;

            resizer.setOpacity( 0 );

            resizeColumn(direction);

            document.removeListener( 'dragstart', cancel );
        }

        function resizeColumn(direction) {
            var cellsCount = leftSideCells.length,
                cellsSaved = 0;

            // Perform the actual resize to table cells, only for those by side of the pillar.
            for ( var i = 0; i < cellsCount; i++ ) {
                var leftCell = leftSideCells[ i ];

                // Defer the resizing to avoid any interference among cells.
                CKEDITOR.tools.setTimeout( function( leftCell, leftOldWidth ) {
                    // 1px is the minimum valid width (http://dev.ckeditor.com/ticket/11626).

                     if (leftCell) {
                       leftCell.setStyle('width', pxUnit(Math.max(leftOldWidth + direction, 1)));
                       leftCell.setStyle('height', 'auto');
                       CKEDITOR.tools.setTimeout(function(leftCell) {
                         leftCell.setStyle('height', pxUnit(leftCell.$.offsetHeight));
                       }, 0, this, leftCell);
                     }

                    // If we're in the last cell, we need to resize the table as well

                    // Cells resizing is asynchronous-y, so we have to use syncing
                    // to save snapshot only after all cells are resized. (http://dev.ckeditor.com/ticket/13388)
                    if ( ++cellsSaved == cellsCount ) {
                        editor.fire( 'saveSnapshot' );
                    }
                }, 0, this, [
                    leftCell, leftCell && getWidth( leftCell )
                ] );
            }
        }

        function onMouseDown( evt ) {

            offsetX = evt.data.getPageOffset().x;

            cancel( evt );

            // Save editor's state before we do any magic with cells. (http://dev.ckeditor.com/ticket/13388)
            editor.fire( 'saveSnapshot' );
            resizeStart();

            document.on( 'mouseup', onMouseUp, this );
        }

        function onMouseUp( evt ) {
            evt.removeListener();
            var direction = evt.data.getPageOffset().x - offsetX;

            resizeEnd(direction);
        }

        function onMouseMove( evt ) {
            move( evt.data.getPageOffset().x );
        }

        document = editor.document;

        resizer = CKEDITOR.dom.element.createFromHtml( '<div data-cke-temp=1 contenteditable=false unselectable=on ' +
            'style="position:absolute;cursor:col-resize;filter:alpha(opacity=0);opacity:0;' +
            'padding:0;background-color:#004;background-image:none;border:0px none;z-index:10"></div>', document );

        // Clean DOM when editor is destroyed.
        editor.on( 'destroy', function() {
            resizer.remove();
        } );

        // Except on IE6/7 (http://dev.ckeditor.com/ticket/5890), place the resizer after body to prevent it
        // from being editable.
        if ( !needsIEHacks )
            document.getDocumentElement().append( resizer );

        this.attachTo = function( targetPillar ) {
            // Accept only one pillar at a time.
            if ( isResizing )
                return;

            // On IE6/7, we append the resizer everytime we need it. (http://dev.ckeditor.com/ticket/5890)
            if ( needsIEHacks ) {
                document.getBody().append( resizer );
                currentShift = 0;
            }

            pillar = targetPillar;

            resizer.setStyles( {
                width: pxUnit( targetPillar.width ),
                height: pxUnit( targetPillar.height ),
                left: pxUnit( targetPillar.x ),
                top: pxUnit( targetPillar.y )
            } );

            // In IE6/7, it's not possible to have custom cursors for floating
            // elements in an editable document. Show the resizer in that case,
            // to give the user a visual clue.
            needsIEHacks && resizer.setOpacity( 0.25 );

            resizer.on( 'mousedown', onMouseDown, this );

            document.getBody().setStyle( 'cursor', 'col-resize' );

            // Display the resizer to receive events but don't show it,
            // only change the cursor to resizable shape.
            resizer.show();
        };

        move = this.move = function( posX ) {
            if ( !pillar )
                return 0;

            if ( !isResizing && ( posX < pillar.x || posX > ( pillar.x + pillar.width ) ) ) {
                detach();
                return 0;
            }

            var resizerNewPosition = posX - Math.round( resizer.$.offsetWidth / 2 );

            if ( isResizing ) {
                if ( resizerNewPosition == leftShiftBoundary || resizerNewPosition == rightShiftBoundary )
                    return 1;

                resizerNewPosition = Math.max( resizerNewPosition, leftShiftBoundary );
                resizerNewPosition = Math.min( resizerNewPosition, rightShiftBoundary );

                currentShift = resizerNewPosition - startOffset;
            }

            resizer.setStyle( 'left', pxUnit( posX ) );

            return 1;
        };
    }

    function rowResizer( editor ) {
        this.dim = 'row';
        var pillar, document, resizer, isResizing, startOffset, currentShift, move, offsetY;

        var leftSideCells, upShiftBoundary, downShiftBoundary;

        function detach() {
            pillar = null;
            currentShift = 0;
            isResizing = 0;

            document.removeListener( 'mouseup', onMouseUp );
            resizer.removeListener( 'mousedown', onMouseDown );
            resizer.removeListener( 'mousemove', onMouseMove );

            document.getBody().setStyle( 'cursor', 'auto' );

            // Hide the resizer (remove it on IE7 - http://dev.ckeditor.com/ticket/5890).
            needsIEHacks ? resizer.remove() : resizer.hide();
        }

        function resizeStart() {

            var rowIndex = pillar.index,
                leftColumnCells = [];

            leftColumnCells.push( pillar.img );

            // Cache the list of cells to be resized.
            leftSideCells = leftColumnCells;

            resizer.setOpacity( 0.5 );
            startOffset = parseInt( resizer.getStyle( 'top' ), 10 );
            currentShift = 0;
            isResizing = 1;

            resizer.on( 'mousemove', onMouseMove );

            // Prevent the native drag behavior otherwise 'mousemove' won't fire.
            document.on( 'dragstart', cancel );
        }

        function resizeEnd(direction) {
            isResizing = 0;

            resizer.setOpacity( 0 );

            resizeRow(direction);

            document.removeListener( 'dragstart', cancel );
        }

        function resizeRow(direction) {

            var
                cellsCount = leftSideCells.length,
                cellsSaved = 0;

            // Perform the actual resize to table cells, only for those by side of the pillar.
            for ( var i = 0; i < cellsCount; i++ ) {
                var leftCell = leftSideCells[ i ]

                table = pillar.table;

                // Defer the resizing to avoid any interference among cells.
                CKEDITOR.tools.setTimeout( function( leftCell, leftOldHeight, tableHeight ) {
                    // 1px is the minimum valid width (http://dev.ckeditor.com/ticket/11626).
                    leftCell && leftCell.setStyle( 'height', pxUnit( Math.max( leftOldHeight + direction, 1 ) ) );

                    // Cells resizing is asynchronous-y, so we have to use syncing
                    // to save snapshot only after all cells are resized. (http://dev.ckeditor.com/ticket/13388)
                    if ( ++cellsSaved == cellsCount ) {
                        editor.fire( 'saveSnapshot' );
                    }
                }, 0, this, [
                    leftCell, leftCell && getHeight( leftCell ),
                    ( !leftCell ) && ( getHeight( leftCell.img ) )
                ] );
            }
        }

        function onMouseDown( evt ) {

            offsetY = evt.data.getPageOffset().y;

            cancel( evt );

            // Save editor's state before we do any magic with cells. (http://dev.ckeditor.com/ticket/13388)
            editor.fire( 'saveSnapshot' );
            resizeStart();

            document.on( 'mouseup', onMouseUp, this );
        }

        function onMouseUp( evt ) {
            evt.removeListener();
            var direction = evt.data.getPageOffset().y - offsetY;

            resizeEnd(direction);
        }

        function onMouseMove( evt ) {
            move( evt.data.getPageOffset().y );
        }

        document = editor.document;

        resizer = CKEDITOR.dom.element.createFromHtml( '<div id="resizer" data-cke-temp=1 contenteditable=false unselectable=on ' +
            'style="position:absolute;cursor:row-resize;filter:alpha(opacity=0);opacity:0;' +
            'padding:0;background-color:#004;background-image:none;border:0px none;z-index:10"></div>', document );

        // Clean DOM when editor is destroyed.
        editor.on( 'destroy', function() {
            resizer.remove();
        } );

        // Except on IE6/7 (http://dev.ckeditor.com/ticket/5890), place the resizer after body to prevent it
        // from being editable.
        if ( !needsIEHacks )
            document.getDocumentElement().append( resizer );

        this.attachTo = function( targetPillar ) {
            // Accept only one pillar at a time.
            if ( isResizing )
                return;

            // On IE6/7, we append the resizer everytime we need it. (http://dev.ckeditor.com/ticket/5890)
            if ( needsIEHacks ) {
                document.getBody().append( resizer );
                currentShift = 0;
            }

            pillar = targetPillar;

            resizer.setStyles( {
                width: pxUnit( targetPillar.width ),
                height: pxUnit( targetPillar.height ),
                left: pxUnit( targetPillar.x ),
                top: pxUnit( targetPillar.y )
            } );

            // In IE6/7, it's not possible to have custom cursors for floating
            // elements in an editable document. Show the resizer in that case,
            // to give the user a visual clue.
            needsIEHacks && resizer.setOpacity( 0.25 );

            resizer.on('mousedown',onMouseDown,this);

            document.getBody().setStyle( 'cursor', 'row-resize' );

            // Display the resizer to receive events but don't show it,
            // only change the cursor to resizable shape.
            resizer.show();
        };

        move = this.move = function( posX ) {

            if ( !pillar )
                return 0;

            if ( !isResizing && ( posX < pillar.y || posX > ( pillar.y + pillar.height ) ) ) {
                detach();
                return 0;
            }

            var resizerNewPosition = posX - Math.round( resizer.$.offsetHeight / 2 );

            if ( isResizing ) {
                if ( resizerNewPosition == upShiftBoundary || resizerNewPosition == downShiftBoundary )
                    return 1;

                resizerNewPosition = Math.max( resizerNewPosition, upShiftBoundary );
                resizerNewPosition = Math.min( resizerNewPosition, downShiftBoundary );

                currentShift = resizerNewPosition - startOffset;

            }

            resizer.setStyle( 'top', pxUnit( posX ) );

            return 1;
        };
    }

    function clearPillarsCache( evt ) {
        var target = evt.data.getTarget();

        if ( evt.name == 'mouseout' ) {
            // Bypass interal mouse move.
            if ( !target.is( 'table' ) )
                return;

            var dest = new CKEDITOR.dom.element( evt.data.$.relatedTarget || evt.data.$.toElement );
            while ( dest && dest.$ && !dest.equals( target ) && !dest.is( 'body' ) )
                dest = dest.getParent();
            if ( !dest || dest.equals( target ) )
                return;
        }

        target.getAscendant( 'table', 1 ).removeCustomData( '_cke_table_pillars' );
        evt.removeListener();
    }

    CKEDITOR.plugins.add( 'imageresizerowandcolumn', {

        init: function( editor ) {
            editor.on( 'contentDom', function() {
                var resizer,
                    editable = editor.editable();

                // In Classic editor it is better to use document
                // instead of editable so event will work below body.
                editable.attachListener( editable.isInline() ? editable : editor.document, 'mousemove', function( evt ) {
                    evt = evt.data;

                    var target = evt.getTarget();

                    // FF may return document and IE8 some UFO (object with no nodeType property...)
                    // instead of an element (http://dev.ckeditor.com/ticket/11823).
                    if ( target.type != CKEDITOR.NODE_ELEMENT )
                        return;

                    var pageX = evt.getPageOffset().x;
                    var pageY = evt.getPageOffset().y;

                    // If we're already attached to a pillar, simply move the
                    // resizer.


                    if ( resizer && resizer.dim == 'col' && resizer.move( pageX ) ) {
                        cancel( evt );
                        return;
                    }
                    else if ( resizer && resizer.dim == 'row' && resizer.move( pageY ) ) {
                        cancel( evt );
                        return;
                    }

                    // Considering table, tr, td, tbody, thead, tfoot but nothing else.
                    var img, pillars;

                    if ( !target.is( 'img' ) ) {
                        return;
                    }

                    img = target.getAscendant( 'img', 1 );

                    // Make sure the table we found is inside the container
                    // (eg. we should not use tables the editor is embedded within)
                    if ( !editor.editable().contains( img ) ) {
                        return;
                    }

                    pillars = buildImgColumnPillars( img );

                    /*if ( !( pillars = table.getCustomData( '_cke_table_pillars' ) ) ) {
                     // Cache table pillars calculation result.
                     table.setCustomData( '_cke_table_pillars', ( pillars = buildTableColumnPillars( table ) ) );
                     table.on( 'mouseout', clearPillarsCache );
                     table.on( 'mousedown', clearPillarsCache );
                     }*/

                    var pillar = getPillarAtPosition( pillars, pageX );
                    if ( pillar ) {
                        resizer = new columnResizer( editor ) ;
                        resizer.attachTo( pillar );
                    }
                    else{
                        /*if ( !( pillars = table.getCustomData( '_cke_table_pillars' ) ) ) {
                         table.setCustomData( '_cke_table_pillars', ( pillars = buildRowPillars( table ) ) );
                         table.on( 'mouseout', clearPillarsCache );
                         table.on( 'mousedown', clearPillarsCache );
                         }*/
                        pillars = buildImgRowPillars( img );

                        /*var pillar = getPillarAtPositionY( pillars, pageY );
                        if ( pillar ) {
                            resizer = new rowResizer( editor ) ;
                            resizer.attachTo( pillar );
                        }*/
                    }
                } );
            } );
        }
    } );

} )();
