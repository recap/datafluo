//****************************************************************************
/** \file TestFlags.H 
 *  \brief Sets flags for which I/O ports to test. 
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  September 27, 2007.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created:  September 27, 2007.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef TESTFLAGSH_
#define TESTFLAGSH_
#include "LogManager.H"
#include <string>

static const string BOTH_ = "b B both Both BOTH a A all All ALL";
static const string INPUT_ = "i I in In IN input Input INPUT";
static const string OUTPUT_ = "o O out Out OUT output Output OUTPUT";

template <int n1, ///Number of input ports
    	  int n2> ///Number of output ports
class TestFlags{
  public:
    //constructors
    TestFlags():size(n1+n2){for (int i=0; i<size; i++)flag[i]=false;}
    TestFlags(string param):size(n1+n2){set(param);}
    
    //starts at 0    
    void set_name(int i, string name_){name[i]=name_;}
    void set_flag(int i, bool f=true){flag[i]=f;}
    string get_name(int i) const {return name[i];}
    bool get_flag(int i) const {return flag[i];}

 
    //starts at 1
    void set_in_name(int i, string name_){name[i-1]=name_;}
    void set_out_name(int i, string name_){name[n1+i-1]=name_;}
    string get_in_name(int i) const {return name[i-1];}
    string get_out_name(int i) const {return name[n1+i-1];}

    bool get_in_flag(int i) const {return flag[i-1];}
    bool get_out_flag(int i) const {return flag[n1+i-1];}
 
    //by name
    void set_flag(string name_, bool f=true){
    	for (int i=0; i<size; i++){
	    if (name_==name[i]){
		flag[i]=f;
		return;
	    }
	}
    	LOG_PTAG(Error)<< "Port " << name_ << " not in list! Returning false" 
	<< flush;
	return false;
    }
    bool get_flag(string name_) const {
    	for (int i=0; i<size; i++){
	    if (name_==name[i])
		return flag[i];
	}
    	LOG_PTAG(Error)<< "Port " << name_ << " not in list! Returning false" 
	<< flush;
	return false;
    }

    //Sets all flags. The string "param" has one of the predefined values
    // in BOTH_, INPUT_, OUTPUT_, or a list with the names of the flags to be
    //set separated by blanc spaces.  
    void set(string param){
	for (int i=0; i<size; i++)
	    flag[i]=false;
    	if (BOTH_.find(param, 0)!= string::npos){
	   for (int i=0; i<size; i++)
		flag[i]=true;
	}
	else if (INPUT_.find(param, 0)!= string::npos){
	   for (int i=0; i<n1; i++)
	    	flag[i]=true;
    	}
	else if (OUTPUT_.find(param, 0)!= string::npos){
	   for (int i=n1; i<size; i++)
	    	flag[i]=true;
    	}
	else{
	    for (int i=0; i<size; i++){
	    	if (param.find(name[i],0)!=string::npos)
		    flag[i]=true;
	    }
	}
    }
	    
  private:
    bool flag[n1+n2];
    string name[n1+n2];
    const int size;
};

template<int n1, int n2> 
inline ostream &operator<<(ostream &s, const TestFlags<n1,n2> &a){
    s << "(";  
    for (int i=0; i<n1+n2-1; i++)
    	s << a.get_name(i) <<"="<< boolalpha << a.get_flag(i)<< ", ";
    s << a.get_name(n1+n2-1) <<"="<< boolalpha << a.get_flag(n1+n2-1)<< ")"; 
    return s;
};

#endif //TESTFLAGSH_
