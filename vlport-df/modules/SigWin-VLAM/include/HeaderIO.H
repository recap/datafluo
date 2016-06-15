//****************************************************************************
/** \file HeaderIO.H 
 *  \brief Reads/Writes a header from/to a stream 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 05, 2007.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: March 17, 2006
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef HEADERIOH_
#define HEADERIOH_

#include <fstream>
#include <string>
#include <sstream>
#include <vector>
#include <map>
#include <stdlib.h>

#include "LogManager.H"
#include "Parser.H"
	
///Reads/Writes a header from/to a stream 
namespace Header{

    struct info{
    	string label;
    	long number;
	 
	info():label(""),number(-1){};
	info(const string& col):label(""),number(-1){
	    set(col.c_str());
	};
	void set(const char* col){
    	    LOG_PTAG(Detail) << "Seting using '" << col <<"'"<< flush;
	    if (*col == '\0'){
	    	LOG_PTAG(Warning)<< "Empty string";
		number= -1;
		label="";
	    }
	    else{
	    	char *endptr;
	    	long aux = strtol(col, &endptr, 10);//string to long
   	    	LOG_PTAG(Detail) << "aux=" << aux 
		    	    	<< " endptr=" << endptr << flush;
		if (*endptr == '\0'){
    	    	    LOG_PTAG(Detail) << "It is a number" << flush;
		    number=aux;
		    label="";
		}
		else{
    	    	    LOG_PTAG(Detail) << "It is a string" << flush;
    	    	    label=col;
		    number=-1;
		}
	    }
	};
    };


    ///reads a file header
    /* Header structure: starts with a <mark>, space after <mark> is optional
    case 1: without labels (order maters)
    	<mark> <size> <ncols> <descr>
    case 2:with labels (descr has to be the last entry)
    	<mark> size=<size> ncols=<ncols> descr=<descr>
    case 3: mixed
        <mark> <size> <ncols> descr=<descr>
        <mark> <size> ncols=<ncols> descr=<descr>
    
    Notes:
    	If <size> is not known it can be omited.
	<ncols> and <descr> can be omited.
	max_size=<max_size> can be used instead of size
    	default value of <mark> is '#' 
    Returns: header line or "".
    */        
    bool scan(istream& inStream, long& size, int& ncols, string& descr,
    	string& line, const char mark='#');
    bool scan(istream& inStream, long& size, string& line, const char mark='#');
        
    void print(ostream& outStream, const long size, const int ncols, 
    	const string descr, 
    	const char mark='#', const bool bare=false);
    void print(ostream& outStream, const long size, const string descr, 
    	const char mark='#', const bool bare=false);
    void print(ostream& outStream, const long size, const char mark='#');
    bool match_description(string& givenDescr, const string& gotDescr, 
    	const string& label="description");
    bool match_descriptions(istream& inStream, vector<info>& cInfo, 
    	const char mark);


    template <class T>
    bool scan_property(const string& name, const string& line, T& value);
    int scan_labels(istream& inStream, vector<string>& label, 
    	const char mark='#');
    void print_labels(ostream& outStream, const vector<string>& label, 
    	const char mark='#');

}
#endif//HEADERIOH_
