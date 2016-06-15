//*****************************************************************************
/** \File LogManager.C
 *  \brief Manages the loging of messages. 
 *  ### Warning #### 
 *  	Something might be wrong with the timimg procedures!
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
#include "LogManager.H"
#include <ctime>
#include <cctype>


static const char* WarnLabel[] = {"EXTREME", "HIGH", "DETAIL", "DEBUG", 
	     	    	    	  "INFO","TIMING","RELEASE",
	     	    	    	  "WARNING","ERROR","FATAL","NOWARN"};
static const int NLabels=11;

string WarnType2string(const WarnType &wt){
    return WarnLabel[wt];
}
ostream &operator>> (ostream &out, const WarnType &level){
    out << WarnLabel[level];
    return out;
}

WarnType string2WarnType(const string &str){
    string saux(str);
    for (unsigned i=0; i<str.size(); i++)
    	saux[i] = std::toupper(str[i]);
    int i(0);
    while (i<=NLabels){
	if (saux==WarnLabel[i])
	    break;
	i++;
    }
    if (i<NLabels)
	return static_cast<WarnType>(i);   
    else{
        Log::ptag(Warning) << "WarnType undefided ("<< saux 
		    	    << ") assigning default WarnType (" 
		    	    << DEFAULTLOGLEVEL << ")" << flush;
	return DEFAULTLOGLEVEL;
    }
}
				  

istream &operator<<(istream& in, WarnType& level){
    string saux;
    in >> saux;

    if (in){// only if the int input worked
    	level = string2WarnType(saux);
    }
    return in;
} 


namespace Log{
   
    namespace { //"private/static" 
    	ostream nullstream(NULL);
    	ostream* FilePtr=&cout;
	ofstream LogFile;
	string LogName;
	string LogFileName;
    	time_t startTime;
	WarnType warnLevel(Info);
//look=====================================================
	const char* 
	WarnLabel[] = 
	    {"EXTREME", "HIGH", "DETAIL", "DEBUG", 
	     "INFO","TIMING","RELEASE",
	     "WARNING","ERROR","FATAL"};
    
    	///Gets unique id for log file: <date>.<time>.<pid>"
	string logID; //unique log identity
    	void get_log_id(void){
     	    time_t rawtime;
    	    time(&rawtime);
    	    struct tm *ltime = localtime(&rawtime);
    	    char id[30];
	    sprintf(id, "%04d-%02d-%02d.%02d-%02d-%02d.%d", ltime->tm_year+1900,
	     	ltime->tm_mon+1, ltime->tm_mday, 
		ltime->tm_hour, ltime->tm_min, ltime->tm_sec, getpid());
	    logID = string(id);
    	}

    }

    ostream& print(const WarnType& Level) {
     	if ((Level == NoWarn)||(Level < warnLevel))
	    return nullstream; 
        return *FilePtr;
    }
    
    ostream& ptag(const WarnType& Level) {	    
     	if ((Level == NoWarn)||(Level < warnLevel))
	    return nullstream; 
	*FilePtr << endl;
	FilePtr->setf(ios_base::left,ios_base::adjustfield);
    	*FilePtr << setw(7) << WarnLabel[Level] <<": ";
	return *FilePtr;
    	//return print(Level) << setw(7) << WarnLabel[Level] <<": ";
    }
    
    //Prints the timestamp if warning level is at least Level
    time_t time_stamp(const WarnType& Level, string label=""){
    	if ((Level==NoWarn)||(Level < warnLevel))
	    return 0;
	time_t rawtime;
    	time ( &rawtime );
	string t(ctime(&rawtime)); 
	t.resize(t.size()-1);
    	ptag(Level) << label <<" : "<< t << " ( "<< rawtime << " ). ";
	return rawtime;
    }

    void set_warn_level(const WarnType Level, const bool log){
	warnLevel=Level; 
	if (log) 
	    ptag(Info)<<"Warn level has been set to " 
	              << WarnLabel[warnLevel] << endl << flush;
    }
    WarnType get_warn_level(void){return warnLevel;}
    
    void init(ostream* file, const WarnType wl){
    	FilePtr=file; 
	set_warn_level(wl); 
    }
    
    string make_log_file_name(const string& dir=DefaultLogDir){
	if (dir.at(dir.size()-1)!= '/')
	    return dir + "/My" + LogName + "." + logID + ".log";
	else
	    return dir + "My" + LogName + "." + logID + ".log";
    }  
    
    void open(const string& name, const WarnType wl){
 	set_warn_level(wl);

    	//Define a unique name for the log file.
	get_log_id();
	LogName=name;
    	string::size_type loc=LogName.find(".log");
	if (loc!=string::npos)
	    LogName.resize(loc);
	    
	LogFileName = make_log_file_name();
		
		
    	//open file
  	if (LogFile.is_open()){
	    ptag(Error) << "Log file alread open. Closing file! ";
	    LogFile.close();
	}
	LogFile.open(LogFileName.c_str());
	if (!LogFile.is_open()){
	    ptag(Error) << "Error opening file " << LogFileName
	    	        << ". Keeping old LogFile. ";
	}
	else{
	    FilePtr=&LogFile;
	    ptag(Info) << "Log file set to " << LogFileName << ". ";
	    startTime = time_stamp(Release, "Starting  " + LogName);
	}
	set_warn_level(wl);    
    }
    
    void close(void){
 	if (!LogFile.is_open()){
	    ptag(Error) << "Cannot close Log file! ";
	}
	else{
	    ptag(Info) << "Closing file. ";
	    time_t finishTime = time_stamp(Release, "Finishing " + LogName);
	    print(Release) << " time elapsed: " << finishTime - startTime 
	    	<< " secs." << endl;
	    ptag(Info) << "Redirecting Log to cout.";
	    LogFile.close();
	    FilePtr=&cout;
	}
    }
   
};
//end_of_File
/* alternative version for <<
    	switch (::tolower(saux[0])){
	    case 'h':
	    	level = High;
		break;
	    case 'i':
	    	level = Info;
		break;
	    case 't':
	    	level = Timing;
		break;
	    case 'r':
	    	level = Release;
		break;
	    case 'w':
	    	level = Warn;
		break;
	    case 'f'
	    	level = Fatal;
		break;
	    case 'n'
	    	level = NoWarn;
		break;
	    case 'd':
	    	if (::tolower(saux[2])=='t'){
	    	    level = Detail;
		    break;
		}
	    	if (::tolower(saux[2])=='b'){
		    level = Debug;
		    break;
		}
    	    	Log::ptag(Warning) << "WarnType undefided ("<< saux 
		    	    	    << ") assigning default WarnType (" 
				    << DEFAULTLOGLEVEL << ")" << flush;
		level = DEFAULTLOGLEVEL;
	    	break;
	    case 'e':
	    	if (::tolower(saux[1])=='x'){
		    level = Extreme;
		    break;
		}
		else if (::tolower(saux[1])=='r'){
		    level = Error;
		    break;
		}
		//fall through
	    default:
    	    	Log::ptag(Warning) << "WarnType undefided ("<< saux 
		    	    	    << ") assigning default WarnType (" 
				    << DEFAULTLOGLEVEL << ")" << flush;
		level = DEFAULTLOGLEVEL;
	    	break;
    	}
*/
