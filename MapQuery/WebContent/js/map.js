lastShape = new Array();
var i=0;

$(function () {    
	$('#tables input').live('click', function (e) {
		//console.log("table - map: "+$(this).val());
		var table = "&table="+$(this).val(); 
        var dados = "cmd=map"+table;
        
        var color = Math.round(0xffffff * Math.random()).toString(16);
        
        //desenha as geometrias, conforme a tabela escolhida
        $.ajax({  
            type: "post",
            url: "Controller",
            dataType: "json",
            data: dados,
            success: function(data) {
            	$('#load-tables').remove();
            	//alert("entrou");
            	//console.log(data);
            	map.geomap( "append", data, {
                    color: "#" + ( color.length == 5 ? "0" : "" ) + color, strokeWidth: "2px", fillOpacity:".5"
                });
            	lastShape[i] = data;            	
            },
            beforeSend: function(){
            	$('#tables').append('<h5 id="load-tables">Loading...</h5>');
            },
            error: function(data){
            	console.log("erro - mapear");
            	console.log(data);
            }
    	});
        i++;
     });
	
	$('#btn-clear').live('click', function (e) {
		//console.log("btn-clear - map");
		//console.log(lastShape[1]);
		
		//funciona, mas visualmente nada acontece, por isso "seto" (configuro) o botão novamente --> se eu colocasse true na propriedade checked, todos os botões apareceriam como checados (depois de terem sido configurados novamente)
		$(".check").prop("checked", false);
	    $("#tables").buttonset();
	    
		for(var x=0; x<lastShape.length; x++)
		{
			//console.log("x: "+x);
			$("#map").geomap("remove", lastShape[x]);
		}
		map.geomap("refresh");
	});
});  