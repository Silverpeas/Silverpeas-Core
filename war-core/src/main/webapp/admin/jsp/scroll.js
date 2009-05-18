// begin absolutely positioned scrollable area object scripts 
function say(){alert("saysaysay")}
function verifyCompatibleBrowser(){ 
	this.ver=navigator.appVersion 
	this.dom=document.getElementById?1:0 
	this.ie5=(this.ver.indexOf("MSIE 5")>-1 && this.dom)?1:0; 
	this.ie4=(document.all && !this.dom)?1:0; 
	this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0; 
	this.ns4=(document.layers && !this.dom)?1:0; 
	this.bw=(this.ie5 || this.ie4 || this.ns4 || this.ns5) 
	return this 
} 
bw=new verifyCompatibleBrowser() 
lstart=42
loop=true  
speed=100
stop=1000
pr_step=1
function ConstructObject(obj,nest){ 
    nest=(!nest) ? '':'document.'+nest+'.' 
	this.el=bw.dom?document.getElementById(obj):bw.ie4?document.all[obj]:bw.ns4?eval(nest+'document.'+obj):0; 
  	this.css=bw.dom?document.getElementById(obj).style:bw.ie4?document.all[obj].style:bw.ns4?eval(nest+'document.'+obj):0; 
	this.scrollHeight=bw.ns4?this.css.document.height:this.el.offsetHeight 
	this.newsScroll=newsScroll; 
	//this.moveIt=b_moveIt; this.x; this.y; 
	this.moveIt=b_moveIt; // netscape fix
    this.obj = obj + "Object" 
    eval(this.obj + "=this") 
    return this 
} 
function b_moveIt(x,y){ 
	this.x=x;this.y=y 
	this.css.left=this.x 
	this.css.top=this.y 
} 
//Makes the object scroll up 
function newsScroll(speed){ 
	if(this.y>-this.scrollHeight){ 
		this.moveIt(0,this.y-pr_step) 
		setTimeout(this.obj+".newsScroll("+speed+")",speed) 
	}else if(loop) { 
		this.moveIt(0,lstart) 
		eval(this.obj+".newsScroll("+speed+")") 
	  } 
} 
//Makes the object 
function InitialiseAutoScrollArea(_visible){ 
	objContainer=new ConstructObject('divASContainer') 
	objContent=new ConstructObject('divASContent','divASContainer') 
	objContent.moveIt(0,lstart) 
	//objContainer.css.visibility='visible' 
	objContainer.css.visibility=_visible
	objContent.newsScroll(speed) 
} 

function noScrollInit(){
	objContainer.css.visibility='hidden'
}

function restartScroll(){
	objContainer.css.visibility='visible'
}
// end absolutely positioned scrollable area object scripts 