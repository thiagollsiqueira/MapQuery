#include <string.h>
#include "sbitvector.h"


int DIM = 2; //retangulo bidimensional

//constructor
Sbitvector::Sbitvector(int k, int ptr, double xMin, double xMax, double yMin, double yMax) {
    key = k;     
    mbr[0] = xMin;
    mbr[1] = yMin;
    mbr[2] = xMax;
    mbr[3] = yMax;
}

Sbitvector::Sbitvector(void){
}

//intersection range query
int Sbitvector::irq(double* querywindow) {    
    int ok, i;
    ok = 0;
    for (i=0; i < DIM; ++i) {
//std::cout << "j=" << j << "\n";
        if ((mbr[i] <= querywindow[i]) && (querywindow[i] <= mbr[i+DIM]))
            ok++;
        if ((querywindow[i] < mbr[i]) && (mbr[i] <= querywindow[i+DIM]))
            ok++;
    }
    if (ok == DIM){
        return(1);
    }
    else
        return(0);
    
}

//containment
bool Sbitvector::crq0(double* querywindow){
    if ( (mbr[0]>= querywindow[0]) && (mbr[0]<=querywindow[2])
        && (mbr[1]>= querywindow[1]) && (mbr[1]<=querywindow[3]) )
        return true;
    return false;
}
  
    //SETTERS
    void Sbitvector::setKey(int k){
        key = k;    
    }        
   
    
    void Sbitvector::setMbr(double xMin, double yMin, double xMax, double yMax){
        mbr[0] = xMin; mbr[1] = yMin;  mbr[2] = xMax; mbr[3] = yMax;
    }
    
    
    //GETTERS
    int Sbitvector::getKey(){
        return key;
    }
    
    
    double Sbitvector::getMbr(int i){
        return mbr[i];
    }
	

	
	
	
	
	
	
	
	
	
	
	/********************************* SEARCH **********************************/

bool Sbitvector::determine_exactquery(double *querywindow)
{
    int ok, i;

	if (memcmp(mbr, querywindow, 2 * DIM * sizeof(double)) == 0)
	    return true;
        else
            return false;
}


//POINT QUERY
bool Sbitvector::determine_pointquery(double* querywindow)
{
    int ok=0, i;
    for (i=0; i< DIM; ++i) {
       if ((mbr[i] <= querywindow[i]) && (mbr[i+DIM] >= querywindow[i+DIM]))
            ok ++;
    }
    if (ok == DIM)
        return true;
    else
        return false;
}

//INTERSECTION RANGE QUERY
bool Sbitvector::determine_irq(double* querywindow)
{
    int ok=0, i;
    for (i=0; i < DIM; ++i) {
        if ((mbr[i] <= querywindow[i]) && (querywindow[i] <= mbr[i+DIM]))
            ok++;
        if ((querywindow[i] < mbr[i]) && (mbr[i] <= querywindow[i+DIM]))
            ok++;
    }
    if (ok == DIM)
        return true;
    else
        return false;
}

//ENCLOSURE RANGE QUERY
bool Sbitvector::determine_erq(double* querywindow)
{
    int ok=0, i;
    for (i=0; i < DIM; ++i) {
        if ((mbr[i] <= querywindow[i]) && (mbr[i+DIM] >= querywindow[i+DIM]))
            ok ++;
    }
    if (ok == DIM)
        return true;
    else
        return false;
}

//CONTAINMENT RANGE QUERY
bool Sbitvector::determine_crq(double* querywindow)
{
        int ok = 0;
        for (int i=0; i < DIM; ++i) {
            if ((mbr[i] >= querywindow[i]) && (mbr[i+DIM] <= querywindow[i+DIM]))
                ok ++;
        }
        if (ok == DIM)
            return true;
        else
            return false;
}

/*
bool sbitvector::relate(double* querywindow, bool pointquery, bool intersectionquery, bool containquery, bool containedquery){
	bool match=false;
	if (pointquery)
        match = determine_pointquery(&querywindow);
    if ((! match) && (intersectionquery))
        match = determine_intersectionquery(&querywindow);
    if ((! match) && (containquery))
        match = determine_containquery(&querywindow);
    if ((! match) && (containedquery))
        match = determine_containedquery(&querywindow);
	return match;
}
*/