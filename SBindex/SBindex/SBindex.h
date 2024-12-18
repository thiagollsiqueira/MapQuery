#ifndef SBINDEX_H_INCLUDED
#define SBINDEX_H_INCLUDED

#include "Sbitvector.h" 

class SBindex{
    public:
      SBindex(char* indexPath, char* gran, char* h_rr_rd);
	  SBindex() {};
	  ~SBindex(){};
        void openForCreation(char* path, int PAGESIZE);
		void closeAfterCreation();
		void write(int* k, double* xm, double* ym, double* XM, double* YM, int length);
		int sizeSbitvector(int PAGESIZE);
		int getQttyCandidates(){return qttyCandidates;};
		int* scanCrq(char *path, double* qWindow/*, double &xm, double &ym, double &XM, double &YM*/);
		int* scanIrq(char *path, double* qWindow/*, double &xm, double &ym, double &XM, double &YM*/);
		int* scanErq(char *path, double* qWindow/*, double &xm, double &ym, double &XM, double &YM*/);
		int* scanPq(char *path, double* qWindow);
		
    private:
        FILE* file;
        bool refFlag; //becomes true after the first refinement
        Sbitvector* page;
        Sbitvector* buffer;
		int cardinality;
		size_t PAGESIZE; //total page size in bytes
		size_t head; //4096 bytes preceeds the vectors in the file
		size_t ENTRIES_PER_PAGE;
		size_t ENTRY_SIZE;
		int qttyCandidates;
};

#endif // SBINDEX_H_INCLUDED
