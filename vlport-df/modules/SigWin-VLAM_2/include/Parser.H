//****************************************************************************
/** \file Parser.H 
 *  \brief Simple parsing functions.
 *
 *  function tokenize copied from http://www.geocities.com/eric6930/cplus.html
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since October 15, 2007.
 */
/*---------------------------------------------------------------------------
 *  History:
 *  Created: March 24, 2006
 *---------------------------------------------------------------------------
 * Author: Marcia A. Inda
 *  	   Integrtive Bioinformatics Unit, UvA
 *         http://staff.science.uva.nl/~indahttp://staff.science.uva.nl/~inda
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef PARSERH_
#define PARSERH_

#include <string>
#include <sstream>
#include <vector>
#include "LogManager.H"
namespace Parser{

    using namespace std;
    
    ///"Trims" a string in place. 
    //Removes all leading and triling occurences of any character in DropList
    inline void trim(string& s, const string& DropList=" \n\t\r"){
    	LOG_PTAG(Debug)<<"s= '"<<s<<"'";
    	LOG_PTAG(Detail)<<"last= "<<
	    s.find_last_not_of(DropList)+1<<" size=" <<s.size()<< flush;	
    	s.erase(s.find_last_not_of(DropList)+1);
    	s.erase(0,s.find_first_not_of(DropList));
    }

    ///returns the "trimed" version of the input string  
    inline string trim(const string& s, const string& DropList=" \n\t\r"){
    	string r(s);
    	trim(r,DropList);
    	return r;
    }

    //from http://www.geocities.com/eric6930/cplus.html
    void tokenize(const string& str, vector<string>& tokens, 
    	const string& DropList=" \n\t\r");
}

#endif //PARSERH_
