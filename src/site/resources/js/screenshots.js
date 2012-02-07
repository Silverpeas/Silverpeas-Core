$(document).ready(function() {							   
  //Thumbnailer.config.shaderOpacity = 1;
  var tn1 = $('.mygallery').tn3({
    skinDir:"skinSlideshow",
    imageClick:"fullscreen",
    image:{
      maxZoom:1,
      crop:true,
      clickEvent:"dblclick",
      transitions:[{
        type:"blinds"
      },{
        type:"grid"
      },{
        type:"grid",
        duration:460,
        easing:"easeInQuad",
        gridX:1,
        gridY:8,
        // flat, diagonal, circle, random
        sort:"random",
        sortReverse:false,
        diagonalStart:"bl",
        // fade, scale
        method:"scale",
        partDuration:360,
        partEasing:"easeOutSine",
        partDirection:"left"
      }]
    }
  });							   
						
  // initialize the dropdown menu
  $( '.topbar' ).dropdown();

  // improve table layout
  jQuery( 'table.bodyTable' ).each( function()
  {
    jQuery( this ).removeClass( 'bodyTable' );
    jQuery( this ).addClass( 'zebra-striped' );
  } );

  // add prettyprint class to all 'pre' element child of 'div' whit class="source"
  var prettify = false;

  jQuery( 'div.source > pre' ).each( function()
  {
    jQuery( this ).addClass( 'prettyprint' );
    prettify = true;
  } );

  // if code blocks were found, bring in the prettifier ...
  if ( prettify )
  {
    var link = jQuery( document.createElement( 'link' ) );
    link.attr(
    {
      type: 'text/css',
      rel: 'stylesheet',
      href: './css/prettify.css'
    } );
    jQuery( 'head' ).append( link );
    jQuery.getScript( './js/prettify.js', function() {
      prettyPrint();
    } );
  }	
});