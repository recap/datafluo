//****************************************************************************
/** \file DataIO.H 
 *  \brief Reads/Writes structures from/to a stream 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since March 31, 2006.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: October 31, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef DATAIOH_
#define DATAIOH_

#include <iostream>
#include<stdlib.h>
#undef SINC
#ifdef SINC
#include "vlostream.h"
#endif
using namespace std;


template <typename T>
bool write_struct(ostream& outStream, T& data){
    if (outStream){
    	outStream.write(reinterpret_cast<const char*>(&data), sizeof(T));
    	Log::print(Extreme) << data <<flush; 
	if (outStream.bad()){
    	    LOG_PTAG(Fatal)<<"Write failed! " << flush;
	    return false;
	}	    
    	outStream.flush();
    	Log::print(Extreme) <<","<<flush; 
#ifdef SINC
   	((VL::vostream*)&outStream)->hardSync();
#endif
	return true;
    }
    else{
    	LOG_PTAG(Fatal)<<"outStream not available! " << flush;
#ifdef SINC
    	((VL::vostream*)&outStream)->hardSync();
#endif
 	return false;
    }
}

template <typename T>
bool read_struct(istream& inStream, T& data, const int i=1){
    if (inStream){
    	inStream.read(reinterpret_cast<char*>(&data), i*sizeof(T));
//    	Log::print(Info) << data << endl;
	if (inStream.eof())
	    return false;
	else
	    return true;
    	Log::print(Extreme) << data << ","<<flush; 
    }
    else{
    	LOG_PTAG(Fatal)<<"inStream not available! " << flush;
	return false;
    }
}

//writes a string: first size then content
bool write_struct(ostream& outStream, const string& data){
    long size = data.size();
    LOG_PPTAG(High) << "size=" << size <<flush;
    write_struct(outStream, size);
    if (outStream){
    	if (size>0){
    	    LOG_PTAG(High) << "value=" <<data<<flush;
    	    outStream.write(data.c_str(), size);
#ifdef SINC
    	    ((VL::vostream*)&outStream)->hardSync();
#endif
	}
	return true;
    }
    else{
    	LOG_PTAG(Fatal)<<"outStream not available! " << flush;
#ifdef SINC
    	((VL::vostream*)&outStream)->hardSync();
#endif
 	return false;
    }
}

//writes a string: first size then content
bool write_struct(ostream& outStream, string& data){
    return write_struct(outStream, (const string)data);
}


//reads a string: first size then content
bool read_struct(istream& inStream, string& data){
    long size;
    read_struct(inStream, size);
    LOG_PPTAG(High) << "size=" << size<<flush;
        
    if (inStream){
    	if (size>0){
    	    char *saux= new char[size];
   	    inStream.read(saux, size);
	    data = string(saux, size);
	    LOG_PTAG(High)<<"value=" << data <<flush;
	    delete[] saux;
	}
	return true;
    }
    else{
    	LOG_PTAG(Fatal)<<"inStream not available! " << flush;
	return false;
    }
}


static int itest_(1234);
static double dtest_(1.234);

void write_test_header(ostream& outStream, bool test=true){
    if (test){
    	LOG_PTAG(Info) << "Writing test header."<<flush;
    	write_struct(outStream, itest_);
    	outStream.flush();
    	write_struct(outStream, dtest_);
    	outStream.flush();
    }
    else{
    	LOG_PTAG(Debug) << "Skipping writing test header."<<flush;
    }
}

bool read_test_header(istream& inStream, bool test=true){
    int iaux;
    if (!test){
   	Log::ptag(Warning) << "Skipping istream test..." << flush;
    	return true;
    }
    bool goodHeader(true);
    if (read_struct(inStream, iaux)){
    	if (iaux != itest_){
    	    LOG_PTAG(Warning) << "Integer does not match: " << itest_ << "<>" 
	    	    	    << iaux << "). ";
    	    goodHeader=false;
	}
    	LOG_PTAG(Detail) << "Integer test: " << itest_ << ", " << iaux << ".";
    }
    else{
    	LOG_PTAG(Error) << "Error reading integer! " << flush;
	goodHeader = false;
    }
	
    double daux;
    if (read_struct(inStream, daux)){
    	if (daux != dtest_){
    	    LOG_PTAG(Error) << "Double does not match: " << dtest_ << "<>" 
	    	    	    << daux  << ")! ";
    	    goodHeader=false;
	}
    	LOG_PTAG(Detail) << "Double test: " << dtest_ << ", " << daux << "! ";
    }
    else{
    	LOG_PTAG(Error) << "Error reading double " << flush;
	goodHeader=false;
    }
    
    return goodHeader;
}  

void test_header(istream& inStream, const string descr, bool test=true){
    if (!test){
    	Log::ptag(Warning) << "Skipping " << descr << " stream test..."<<flush;
	return;
    }
    Log::ptag(Info) << "Testing " << descr << " stream..." << flush;
    if (read_test_header(inStream) == false){
	LOG_PTAG(Fatal) << "Problem reading " << descr << " header!\n" <<flush;
	exit(1);
    }
    Log::print(Info) << "OK" << flush;
}

bool warn_test_header(istream& inStream, const string descr){
    Log::ptag(Info) << "Testing " << descr << " stream..." << flush;
    if (read_test_header(inStream) == false){
	LOG_PTAG(Warning) << "Problem reading " << descr << " header. "
	    << "Rewinding and proceeding!" <<flush;
	inStream.seekg(0);
	return false;
    }
    else{
    	Log::print(Info) << "OK" << flush;
	return true;
    }
}

#endif //DATAIO_
//end_of_file
