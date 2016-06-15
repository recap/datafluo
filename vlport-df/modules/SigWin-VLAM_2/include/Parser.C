//****************************************************************************
/** \file Parser.C 
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
#include "Parser.H"
namespace Parser{

    //from http://www.geocities.com/eric6930/cplus.html
    void tokenize(const string& str, vector<string>& tokens, 
    	const string& delimiters){
    	    	
	// Skip delimiters at beginning.
    	string::size_type lastPos = str.find_first_not_of(delimiters, 0);
     	// Find first "non-delimiter".
    	string::size_type pos     = str.find_first_of(delimiters, lastPos);
    	while (string::npos != pos || string::npos != lastPos){
            // Found a token, add it to the vector.
            tokens.push_back(str.substr(lastPos, pos - lastPos));
            // Skip delimiters.  Note the "not_of"
            lastPos = str.find_first_not_of(delimiters, pos);
            // Find next "non-delimiter"
            pos = str.find_first_of(delimiters, lastPos);
    	}
    }
}
