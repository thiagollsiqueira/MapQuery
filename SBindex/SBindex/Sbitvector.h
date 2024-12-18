#define DIMENSIONPREDEFINED 2

//int DIM = 2; //how many dimensions the query window has (polygon = 2 dimensions)
 
#ifndef _SBITVECTOR_H
#define	_SBITVECTOR_H

class Sbitvector {
  public:
      Sbitvector(void);
      Sbitvector(int, int, double, double, double, double); //constructor
//int choosePredicateQuery (char); //evaluates predicate and redirect to one of the specific funcions
      int eq (double*); //exact query
      int pq (double*); //point query
      int irq (double*); //intersection range query
      int crq (double*); //containment range query
      bool crq0(double*);
      int erq (double*); //enclosure range query (contained)
      void setKey(int);
      void setBitvector_ptr(int);
      void setMbr(double, double, double, double);
      int getKey();
      int getBitvector_ptr();
      double getMbr(int);
	  
      bool determine_exactquery(double *querywindow);
      bool determine_pointquery(double* querywindow);
      bool determine_irq(double* querywindow);
      bool determine_erq(double* querywindow);
      bool determine_crq(double* querywindow);
	  //bool relate(double* querywindow, bool pointquery, bool intersectionquery, bool containquery, bool containedquery);
      
  private:
    int key;    
    double mbr [DIMENSIONPREDEFINED*2];        
};

#endif	/* _SBITVECTOR_H */c
