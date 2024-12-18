function createTables()
{
	var tables = '<div class="clear"></div><div id="tables" class="left">';
		tables += '<h3>Select the layer(s): </h3>';
		tables += '<input type="checkbox" name="layers[]" id="city" value="city" /><label for="city">city</label><br/>';
		tables += '<input type="checkbox" name="layers[]" id="nation" value="nation" /><label for="nation">nation</label><br/>';
		tables += '<input type="checkbox" name="layers[]" id="region" value="region" /><label for="region">region</label><br/>';
		tables += '<input type="checkbox" name="layers[]" id="teste" value="teste" /><label for="teste">teste</label><br/>';
		tables += '<input type="checkbox" name="layers[]" id="alo" value="alo" /><label for="alo">alo</label><br/>';
		tables += '</div>';
	return tables;
}



$(function (onLoad) {
	$('#camadas').live('click', function (e) {
		    console.log("layers");
		    $('#bld').remove();
	    	$('#ibjs').remove();
	    	$('#combo').remove();
	    	$('#res').remove(); //resultado da consulta
	    		//textareas
	    	$('#select').remove();
	    	$('#where').remove();
	    	$('#espacial').remove();
	    	$('#layers').remove();
	    	$('#tables').remove();
	    	$('#modes').remove();
	    	$('#btns').remove();
	    	$('#types').remove();
	    	
		    //cria o mapa
	    	map = $("#map").geomap( {
	    		bbox: [ -174.57275139372243, 7.166177986562425, -18.479001393763934, 70.13285651763942 ],
	    		zoom: 3,
	    		drawStyle: { color: "#000099", strokeWidth: "2px", fillOpacity:".4" },
	    		mode: "pan",
	    		shape: function( e, geo ) {
	    			
	    			//make a copy of the current drawStyle
	    			var drawStyle = $.extend( { }, map.geomap( "option", "drawStyle" ) );
	    			
	    			//desenha o retângulo com o estilo definido e escreve Q1, Q2 etc perto dele e em negrito
	    			map.geomap("append", geo, drawStyle, "<b id='q'>Q"+(q+1)+"</b>");
	    			shape[q] = geo;
	    			coord[q] = geo.coordinates; //arraylist que guarda as coordenadas do retângulo desenhado pelo usuário
	    			type[q] = geo.type; //guarda os tipos das coordenadas (ponto ou polígono)
	    			tables[q] = table; //guarda a tabela (city, nation, region) de cada retângulo - nível espacial de cada consulta
	    			columns[q] = comboValue;
	    			
	            	queryText(q, comboText, queryType);
	            	
	    			q++;
	    		} //fim shape
	    	});
	    	
	    	//mostra as opções de tabela (city, nation, region)
	        $("#write").append(createTables());
	        $("#tables").buttonset();
	});
	
	$("#tables select").live('click', function(e) {
    	console.log($(this).val());
    	//comboValue = $(this).val();
    	//comboText = $("#combo option:selected").text();
    });
    
});