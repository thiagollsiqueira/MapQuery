#ifndef SPATIALQUERYWINDOW_H_INCLUDED
#define SPATIALQUERYWINDOW_H_INCLUDED

#include "Sbitvector.h"

class SpatialQueryWindow {
	public:
		SpatialQueryWindow(double* queryWindow);
		SpatialQueryWindow() {};
		double* getQueryWindow();

	private:
		double* queryWindow;
};


#endif // SPATIALQUERYWINDOW_H_INCLUDED