//*****************************************************************************
/** \File LogManager.H
 *  \brief Manages the loging of messages. 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 03, 2006.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: November 07, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef LOGMANAGERH_
#define LOGMANAGERH_  /* flag to indicate that this File is loaded */

#include <iostream>
#include <fstream>
#include <iomanip>
#include <unistd.h>
#include <sstream>

#include "LogDefs.H"

using namespace std;

enum WarnType {Extreme, High, Detail, Debug, 
    	       Info, Timing, Release, 
	       Warning, Error, Fatal, NoWarn};

ostream &operator>> (ostream &out, const WarnType &level);
istream &operator<< (istream& in, WarnType& level);

string WarnType2string(const WarnType &wt);
WarnType string2WarnType(const string &str);
    
namespace Log{
    ostream& print(const WarnType& Level);
    ostream& ptag(const WarnType& Level);
        
    time_t time_stamp(const WarnType& Level, string label);
    void start_timing(const WarnType& Level, string label);
    
    void init(ostream* file, const WarnType wl=Info);
    void open(const string& name, const WarnType wl=Info);
    void close(void);
    void set_warn_level(const WarnType Level, const bool log=true);
    WarnType get_warn_level(void);
}

#define LOG_PFTAG(WT) Log::ptag(WT) << __func__ << ". "
#define LOG_TIME(WT) (Log::time_stamp(WT, __func__), Log::print(WT))
#define LOG_PPTAG(WT) Log::ptag(WT) << __PRETTY_FUNCTION__ << ".\n       " 
#define LOG_PFFTAG(WT) Log::ptag(WT) << __FILE__ << " " << __func__ << ". "
#define LOG_PTAG(WT) LOG_PFTAG(WT)

#endif //LOGMANAGERH_
//end_of_File
