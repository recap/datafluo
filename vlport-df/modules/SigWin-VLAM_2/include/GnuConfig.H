//****************************************************************************
/** \file GnuConfig.H 
 *  \brief Functions for creating gnuplot comands.
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 08, 2007.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: April 10, 2006
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef GNUCONFIGH_
#define GNUCONFIGH_

#include <fstream>
#include <string>

#include "LogManager.H"

using namespace std;

struct GnuGraph{
    double origin[2];
    double size[2];
};  
    

template <class T>
struct Range { 
    string axis; 
    T min; 
    T max;
    Range(){};
    Range(const string& name):axis(name){};
    Range(const string& name, const T& Min, const T& Max)
    	:axis(name),min(Min), max(Max){};
    Range(const char* name, const T& Min, const T& Max)
    	:axis(name),min(Min), max(Max){};
};  
    
namespace GnuConfig{

    void labels(ostream& outStream, const string& title, const string& x, 
    	const string& y);
    void range(ostream& outStream, const Range<float>& range);
    inline void range(ostream& outStream, const string& axis, const float min, 
    	const float max){range(outStream, Range<float>(axis, min, max));}
    void viewMap(ostream& outStream);
    
    void plot(ostream& outStream, const string& file, const string& legend);	
    void sPlot(ostream& outStream, const string& file, const string& legend);
    void sPlot(ostream& outStream, const string& file, const string& legend, 
    	const int x=1, const int y=2, const int z=2,     
    	const int pi=1, const int bi=1, const int sp=0, const int sb=0, 
    	const int ep=0, const int eb=0);

    void sRePlot(ostream& outStream, const string& file, 
    	const string x="1", const string y="2", const string z="3",
    	const int pi=1, const int bi=1, const int sp=0, const int sb=0, 
    	const int ep=0, const int eb=0);

    //draws equilateral triangle border
    void triangle(ostream& outStream, const string& file, 
    	const float size,const float yMin, const float yMax);

}
   
#endif //GNUCONFIGH_
   
//end_of_file
