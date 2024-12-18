#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <iostream>
#include "SBindex.h"
#include "Sbitvector.h"
#include "SpatialQueryWindow.h"

using namespace std;

void SBindex::openForCreation(char* path, int pagesize){
	PAGESIZE = pagesize; //parâmetro do Java

	file = fopen(path, "w+b"); //"wb"

	head = 4096;
    fseek(file, head, 0); //file pointer goes after the head

	page = (Sbitvector*) malloc (PAGESIZE);    

}

void SBindex::closeAfterCreation()
{
    free(buffer);  
	free(page);

    //write cardinality to file header
    fseek(file, 0, 0);

    int c[2];    
	c[0] = cardinality; //número de valores que tem no arquivo (total de registros)
	c[1] = PAGESIZE; //é necessário para saber de quantos em quantos registros percorrer para ler o arquivo

    fwrite(c, sizeof(int), 2, file); 
    fclose(file);

	//cout<<"\n";
}

int SBindex::sizeSbitvector(int pagesize)
{
	PAGESIZE = pagesize;
	
	ENTRY_SIZE = sizeof(Sbitvector);
	ENTRIES_PER_PAGE = PAGESIZE/ENTRY_SIZE;

	return ENTRIES_PER_PAGE;
}

//constructor: reads the key/mbr file to instantiate arrays of Sbitvector and store them in 4096 bytes pages
void SBindex::write(int* k, double *xm, double *ym, double *XM, double *YM, int length)
{	
	Sbitvector s;
	ENTRY_SIZE = sizeof(Sbitvector); //era 'size'
	ENTRIES_PER_PAGE = length; //era 'array_size'
	
	/*cout<<"ENTRIES_PER_PAGE*ENTRY_SIZE: "<<ENTRIES_PER_PAGE*ENTRY_SIZE<<endl;
	cout<<"ENTRIES_PER_PAGE: "<<ENTRIES_PER_PAGE;
	cout<<"  ENTRY_SIZE: "<<ENTRY_SIZE<<endl;
	cout<<"sizeof(s.getKey()): "<<sizeof(s.getKey())<<endl; */
	//exit(0); //teste das variáveis

	Sbitvector* buffer = (Sbitvector*) malloc (ENTRIES_PER_PAGE*ENTRY_SIZE); //4080bytes
	
	//lê os vetores k, xm, ym, XM, YM enviados pelo Java
	for(int i = 0; i < length; i++)
    {
		//verifica o que foi recebido
        //cout<<"key: " <<k[i]<<endl;//<< " xm: " <<xm[i]<< " ym: " <<ym[i]<< " XM: " <<XM[i]<< " YM: " <<YM[i] <<"\n";

        s.setKey(k[i]);
        s.setMbr(xm[i], ym[i], XM[i], YM[i]);
        buffer[i] = s;		

		//verifica o que foi escrito no buffer
		//cout<<"write - buffer[i]: "<<buffer[i].getKey()<<endl;
    }
	cardinality += length;
	//cout<<"write - cardinality: "<<cardinality<<endl;

    //copia conteudo do buffer para pagina
    memcpy (page, buffer, ENTRIES_PER_PAGE*ENTRY_SIZE);
	
    //escreve pagina no arquivo:
	fwrite(page, PAGESIZE, 1, file); //grava 1 objeto de PAGESIZE bytes de page no arquivo file
	//constructionDA++;
	
	//cout<<"\n";
	return;
}

int* SBindex::scanCrq(char *path, double *qWindow)
{
	cout<<"\nscanCrq - inicio"<<endl;

	SpatialQueryWindow sqw(qWindow);

	/*cout<<"querywindow[0]: "<<sqw.getQueryWindow()[0]<<endl;
	cout<<"querywindow[1]: "<<sqw.getQueryWindow()[1]<<endl;
	cout<<"querywindow[2]: "<<sqw.getQueryWindow()[2]<<endl;
	cout<<"querywindow[3]: "<<sqw.getQueryWindow()[3]<<endl;*/

	file = fopen(path, "r+b");
	fseek(file, 0, SEEK_SET); 

    int c[2];
    fread(c, sizeof(int), 2, file);
    //queryDA++;
    cardinality = c[0]; //'cardinality' é o total de registros no arquivo 
	//cout<<"cardinality: "<<cardinality;
	PAGESIZE = c[1];
	//cout<<"   PAGESIZE: "<<PAGESIZE<<endl;
	
	ENTRIES_PER_PAGE = PAGESIZE/sizeof(Sbitvector); 
	//cout<<"scanCrq - ENTRIES_PER_PAGE: "<<ENTRIES_PER_PAGE<<endl;

    Sbitvector* page = (Sbitvector*) malloc (PAGESIZE); 
	Sbitvector* buffer = (Sbitvector*) malloc (PAGESIZE);

	head = PAGESIZE;
    int offset = head; //position after the head

	//usando-se: PAGESIZE = 4096, temos entradas = 102 ( pois sizeof(Sbitvector = 40)), considerando-se 250 registros no total
	//vetor 'collection' com 250 ('cardinality') posições para o caso de todas as entradas se tornarem verdadeiras 
	int *collection = (int*) malloc (cardinality*sizeof(int)); 

	//these variables reffer to the page and element pointed
    int i;
	int entriesCounter = 0; //número total de entradas até o momento

	qttyCandidates = 0; //número de vezes que crq é verdadeiro

	//sequential scan on file, reading one page per step
	while (entriesCounter < cardinality)
	{ 
		fseek(file, offset, SEEK_SET); 
		fread(page, PAGESIZE, 1, file);
		//queryDA++;
		
		memcpy(buffer, page, ENTRIES_PER_PAGE*ENTRY_SIZE);

        //sequential scan on buffer
        for (i = 0; i < ENTRIES_PER_PAGE && entriesCounter < cardinality; i++, entriesCounter++)
		{
			//cout<<"buffer[i]: "<<buffer[i].getKey()<<endl;//<<" "<<buffer[i].getMbr(0)<<" "<<buffer[i].getMbr(1)<<" "<<buffer[i].getMbr(2)<<" "<<buffer[i].getMbr(3)<<endl;
			//cout<<"entriesCounter: "<<entriesCounter<<endl;

            //tests spatial predicate CRQ
			//cout << buffer[i].getMbr(0) << " " << buffer[i].getMbr(1) << " " <<buffer[i].getMbr(2) << " " <<buffer[i].getMbr(3) << " / " << queryWindow[0]<< " "  << queryWindow[1]<< " "  << queryWindow[2]<< " "  << queryWindow[3]<<endl; 
			if (buffer[i].determine_crq(sqw.getQueryWindow()))
			{
				collection[qttyCandidates] = buffer[i].getKey();
				cout<<"collection["<<qttyCandidates<<"]: "<<collection[qttyCandidates]<<endl;
				qttyCandidates++;
				//cout <<"for - qttyCandidates: "<<qttyCandidates<<endl;
            }
        }
        offset = offset+PAGESIZE;   //increment pointer to the next page
    }
	fseek(file, 0, SEEK_SET);	
	fclose(file);
	free(buffer);
	free(page);

	cout<<"scanCrq - fim"<<endl;

	int *candidates = NULL;
	if(qttyCandidates > 0)
	{
		candidates = (int*) malloc (qttyCandidates*sizeof(int));
		//cout <<"\nqttyCandidates: " <<qttyCandidates<< endl;

		memcpy(candidates, collection, qttyCandidates*sizeof(int));
	}
	free(collection);
	return candidates;  //o método deve retornar o vetor 'candidates' para Java
}


int* SBindex::scanIrq(char *path, double *qWindow)
{
	cout<<"scanIrq - inicio"<<endl;

	SpatialQueryWindow sqw(qWindow);

	/*cout<<"querywindow[0]: "<<sqw.getQueryWindow()[0]<<endl;
	cout<<"querywindow[1]: "<<sqw.getQueryWindow()[1]<<endl;
	cout<<"querywindow[2]: "<<sqw.getQueryWindow()[2]<<endl;
	cout<<"querywindow[3]: "<<sqw.getQueryWindow()[3]<<endl;*/
	
	file = fopen(path, "r+b");
	fseek(file, 0, SEEK_SET); 

    int c[2];
    fread(c, sizeof(int), 2, file);
    //queryDA++;
    cardinality = c[0]; //'cardinality' é o total de registros no arquivo 
	//cout<<"cardinality: "<<cardinality;
	PAGESIZE = c[1];
	//cout<<"   PAGESIZE: "<<PAGESIZE<<endl;
	
	ENTRIES_PER_PAGE = PAGESIZE/sizeof(Sbitvector); 
	//cout<<"scanIrq - ENTRIES_PER_PAGE: "<<ENTRIES_PER_PAGE<<endl;

    Sbitvector* page = (Sbitvector*) malloc (PAGESIZE); 
	Sbitvector* buffer = (Sbitvector*) malloc (PAGESIZE);

	head = PAGESIZE;
    int offset = head; //position after the head

	//usando-se: PAGESIZE = 4096, temos entradas = 102 ( pois sizeof(Sbitvector = 40)), considerando-se 250 registros no total
	//vetor 'collection' com 250 ('cardinality') posições para o caso de todas as entradas se tornarem verdadeiras 
	int *collection = (int*) malloc (cardinality*sizeof(int)); 

	//these variables reffer to the page and element pointed
    int i;
	int entriesCounter = 0; //número total de entradas até o momento

	qttyCandidates = 0; //número de vezes que irq é verdadeiro

	//sequential scan on file, reading one page per step
	while (entriesCounter < cardinality)
	{ 
		fseek(file, offset, SEEK_SET); 
		fread(page, PAGESIZE, 1, file);
		//queryDA++;
		
		memcpy(buffer, page, ENTRIES_PER_PAGE*ENTRY_SIZE);
		
        //sequential scan on buffer
        for (i = 0; i < ENTRIES_PER_PAGE && entriesCounter < cardinality; i++, entriesCounter++)
		{
			//cout<<"scanIrq - entriesCounter: "<<entriesCounter<<endl;
			//<<" "<<buffer[i].getMbr(0)<<" "<<buffer[i].getMbr(1)<<" "<<buffer[i].getMbr(2)<<" "<<buffer[i].getMbr(3)<<endl;
			
            //tests spatial predicate IRQ
			if (buffer[i].determine_irq(sqw.getQueryWindow())) // ou irq
			{
				collection[qttyCandidates] = buffer[i].getKey();
				cout<<"collection["<<qttyCandidates<<"]: "<<collection[qttyCandidates]<<endl;
				qttyCandidates++;
				//cout <<"for - qttyCandidates: "<<qttyCandidates<<endl;
            }
        }
        offset = offset+PAGESIZE;   //increment pointer to the next page
    }
	fseek(file, 0, SEEK_SET);	
	fclose(file);
	free(buffer);
	free(page);

	cout<<"scanIrq - fim"<<endl;

	int *candidates = NULL;
	if(qttyCandidates > 0)
	{
		candidates = (int*) malloc (qttyCandidates*sizeof(int));
		//cout <<"\nqttyCandidates: " <<qttyCandidates<< endl;

		memcpy(candidates, collection, qttyCandidates*sizeof(int));
	}
	
	free(collection);
	return candidates;
}


int* SBindex::scanErq(char *path, double *qWindow)
{
	SpatialQueryWindow sqw(qWindow);
	
	/*cout<<"querywindow[0]: "<<sqw.getQueryWindow()[0]<<endl;
	cout<<"querywindow[1]: "<<sqw.getQueryWindow()[1]<<endl;
	cout<<"querywindow[2]: "<<sqw.getQueryWindow()[2]<<endl;
	cout<<"querywindow[3]: "<<sqw.getQueryWindow()[3]<<endl;*/

	cout<<"scanErq - inicio"<<endl;

	file = fopen(path, "r+b");
	fseek(file, 0, SEEK_SET); 

    int c[2];
    fread(c, sizeof(int), 2, file);
    //queryDA++;
    cardinality = c[0]; //'cardinality' é o total de registros no arquivo 
	//cout<<"cardinality: "<<cardinality;
	PAGESIZE = c[1];
	//cout<<"   PAGESIZE: "<<PAGESIZE<<endl;
	
	ENTRIES_PER_PAGE = PAGESIZE/sizeof(Sbitvector); 
	//cout<<"scanCrq - ENTRIES_PER_PAGE: "<<ENTRIES_PER_PAGE<<endl;

    Sbitvector* page = (Sbitvector*) malloc (PAGESIZE); 
	Sbitvector* buffer = (Sbitvector*) malloc (PAGESIZE);

	head = PAGESIZE;
    int offset = head; //position after the head

	//usando-se: PAGESIZE = 4096, temos entradas = 102 ( pois sizeof(Sbitvector = 40)), considerando-se 250 registros no total
	//vetor 'collection' com 250 ('cardinality') posições para o caso de todas as entradas se tornarem verdadeiras 
	int *collection = (int*) malloc (cardinality*sizeof(int)); 

	//these variables reffer to the page and element pointed
    int i;
	int entriesCounter = 0; //número total de entradas até o momento

	qttyCandidates = 0; //número de vezes que erq é verdadeiro

	//sequential scan on file, reading one page per step
	while (entriesCounter < cardinality)
	{ 
		fseek(file, offset, SEEK_SET); 
		fread(page, PAGESIZE, 1, file);
		//queryDA++;
		
		memcpy(buffer, page, ENTRIES_PER_PAGE*ENTRY_SIZE);

        //sequential scan on buffer
        for (i = 0; i < ENTRIES_PER_PAGE && entriesCounter < cardinality; i++, entriesCounter++)
		{
			//cout<<"buffer[i]: "<<buffer[i].getKey()<<endl;//<<" "<<buffer[i].getMbr(0)<<" "<<buffer[i].getMbr(1)<<" "<<buffer[i].getMbr(2)<<" "<<buffer[i].getMbr(3)<<endl;
			//cout<<"entriesCounter: "<<entriesCounter<<endl;
            //tests spatial predicate ERQ
			if (buffer[i].determine_erq(sqw.getQueryWindow()))
			{
				collection[qttyCandidates] = buffer[i].getKey();
				cout<<"collection["<<qttyCandidates<<"]: "<<collection[qttyCandidates]<<endl;
				qttyCandidates++;
				//cout <<"for - qttyCandidates: "<<qttyCandidates<<endl;
            }
        }
		
        offset = offset+PAGESIZE;   //increment pointer to the next page
    }
	
	fseek(file, 0, SEEK_SET);	
	fclose(file);
	free(buffer);
	free(page);

	cout<<"scanErq - fim"<<endl;

	int *candidates = NULL;
	if(qttyCandidates > 0)
	{
		candidates = (int*) malloc (qttyCandidates*sizeof(int));
		//cout <<"\nqttyCandidates: " <<qttyCandidates<< endl;

		memcpy(candidates, collection, qttyCandidates*sizeof(int));
	}

	free(collection);
	return candidates;  //o método deve retornar o vetor 'candidates' para Java
}


int* SBindex::scanPq(char *path, double *qWindow)
{
	SpatialQueryWindow sqw(qWindow);
	
	/*cout<<"querywindow[0]: "<<sqw.getQueryWindow()[0]<<endl;
	cout<<"querywindow[1]: "<<sqw.getQueryWindow()[1]<<endl;
	cout<<"querywindow[2]: "<<sqw.getQueryWindow()[2]<<endl;
	cout<<"querywindow[3]: "<<sqw.getQueryWindow()[3]<<endl;*/

	cout<<"scanPq - inicio"<<endl;

	file = fopen(path, "r+b");
	fseek(file, 0, SEEK_SET); 

    int c[2];
    fread(c, sizeof(int), 2, file);
    //queryDA++;
    cardinality = c[0]; //'cardinality' é o total de registros no arquivo 
	//cout<<"cardinality: "<<cardinality;
	PAGESIZE = c[1];
	//cout<<"   PAGESIZE: "<<PAGESIZE<<endl;
	
	ENTRIES_PER_PAGE = PAGESIZE/sizeof(Sbitvector); 
	//cout<<"scanCrq - ENTRIES_PER_PAGE: "<<ENTRIES_PER_PAGE<<endl;

    Sbitvector* page = (Sbitvector*) malloc (PAGESIZE); 
	Sbitvector* buffer = (Sbitvector*) malloc (PAGESIZE);

	head = PAGESIZE;
    int offset = head; //position after the head

	//usando-se: PAGESIZE = 4096, temos entradas = 102 ( pois sizeof(Sbitvector = 40)), considerando-se 250 registros no total
	//vetor 'collection' com 250 ('cardinality') posições para o caso de todas as entradas se tornarem verdadeiras 
	int *collection = (int*) malloc (cardinality*sizeof(int)); 

	//these variables reffer to the page and element pointed
    int i;
	int entriesCounter = 0; //número total de entradas até o momento

	qttyCandidates = 0; //número de vezes que erq é verdadeiro

	//sequential scan on file, reading one page per step
	while (entriesCounter < cardinality)
	{ 
		fseek(file, offset, SEEK_SET); 
		fread(page, PAGESIZE, 1, file);
		//queryDA++;
		
		memcpy(buffer, page, ENTRIES_PER_PAGE*ENTRY_SIZE);

        //sequential scan on buffer
        for (i = 0; i < ENTRIES_PER_PAGE && entriesCounter < cardinality; i++, entriesCounter++)
		{
			//cout<<"buffer[i]: "<<buffer[i].getKey()<<endl;//<<" "<<buffer[i].getMbr(0)<<" "<<buffer[i].getMbr(1)<<" "<<buffer[i].getMbr(2)<<" "<<buffer[i].getMbr(3)<<endl;
			//cout<<"entriesCounter: "<<entriesCounter<<endl;
            //tests spatial predicate ERQ
			if (buffer[i].determine_pointquery(sqw.getQueryWindow()))
			{
				collection[qttyCandidates] = buffer[i].getKey();
				cout<<"collection["<<qttyCandidates<<"]: "<<collection[qttyCandidates]<<endl;
				qttyCandidates++;
				//cout <<"for - qttyCandidates: "<<qttyCandidates<<endl;
            }
        }
		
        offset = offset+PAGESIZE;   //increment pointer to the next page
    }
	
	fseek(file, 0, SEEK_SET);	
	fclose(file);
	free(buffer);
	free(page);

	cout<<"scanPq - fim"<<endl;

	int *candidates = NULL;
	if(qttyCandidates > 0)
	{
		candidates = (int*) malloc (qttyCandidates*sizeof(int));
		//cout <<"\nqttyCandidates: " <<qttyCandidates<< endl;

		memcpy(candidates, collection, qttyCandidates*sizeof(int));
	}

	free(collection);
	return candidates; //o método deve retornar o vetor 'candidates' para Java
}
