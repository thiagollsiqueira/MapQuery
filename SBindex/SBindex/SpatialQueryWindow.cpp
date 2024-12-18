#include <string.h>
#include "SpatialQueryWindow.h"

SpatialQueryWindow::SpatialQueryWindow(double* qWindow) {
	queryWindow = qWindow;
}


double* SpatialQueryWindow::getQueryWindow()
{
	return queryWindow;
}
