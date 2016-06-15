//****************************************************************************
/** \file ArrayIO.H 
 *  \brief Reads/Writes arrays from/to a stream. 
 *
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since Feb 13, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Feb 13, 2008: Removed "fromString".
 *  Created: November 02, 2005
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/

#ifndef ARRAYIOH_
#define ARRAYIOH_

#include <fstream>
#include <iostream>
#include <sstream>


#include "LogManager.H"
#include "DataIO.H"
#include "HeaderIO.H"
using namespace std;

//****************************************************************************
//prototypes
//****************************************************************************
/// Writes raw array of size s to outStream
template<class T> void write_array(ostream& outStream, const T* array, long s);
/// Reads raw array from inStream
template<class T> long read_array(istream& inStream, T* array, long size);

/// Prints array of size s to outFile (formated)
template <class T> void print_array(ostream& outFile, const T* array, long s);
/// Reads array from inFile, array must be previously allocated.
template<class T> void scan_array(istream& inFile, T* array, const long size);


//****************************************************************************
//implementatios
//****************************************************************************
/// Writes raw array to outStream
template <class T>
void write_array(ostream& outStream, T* array, long size,
    const string descr="", const char mark='#'){

    if (outStream){
    	Header::print(outStream, size, descr, mark);
    	LOG_PTAG(Detail)<<"# " << size <<" "<< flush;
    }
    else{
    	LOG_PTAG(Fatal) << size << endl << flush;
	exit(1);
    }
    //for_each(array, array+size, bind1st(write_struct<T>, *outStream));
    int div = (Log::get_warn_level() <= Detail? (size<=5? 1: size/5) : size);
    for(long i=0; i<size; ++i){
//    	Log::print(Info) << array[i] << endl;
    	if (!write_struct(outStream, array[i])){
    	    LOG_PTAG(Fatal) << "Could not write [" << i << "]= " 
	    	    	    << array[i] << endl << flush;
	    exit(1);
	}
    	if (i % div==0)
   	    Log::print(Detail) << i << "=" << array[i] <<", ";
    } 
    outStream.flush();
    Log::print(Detail)<<"Done" <<flush;

}

/// Reads raw array from inStream, array must be previously allocated.
template<class T> 
long read_array(istream& inStream, T* array, long size)
{
    string saux;
    if (!getline(inStream,saux))
    	return 0;
    if (saux[0]!='#'){//inStream is bad
     	LOG_PTAG(Fatal)<< "Header missing: " << saux << endl << flush;
	exit(1);
    }
    if (saux[1]==' '){//size is known 
    	std::istringstream is(saux.substr(1));
    	long size_aux;
    	is >> size_aux;
    	if (size_aux != size){
    	    LOG_PTAG(Warning) << "Allocated size and input size differ! (al= "
	     	    	   << size << ", in= " << size_aux << ").";
	    
	    size = (size_aux > size ? size : size_aux);
	    LOG_PTAG(Warning)<<"Reading first "<< size << " elements." << flush;
	}
    }
    LOG_PTAG(Detail) << "Size= " << size << flush; 

    for(long i=0; i<size; ++i){
    	T t;
    	if (read_struct(inStream, t)){
    	    array[i] = t;
     	    LOG_PTAG(Extreme) << i << "= " << t << flush; 
	}
	else{
   	    LOG_PTAG(Warning) << "Actual size (" << i << ") <> header size (" 	    	    	    	   << size << ")" << flush;
	    return i;
	}
    }    
    LOG_PTAG(Detail)<<"Done array["<<size<<"-1]="<<array[size-1] <<flush;
    return size;
}

/// Prints array to outFile (formated)
template <class T>
void print_array(ostream& outFile, T* array, long size,
    const string descr="", const char mark='#')
{
    Header::print(outFile, size, descr, mark);
    for (int i=0; i<size; ++i)
    	outFile << array[i] << endl;
    outFile.flush();
}


/// Reads array from inFile, array must be previously allocated
template<class T> void scan_array(istream& inFile, T* array, const long size){
    long size_aux;
    char caux[150];
    inFile.get(caux, sizeof(caux));
    sscanf(caux, "# %ld", &size_aux);
    if (size_aux != size){
    	LOG_PTAG(Warning) << "Allocated size and input size differ! (al= "
	               << size << ", in= " << size_aux << ").";
	    
	size = (size_aux > size ? size : size_aux);
	LOG_PTAG(Warning) << "Reading first " << size << " elements." << flush;
    }
    LOG_PTAG(Detail) << "Size= " << size << flush; 
    
    for (int i=0; i<size; i++)
 	if (!inFile.eof())
    	    inFile >> array[i];
    	else{
    	    LOG_PTAG(Error) << "File corrupted! (eof=" << i << " <> " 	    	    	    	            << size << ")" << flush;
	    return;
	}
}


#endif //ARRAYIOH_
//end_of_file
