//*****************************************************************************
/** \File TimeLag.H
 *  \brief Class for storing time lapse information. 
 *  ########### Might have problems with measuring process time #############
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  September 15, 2006.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 2008: Changed storage type from array to vector.
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created:  September 15, 2006.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef TIMELAGH_
#define TIMELAGH_

#include <iostream>
#include <fstream>
#include <unistd.h>
#include <ctime>
#include "LogManager.H"

enum TimeLagMode {Simple, Cumulative, Average};

class TimeLag{
  public:
    TimeLag() : id(""), mode(Simple){};
    void start(const string Label);
    void finish(const string Label);
	
    void pause(const string Label);
    void cont(const string Label);
    
    void stop(const string Label);
    void restart(const string Label);
    
    void reset(void){ id="";}
    void print(ostream& file=cout){
    	file << "Label= " << id << ". Start= " << startString 
	    << ". Wall time elapsed= " << dwall 
	    << " seconds. Process time elapsed= " << dclock << " seconds."; 
    }
    
    //prints id mode wall_time_start wall_time_elapsed proccess_time_elapsed
    void print_table(ostream& file=cout){
    	
    	file << id <<"\t" << TLlabel(mode) << "\t" << startString <<"\t"
	    << dwall <<"\t"<< dclock <<endl<<flush;
    }
    
  private:	
    enum TimeLagMode {Simple, Cumulative, Average};
    string id;
    TimeLagMode mode;
    time_t wallFirstStart;
    double dwall, sumdwall;
    double dclock, sumdclock;
    int nStops;

    clock_t procStart, procEnd;
    time_t wallStart, wallEnd;
    string startString;
    bool paused, stoped;

    string TLlabel(const TimeLagMode Label){ 
    	return (Label==Simple? "simple" : 
	    	    (Label==Cumulative? "cumulative" : "average"));
    }

    double dclock_time(void){
    	 LOG_PTAG(Debug) << procStart << " " << procEnd << " " << CLOCKS_PER_SEC << flush; 
	 return double(procEnd-procStart)/double(CLOCKS_PER_SEC);
    }
    
};

#endif //TIMELAGH_
