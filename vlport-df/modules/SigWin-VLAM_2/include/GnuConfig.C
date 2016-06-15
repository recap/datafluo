//****************************************************************************
/** \file GnuConfig.C 
 *  \brief create a gnuplot comands
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

#include "GnuConfig.H"
    
namespace GnuConfig{

    void labels(ostream& outStream, const string& title, const string& x, 
    	const string& y){
    	outStream << "set title \"" << title <<"\""<< endl<<flush;
    	outStream << "set xlabel \"" << x <<"\""<< endl<<flush;
    	outStream << "set ylabel \"" << y <<"\""<< endl<<flush;
    }
        
    void range(ostream& outStream, const Range<float>& r){
    	outStream << "set " << r.axis << "range [" << r.min << ":" << r.max 
	    	<< "]" << endl<< flush;
    }

    void viewMap(ostream& outStream){
    	outStream << "set view map\n";
//    	outStream << "set view 0,0,1,1\n";
	outStream << "unset ztics\n"; 
    	outStream << "set palette\n" << flush;
    }

    void plot(ostream& outStream, const string& file, 
    	const string& legend){};
	
    void sPlot(ostream& outStream, const string& file, const string& legend){
    	outStream << "splot \"" << file << "\" title \"" << legend<<endl<<flush;
    }

    void sPlot(ostream& outStream, const string& file, const string& legend, 
    	const int x, const int y, const int z,     
    	const int pi, const int bi, const int sp, const int sb, 
    	const int ep, const int eb){
    	outStream << "splot \"" << file << "\" using " << x <<":"<<y<<":"<<z 
    	    	  <<" every " << pi <<":"<< bi <<":"<< sp <<":"<< sb;
    	if (ep>0){
	    outStream <<":" << ep;
	    if (eb>0)
    	    	outStream <<":" << eb;
    	}
    	else if (eb>0)
    	    outStream <<":: " << eb;
    	outStream << " palette title \"" << legend << "\""; 
    	outStream << endl<<flush;
    }

    void sRePlot(ostream& outStream, const string& file, 
    	const string x, const string y, const string z,
    	const int pi, const int bi, const int sp, const int sb, 
    	const int ep, const int eb){
    	outStream << "replot \"" << file << "\" using " << x <<":"<<y<<":"<<z 
    	    	  << " every " << pi <<":"<< bi <<":"<< sp <<":"<< sb;
     	if (ep>0){
	    outStream <<":" << ep;
	    if (eb>0)
    	    	outStream <<":" << eb;
    	}
    	else if (eb>0)
    	    outStream <<":: " << eb;
    	outStream <<" with lines palette notitle"<< endl<<flush;
    	//outStream << " with dots palette notitle" << endl<<flush;
    }
    

    //draws equilateral triangle border
    void triangle(ostream& outStream, const string& file, 
    	const float size,const float yMin, const float yMax){
    	float s(size+1);
    	outStream << "set view map\n"
    	      	<< "unset ztics\n"
    	      	<< "set palette\n" 
	      	<< "unset border\n"
	      	<< "set xrange ["<<0<<":"<<s<<"]\n"
	      	<< "set yrange ["<<yMin-1<<":"<<yMax+1<<"]\n"
	      	<< "set parametric\n"
	      	<< "set vrange[" << 0 <<":"<<s<<"]\n"
	      	<< "set urange[" << 0 <<":"<<s<<"]\n"
	      	<< "splot v, (v<=" << s/2 << "? 2*v : -2*(v-" << s <<")),0 "
	      	<< "with l lt -1 notitle, v, 0, 0 with l lt -1 notitle\n"
	      	<< flush;
    }

}
//end_of_file
