//funções
function createModes()
{
	var modes = '<div id="modes" class="left">';
		modes += '<input type="radio" id="pan" name="mode" value="pan" checked="checked"/><label for="pan">pan</label>';
		modes += '<input type="radio" id="dragBox" name="mode" value="dragBox" /><label for="dragBox">draw rect</label>';
		modes += '</div>';
	return modes;
}

function createBtns()
{
	var btns = '<div id="btns">';
		btns += '<input type="button" id="btn-clear" name="clear" value="clear"/>';
		btns += '<div class="clear"></div>';
		btns += '<input type="button" id="query" name="query" value="execute"/>';
		btns += '</div>';
	return btns;
}


function createTypes()
{
	var types = '<div id="types" class="left">';
		types += '<input type="radio" id="irq" name="type" value="irq" checked="checked"/><label for="irq">intersects</label>';
		types += '<input type="radio" id="crq" name="type" value="crq" /><label for="crq">within</label>';
		types += '<input type="radio" id="erq" name="type" value="erq"/><label for="erq">inside of</label>';
		types += '</div>';
	return types;
}

function createCombo(atribs, atrib)
{
	var combo = ''; 
		combo += '<div id="combo">';
		combo += '<h4>Select the attribute</h4>';
		combo += '<select size="1" name="D1">';
		
		for(var a=0; a<atrib.length; a++)
		{
			if(a == 0)
				combo += '<option selected value="'+atrib[a]+'">'+atribs[a]+'</option>';
			else
				combo += '<option value="'+atrib[a]+'">'+atribs[a]+'</option>';
		}
		combo += '</select></div><br/>';
	return combo;
}

function separar(dado)
{
	var str = dado.split(", ");
	//console.log(str);
	return str;
}

function createTables()
{
	var tables = '';
	
	var dd = "cmd=queryLayers&ibj="+ibj;
		
	$.ajax({  
	    type: "post",
	    url: "Controller",
	    dataType: "json",
	    data: dd,
	    success: function(data) {
	    	$("#load").remove();
	    	var result = separar(data.geoms);
	    	tables += '<div class="clear"></div><div id="tables" class="left">';
			tables += '<h3>Select the layer(s): </h3>';
			//verificação dos atributos do índice escolhido. Mostrar apenas as camadas existentes no índice.
			for(var r=0; r<result.length; r++)
			{
				tables += '<input type="checkbox" name="layers[]" id="'+result[r]+'" value="'+result[r]+'"/><label for="'+result[r]+'">'+result[r]+'</label><br/>';
			}
			//tables += '<input type="checkbox" name="layers[]" id="nation" value="nation" /><label for="nation">nation</label><br/>';
			//tables += '<input type="checkbox" name="layers[]" id="region" value="region" /><label for="region">region</label><br/>';
			tables += '</div>';
			
			$("#write").append(tables);
	        $("#tables").buttonset();
	    },
	    beforeSend: function(){
	    	$('#write').append('<h5 id="load">Loading...</h5>');
	    },
	    error: function(data){
	    	console.log("erro - consultar - camadas");
	    	console.log(data);
	    }
	});
}

function mostrarIbjs(indexes)
{
	var rd = '<div id="ibjs" class="left">';
		rd += '<h3> Select index: </h3>';
		for(var c=0; c<indexes.length; c++)
			rd += '<input class="radio" type="radio" name="indices" id="'+indexes[c]+'" value="'+indexes[c]+'"/><label for="'+indexes[c]+'">'+indexes[c]+'</label><br/>';
		rd += '</div>';
	return rd;
}

function pegarIbjs() 
{	
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
	$('#draw').remove();
	
	//pega todos os ibjs existentes
	var dt = "cmd=ibjs";
	
	$.ajax({  
        type: "post",
        url: "Controller",
        dataType: "json",
        data: dt,
        success: function(data) {
        	var indices = separar(data.ibjs);
            var idx = mostrarIbjs(indices);
            
            //mostra os ibjs
            $("#write").append(idx);
    	    $("#ibjs").buttonset();
        },
        beforeSend: function(){
        	//alert("antes");
        },
        error: function(data){
        	console.log("erro - consultar - ibjs");
        	console.log(data);
        }
	});
}

//inicia a textarea
function initiateText(indice)
{
	//ibj -> preciso saber os atributos do índice para poder imprimir os atributos e o sql
	var dd = "cmd=queryText&ibj="+indice;
	
	$.ajax({  
	    type: "post",
	    url: "Controller",
	    dataType: "json",
	    data: dd,
	    success: function(data) {
			$("textarea[name=\"select\"]").val($("textarea[name=\"select\"]").val() + data.sql);
	    },
	    beforeSend: function(){
	    	//alert("antes");
	    },
	    error: function(data){
	    	console.log("erro - consultar");
	    	console.log(data);
	    }
	});
}

//preenche a textarea conforme a consulta
function queryText(w, geo_type, query_type)
{
	if(w==0)
		$("#write").append("<textarea id='espacial' name='espacial'></textarea>");
	
	//verificar se é irq, crq ou erq para, então, preencher a textarea
	if("irq" == query_type.valueOf())
	{
		if(w>0)
			$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() +"OR INTERSECTS (Q"+(w+1)+", "+geo_type+")\n");
		else
		{
	    	$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() + "INTERSECTS (Q"+(w+1)+", "+geo_type+")\n");
		}
	}
	else if("crq" == query_type.valueOf())
	{
		if(w>0)
			$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() +"OR WITHIN (Q"+(w+1)+", "+geo_type+")\n");
		else
		{
	    	$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() + "WITHIN (Q"+(w+1)+", "+geo_type+")\n");
		}
	}
	else
	{
		if(w>0)
			$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() +"OR INSIDE OF (Q"+(w+1)+", "+geo_type+")\n");
		else
		{
			$("textarea[name=\"espacial\"]").val($("textarea[name=\"espacial\"]").val() + "INSIDE OF (Q"+(w+1)+", "+geo_type+")\n");
		}
	}
}

function separarLinhas(linha)
{
	var str = linha.split("],[");
	//console.log(str);
	return str;
}

function mostrarResultado(linhas)
{
	var res = '<div id="res" class="left">';
		if(linhas.valueOf() == "")
		{
			res += '<h3>Nenhum resultado obtido.</h3>';
		}
		else
		{
			
			res += '<h3 align="center">Resultado da consulta</h3>';
			res += '<table id="tbl" border="1.7" cellpadding="5" align="center">';
			for(var l=0; l<linhas.length; l++)
			{
				res += '<tr>';
				var colunas = separar(linhas[l]);
				for(var c=0; c<colunas.length; c++)
				{
					res += '<td>'+colunas[c]+'</td>';
				}
				res += '</tr>';
			}
			res += '</table>';
		}
		res += '</div>';
	return res;
}

function ajaxConsulta(coords, types, grid, cols){
	  /* geo.coordinates[0][0] 	//xmin, ymin
	   * geo.coordinates[0][1] 	//xmin, ymax
	   * geo.coordinates[0][2] 	//xmax, ymax
	   * geo.coordinates[0][3] 	//xmax, ymin */
	
	  //console.log(coords);
	  //console.log(coords[0][0]);
	  //console.log(types);
	  //console.log(types[0]);
	  
	  for(var aux=0; aux<coords.length; aux++)
	  {
		  if(types[aux] == "Polygon")
		  {
		  	  dados += "&type"+aux+"=polygon&atributo"+aux+"="+cols[aux]+"&table"+aux+"="+grid[aux]+
		  	  "&a0"+aux+"="+coords[aux][0][0][0]+"&a1"+aux+"="+coords[aux][0][0][1]+
		  	  "&b0"+aux+"="+coords[aux][0][2][0]+"&b1"+aux+"="+coords[aux][0][2][1];
		  	  //console.log("dados :"+dados);
		  	  //xmin, ymin, xmax, ymax
		  	  /*a00 == a0 da janela 0 --> a0 == xmin
		  	   *a10 == a1 da janela 0 --> a1 == ymin
		  	   *b00 == b0 da janela 0 --> b0 == xmax
		  	   *b10 == b1 da janela 0 --> b1 == ymax*/
		  }
		  else //geo.type == "Point"
		  {
			  dados += "janelas="+q+"&type=point&a0="+coords[0][0]+"&a1="+ 
			  coords[0][1]+table;
			  //x, y
		  }
	  }
	  
	  $('input:radio[name=indices]').each(function() {
	      //Verifica qual está selecionado
	      if ($(this).is(':checked'))
	          dados += "&ibj="+$(this).val();
	  });

	  dados += "&queryType="+queryType+"&q="+coords.length;
	  console.log("dados: "+dados);
	  
	  $.ajax({  
      type: "post",
      url: "Controller",
      dataType: "json",
      data: dados,
      success: function(data) {
      	//realiza a consulta
    	//console.log(data.result);
    	result = separarLinhas(data.result);
    	$('#imgs').remove();
    	//se o usuário tinha realizado a construção de um índice e depois decidiu fazer uma consulta, as opções de construção de índice devem ser removidas, para assim ficar apenas a consulta
    	$('#bld').remove();
    	//caso o usuário clique novamente no botão de consulta (apesar de já estar na consulta)
    	$('#draw').remove();
    	$('#ibjs').remove();
    	$('#combo').remove();
    	$('#res').remove(); //resultado da consulta
    		//textareas
    	$('#select').remove();
    	$('#where').remove();
    	$('#espacial').remove();
    	//$('#tabs').remove();
    	$('#tables').remove();
    	
    	$('#build').append('<h4 id="query-succ">Consulta realizada com sucesso.</h4>');
    	    	
      },
      beforeSend: function(){
      	//alert("antes");
      },
      error: function(data){
      	console.log("erro - consultar");
      	console.log(data);
      }
	});
} //fim função ajaxConsulta

function createTabs()
{
	var tabs = '<div id="tabs" class="left ">';
		tabs += '<div class="tabs"><p><b><a id="indices" href="#">index</a></b></p></div>';
		tabs += '<div class="tabs"><p><b><a id="camadas" href="#">layers</a></b></p></div>';
		tabs += '<div class="tabs"><p><b><a id="consulta" href="#">query</a></b></p></div>';
		tabs += '<div class="tabs"><p><b><a id="resultado" href="#">results</a></b></p></div>';
		tabs += '</div>';
	return tabs;
}

//variáveis
var table = "";
var queryType = "irq";
var dados = "cmd=query";
var q=0;
shape = new Array();
coord = new Array();
type = new Array();
tables = new Array();
columns = new Array();
var map;
var ibj = "";
var comboValue;
var comboText;
var result;

$(function (onLoad) {
	// jQuery UI for pretty buttons
    $("#options").buttonset();
               
    $("#btn-query").click(function(){
    	q = 0;
    	table = "";
    	queryType = "irq";
    	dados = "cmd=query";
    	shape = new Array();
    	coord = new Array();
    	type = new Array();
    	tables = new Array();
    	columns = new Array();
    	map;
    	ibj = "";
    	comboValue = "";
    	comboText = "";
    	
    	console.log("consultar");
    	
    	$('#imgs').remove();
    	//se o usuário tinha realizado a construção de um índice e depois decidiu fazer uma consulta, as opções de construção de índice devem ser removidas, para assim ficar apenas a consulta
    	$('#bld').remove();
    	//caso o usuário clique novamente no botão de consulta (apesar de já estar na consulta)
    	//$('#draw').remove();
    	$('#ibjs').remove();
    	$('#combo').remove();
    	$('#res').remove(); //resultado da consulta
    		//textareas
    	$('#select').remove();
    	$('#where').remove();
    	$('#espacial').remove();
    	$('#tables').remove();
    	$('#tabs').remove();
    	$('#map').remove();
    	
    	$("#abas").append(createTabs());
    	$(".tabs").buttonset();
    	
    	//já inicia a consulta com a aba 'index'
    	ibj = "";
    	$("#indices").click(); //chama o "método" como se a aba tivesse sido clicada
    });
    
    $('#indices').live('click', function (e) {
    	//mostra os ibjs existentes
        pegarIbjs();
    });
    
    $('#camadas').live('click', function (e) {
	    //console.log("layers");
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
    	
    	//adiciona as divs que serão utilizadas
    	$("#all").append('<div id="draw"></div>');
    	$("#draw").append('<div id="map"></div><br/><br/>');
    	
    	
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
    			//console.log("table: "+table);
    			
            	queryText(q, comboText, queryType);
            	
    			q++;
    		} //fim shape
    	});
	    	
    	//mostra as opções de tabela (city, nation, region)
    	createTables();
	});
	
	$("#tables select").live('click', function(e) {
		//console.log($(this).val());
		comboValue = $(this).val();
		comboText = $("#combo option:selected").text();
	});
	
	$('#resultado').live('click', function(e) {
    	$('#imgs').remove();
    	//se o usuário tinha realizado a construção de um índice e depois decidiu fazer uma consulta, as opções de construção de índice devem ser removidas, para assim ficar apenas a consulta
    	$('#bld').remove();
    	//caso o usuário clique novamente no botão de consulta (apesar de já estar na consulta)
    	$('#draw').remove();
    	$('#ibjs').remove();
    	$('#combo').remove();
    	$('#res').remove(); //resultado da consulta
    		//textareas
    	$('#select').remove();
    	$('#where').remove();
    	$('#espacial').remove();
    	//$('#tabs').remove();
    	$('#tables').remove();
    	$('#query-succ').remove();
		$('#build').append(mostrarResultado(result));
	});
    
    $('#consulta').live('click', function (e) {
		  	//console.log("query");
		  	q = 0;
	    	queryType = "irq";
	    	dados = "cmd=query";
	    	shape = new Array();
	    	coord = new Array();
	    	type = new Array();
	    	tables = new Array();
	    	columns = new Array();
	    	comboValue = "";
	    	comboText = "";
	    	
	    	 dados += "&ibj="+ibj;
			    var dd = "cmd=queryIndexAttrt&ibj="+ibj;
		    	
		    	$.ajax({  
		    	    type: "post",
		    	    url: "Controller",
		    	    dataType: "json",
		    	    data: dd,
		    	    success: function(data) {
		    	    	$('#load').remove();
		    	    	var attrs = separar(data.attributes);
		    	    	var attr = separar(data.attribute);
		    	    	//console.log(attrs);
		            	$("#write").append(createCombo(attrs, attr));
		                //$("#combo").buttonset();
		            	comboValue = $("#combo option:selected").val();
		            	comboText = $("#combo option:selected").text();
		            	$("#write").append("<textarea id='select' name='select'></textarea>");
		            	$("#write").append("<textarea id='where' name='where'></textarea>");
		        		$("textarea[name=\"where\"]").val($("textarea[name=\"where\"]").val() + "WHERE ");
		            	initiateText(ibj);
		    	    },
		    	    beforeSend: function(){
		    	    	$('#write').append('<h5 id="load">Loading...</h5>');
		    	    },
		    	    error: function(data){
		    	    	console.log("erro - consultar");
		    	    	console.log(data);
		    	    }
		    	});  
	    	    	
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
	    	
	    	//opções mode: pan ou draw rect (desenhar retângulo)
	    	$("#map").append(createModes());
	    	$("#modes").buttonset();
	    		    	
	    	//botões: clear (limpa tudo o que está no mapa) e execute (executa a consulta)
	    	$("#map").append(createBtns());
	        $("#btns").buttonset();
	        
	        //limpa a propriedade float do css
	        $("#draw").append('<div class="clear"></div>');
	        
	        //tipos de consultas (irq, crq, erq)
	        $("#map").append(createTypes());
	        $("#types").buttonset();
    });
    
    $('#modes input').live('click', function (e) {
    	//console.log("mode: "+$(this).val());
    	map.geomap("option", "mode", $(this).val());
    });
    
    $('#tables input').live('click', function (e) {
        //console.log("table - query: "+$(this).val());
        table = $(this).val();
     });
    
    $('#types input').live('click', function (e) {
		  //console.log("type: "+$(this).val());
		  queryType = $(this).val();
     });
    
    $('#btn-clear').live('click', function (e) { 
    	//console.log("btn-clear - query");
    	//limpa os retângulos desenhados pelo usuário e a caixa de texto, desmarca as checkboxes 
    	//e reinicia o contador de retângulos.
		$(".radio").prop("checked", false);
	    $("#ibjs").buttonset();
    	
    	for(var y=0; y<shape.length; y++)
		{
			$("#map").geomap("remove", shape[y]);
		}

    	$('#select').remove();
    	$('#where').remove();
    	$('#espacial').remove();
    	$('#combo').remove();
		map.geomap("refresh");
		table = "";
    	queryType = "irq";
    	dados = "cmd=query";
    	shape = new Array();
    	coord = new Array();
    	type = new Array();
    	tables = new Array();
    	columns = new Array();
    	map;
    	ibj = "";
    	comboValue = "";
    	comboText = "";
    	q = 0;
		
		//mostrar ibjs
		pegarIbjs();
	});
    
    $('#query').live('click', function(e) {
    	//console.log(tables);
    	//console.log(columns);
    	dados += "&select="+$("textarea[name=\"select\"]").val()+"&where="+$("textarea[name=\"where\"]").val();
    	//console.log($("textarea[name=\"where\"]").val());
        ajaxConsulta(coord, type, tables, columns); 
    }); 
    
    $('#ibjs input').live('click', function(e) {
    	$("#choption").remove();
    	ibj = $(this).val();
    	$('#ibjs').append('<br/><h4 id="choption">Índice escolhido: '+ibj+'</h4>');
    	//console.log("ibj: "+ibj);
    });
    

    $("#combo select").live('click', function(e) {
    	//console.log($(this).val());
    	comboValue = $(this).val();
    	comboText = $("#combo option:selected").text();
    });
});