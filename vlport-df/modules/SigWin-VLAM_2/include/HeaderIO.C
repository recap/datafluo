//****************************************************************************
/** \file HeaderIO.C 
 *  \brief Reads/Writes a header from/to a stream 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since April 24, 2007.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Apr 24, 2007:  Changed the scan function. 
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
#include "HeaderIO.H"

namespace Header{///Deals with headers

    
    namespace{//"private/static" 
    	///reads label "size"
   	void scan_size(const string& line, long& size){
    	    string::size_type pos=line.find("size=");
    	    if (pos < line.size())
	    	size = atol(line.substr(pos+sizeof("size=")-1).c_str());
    	    pos=line.find("max_size=");
    	    if (pos<line.size())
    	    	size = -atol(line.substr(pos+sizeof("max_size=")-1).c_str());
    	}

    	///reads label "ncols"
    	void scan_ncols(const string& line, int& ncols){
    	    string::size_type pos=line.find("ncols=");
    	    if (pos<line.size())
    	    	ncols = atoi(line.substr(pos+sizeof("ncols=")-1).c_str());
    	}

    	///reads label "descr"
    	void scan_descr(const string& line, string& descr){
    	    string::size_type pos=line.find("descr=");
    	    if (pos<line.size()){
    		descr = line.substr(pos+sizeof("descr=")-1);
		Parser::trim(descr);
	    }
    	}
		
    	//reads header line into the string line
    	int scan_line(istream& inStream, string& line, char mark='#'){

    	    LOG_PTAG(High)<<"Reading header " << flush;
	    char c=inStream.peek();
            LOG_PTAG(Extreme)<< "c="<<c<<", mark="<<mark<< flush;
    	    if (c==EOF){
         	LOG_PTAG(Warning)<< "End-of-file! " << flush;
    		return -1;
	    }
            LOG_PTAG(Extreme)<< "c="<<c<<", mark="<<mark<< flush;
   	    if (c!=mark){
         	LOG_PTAG(Warning)<< "Header is missing! (mark="<<c<<")"<< flush;
		return 0;
	    }
	    
   	    if (!getline(inStream,line)){
    	     	LOG_PTAG(Fatal)<< "File corrupted!\n"<<flush;
        	exit(1);
    	    }
	    Parser::trim(line);
    	    LOG_PTAG(Detail)<<"line= '"<< line <<"'"<< flush;
    	    return 1;
	}
    }

    ///Reads the header.
    /** Header structure: starts with a <mark>, space after <mark> is optional
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
    */        
   bool scan(istream& inStream, long& size, int& ncols, string& descr,
    	string& line, const char mark){

    	size=0;
    	ncols=0;
    	descr="";
	LOG_PTAG(High)<<" mark= " << mark<<flush;
	int aux = scan_line(inStream, line, mark);
	LOG_PTAG(High)<<" scan_line return state " << aux<<flush;
	if(aux<0)
	    return false;
	if(aux<1)
	    return true;
    
    
   	LOG_PTAG(Extreme)<<"size=" << size << " ncols="<< ncols 
	    <<	" descr=" << descr <<" " <<flush;
	    
    	LOG_PTAG(High)<<"Processing arguments without label. "<<flush;
    	std::istringstream inString(line.substr(1));
    	string saux;
	if ((inString >> saux)&&(isdigit(saux[0]))){//reads vector size
	    size = atol(saux.c_str());
    	    if (inString >> saux){
	    	if(isdigit(saux[0])){//reads ncols
	    	    ncols = atoi(saux.c_str());
	    	}
	    	else
	    	    descr = saux;
	    }
	    if (getline(inString,saux))
	    	descr += saux;   
	    Parser::trim(descr);
    	}
   	LOG_PTAG(High)<<"size=" << size << " ncols="<< ncols 
	    <<	" descr=" << descr <<" " <<flush;
	
    	LOG_PTAG(High)<<"Processing arguments with label. " <<flush;
    	scan_size(line, size);
    	scan_ncols(line, ncols);
    	scan_descr(line, descr);
	
   	LOG_PTAG(Detail)<<"  <...>  size=" << size << " ncols="<< ncols 
	    <<	" descr=" << descr <<" " <<flush;
 	
    	return true;
    }    


    bool scan(istream& inStream, long& size, string&  line, const char mark){
	
    	if (scan_line(inStream, line, mark)<0)
	    return false;
    	if (scan_line(inStream, line, mark)<1)
	    return true;
    
    	size=0;
    	// reads arguments without label   
    	std::istringstream inString(line.substr(1));
    	string saux;
    	inString >> saux;
    	if (isdigit(saux[0]))//reads vector size
	    size = atol(saux.c_str());
	
    	//reads arguments with label
    	scan_size(line, size);
    	return true;
    }    

    void print(ostream& outStream,
    	const long size, const int ncols, const string descr, 
    	const char mark, const bool bare){
    	if (bare){
    	    LOG_PTAG(Detail)<<"Printing bare header. "  << flush;
    	    outStream <<mark <<" "<<size<<" "<<ncols<<" "<< descr <<endl<<flush;
    	}
    	else{
    	    LOG_PTAG(Detail)<<"Printing labeled header: " << flush;
    	    LOG_PTAG(High)<<mark << "size= " << size << " ncols= " << ncols
    	      	      << " descr= " << descr <<flush;
    	    outStream <<mark << "size= " << size << " ncols= " << ncols
    	      	      << " descr= " << descr << endl <<flush;
    	    //cout <<mark << "size= " << size << " ncols= " << ncols
    	     // 	      << " descr= " << descr << endl <<flush;
    	}
    } 
      
    void print(ostream& outStream, const long size, const string descr, 
	const char mark, const bool bare){
    	if (bare){
    	    LOG_PTAG(Detail)<<"Printing bare header. " << flush;
    	    outStream <<mark <<" "<<size<<" "<< descr <<endl<<flush;
    	}
    	else{
    	    LOG_PTAG(Detail)<<"Printing labeled header. " << flush;
    	    LOG_PTAG(High)<<mark <<"size= "<< size <<" descr= "<<descr<<flush;
    	    outStream <<mark <<"size= "<<size <<" descr= "<<descr <<endl<<flush;
    	    //cout <<mark <<"size= "<<size <<" descr= "<<descr <<endl<<flush;
    	}
    }   

    void print(ostream& outStream, const long size, const char mark){
    	LOG_PTAG(Detail)<<"Writing bare header. "  << flush;
    	outStream << mark << " " << size <<endl<<flush;
    }   
    
    bool match_description(string& givenDescr, const string& gotDescr, 
    	const string& label){
    	LOG_PTAG(High)<<"given=(" << givenDescr<< ") got=("
	    << gotDescr <<") "<< flush;
    	if ((givenDescr!="")&&(givenDescr!=gotDescr)){
    	    LOG_PTAG(Error) << "Labels do for "<< label << " not match! ";
    	    LOG_PTAG(Error) << " given= ";
    	    LOG_PTAG(Error) << givenDescr;
    	    LOG_PTAG(Error) << " got= ";
    	    LOG_PTAG(Error) << gotDescr <<flush;
	    return false;
    	}
    	else{
    	    givenDescr = gotDescr;
    	    return true;
    	}
    }
    
    int scan_labels(istream& inStream, vector<string>& label, 
    	const char mark){
    	string line;
    	if (!scan_line(inStream, line, mark))
	    return 0;
    	std::istringstream inString(line.substr(1));
	string saux;
	int count(0);
    	while (inString >> saux){
    	    label.push_back(saux);	    
	    count++;
	}
	return count;
    }
    
    bool match_descriptions(istream& inStream, vector<info>& cInfo, 
    	const char mark){
    	vector<string> label;
    	int nLabels = scan_labels(inStream, label, mark);    

    	for (vector<info>::iterator it=cInfo.begin();it!=cInfo.end(); it++){
	    long nr(it->number);
    	    if ((nr>=1) && (nr<=nLabels)){
	    	LOG_PTAG(Info)<<"Getting column " << nr 
		    	      << " labeled " << label.at(nr-1)<< flush;
	    	it->label = label.at(nr-1);
    	    }
	    else{
	    	vector<string>::iterator it2=label.begin();
	    	int count(1);
	    	while (it2!=label.end()){
		    if (*it2 == it->label){
		    	LOG_PTAG(Info)<<"Getting column " 
		    	    << count  << " labeled " << it->label<< flush;
		    	it->number = count;
		    	break;
		    }
		    it2++;
		    count++;
	    	}
	    	if (count>nLabels){
	    	    LOG_PTAG(Error)<<"Label "<<it->label<<" not found!"<< flush;
    	    	    return false;
	    	}
	    }
	}
	return true;	    
    }  
        
    template <class T>
    bool scan_property(const string& name, const string& line, T& value){
    	string tag= ( name[name.size()-1]=='=' ? name : name+"=");
    	string::size_type pos=line.find(tag);
    	if (pos < line.size()){
    	    std::istringstream inString(line.substr(pos+sizeof(tag)-1));
	    if (inString >> value)
	    	return true;
	    else{
    	    	LOG_PTAG(Error)<<"Error converting value for property "  <<
		    	    	name << " " << flush;
	    	return false;
	    }
	}
    	LOG_PTAG(Warning)<<"Property "  <<name << " not found!" << flush;
	return false;
    }
    
    void print_labels(ostream& outStream, const vector<string>& label, 
    	const char mark){
    	outStream << mark;
	for (string::size_type i=0; i<label.size(); ++i)
	    outStream << label.at(i) << "\t";
	outStream << endl << flush;	
    }

}
//end-of-file
