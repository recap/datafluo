//****************************************************************************
/** \file FDRAux.H 
 *  \brief Routines to compute FDR threshold.
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  January 20, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: January 20, 2008.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#include <fstream>
#include <string>

#include "DataIO.H"
#include "LogManager.H"
#include "TimeLag.H"

template <class T> 
long threshold_index(const vector<T>& freqData, const vector<T>& probData,
    const bool invert, const float FDRPerc){
    T ratio(1);
    T sumFreq(0);
    T sumProb(0);
    int size = freqData.size();
	//cout << "freqData.size(): " << size << endl; //RRR
    int index(size-1);
	//cout << "index: " << index <<endl; //RRR
    int i0(size-1), incr(-1);
    if (invert){
    	index=0;
	i0=0;
	incr=1;
    }
	//cout << "index: " << index <<endl;//RRR
    LOG_PTAG(Detail)<< FDRPerc;
  
    for (int counter=0, i=i0; counter<size; ++counter, i+=incr){
	sumFreq += freqData.at(i);
	sumProb += probData.at(i);
	Log::print(Detail) <<"("<<i<<":";
	if (sumFreq>0){
	    T r = sumProb/sumFreq;
	    Log::print(Detail) <<r;
	    if (r <= FDRPerc){
	    	ratio=r;
	    	index=i+incr;//correct to take value just beneath (above) the 
		    	    //threshold
    	    	Log::print(Debug)<< "(##" << i << " r=" << r<<"##)";
	    }
	}
	//cout << "index: " << index <<endl;//RRR
	Log::print(Detail) <<")";
    }
    
    LOG_PTAG(Detail)<<"ratio="<< ratio << " index=" << index << flush;
	//cout << "index: " << index <<endl;//RRR

	if (index < 0) 	index = 0; //RRR
		


    return index;		
}			

//end-of-file
