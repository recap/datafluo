//****************************************************************************
/*  \file Rank.H 
 *  \brief A class to access a vector in its ranked order 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since January 18, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 20, 2008: Added functions: get_sorted and get_sorted_no_duplicates. 
 *  Jan 18, 2008: Added a description string. When writing the Rank structure
 *  	    	The description string is passed through the vector value.
 *  Created: August 03, 2005
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/

#ifndef RANK_
#define RANK_

#include <algorithm>
#include <iterator>
#include <vector>
#include <iostream>

#include "LogManager.H" 
#include "DataIO.H"
#include "ArrayIO.H"
#include "VecIO.H"

using namespace std;



///sorts pointers on the values they point to. 
template<class T> class ptrLess: public binary_function <T, T, bool> {
  public: typedef typename vector<T>::const_iterator vec_citer;
  private: vec_citer v_it;
  public:
    ptrLess(const vector<T>& vec) : v_it(vec){};
    ptrLess(const typename vector<T>::const_iterator start) : v_it(start){};
    bool operator()(const int& left, const int& right) const
    	{return v_it[left] < v_it[right];}
};

/** \brief A class to access a vector in its ranked order.  
 *  	This class provides direct access to a sequence in its ranked order. 
 */ 
template <class T> class Rank {
  public:
    typedef typename vector<T>::const_iterator vec_citer;
    
  private:
    long N;
    bool KeepCopy;

    string descr;//> Description.

    vector<T> value;
    vec_citer value_it;
    int *index;///index[n] is the index element ranked n. (inverse of rank)
    int *rank; ///rank[i] is the rank of element with index i. (inv. of index)

  public:
  
  /// @cond CODE
    //constructors & destructors
    Rank<T>() : N(0), KeepCopy(true) {alloc();}
    Rank<T>(const vector<T>& vec, const bool save=false) 
    	: N(vec.size()), KeepCopy(save) {init(vec.begin(), vec.end());}
    Rank<T>(const vec_citer start, const vec_citer end, const bool save=true)
    	: N(end-start), KeepCopy(save) {init(start, end);}    

    Rank<T>(const vector<T>& vec, const string& s, const bool save=false) 
    	: N(vec.size()), KeepCopy(save), descr(s) {init(vec.begin(),vec.end());}
    Rank<T>(const vec_citer start, const vec_citer end, const string& s, 
    	const bool save=true): N(end-start), KeepCopy(save), descr(s) {
	init(start, end);
    }    


    /// copy constructor
    Rank<T>(const Rank<T>& rank);
     
    ~Rank<T>() {clear();} 
    


    //functions
    long size(void) const {return N;}
    T get(const int i) const {return value_it[i];}
    T get_ranked(const int n) const {return value_it[index[n]];}
    T at(const int n) const {return value_it[index[n]];}//same as get_ranked
    int get_index_ranked(const int n) const {return index[n];}
    int rank_of(const int i) const {return rank[i];}
    
    ///Median 
    double median(void) const {
    	return (N%2==1 ? value_it[index[N/2]]                             
	               : (value_it[index[N/2]] + value_it[index[N/2-1]])/2.0);
    }  
    ///Median assuming Size is odd.
    double median_odd(void) const { return value_it[index[N/2]];}
   
    ///Returns the index value where the median is stored. 
    ///If N is even retuns the index of the smallest median value.
    int median_index(void) const { return index[(N-1)/2];}  
    

    vector<T> get_sorted(void) const;
    vector<T> get_sorted_no_duplicates(void) const;

    void write_parameters(ostream& outStream) const;
    void write_data(ostream& outStream) const;
    void write(ostream& out) const {write_parameters(out), write_data(out);}
    //bool read(istream& inStream); use read version below instead
    bool read(istream& inStream, const vec_citer start=vec_citer(0));

    void write_sorted(ostream& outStream) const;
    long write_sorted_no_duplicates(ostream& outStream) const;
      
    void print(ostream& outStream=cout) const {
    	outStream << descr;
    	for (int i=0; i<N; i++)
	    outStream << i << "-th index= " << index[i]
	              << " value=" << value_it[index[i]] << endl;
    } 
    void print_index(ostream& outStream=cout) const {
    	outStream << descr;
    	for (int i=0; i<N; i++)
	    outStream << i << "-th index= " << index[i] << endl;
    } 

    void print_value(ostream& outStream=cout) const {
    	outStream << descr;
    	for (int i=0; i<N; i++)
	    outStream << i << "-th value= " << value_it[index[i]] << endl;
    } 
    void print_vector(ostream& outStream=cout) const {
    	outStream << descr << " of size " << N <<": ";
    	for (int i=0; i<N; i++)
	    outStream << value_it[i] << " ";
	outStream << endl;
    }

    void set_description(const string& a) {descr=a;}
    string get_description(void) const {return descr;}
    string description(void) const {return descr;}
    
  private:
    void init(const vec_citer start, const vec_citer end){
    	alloc();
	rerank(start, end);
    }
    
    void rerank(const vec_citer start, const vec_citer end);
    
    void alloc(void);
    void clear(void){delete[] rank; delete[] index;}
    void realloc(const int n){clear(); N=n; alloc();}
};	

// copy constructor
template <class T> Rank<T>::Rank(const Rank<T>& v) :
    N(v.N),
    KeepCopy(v.KeepCopy),
    descr(v.descr) { 
    alloc();
    if (v.KeepCopy){
	value = v.value;
	value_it = value.begin();
    }
    else 
        value_it = v.value_it;
    
    for (int i=0; i<N; i++){ 
	index[i] = v.index[i];
	rank[i] = v.rank[i];
    }
}

template <class T> void Rank<T>::alloc(void){
    index = new (nothrow) int[N]; 
    if (index==0){
	LOG_PPTAG(Fatal)<<"Out of memory allocating index. Size="<< N  << endl 
	    <<flush;
	exit(1);
    }
    rank = new (nothrow) int[N];
    if (rank==0){
	LOG_PPTAG(Fatal)<<"Out of memory allocating rank. Size="<< N  << endl 
	    <<flush;
	exit(1);
    }
}
     
template <class T> 
void Rank<T>::rerank(const vec_citer start, const vec_citer end) { 
    if (end-start != N) 
    	LOG_PTAG(Error) << "wrong vector size."; 

    if (KeepCopy){
	value.assign(start, end);
	value_it = value.begin();
    }
    else
    	value_it = start;
    
    for (int i=0; i<N; i++)
    	index[i] = i;

    sort(index, index+N, ptrLess<T>(start) );
    for (int i=0; i<N; i++)
	rank[index[i]]=i;
}

template <class T> 
vector<T> Rank<T>::get_sorted(void) const {
    vector<T> outVec;
    outVec.reserve(N);
    for(long i=0; i<N; ++i)
    	outVec.push_back(get_ranked(i));
    return outVec;
}

template <class T> 
vector<T> Rank<T>::get_sorted_no_duplicates(void) const {
    vector<T> outVec;
    T oldValue(get_ranked(0));
    outVec.push_back(oldValue);
    for(long i=1; i<N; ++i){
    	T newValue(get_ranked(i));
	if (newValue != oldValue){
    	    outVec.push_back(newValue);
	    oldValue = newValue;
	}
    } 
    return outVec;
}

template <class T> 
void Rank<T>::write_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Writing parameters..." << flush; 
    write_struct(outStream, N);
    outStream.flush();
    write_struct(outStream, KeepCopy);
    outStream.flush();
}

template <class T> 
void Rank<T>::write_data(ostream& outStream) const {
    LOG_PTAG(Debug) << "Writing data..." << flush; 
    if (KeepCopy){
    	LOG_PTAG(Debug) << "Writing vector..." << flush; 
    	write_vector(outStream, value, descr);
    }
    else 
    	LOG_PTAG(Warning)<<"Assuming vector is available" << flush;
    
    LOG_PTAG(Debug) << "Writing index..." << flush; 
    write_array(outStream, index, N);
    LOG_PTAG(Debug) << "Writing rank..." << flush; 
    write_array(outStream, rank, N);
}

template <class T> 
bool Rank<T>::read(istream& inStream, const vec_citer start) {
    LOG_PTAG(Debug) << "Reading parameters..." << flush; 
    long Naux;
    bool KeepCopyAux;
    if (!read_struct(inStream, Naux)) return false;
    if (!read_struct(inStream, KeepCopyAux)){
    	LOG_PTAG(Error)<<"Problem reading Rank::KeepCopyAux!";
	return false;
    }
    if (Naux!=N)
    	realloc(Naux);
	
    if (start==vec_citer(0)){
    	if (KeepCopy){
    	    LOG_PTAG(Debug) << "Reading vector..." << flush; 
    	    value.clear();
	    descr="";
    	    if (!read_vector(inStream, value, descr)){
    	    	LOG_PTAG(Error)<<"Problem reading Rank::value!";
	    	return false;
	    }
	    value_it=value.begin();
    	}	
    	else 
    	    LOG_PTAG(Warning)<<"Keeping old values vector address" << flush;
    }
    else{
    	if (KeepCopy){
    	    LOG_PTAG(Error)<<"Vector should be passed separately" << flush;
    	}	
    	else{
	    value_it=start;
    	    LOG_PTAG(Warning)<<"Assuming vector is available" << flush;
    	}
    }
    LOG_PTAG(Debug) << "Reading index..." << flush; 
    if (read_array(inStream, index, N) != N){
    	LOG_PTAG(Error)<<"Problem reading Rank::index!";
	return false;
    }
    
    LOG_PTAG(Debug) << "Reading rank..." << flush; 
    if (read_array(inStream, rank, N) != N){
    	LOG_PTAG(Error)<<"Problem reading Rank::rank!";
	return false;
    }
   
    return true;
}


template <class T> void Rank<T>::write_sorted(ostream& outStream) const {
    outStream << "#size= " << N << " descr=" << descr <<":sorted" <<endl<<flush;
    for(long i=0; i<N; ++i){
    	T aux = get_ranked(i);
    	write_struct(outStream, aux);
    } 
    outStream.flush();
}

template <class T> 
long Rank<T>::write_sorted_no_duplicates(ostream& outStream) const {
    outStream<<"#max_size= "<<N<<" descr=" <<descr<<":sorted-no-ties"<<endl;
    outStream.flush();
    
    LOG_PTAG(Debug) <<"N= "<< N << endl << flush;     
    T oldValue(get_ranked(0));
    write_struct(outStream, oldValue);
    long count(1);
    for(long i=1; i<N; ++i){ 
    	T newValue(get_ranked(i));
	if (newValue != oldValue){
    	    write_struct(outStream, newValue);
	    oldValue = newValue;
	    LOG_PTAG(Detail)<< count << " = " << newValue << flush;
	    ++count;
	}
    }
	 
    outStream.flush(); 
    return count;
}				 
    
#endif //Rank

