/*@cc_on
@if (@_win32 && @_jscript_version>4)

var rulersCreated = 0
    
function fixwidth() {
    var el;
    el=document.getElementById('visual-portal-wrapper');
    if (!rulersCreated){
        ruler = document.createElement('div');
        ruler.style.width="70em";
        ruler.style.position="absolute";
        ruler.style.top="-10px"
        ruler.style.visibility="hidden";
        document.body.insertBefore(ruler, document.body.firstChild)
        
        ruler2 = document.createElement('div');
        ruler2.style.position="relative";
        ruler2.style.height="1px"
        ruler2.style.visibility="hidden";
        ruler2.style.clear="both"
        document.body.appendChild(ruler2)
        
        rulersCreated=1
    }
    if (ruler2.offsetWidth < ruler.offsetWidth){
        el.style.width="70em"
    }else{
        el.style.width=ruler2.offsetWidth+'px'
    }
    //alert(""+ ruler2.offsetWidth + ":" + ruler.offsetWidth) //debugging info
}

window.attachEvent('onload', fixwidth);
window.attachEvent('onresize', fixwidth);
@end @*/
