//*****************************************************************************
/** \File TimeLag.C
 *  \brief Class for storing time lapse information. 
 */
/*---------------------------------------------------------------------------
 *  Created:  September 15, 2006      Last Update: September 15, 2006
 *---------------------------------------------------------------------------
 *  Author: Marcia Alves de Inda                                             
 *****************************************************************************/
#include "TimeLag.H"

void TimeLag::start(string Label){
    if (Log::get_warn_level() <= Timing){
        if (id!=""){
	    LOG_PTAG(Error)<<"TimeLag variable in use with label " << id 
	    <<flush;
	    return;
	}
    	id = Label;
	mode=Simple;
	nStops=0;
	sumdclock=0;
	sumdwall=0;
	dwall=0;
	dclock=0;
	
	paused=false;
	stoped=false;
	
	time(&wallStart);
	wallFirstStart=wallStart;
	procStart=clock();
    }
}
	
void TimeLag::pause(const string Label){
    if (Log::get_warn_level() <= Timing){
	procEnd=clock();
	time(&wallEnd);
     	if (id!=Label){
 	    LOG_PTAG(Error) << "TimeLag label does not match. Expected: " <<
	    id << ", got: " << Label << flush;
	    return;
	}
	paused=true;
    	if (mode<Cumulative){
	    mode = Cumulative;
	    LOG_PTAG(Detail) << "Timing in cumulative mode.";
	}
	dclock += dclock_time();
	dwall += wallEnd-wallStart;
    }
}

void TimeLag::cont(string Label){
    if (Log::get_warn_level() <= Timing){
      	if (id!=Label){
 	    LOG_PTAG(Error) << "TimeLag label does not match. Expected: " <<
	    id << ", got: " << Label<< flush;
	    return;
	}
    	if (!paused){
	    LOG_PTAG(Error) << "Not paused.";
	    return;
	}
	paused=false;
	time(&wallStart);
	procStart=clock();
    }
}

void TimeLag::stop(string Label){
    if (Log::get_warn_level() <= Timing){
	procEnd=clock();
	time(&wallEnd);
     	if (id!=Label){
 	    LOG_PTAG(Error) << "TimeLag label does not match. Expected: " <<
	    id << ", got: " << Label<< flush;
	    return;
	}
    	if (mode<Average){
	    mode = Average;
	    LOG_PTAG(Detail) << "Timing in average mode.";
	}
	if (!paused){
	    dclock += dclock_time();
	    dwall += wallEnd-wallStart;
	    paused=true;/*check this*/
	}
	nStops++;
	sumdclock+=dclock;
	sumdwall+=dwall;
	stoped=true;
    }
}
void TimeLag::restart(string Label){
    if (Log::get_warn_level() <= Timing){
      	if (id!=Label){
 	    LOG_PTAG(Error) << "TimeLag label does not match. Expected: " <<
	    id << ", got: " << Label<< flush;
	    return;
	}
    	if (!stoped){
	    LOG_PTAG(Error) << "Not stoped.";
	    return;
	}
	dwall=0;
	dclock=0;
	stoped=false;
	time(&wallStart);
	procStart=clock();
    }
}

void TimeLag::finish(string Label){
    if (Log::get_warn_level() <= Timing){
	procEnd=clock();
	time(&wallEnd);
    	if (id!=Label){
	    LOG_PTAG(Error) << "TimeLag label does not match. Expected: "
		<< id << ", got: " << Label<< flush;
	    return;
	}
	LOG_PTAG(Debug)<< dclock<<flush;
	if ((!stoped)||(!paused)){
	    dclock += dclock_time();
	    dwall += wallEnd-wallStart;
	}
	if (!stoped){
	    sumdclock+=dclock;
	    sumdwall+=dwall;
	    nStops++;
	}
    	if (mode==Average){
	    dclock=sumdclock/nStops;
	    dwall=sumdwall/nStops;
	}
	startString = ctime(&wallFirstStart); 
	startString.resize(startString.size()-1);
	
	Log::ptag(Timing)<< "Label= " << setw(20) << id << " start= " 
	    << wallFirstStart 
	    << "\t dProc= " << dclock << "\t dWall= " << dwall  << flush;
	if (mode>Simple)
	    Log::print(Timing)<<" "<< TLlabel(mode)<<flush;
	if (mode==Average)
	    Log::print(Timing) << " over " << nStops <<flush;
	    	
    }
}
//end-of-file
