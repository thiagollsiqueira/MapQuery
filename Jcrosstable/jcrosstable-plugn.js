(function($) {

	/*
	* Melhorar o codigo na parte de definir os eixos das linhas e das colunas
	*
	*/

	//eixo das colunas
	//var columnsAxis = new Array();
	//eixo das linhas
	//var rowAxis = new Array();
	
	//matriz dos dados
	var matriz = new Array();

	//opcoes corrente para construir a tabela
	var opts;

	/*
	*	Fun��o que grupa e devolve um map sendo o �ndice do map o valor agregado
	* primeiro argumento � o vetor de dados, segundo � o vetor de �ndices a serem agrupados 
	*
	*/
	function groupBy(data, indices) {
		var o = "";
		var other = {};
		$.each(data, function(i, value) {
			$.each(indices, function(j, index) {				
				o += data[i][index] + "-";
			});
			if (!(o in other))
				other[o] = [];
			other[o].push(data[i]);
			o = "";
		});
		return other;			
	}
	
	function countStartWith(data, filter) {
		var count = 0;
		var patt = new RegExp("^"+filter);
		$.each(data, function(i, value) {			
			if(i.match(patt))
				++count;
		});
		return count;
	}
	
	function filter(data, filter) {
		var obj = {};
		patt = new RegExp("^"+filter);
		$.each(data, function(index, value) {			
			if(index.match(patt)) {
				obj[index] = data[index];
			}
		});
		return obj;
	}
				
	//aqui � onde as medidas s�o montadas em cada td (celula da tabela)
	function measures(obj) {
		if(obj != undefined) {					
			var mea = {};
			var respon = new Array();
			//cada medida � visualizada em uma div
			$.each(opts.measures, function(j, m) {
				//AQUI AGRUPA DE ACORDO COM A FUN��O DE AGREGA��O ESCOLHIDA
				//TEM QUE VALIDAR SE EXISTE MESMO ESSES PARAMETROS 
				//SE N�O TIVER FUN��O DE AGREGA��O, MOSTRA TODOS OS VALORES UM DO LADO DO OUTRO COMO STRINGS...
				//isso dexar� mais flexivel, pois pode montar uma crosstab soh de strings
				if(aggFunctions[m.aggregateFunction]) {
					mea = aggFunctions[m.aggregateFunction](obj, m.index-1);
					respon.push(mea);
				}
				else {
					var txt = "";
					for(var i = 0; i< obj.length;i++) {
						//pode ser dentro de SPAM... daih pode ver como fica melhor.
						txt += " " + obj[i][m.index-1];
					}
					respon.push(txt);
				}					
			});
			return respon;
		} else {			
			return;
		}	
	}
	
	//fun��es de agrega��es....
	var aggFunctions = {
		//COUNT - CONTA OS VALORES QUE TEM NO ARRAY...
		count:function(arr, measure) {
			return arr.length;
		},
		//SUM - SOMA TODOS OS VALORES DE UMA MEDIDA
		sum:function(arr, measure) {
			var value = 0;
			for (var i=0;i<arr.length;i++) { value += arr[i][measure]; }
			return value;
		},
		//PRODUCT - MULTIPLA TODOS OS VALORES DE UMA MEDIDA
		product:function(arr, measure) {
			var value = 1;
			for (var i=0;i<arr.length;i++) { value *= arr[i][measure]; }
			return value;
		},
		//AVG - M�DIA ARITM�TICA DE UMA MEDIDA
		avg:function(arr, measure) {
			var value = 0;
			for (var i=0;i<arr.length;i++) { value += arr[i][measure]; }
			value = (arr.length ? value / arr.length : 0);
			return value;
		},
		//MAX - N�MERO MAIOR DE UMA MEDIDA
		max:function(arr, measure) {
			var value = Number.MIN_VALUE;
			for (var i=0;i<arr.length;i++) if (arr[i][measure] > value) { value = arr[i][measure]; }
			return value;
		},
		//MIN - N�MERO MENOR DE UMA MEDIDA
		min:function(arr, measure) {
			var value = Number.MAX_VALUE;
			for (var i=0;i<arr.length;i++) if (arr[i][measure] < value) { value = arr[i][measure]; }
			return value;
		},
		//DISTINCT - QUANTIDADE DE N�MEROS DISTINTOS DE UMA MEDIDA
		distinct:function(arr, measure) {
			var value = 0;
			var values = {};
			for (var i=0;i<arr.length;i++) { values[arr[i][measure]] = 1; }
			for (p in values) { value++; }
			return value;
		},
		//STDDEV - DESVIO PADR�O DE UMA MEDIDA
		stddev:function(arr, measure) {
			var v = aggFunctions["variance"](arr, measure);
			return Math.sqrt(v);
		},
		//VARIANCE - VARIANCIA DE UMA MEDIDA
		variance:function(arr, measure) {
			if (arr.length < 2) { return 0; }
			var value = 0;
			var avg = aggFunctions["avg"](arr, measure);
			for (var i=0;i<arr.length;i++) { value += (arr[i][measure]-avg)*(arr[i][measure]-avg); }
			return (value / (arr.length-1));
		},
		//MEDIAN - MEDIANA DE UMA MEDIDA
		median:function(arr, measure) {
			var vector = [];
			for(var i=0;i<arr.length;i++) {vector.push(arr[i][measure]);}
			var sorted = vector.sort(function(a,b){return a-b;});
			var i = Math.floor(vector.length/2);
			return sorted[i];
		},
		//MODE - MODO PADR�O DA MEDIDA
		mode:function(arr, measure) {
			var conversion = {};
			for (var i=0;i<arr.length;i++) {
				var val = arr[i][measure];
				var index = val+"";
				if (!(index in conversion)) { conversion[index] = 1; } else { conversion[index]++; }
			}
			var max = 0;
			var prop = "";
			for (var p in conversion) {
				var cnt = conversion[p];
				if (cnt > max) {
					max = cnt;
					prop = p;
				}
			}
			return parseFloat(prop);
		}
	};

	function getVectorColumn(x, begin, end) {
		var ret = new Array();		
		for(;end >= begin; begin++) {	
			//se ele tem algum valor e nao for totals ele agrega para o total da hierarquia corrente
			if(matriz[x][begin]!=undefined && matriz[x][begin][0].totals==undefined && matriz[x][begin][0].invalid==undefined) {
				ret.push(matriz[x][begin]);
			} 
		}	
		return ret;
	} 

	//aqui eh o cruzamento de totals
	function getVectorColumnTotal(x, begin, end) {
		var ret = new Array();		
		if(begin!=0)
			begin = begin+1;
		var aux = new Array();
		for(;end >= begin; begin++) {	
			//se ele tem algum valor e nao for totals ele agrega para o total da hierarquia corrente
			if(matriz[x][begin]!=undefined && matriz[x][begin][0].totals!=undefined && matriz[x][begin][0].invalid==undefined) {
				aux = new Array();
				$.each(matriz[x][begin], function(i, v) {
					aux.push(v.value);
				});
				ret.push(aux);
			}
		}	
		return ret;
	}
	//estou assumindo que deve descartar todas as medidas do totals... mas da pra reaproveitar futuramente
	//para agregar somente as medidas que tem dados numericos, tem que percorrer o vetor deles e verificar	
	function getVectorRow(column, begin, end) {
		var ret = new Array();		
		for(;end >= begin; begin++) {	
			//se ele tem algum valor e nao for totals ele agrega para o total da hierarquia corrente
			if(matriz[begin][column]!=undefined && matriz[begin][column][0].totals==undefined && matriz[begin][column][0].invalid==undefined)
				ret.push(matriz[begin][column]);
		}		
		return ret;
	}
		
	function totalMeasures(obj) {
		if(obj != undefined) {					
			var mea = {};
			var respon = new Array();
			//cada medida � visualizada em uma div
			$.each(opts.measures, function(j, m) {
				//AQUI AGRUPA DE ACORDO COM A FUN��O DE AGREGA��O ESCOLHIDA
				//TEM QUE VALIDAR SE EXISTE MESMO ESSES PARAMETROS 

				//AQUI TEM QUE PEGAR A FUNCAO DE AGREGACAO DO TOTAL...				
				if(aggFunctions[m.aggregateTotalFunction]) {
					mea = {totals:true, value:aggFunctions[m.aggregateTotalFunction](obj, j)};
					respon.push(mea);
				}
				else {
					respon.push = {totals:true, value:""};
				}					
			});
			return respon;
		} else {			
			return;
		}	
	}

	//construcao do cabecalho do eixo das colunas no tbody
	function constructionColumnsHeader(tbody, firstTh) {			
		var indices = new Array();
		var result = new Array();	
		$.each(opts.columns, function(i, value) {							
			indices.push(value.index-1);
			result[i] = groupBy(opts.data, indices); //aplicar a ordena��o aqui!			
		});	
		var obj = result[0];
		var allValuesColumns = {};

		var allColumnsHeader = new Array();
		var counn = 0;

		function runColumns(obj, result, k, allValuesColumns) {
			var th;
			$.each(obj, function(index, value) {

				count = countStartWith(result[result.length-1], index);
				//CONDICAO PARA ENTRAR SOMENTE SE EXISTIR O TOTALS ASSOCIADO A COLUNA
				if(count>1 && (result.length-k) > 2 && opts.measures[0].aggregateTotalFunction!=undefined) { 
					for(var i = k; i < result.length-2; i++) 
						count = (count+countStartWith(result[i+1], index)); //calculo para o colspan			
				}			
					

				th = $("<th>").attr({
					colspan:count}).text(value[0][opts.columns[k].index-1]);
				if (!(k in allValuesColumns))
					allValuesColumns[k] = [];
		
				allValuesColumns[k].push({colTh: th});
		
				//se tiver mais coluna ainda.. continua montando o header
				if(k != result.length-1) {		
					runColumns(filter(result[k+1], index), result, k+1, allValuesColumns);
			
					//CONDICAO PARA ENTRAR SOMENTE SE EXISTIR O TOTALS ASSOCIADO A COLUNA
					if(opts.measures[0].aggregateTotalFunction!=undefined) {
						th = $("<th>").attr({
								rowspan:(result.length-k)}).text("TOTAL for " + value[0][opts.columns[k].index-1]);
						counn++;				
						allValuesColumns[k].push({colTh: th, endIndex: counn});		
						//TOTAL para cada escala de hierarquia no eixo das colunas
						allColumnsHeader.push({indexColumn: index, totals: true, begin:0, end:0}); 
					}
				} else {	
					counn++;		
					allColumnsHeader.push({indexColumn: index, totals: false});				
				}
			});
		}

		runColumns(obj, result, 0, allValuesColumns);
		var total = 1;
		var tr;
		$.each(allValuesColumns, function(index, value) {				
			tr = $("<tr>");
			if(index==0) {
				tr.append(firstTh); 
			}
			var before=0;				
			//aqui que vai fazer as opera��es de slice-and-dice, pivoting, ordena��es e etc
			var th = $("<th>").attr({
				class:"columns", 
				colspan:1});
				//primeiro valor � sobre o �ndice da coluna para manipular o PIVOTING!
			th.append($("<span>").attr({class:"pivoting-draggable pivoting-droppable", id:opts.columns[index].index}).text(opts.columns[index].text));
			tr.append(th);
			
			$.each(value, function(i, v) {
				if(v.endIndex != undefined) {							
					//, begin:0, end:0									
					allColumnsHeader[v.endIndex-1].end = v.endIndex-1;
					if(before!=0)
						allColumnsHeader[v.endIndex-1].begin = value[before].endIndex-1;
					before = i;				
				}
				tr.append(v.colTh);
				total++;
			});
			tbody.append(tr);
		});
		return {header: allColumnsHeader, totals:total};		
	}
	
	function constructionRowsHeader(tbody, total) {
		var tr = $("<tr>");
		var result = new Array();	
		var indices = new Array();	
		var th;
		$.each(opts.rows, function(i, value) {
			//aqui que vai fazer as opera��es de slice-and-dice, pivoting, ordena��es e etc
			th = $("<th>").attr({
				rowspan:1});	
				//primeiro valor � sobre o �ndice da coluna para manipular o PIVOTING! NO ID!	
			th.append($("<div>").attr({class:"pivoting-draggable pivoting-droppable", id:opts.rows[i].index}).text(value.text));
			tr.append(th);			
				
			indices.push(value.index-1);
			result[i] = groupBy(opts.data, indices); //aplicar a ordena��o aqui!						
		});			
		var counn = 0;

		var thBefore = new Array();	
		var allValuesRows = {};
		var before = new Array();
		function runRows(obj, result, k) {	
			var th;
			var aux = 0; //saber se � o primeiro elemento de cada hierarquia
			var totals;
			$.each(obj, function(index, value) {
				//se tiver no ultimo nivel da hierarquia (result.length-1)
				if(k == result.length-1) {
					var tr = $("<tr>");
					if(aux==0) {
						$.each(thBefore, function(i, v) {
							tr.append(v);
						});
						thBefore = new Array();
					}
					th = $("<th>").attr({
						rowspan:1}).text(value[0][opts.rows[k].index-1]);
					tr.append(th);
				
					counn++;
								
					if (!(index in allValuesRows))
						allValuesRows[index] = [];
					allValuesRows[index].push({row:tr, totals:false});				
				} else {
					count = countStartWith(result[result.length-1], index);

					//CONDICAO PARA ENTRAR SOMENTE SE EXISTIR O TOTALS ASSOCIADO A COLUNA
					if(count>1 && (result.length-k) > 2 && opts.measures[0].aggregateTotalFunction!=undefined) { 
						for(var i = k; i < result.length-2; i++) 
							count = (count+countStartWith(result[i+1], index)); //calculo para o colspan		
					}	
				
					th = $("<th>").attr({
						rowspan:count}).text(value[0][opts.rows[k].index-1]);
					thBefore[k] = th;									
					runRows(filter(result[k+1], index), result, k+1);

					//CONDICAO PARA ENTRAR SOMENTE SE EXISTIR O TOTALS ASSOCIADO A COLUNA
					if(opts.measures[0].aggregateTotalFunction!=undefined) {
						var tr = $("<tr>");
						th = $("<th>").attr({
								colspan:(result.length-k)}).text("TOTAL for " + value[0][opts.rows[k].index-1]);	
						tr.append(th);	

						//COLUNA DOS TOTALS
						totals = {row: tr, totals:true, begin:0, end:counn};
						if (!(k in before))
							before[k] = [];
						before[k].push({end:counn,indexRow:index});
						counn++;

						if (!(index in allValuesRows))
							allValuesRows[index] = [];
						allValuesRows[index].push(totals);
					}
				
				}
				++aux;
			});
		}
		
		runRows(result[0], result, 0);	
		if(opts.columns.length>0)
			tr.append($("<td>").attr({
				class:"blank-space", rowspan:1,
				colspan:total}).text(""));
		//CONDICAO DE VERIFICAR SE TEM TOTALS
		if(opts.measures[0].aggregateTotalFunction != undefined) {
			function updateRowsTotalsIndex(allValuesRows) {
				var b;
				$.each(before, function(index, value) {	
					$.each(value, function(i, v) {
						//, begin:0, end:0									
						if(i!=0) {
							//{end:counn,indexRow:index}
							allValuesRows[v.indexRow][0].begin = value[b].end;		
						}						
						b = i;
					});
				});
			}
			updateRowsTotalsIndex(allValuesRows);		
		
		}
		tbody.append(tr);
		return allValuesRows;
	}	

	//calcula a funcao de agregacao e coloca na celula correspondente, LINHA POR LINHA
	function populateMeasureInCell(x, vectorMeasures, tr) {
		var td = $("<td>");		
		$.each(vectorMeasures, function(j, mea) {
			if(mea.value==undefined) { //valor normal
				td.append($("<div>").attr("class", j).text(mea));
			}
			else //valor de total
				td.append($("<div>").attr("class", j).text(mea.value));	
		});	
		matriz[x].push(vectorMeasures);	
		tr.append(td);
	}

	//funcao responsavel por montar a parte das medidas na tabela cruzada
	function computeMeasures(columns, rows, allColumnsHeader, allValuesRows, allGroup, tbody) {
		//se tiver colunas e linhas na tabela vai ser assim...		
		if(columns && rows) {			
			var x = 0; //eixo x das linhas
			$.each(allValuesRows, function(index, value) {						
				matriz[x] = new Array();								
				tr = value[0].row;
				tr.append($("<td>"));
				$.each(allColumnsHeader, function(i, v) {					
					if(allGroup[index+""+v.indexColumn] != undefined) {
						vectorMeasures = measures(allGroup[index+""+v.indexColumn]);
						populateMeasureInCell(x, vectorMeasures, tr);
					} else {						
						if(v.totals) { //totals do allColumnsHeader = {indexColumn: index, totals: true};
							//totals in columns, get from line a fix number of columns
							if(value[0].totals)
								vectorMeasures=totalMeasures(getVectorColumnTotal(x, v.begin, v.end));
							else
								vectorMeasures = totalMeasures(getVectorColumn(x, v.begin, v.end));
							populateMeasureInCell(x, vectorMeasures, tr);						
						}	//totals do allValuesRows = {row: tr, totals: true};
						else if(value[0].totals && !v.totals) {							
							vectorMeasures = totalMeasures(getVectorRow(i, value[0].begin, value[0].end-1));
							populateMeasureInCell(x, vectorMeasures, tr);		
						} else { //caso a tabela nao "feche"
							var td = $("<td>");	
							var ap = new Array();	
							$.each(opts.measures, function(i, m) {
								td.append($("<div>").attr("class", "sem nada").text(""));	
								ap.push({invalid:true});
							});	
							matriz[x].push(ap);	
							tr.append(td);
						}				
					}							
				});		
				x++;
				tbody.append(tr);	
			});	
		} else if (rows) {
			var x = 0;		
			$.each(allValuesRows, function(index, value) {
				matriz[x] = new Array();				
				tr = value[0].row;
				if(allGroup[index] != undefined) {					
					vectorMeasures = measures(allGroup[index]);
					populateMeasureInCell(x, vectorMeasures, tr);	
				} else {
					//totals do allValuesRows = {row: tr, totals: true};
					if(value[0].totals) {	
						vectorMeasures = totalMeasures(getVectorRow(0, value[0].begin, value[0].end-1));
						populateMeasureInCell(x, vectorMeasures, tr);		
					}
				}
				x++;
				tbody.append(tr);	
			});	
		} else if (columns) {
			tr = $("<tr>");
			//adiciona 2 td porque existem o nome da tabela e o nome das colunas			
			tr.append($("<td>").attr({class:"pivoting-droppable", id:"especial-row"}));
			tr.append($("<td>"));
			matriz[0] = new Array();
			$.each(allColumnsHeader, function(i, v) {
				if(allGroup[v.indexColumn] != undefined) {					
					vectorMeasures = measures(allGroup[v.indexColumn]);
					populateMeasureInCell(0, vectorMeasures, tr);	
				} else {
					//totals do allValuesRows = {row: tr, totals: true};
					if(v.totals) {	
						vectorMeasures = totalMeasures(getVectorColumn(0, v.begin, v.end));
						populateMeasureInCell(0, vectorMeasures, tr);		
					}
				}									
			});		
			tbody.append(tr);	
		}
	}		
		
	$.extend($.fn, {
		jcrosstable: function(options) {
			opts = $.extend({}, $.fn.jcrosstable_defaults, options);						
			var div = $(this);
			function drawCrossTable(div) {		
				var tab = $("<table>").attr({
					class: "crosstab",
					border: "1",
					cellspacing: "10"
				});	
				var tbody = $("<tbody>");
				
				var columns = 0;
				if(opts.columns.length >=1)
					columns = opts.columns.length;
					
				var rows = 0;
				if(opts.rows.length>=1)
					rows = opts.rows.length;		
										
				var firstTh = $("<th>").attr({
					rowspan:columns, 
					colspan:rows}).text(opts.title);	
				
				var indices = new Array();
				$.each(opts.rows, function(i, value) {
					indices.push(value.index-1);
				});
				$.each(opts.columns, function(i, value) {
					indices.push(value.index-1);
				});

				var allGroup = {};				
				allGroup = groupBy(opts.data, indices);	
				
				var tr;
				var hasFirstTh = false;	
				var total = 0;	
				var allColumnsHeader;
				//se tiver colunas.... para construir o cabe�alho da tabela da parte de colunas						
				if(columns>=1) {
					var col = constructionColumnsHeader(tbody, firstTh);//retorna o total de colunas montadas	
					total = col.totals;
					allColumnsHeader = col.header;		
					hasFirstTh = true;
				}
				
				//se tiver linhas.... vai ser um jeito de constru��o
				if(rows>=1) {
					//aqui entra somente se n�o tiver header em colunas
					if(!hasFirstTh) {
						tbody.append($("<tr>").append(firstTh).append($("<td>").attr({class:"pivoting-droppable", id:"especial-column"})));					
					}
					//construcao de parte do cabecalho do eixo das linhas
					var allValuesRows = constructionRowsHeader(tbody, total);
					
					if(columns>=1) {	
						//columns and rows					
						computeMeasures(true, true, allColumnsHeader, allValuesRows, allGroup, tbody);
					} else { 
						//only rows
						computeMeasures(false, true, undefined, allValuesRows, allGroup, tbody);						
					}
				} else { 
					//only columns
					computeMeasures(true, false, allColumnsHeader, undefined, allGroup, tbody);		
				}
				
				tab.append(tbody);
				
				div.append(tab);
				if(opts.pivoting)
					pivoting();
			}
			//desenha a jcrosstable
			drawCrossTable(div);
			function redrawCrossTable(div) {
				div.empty();
				drawCrossTable(div);
			}
			
			//fun��o que faz o pivoting... se ele quiser
			function pivoting() {
			
				//pivoting come�a aqui
				$(".pivoting-draggable").attr({style: "cursor: pointer;"});
				$(".pivoting-draggable").draggable({
					helper:"clone", 
					start: function(event, ui) {
						ui.helper.attr({class:"ui-widget-content pivoting"});
						
					}
				});			
				$(".pivoting-droppable").droppable({				
					activeClass: "ui-state-hover",
					hoverClass: "ui-state-active",
					tolerance: "pointer",
					drop: function(event, ui) {
					
						var column = true;
						var op = {};
								
						//fun��o para saber de qual eixo pertence o �ndice 0 para eixo das colunas e 1 para linhas
						function index(index) {
							$.each(opts.columns, function(i, v) {
								if(v.index == index) {
									column = true;
								}
							});
							$.each(opts.rows, function(i, v) {
								if(v.index == index) {
									column = false;
								}
							});
						}
						
						//fun��o que retorna as op��es do eixo que pertence o �ndice
						function getValueOfIndex(index) {
							$.each(opts.columns, function(i, v) {
								if(v.index == index) {
									op = v;
								}
							});
							$.each(opts.rows, function(i, v) {
								if(v.index == index) {
									op = v;
								}
							});
						}
						
						//aqui remove o origem daonde ele veio.. pois n�o vai ter mais nd
						function removeOrigin(originIndex) {
							index(originIndex);						
							if(column) {
								for(var i = 0; i < opts.columns.length; i++) {
									if(opts.columns[i].index == originIndex) {
										opts.columns.splice(i, 1);
										break;
									}
								};	
							} else {
								for(var i = 0; i < opts.rows.length; i++) {
									if(opts.rows[i].index == originIndex) {
										opts.rows.splice(i, 1);
										break;
									}
								};	
							}
						}			
						var originIndex = ui.draggable.attr("id");
						var destinyIndex = $(this).attr("id");							
						var meio = $(this).outerWidth()/2 + $(this).position().left;
						getValueOfIndex(originIndex);
						removeOrigin(originIndex);			

						if(destinyIndex=="especial-row") {
							opts.rows.push(op);						
						} else if (destinyIndex=="especial-column") {
							opts.columns.push(op);
						} else {					
							if(event.clientX>=meio){
								//agora vai adicionar o novo item na direita
								index(destinyIndex);						
								if(column) {	
									for(var i = 0; i < opts.columns.length; i++) {
										if(opts.columns[i].index == destinyIndex) {
											opts.columns.splice(i+1, 0, op);
											break;
										}
									};							
								} else {
									for(var i = 0; i < opts.rows.length; i++) {
										if(opts.rows[i].index == destinyIndex) {
											opts.rows.splice(i+1, 0, op);
											break;
										}
									};
								}			
							} else {
								//agora vai adicionar o novo item na esquerda	
								index(destinyIndex);						
								if(column) {						
									for(var i = 0; i < opts.columns.length; i++) {
										if(opts.columns[i].index == destinyIndex) {
											opts.columns.splice(i, 0, op);
											break;
										}
									};						
								} else {
									for(var i = 0; i < opts.rows.length; i++) {
										if(opts.rows[i].index == destinyIndex) {
											opts.rows.splice(i, 0, op);
											break;
										}
									};
								}		
							}
						}
						$('body').css("cursor", "auto");
						redrawCrossTable(div);		
						//chama a fun��o passada...
						opts.onPivoting("testando", "teste2");
					}
				});
				
			} //fim do pivoting
			
			return $(this);						
		},
		/*
		* plugn defaults
		*/
		jcrosstable_defaults: {
			data: [],
			columns: [],
			rows: [],
			measures: [],
			pivoting: true,
			onPivoting: function(columns, rows) {},
			filters: true,
			title: "T�tulo"
		}
		
	});	
}(jQuery));	
