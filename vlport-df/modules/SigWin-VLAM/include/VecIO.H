//****************************************************************************
/** \file VecIO.H 
 *  \brief Reads/Writes a vector from/to a stream 
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  April 24, 2007.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created:  October 17, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef VECIOH_
#define VECIOH_

#include <fstream>
#include <iostream>
#include <vector>
#include <string>
#include <sstream>
#include <typeinfo>
#include <stdlib.h>

#include "LogManager.H"
#include "DataIO.H"
#include "HeaderIO.H"
#include "Interval.H"

using namespace std;
//****************************************************************************
//prototypes
//****************************************************************************
///prints a vector to cout
template<class T> void  print_vector(const vector<T>& vec, const char* name="");

/// Writes raw vector to outStream
template<class T> 
void write_vector(ostream& outStream, const vector<T>& vec,
    const string descr="", const char mark='#');
/// Reads raw vector from inStream
template<class T> 
bool read_vector(istream& inStream, vector<T>& vec, string& descr,
    const char mark='#');

/// Prints vector to outFile (formated)
template<class T> 
void print_vector(ostream& outFile, const vector<T>& vec,
    const string descr="", const int vPerLine=20, const char mark='#');
/// Scans vector from inFile
template<class T> 
bool scan_vector(istream& inFile, vector<T>& vec, string& descr,
    const char mark='#');

/// Scans 1 column from input file. 
template<class T> 
bool scan_column(istream& inFile, vector<T>& vec, string& descr, const int col,
    string& colDescr, const char mark='#');

/// Scans 2 columns from input file. 
template<class T, class U> 
bool scan_columns(istream& inFile, string& descr, 
    vector<T>& vec1, const int col1, string& label1,
    vector<U>& vec2, const int col2, string& label2,
    const char mark='#');


/// Scans 3 columns from input file.
template<class T, class U, class V> 
bool scan_columns(istream& inFile, string& descr, 
    vector<T>& vec1, const int col1, string& label1,
    vector<U>& vec2, const int col2, string& label2,
    vector<V>& vec3, const int col3, string& label3,
    const char mark='#');

/// Scans 3 columns from input file. 
template<class T, class U, class V> 
bool scan_columns(istream& inFile, string& descr, 
    vector<Header::info>& cInfo, 
    vector<T>& vec1, vector<U>& vec2, vector<V>& vec3, 
    const char mark='#');

/// Streams vector from inFile (formated) to outStream (raw)
template<class T> 
long stream_vector2raw(istream& inFile, ostream& outStream,
    string& descr, const char mark='#');
/// Streams vector from inStream (raw) to outFile (formated)
template<class T> 
long stream_vector2ascii(istream& inStream, ostream& outFile,     
    string& descr, const int vPerLine=20, const char mark='#');


//****************************************************************************
//implementations
//****************************************************************************

///prints a vector to cout
template<class T> 
void  print_vector(const vector<T>& vec, const char* name){
    cout << name << endl;
    for (unsigned i=0; i<vec.size(); ++i) 
	cout << vec[i] << " ";
    cout << endl;
}

/// Writes raw vector to outStream
template<class T> 
void write_vector(ostream& outStream, const vector<T>& vec,
    const string descr, const char mark){
    LOG_PTAG(Detail)<< "Size=" << vec.size()<<flush;
    Header::print(outStream, vec.size(), descr, mark);
    //for_each(vec.begin(), vec.end(), bind1st(write_struct<T>, *outStream));
    for(unsigned i=0; i<vec.size(); ++i){
    	T aux = vec.at(i);
    	write_struct(outStream, aux);
	//LOG_PTAG(High)<<"i= " << i <<" value= " << vec[i] << flush;
	LOG_PTAG(High)<<"i= " << i <<" value= " << vec.at(i) << flush;
    }
    outStream.flush();
}
///In the case of string use "<<" instead
template<> 
void write_vector<>(ostream& outStream, const vector<string>& vec,
    const string descr, const char mark){
    LOG_PPTAG(High)<<"Writing with print"<<flush;
    print_vector<string>(outStream, vec, descr, 1, mark);
}


/// Prints vector to outFile (formated)
template <class T> 
void print_vector(ostream& outFile, const vector<T>& vec,
    const string descr, const int vPerLine, const char mark){
    Header::print(outFile, vec.size(), descr, mark);
    for (unsigned i=0; i<vec.size(); ++i){
    	outFile << vec.at(i) << " ";
	Log::print(High) << i<<"="<< vec.at(i) <<", "<<flush; 
	if (long(i)%vPerLine==vPerLine-1)
	    outFile << endl;
    }
    outFile <<endl << flush;
}



void warn_diff(const int vs, const int hs, const string& name="header"){
    LOG_PTAG(Warning) << "Vector size (" << vs << ") differs from " << name 
    	           << " value (" << hs << ") " << flush;
}



template <class T, class U>
bool _read_vec_known_(istream& inFile, long size, 
    bool (*read_func)(istream&, T&, const int), const int readParameter,
    bool (*write_func)(U&, T&), U& writeObject){

    //int div = (Log::get_warn_level() <= Detail? (size<=5? 1: size/5) : size);
    
    for (long i=0; i<size; ++i){
	T value;
	if (read_func(inFile, value, readParameter)){
	    write_func(writeObject, value);
	}
	else{
  	    warn_diff(i, size);
	    return false;
	}
	
    //	if (i % div==0)
   	    Log::print(Detail) << i << "=" << value << ", ";
    }
    Log::print(Detail) << " Done"<< flush;
    return true;
}

///Reads a vector from file. Stops on non class T value.
//output: number of items read
template <class T, class U>
long _read_vec_not_known_(istream& inFile, 
    bool (*read_func)(istream&, T&, const int), const int readParameter,
    bool (*write_func)(U&, T&), U& writeObject,
    const char mark='#'){ 

    LOG_PTAG(Detail)<<"input vector size not know"<<flush;
    
    long count(0);
    char c;
    c=inFile.peek();
    while ((c != mark)&&(c!=EOF)){
	T aux;
	if (read_func(inFile, aux, readParameter)){
    	    write_func(writeObject, aux);
	    ++count;
	}
	else{
	    LOG_PTAG(Warning) << "Input stream damaged! " << flush;
	    return count;
	}
	c=inFile.peek();
	while ((c==' ')||(c=='\n')||(c=='\t')){
	    inFile.get();
	    c=inFile.peek();
	} 
	Log::print(High) <<"peek=(" << c <<") "<<flush;  
    }
    if (c==mark){
    	string line;
    	getline(inFile,line);
    	long size;
    	size = atol(line.substr(1).c_str());		    
    	if ((size>0)&&(count!=size))
  	    warn_diff(count, size, "trailer");
    }
    LOG_PTAG(Debug) << "Input size =" << count<<flush;
    return count;
}



template <class T>
bool vec_push_back(vector<T>& vec, T& value){vec.push_back(value);return true;}

template <class T>
inline bool scan_value(istream& inFile, T& value, const int i=1){ 
    if (inFile >> value){
    	LOG_PTAG(High)<< value<<", "<<flush;
	return true;
    }
    else{
    	LOG_PPTAG(Warning) << "Problem reading inFile: "<<flush;
    	if ( (inFile.rdstate() & ifstream::failbit ) != 0 )
    	    Log::print(Warning)<< "failbit "<<flush;
    	if ( (inFile.rdstate() & ifstream::eofbit ) != 0 )
    	    Log::print(Warning)<< "eofbit "<<flush;
    	if ( (inFile.rdstate() & ifstream::badbit ) != 0 )
    	    Log::print(Warning)<< "badbit "<<flush;
    	return false;
    }
}



template <class T>
inline bool scan_ith_value(istream& inFile, T& value, const int col){ 
    string line, saux;
    getline(inFile,line);
    std::istringstream inString(line);
    for (int i=1; i<col; ++i)
    	inString >> saux;
    return (inString >> value);
}

//assumes col1<col2
template <class T, class U>
inline bool scan_values(istream& inFile, T& val1, const int col1, 
    U& val2, const int col2){ //col1<col2
    string line, saux;
    getline(inFile,line);
    std::istringstream inString(line);
    for (int i=1; i<col1; ++i)
    	inString >> saux;
    if (!(inString >> val1))
    	return false;
    for (int i=col1+1; i<col2; ++i)
    	inString >> saux;
    if (!(inString >> val2))
    	return false;
    return true;
}

template <class T, class U, class V>
inline bool scan_values(istream& inFile, T& val1, const int col1, 
    U& val2, const int col2, V& val3, const int col3){ //col1<col2<col3
    string line, saux;
    getline(inFile,line);
    LOG_PTAG(High) << "line= " << line << flush;
    std::istringstream inString(line);
    for (int i=1; i<col1; ++i)
    	inString >> saux;
    if (!(inString >> val1)){
    	LOG_PTAG(Error) << "Error converting value from column "
	    <<col1 << " to " 
	    << typeid(val1).name() << ". line= " << line << flush;
    	return false;
    }
    for (int i=col1+1; i<col2; ++i)
    	inString >> saux;
    if (!(inString >> val2)){
    	LOG_PTAG(Error) << "Could not convert column " <<col2 << " to " 
	    << typeid(val2).name() << flush;
    	return false;
    }
    for (int i=col2+1; i<col3; ++i)
    	inString >> saux;
    if (!(inString >> val3)){
    	LOG_PTAG(Error) << "Could not convert column " <<col3 << " to " 
	    << typeid(val3).name() << flush;
    	return false;
    }
    return true;
}

template <class T>
inline bool scan_values(istream& inFile, vector<T>& value, 
    const vector<int> col){ //value.size()==col.size()
    string line, saux;
    getline(inFile,line);
    std::istringstream inString(line);
    for (unsigned j=0; j<col.size(); j++){
    	for (int i=1; i<col.at(j); ++i)
    	    inString >> saux;
    	if (!(inString >> value.at(j)))
    	    return false;
    }
    return true;
}

/// Reads raw vector from inStream. Stops at EOF, if size is not known.
template<class T> 
bool read_vector(istream& inStream, vector<T>& vec, string& descr, 
    const char mark){
    LOG_PTAG(Detail)<<"Reading vector header. " << flush;
    long size(0);
    int ncols(0);
    string auxDescr="", line="";
   
    if (inStream.eof()){
    	LOG_PTAG(Detail) << "End-of-file! "<<flush;
    	return false;
    }
    if (!Header::scan(inStream, size, ncols, auxDescr, line, mark)){
    	LOG_PTAG(Detail) << "Problem reading header! "<<flush;
    	cout << "Problem reading header! "<<flush;
	return false; 
    }
    if (!Header::match_description(descr, auxDescr)){
    	LOG_PTAG(Debug)<<"Wrong description!"<<flush;
    	cout<<"Wrong description!"<<flush;
    	return false;
    }
	     
    bool success(true);
    if (size>0){//size is known 
    	LOG_PTAG(Debug)<<"Reading vector entries (vector size="<< size <<
	    "). " << flush;
   	vec.reserve(size);
	success = _read_vec_known_(inStream, size, &read_struct,1,  
	    	    	    	    &vec_push_back<T>, vec);
    }
    else{//size is not known
    	LOG_PTAG(Debug)<<"Reading vector entries (unknown vector size). " <<
	    flush;
	T aux;
	while(read_struct(inStream, aux)){
    	    LOG_PTAG(High) << "v=" << aux <<flush;
    	    vec.push_back(aux);
    	}
    }
    if (success){
    	Log::print(Debug)<<" success! vec.at(" << vec.size() 
	    << "-1)=" << vec.at(vec.size()-1) <<flush;
    	return true;
    }
    else{
    	LOG_PTAG(Warning)<<" Read vector failed"<<flush;
    	return false; 
    }
}
///in the case of strings read becomes scan
//######## this is not consitent with write_vector because write will
//######## write non-blank characters but read will only read 
//######## non-blank characters
template<> 
bool read_vector<>(istream& inStream, vector<string>& vec, string& descr, 
    const char mark){
    LOG_PPTAG(High)<< "Reading vector with scan" << flush;
    return scan_vector<string>(inStream, vec, descr,mark);
}


///Reads vector from inFile
///in the case of strings read becomes scan
//######## this is not consitent with print_vector because print will
//######## print non-blank characters but scan will only scan 
//######## non-blank characters
template <class T>
bool scan_vector(istream& inFile, vector<T>& vec, string& descr, 
    const char mark){
    
    LOG_PTAG(Detail)<<"Begin " << flush;
    long size(0);
    int ncols(0);
    string auxDescr, line="";
   
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark)){
    	LOG_PTAG(Detail) << "Problem reading header! "<<flush;
    	return false; 
    }
    if (!Header::match_description(descr, auxDescr)){
   	LOG_PTAG(Debug)<<"Wrong description!"<<flush;
    	return false;
    }
    bool success(true);
    if (size>0){//size is known
    	vec.reserve(size);
	success = _read_vec_known_(inFile, size, &scan_value<T>,1, 
	    	    	    	    &vec_push_back<T>,vec);	
    }
    else
	_read_vec_not_known_(inFile, &scan_value<T>,1,&vec_push_back<T>,vec, 	    	    mark);	
    return true;
}

/// Scans vector from many column inFile 
template<class T> 
bool scan_column(istream& inFile, vector<T>& vec, string& descr, 
    const int col, string& colDescr, 
    const char mark){
    
    LOG_PTAG(Detail)<<"Begin " << flush;
    long size(0);
    int ncols(0);
    string auxDescr, line="";
   
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
    if (col > ncols){
    	LOG_PTAG(Fatal)<<"ncols ("<< ncols <<") < col ("<< col <<")\n"<<flush;
	exit(1);
    }
    
    vector<string> label;
    int nLabels = Header::scan_labels(inFile, label, mark);    
    if (nLabels != ncols){
    	LOG_PTAG(Error) << "Wrong number of labels! "<<flush;
    }
    else if (!Header::match_description(colDescr, label.at(col-1), 
    	"vector label")
	){
    	return false;
    }  
    
    if (size>0){//size is known
    	vec.reserve(size);
	_read_vec_known_(inFile, size, &scan_ith_value<T>, col,
	    &vec_push_back<T>, vec);	
    }
    else
	_read_vec_not_known_(inFile, &scan_ith_value<T>, col, 
	    &vec_push_back<T>, vec, mark);	
    return true;
    
}

template <class T, class U>
void scan_columns_(long size, istream& inFile, vector<T>& vec1, const int col1,
    vector<U>& vec2, const int col2, const char mark='#'){
    if (size>0){//size is known
    	LOG_PTAG(Detail)<< "Size=" << size<<flush;
        vec1.resize(size);
        vec2.resize(size);
        for (long i=0; i<size; i++){
	    if (!scan_values(inFile,vec1.at(i),col1,vec2.at(i),col2)){
		LOG_PTAG(Fatal) << "Error reading vector!\n"<< flush;
	    	exit(1);
	    }
	}
	
    }
    else{
        long count(0);
        while (inFile.peek() != mark){
            T aux1;
	    U aux2;
	    if (scan_values(inFile,aux1,col1,aux2,col2)){
		vec1.push_back(aux1);
		vec2.push_back(aux2);
		count++;
	    }
	    else{
		LOG_PTAG(Fatal) << "Error reading vector!\n"<< flush;
	    	exit(1);
	    }
    	}
    	string line;
    	getline(inFile,line);
    	long size1;
    	size1 = atol(line.substr(1).c_str());		    
    	if ((size1>0)&&(count!=size1))
  	    warn_diff(count, size1, "trailer");
    }
}

/// Scans 2 vectors from many column inFile 
template<class T, class U> 
bool scan_columns(istream& inFile, string& descr, 
    vector<T>& vec1, const int col1, string& label1,
    vector<U>& vec2, const int col2, string& label2,
    const char mark){
    
    LOG_PTAG(Detail)<<"Begin " << flush;
    long size(0);
    int ncols(0);
    string auxDescr, line="";
   
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
	
    
    if ((col1 > ncols)||(col2> ncols)){
    	LOG_PTAG(Fatal)<<"Wrong number of columns.\n"<<flush;
	exit(1);
    }

    vector<string> label;
    int nLabels = Header::scan_labels(inFile, label, mark);    
    if (nLabels != ncols){
    	LOG_PTAG(Error) << "Wrong number of labels! "<<flush;
    }
    else if (!Header::match_description(label1, label.at(col1-1), 
    	"vector1 label")
	){
    	return false;
    }  
    else if (!Header::match_description(label2, label.at(col2-1), 
    	"vector2 label")
	){
    	return false;
    }  
    
    if (col1<col2)
    	scan_columns_(size,inFile,vec1,col1,vec2,col2,mark);
    else
    	scan_columns_(size,inFile,vec2,col2,vec1,col1,mark);
    
    LOG_PTAG(Detail)<< "Size=" << vec1.size()<<flush;
   
    return true;
    
}

template<class T, class U, class V> 
void scan_columns_(long size, istream& inFile, vector<T>& vec1, const int col1, 
    vector<U>& vec2, const int col2, vector<V>& vec3, const int col3,
    const char mark='#'){
    if (size>0){//size is known
        vec1.resize(size);
        vec2.resize(size);
        vec3.resize(size);
        for (long i=0; i<size; i++){
	    if (!scan_values(inFile, vec1.at(i),col1, vec2.at(i),col2,    	    	    	vec3.at(i),col3)){
		LOG_PTAG(Fatal) << "Error reading vector!\n"<< flush;
	    	exit(1);
	    }
	}
    }
    else{
        long count(0);
        while (inFile.peek() != mark){
            T aux1;
	    U aux2;
	    V aux3;
	    if (scan_values(inFile,aux1,col1,aux2,col2,aux3,col3)){
		vec1.push_back(aux1);
		vec2.push_back(aux2);
		vec3.push_back(aux3);
		count++;
	    }
	    else{
		LOG_PTAG(Fatal) << "Error reading vector!\n"<< flush;
	    	exit(1);
	    }
    	}
    	string line;
    	getline(inFile,line);
    	long size;
    	size = atol(line.substr(1).c_str());		    
    	if ((size>0)&&(count!=size))
  	    warn_diff(count, size, "trailer");
    }
}

/// Scans 3 vectors from many column inFile 
template<class T, class U, class V> 
bool scan_columns(istream& inFile, string& descr, 
    vector<T>& vec1, const int col1, string& label1,
    vector<U>& vec2, const int col2, string& label2,
    vector<V>& vec3, const int col3, string& label3,
    const char mark){
    
    LOG_PTAG(Detail)<<"Begin " << flush;
    long size(0);
    int ncols(0);
    string auxDescr, line="";
   
    if (inFile.eof())
    	return false;
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
	
    
    if ((col1 > ncols)||(col2> ncols)||(col3>ncols)){
    	LOG_PTAG(Fatal)<<"Wrong number of columns.\n"<<flush;
	exit(1);
    }

    vector<string> label;
    int nLabels = Header::scan_labels(inFile, label, mark);    
    if (nLabels != ncols){
    	LOG_PTAG(Error) << "Wrong number of labels! "<<flush;
    }
    else if (!Header::match_description(label1, label.at(col1-1), 
    	"vector1 label")){
    	return false;
    }  
    else if (!Header::match_description(label2, label.at(col2-1), 
    	"vector2 label")){
    	return false;
    }  
    else if (!Header::match_description(label3, label.at(col3-1), 
    	"vector3 label")){
    	return false;
    }  
    
    if (col1<col2){
    	if (col2<col3)
    	    scan_columns_(size,inFile,vec1,col1,vec2,col2,vec3,col3,mark);
	else if (col1<col3)
    	    scan_columns_(size,inFile,vec1,col1,vec3,col3,vec2,col2,mark);
	else
    	    scan_columns_(size,inFile,vec3,col3,vec1,col1,vec2,col2,mark);
    }
    else{
    	if (col1<col3)
    	    scan_columns_(size,inFile,vec2,col2,vec1,col1,vec3,col3,mark);
	else if (col2<col3)
    	    scan_columns_(size,inFile,vec2,col2,vec3,col3,vec1,col1,mark);
	else
	    scan_columns_(size,inFile,vec3,col3,vec2,col2,vec1,col1,mark);
    }
	
    
    return true;
    
}	    

template<class T, class U, class V> 
bool scan_columns(istream& inFile, string& descr, 
    vector<Header::info>& cInfo, 
    vector<T>& vec1, vector<U>& vec2, vector<V>& vec3, 
    const char mark){
    
    LOG_PTAG(Detail)<<"Begin " << flush;
    long size(0);
    int ncols(0);
    string auxDescr, line="";
   
    //scan Header
    if (inFile.eof())
    	return false;
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
	
    //scan labels
    if (!Header::match_descriptions(inFile, cInfo, mark))
    	return false;
    
    int col1 = cInfo.at(0).number;
    int col2 = cInfo.at(1).number; 
    int col3 = cInfo.at(2).number;
        
    if (col1<col2){
    	if (col2<col3)
    	    scan_columns_(size,inFile,vec1,col1,vec2,col2,vec3,col3,mark);
	else if (col1<col3)
    	    scan_columns_(size,inFile,vec1,col1,vec3,col3,vec2,col2,mark);
	else
    	    scan_columns_(size,inFile,vec3,col3,vec1,col1,vec2,col2,mark);
    }
    else{
    	if (col1<col3)
    	    scan_columns_(size,inFile,vec2,col2,vec1,col1,vec3,col3,mark);
	else if (col2<col3)
    	    scan_columns_(size,inFile,vec2,col2,vec3,col3,vec1,col1,mark);
	else
	    scan_columns_(size,inFile,vec3,col3,vec2,col2,vec1,col1,mark);
    }
	
    
    return true;
    
}	    

/// Streams vector from inFile (formated) to outStream (raw)
template <class T> 
long stream_vector2raw(istream& inFile, ostream& outStream, 
    string& descr, const char mark){
    long size(0);
    int ncols(0);
    
    string auxDescr, line="";
   
    if (inFile.eof())
    	return false;
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
          
    if (line != "")
	outStream << line << endl << flush;
//    else
//	outStream << mark <<"size-not-known descr=" << descr << endl << flush;
    
    if (size>0)
	_read_vec_known_(inFile,size,&scan_value<T>,1, &write_struct,outStream);
    else
	size = _read_vec_not_known_(inFile, &scan_value<T>,1, 	
	    &write_struct<T>,outStream, mark);	
    outStream.flush();
    
    return size;
}

/// Streams vector from inStream (raw) to outFile (formated)
template <class T>
long stream_vector2ascii(istream& inStream, ostream& outFile, 
    string& descr, const int vPerLine, const char mark){
    long size(0);
    int ncols(0);
    
    string auxDescr, line="";
   
    if (inStream.eof())
    	return false;
    if (!Header::scan(inStream, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;

    if (line != "")
	outFile << line << endl << flush;
//    else
//	outFile << mark <<"size-not-known descr=" << descr << endl << flush;

    if (size>0){//size is known 
	for (long i=0; i<size; i++){
     	    T aux;
    	    if (read_struct(inStream, aux)){
	    	outFile << aux << " ";
	    }
    	    else{
    	    	warn_diff(i, size);
 	    	break;
	    }
    	    if (i%vPerLine==vPerLine-1)
	        outFile << endl;
	}
    }
    else{//size is not known
	T aux;
    	while (read_struct(inStream, aux)){
	    outFile << aux << " ";
	    ++size;
    	    if (size%vPerLine==0)
	        outFile << endl;
    	}
    }
    outFile << endl << flush;
    return size;
}

inline bool print_string(ostream& outFile, string& value){ 
    return (outFile << value << " ");
}

/// Streams vector from inFile (formated) to outStream (raw)
long stream_string_vector2raw(istream& inFile, ostream& outStream, 
    string& descr, const char mark='#'){
    long size(0);
    int ncols(0);
    
    string auxDescr, line="";
   
    if (!Header::scan(inFile, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;
          
    if (line != "")
	outStream << line << endl << flush;
    
    if (size>0)
	_read_vec_known_(inFile,size,&scan_value<string>,1, &print_string,
	    outStream);
    else
	size = _read_vec_not_known_(inFile, &scan_value<string>,1,
	    &print_string, outStream, mark);	
    outStream.flush();
    
    return size;
}

/// Streams vector from inStream (raw) to outFile (formated)
long stream_string_vector2ascii(istream& inStream, ostream& outFile, 
    string& descr, const int vPerLine=20, const char mark='#'){
    long size(0);
    int ncols(0);
    
    string auxDescr, line="";
   
    if (!Header::scan(inStream, size, ncols, auxDescr, line, mark))
    	return false; 
    if (!Header::match_description(descr, auxDescr))
    	return false;

    if (line != "")
	outFile << line << endl << flush;

    if (size>0){//size is known 
	for (long i=0; i<size; i++){
     	    string aux;
    	    if (scan_value<string>(inStream, aux)){
	    	outFile << aux << " ";
	    }
    	    else{
    	    	warn_diff(i, size);
 	    	break;
	    }
    	    if (i%vPerLine==vPerLine-1)
	        outFile << endl;
	}
    }
    else{//size is not known
	string aux;
    	while (scan_value<string>(inStream, aux)){
	    outFile << aux << " ";
	    ++size;
    	    if (size%vPerLine==0)
	        outFile << endl;
    	}
    }
    outFile << endl << flush;
    return size;
}
#endif //VECIOH_


//end_of_file
