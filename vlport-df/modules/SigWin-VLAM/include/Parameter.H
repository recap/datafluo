//****************************************************************************
/** \file Parameter.H 
 *  \brief Gets parameters from WS-VLAM. 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 05, 2007.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: November 18, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef PARAMETERH_
#define PARAMETERH_

//#include <vlapp.h>
#include <map>
#include <fstream>
#include <typeinfo> 

//#include <rtsm.h>

//#include <mutexg.h>
//#include <threadg.h>
//#include <excepti.h>

#include "LogManager.H"
#include "Parser.H"

template <class T>
class Parameter{
  public:
    const string name;  
    T value;
     
    Parameter(){};
    Parameter(const char* ParName) : name(ParName){};
    Parameter(const char* ParName, const T ParValue) 
    	: name(ParName), value(ParValue){};
    Parameter(const char* ParName, const char* ParValue) 
    	: name(ParName), value(ParValue){};   
     
    void getValue(const std::string& s){
    	std::istringstream is(s);
    	is >> value;
    }
};
    
template <>
void Parameter<bool>::getValue(const std::string& s){
    value=(((s[0]=='y')||(s[0]=='Y')||(s[0]=='t')||(s[0]=='T'))? true : false);
}

template <>
void Parameter<char>::getValue(const std::string& s){
    value=s[0];
}

template <>
void Parameter<WarnType>::getValue(const std::string& s){
    value=string2WarnType(s);
}

template <>
void Parameter<string>::getValue(const std::string& s){
    value=s;
}

template <>
void Parameter<vector<string> >::getValue(const std::string& s){
    // get input types
    Parser::tokenize(s, value);
}

template<class T> inline ostream &operator<<(ostream &s, const Parameter<T> &a){
    s << a.name << "<" << typeid(a.value).name() <<"> = " << a.value;
    return s;
};

template<> inline ostream &operator<<(ostream &s, const Parameter<bool> &a){
    s << a.name << "<"<< typeid(a.value).name() <<"> = "<< boolalpha<< a.value;
    return s;
};

inline ostream &operator<<(ostream &s,
     	    	           Parameter<vector<string> > &a){
    s << a.name << "<vector<string>> = ";
    for (vector<string>::iterator it=a.value.begin(); it!=a.value.end(); it++)
    	s <<  *it << " " ;
    return s;
};

template <class T> 
inline bool operator==(const Parameter<T> &x,const Parameter<T> &y){
    if (x.name != y.name) return false;
    if (x.value != y.value) return false;
    return true;
}

static string _get_par_string_;
#define GET_PARAMETER(PARAMETER) \
    _get_par_string_="",\
    getParameter(PARAMETER.name, _get_par_string_), \
    PARAMETER.getValue(_get_par_string_),\
    Log::ptag(Release) << PARAMETER << flush
    
    
#endif //PARAMETERH_
