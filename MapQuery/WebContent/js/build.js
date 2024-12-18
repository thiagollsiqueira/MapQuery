function createDiv(spatial, conventional)
{
	var div = '';
		//nome do índice
		div += '<h3> Nome do índice: </h3>';
		div += '<input id="nomeIndice" name="nomeIndice" style="width: 98px;">';
		
		//seleção de atributos
		div += '<h3> Selecione os atributos: </h3>';
		div += '<div class="clear"></div>';
		//espaciais
		div += '<div id="espaciais">';
			div += '<h4>espaciais</h4>';
			for(var s=0; s<spatial.length; s++)
				div += '<input class="espacial" type="checkbox" name="colunas[]" id="'+spatial[s]+'" value="'+spatial[s]+'" /><label class="lbl-espacial" for="'+spatial[s]+'">'+spatial[s]+'</label><br/>';
		div += '</div>';
		//convencionais
		div += '<div id="convencionais">';
			div += '<h4>convencionais</h4>';
			for(var c=0; c<conventional.length; c++)
				div += '<input class="convencional" type="checkbox" name="colunas[]" id="'+conventional[c]+'" value="'+conventional[c]+'" /><label class="lbl-convencional" for="'+conventional[c]+'">'+conventional[c]+'</label><br/>';
		div += '</div>';
		//botão de construção
		div += '<div id="btns-bld" class="right">';
			div += '<br/><input type="button" id="exeBuild" name="build" value="build"/>';
		div += '</div>';
	return div;
}

function separarAtributos(dado)
{
	var str = dado.split(", ");
	//console.log(str);
	return str;
}


//variáveis
var bld = '';
geos = new Array();
var fks;
var verd = 0;

$(function (onLoad) {	
	//apenas quando a página for recarregada/atualizada, haverá consulta ao banco de dados para posteriormente serem mostradas as colunas das tabelas existentes.
	//para isso, foi criada uma variável 'global' que armazenada a div já com as colunas (depois do acesso ao banco). 
	//nas outras vezes que o usuário selecionar para construir um índice, deve ser mostrada apenas essa div (sem mais acessos ao banco).
	var dados = "cmd=buildOptions";
    
    $.ajax({  
        type: "post",
        url: "Controller",
        dataType: "json",
        data: dados,
        success: function(data) {
        	//console.log(data);
        	var colunasEspaciais = separarAtributos(data.espaciais);
        	var colunasConvencionais = separarAtributos(data.convencionais);
        	
            bld = createDiv(colunasEspaciais, colunasConvencionais);
        },
        error: function(data){
        	console.log("erro - build");
        	console.log(data);
        }
	});
    
    $("#btn-build").click(function(){
    	//console.log("construir");
    	
    	//caso o usuário tenho clicado em consultar e depois decidiu contruir outro índice, o mapa e as opções de consulta devem ser removidos
    	$('#imgs').remove();
    	$('#draw').remove();
    	$('#bld').remove();
    	$('#sql').remove();
    	$('#ibjs').remove();
    	$('#combo').remove();
    	$('#res').remove(); //resultado da consulta
    	$('#tabs').remove();
    	
    	//cria a div que será utilizada
	    $("#build").append('<div id="bld" class="left"></div>');
	    
	    //imprime as opções (colunas/atributos) para a construção do índice
        $("#bld").append(bld);
        $("#bld").buttonset();
    });
    
    $('#espaciais input').live('click', function (e) {
    	//caso o usuário tenha tirado uma das opções que foi escolhida anteriormente, a seleção dos atributos convencionais é atualizada conforme a escolha atual dele
    	$(".convencional").prop("checked", false);
		$(".convencional").attr("disabled",false);
		
    	//console.log("-------------");
    	$('.espacial:checkbox').each(function() {
    		
    		//verifica qual está selecionado
    		if ($(this).is(':checked'))
	  	    {   
    			//console.log("each: "+$(this).val());
    			
    			//são retornadas as chaves estrangeiras
    			var dd = "cmd=verifyAttr&espacial="+$(this).val();
    	    	
    	    	$.ajax({  
    		        type: "post",
    		        url: "Controller",
    		        dataType: "json",
    		        data: dd,
    		        success: function(data) {
    		        	//selecionar fks
    		        	fks = separarAtributos(data.fks);
    		        	
    		        	for(var f=0; f<fks.length; f++)
    		        	{
    		        		$("#"+fks[f]).prop("checked", true);
    		        		$("#"+fks[f]).attr("disabled",true);
    		        	}
    		        	$("#convencionais").buttonset();
    		        },
    		        error: function(data){
    		        	console.log("erro - exeBuild");
    		        	console.log(data);
    		        }
    			});
	  	    }
  	  	});
    });
    
    //construção dos índices
	$('#exeBuild').live('click', function (e) {
    	console.log("exeBuild.click");
    	
    	var indexName = $('#nomeIndice').val();
    	//se o nome do índice não estiver vazio, será feita a construção
    	if(indexName != "")
    	{
	    	var dados = "cmd=build&name="+ indexName;
	    	
	    	atributos = new Array();
	    	//verifica quais elementos checkbox foram selecionados, estes são adicionados a um array
	    	$("input[type=checkbox][name='colunas[]']:checked").each(function(){
	    		atributos.push($(this).val());
	    	});
	    	dados += "&atributos=["+atributos+"]";
	    	//console.log(dados);
	    	
	    	$.ajax({  
		        type: "post",
		        url: "Controller",
		        dataType: "json",
		        data: dados,
		        success: function(data) {
		        	$('#bld').remove();
		        	$("#build").append('<div id="bld"><br/><br/><h3>'+data.result+'</h3></div>');
		        },
		        error: function(data){
		        	console.log("erro - exeBuild");
		        	console.log(data);
		        }
			});
    	}
    	else //se o nome está vazio, será indicado um erro
    	{
    		$('#bld').remove();
        	$("#build").append('<div id="bld"><br/><br/><h3>Erro na construção: nome vazio.</h3></div>');
    	}
    });    
});