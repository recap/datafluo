//****************************************************************************
/** \file SWMedianLogProbNoStore.H 
 *  \brief Computes sliding window median probabilities.
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  Jan 31, 2008.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 2008: Changed storage type from array to vector.
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created: March 14, 2007.
 *  Based on SWMedianLogProb.H, created August 31, 2005
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SWMEDIANLOGPROBNOSTORE_
#define SWMEDIANLOGPROBNOSTORE_

#include <cmath>
#include "SlideWindowBaseNoStore.H"
#include "Rank.H"


//****************************************************************************
/** \brief A class to store sliding window median exact probabilities of 
 *         a sequence of numbers.
 *
 *  	A class to compute and store the probability that the element ranked
 *  \f$j\f$ in a sequence of \f$N\f$ elements is the median in a subsequence 
 *  (or window) of size \f$M=2k+1\f$ of the original sequence.
 *  This probability is given by:
 *  \f[
 *     \sigma_{2k+1,N}(j) = \frac{C^j_k C^{N-j}_k}{C^N_{2k+1}}.
 *  \f]
 *  Here \f$C^N_M\f$ is the number of combinations of \f$N\f$ elements taken
 *  \f$M\f$ at a time.
 *
 * Notes:
 *  - The value of \f$\sigma_{2k+1,N}(j)\f$ for \f$j<k\f$ and 
 *    \f$j\geq N-2k+1\f$ is zero. Those values are not stored.
 *  - This probability function is symmetric: 
 *    \f$\sigma_{2k+1,N}(j) = \sigma_{2k+1,N}(n-j-1)\f$. We only store one 
 *    of those values.
 * 
 */ 
template <class T>
class SWMedianProbOddNoStore : public SlideWindowBaseNoStore<T>{
  public:
    SWMedianProbOddNoStore(const long n, const long min, const long max, 
    	const long step=2, const string d="") :
	SlideWindowBaseNoStore<T>(n,min,max,step,ProbSym,d),
	Ceil_N_2(ceil_div_2(n)){ 	
	init(); 
    }

    SWMedianProbOddNoStore(const long n=0, const long step=2, const string d="")
     	: SlideWindowBaseNoStore<T>(n,step,ProbSym,d), Ceil_N_2(ceil_div_2(n)){
	init();
    }

    SWMedianProbOddNoStore(const SlideWindowParameters p) :
    	SlideWindowBaseNoStore<T>(p), Ceil_N_2(ceil_div_2(p.size())){ 
	init();
    }
   
    void init(){
    	if ((N()!=0)&&((wMin()%2==0)||(wStep()%2!=0))){
	    LOG_PTAG(Fatal) << "Window size not odd: "<< definition() << endl;
	    exit(1);
    	}  
    	//this is probably too much
	v.reserve(max_nWindows());  
    }

    
    //functions for getting information about parameters
    SlideWindowParameters definition(void) const {return this->def;}
    long N(void) const {return definition().size();}
    long wMin(void) const {return definition().wMin();}
    long wMax(void) const {return definition().wMax();}
    long wStep(void) const {return definition().wStep();}
    long nWSizes(void) const {return definition().nWSizes();}
    long nWindows(long wSize) const{return definition().nWindows(wSize);}
    long min_nWindows(void) const {return definition().min_nWindows();}
    long max_nWindows(void) const {return definition().max_nWindows();}
    DataType type(void) const {return definition().datatype();}
    string description(void) const {return definition().get_description();}
    
    //functions for setting parameters   
    void set_type(const DataType t) {definition.set_datatype(t);}
    void set_description(const string d) {definition.set_description(d);}
       
    //set/get current window size
    long current_window_size(void) const {return this->currWsize;}
    void set_current_window_size(const long WindowSize){
    	this->window_index_test(WindowSize);
	this->currWsize=WindowSize;
	v.resize(current_storage_size());
    }

    // Moving median probability for rank at current windowSize 
    inline T get(const long rank) const;

    /// Moving median probability of an interval for current windowSize
    template <class U>
    T get(const Rank<U>& sequence, const U x, const U dx=0) const;
   
    /// Moving median probability of an interval for current windowSize
    template <class U>
    T get(const vector<U>& sequence, const U x, const U dx=0) const;

    //Compute/Set moving median probabilities at current windowSize.
    void median_probability(void){ 	    	
    	median_probability(current_window_size()/2,
	    log_first_element_prob(current_window_size()/2));
    }
   
    /// Checks if probability sum of current windowSize is acceptable (~1). 
    bool check_sum(const T MaxErr, const bool print=false) const;
	    
    ///computes probability sum of current windowSize
    T integrate(const bool print=false) const;
    void print_integrate(void) const;
    
    ///renormalizes probability if error larger than MaxErr
    void renormalize(const T MaxErr);	

    // PDF of WindowSize
    vector<T> window_content(void) const;
    
    /// Print sliding window median probability
    void print_window(ostream& outFile=cout, const string &s="") const ;
    void write_window(ostream& outStream, const string &s="") const ;

    
    //print probabilities of a list with duplicates
    template <typename U, template<typename> class W> //W = Rank or vector
    void print_prob(const long nNoD, const W<U>& seq, ostream& file=cout);
    template <typename U, template<typename> class W> //W = Rank or vector
    long print_current_prob(const long nNoD, const W<U>& seq, 
    	ostream& file=cout);
   
    //write probabilities of a list with duplicates
    template <typename U, template<typename> class W> //W = Rank or vector
    void write_prob(const long nNoD, const W<U>& seq, ostream& outStream);
    template <typename U, template<typename> class W> //W = Rank or vector
    long write_current_prob(const long nNoD, const W<U>& seq, 
     	ostream& outStream);

    //return the probabilities of a list with duplicates
    template <typename U, template<typename> class W>
    vector<T> get_current_prob(const W<U>& sequence);

    bool read_parameters(istream& inStream);

  protected:    

    long current_storage_size(void) const {
    	return Ceil_N_2-current_window_size()/2;}
  
  private:
    vector<T> v;//data vector   
   
    long Ceil_N_2; //> Ceil(N div 2)
    
    //functions 
    
    long storage_size_k(const long k) const {return Ceil_N_2-k;}
    long ceil_div_2(const long num) const {return (num+1)/2;}
    
    T get_raw(const long k, const long rank) const;
    
    // Probability of element k for window size = 2k+1.
    T first_element_prob(const long k) const;
    T log_first_element_prob(const long k) const;
    
    // Factor that should be multiplied by \sigma_{2k+1,N}(k)
    // to obtain \sigma_{2(k+1)+1,N}(k+1).
    T first_element_prob_factor(const long k) const;

    // Factor that should be multiplied by the probability of the j-th element
    // to obtain the probability of the j+1-th element.
    T next_element_factor(const long element, const long k) const;
    T log_next_element_factor(const long element, const long k) const;
    
    //Sliding window probability of the median at window size 2k+1.
    void median_probability(const long k, const T logFirst);	

    /// Replaces value[*][*] by its cumulative tail. Only second half of tail.
    void sum_tail(void);
   
};

//****************************************************************************
//functions
//****************************************************************************


//****************************************************************************
///Moving median probability for the element ranked rank at current windowSize 
template <class T>
inline T SWMedianProbOddNoStore<T>::get(const long rank) const {
    long k(current_window_size()/2);
    if ((rank < k) || (rank > N()-k-1))
    	return 0;
    else if (rank-k < storage_size_k(k))
    	return v.at(rank-k);
    else
    	return v.at(N()-rank-1-k);
}

/// Sliding window median probability for element ranked rank at windowSize=2k+1
//############ does not check window size #############
template <class T>
inline T SWMedianProbOddNoStore<T>::get_raw(const long k, const long rank) 
const {
    if ((rank < k) || (rank > N()-k-1))
    	return 0;
    else if (rank-k < storage_size_k(k))
    	return v.at(rank-k);
    else
    	return v.at(N()-rank-1-k);
}

/// Sliding window median probability of a interval for windowSize
// Simple algorithm
template <class T> template <class U>
T SWMedianProbOddNoStore<T>::get(const Rank<U>& sequence, const U x, const U dx)
const {
    long first(0);
    while ((first<N())&&(sequence.get_ranked(first) < x))
    	++first;
	
    long last(first);
    while ((last<N())&&(sequence.get_ranked(last) <= x+dx))
    	++last;
    
    long i(first);
    T sum(0);
    while ((i<last)&&(i<N())){
    	sum += get(i);
	++i;
    }
    return sum;
}				    

/// Sliding window median probability of a interval for windowSize
// Simple algorithm
template <class T> template <class U>
T SWMedianProbOddNoStore<T>::get(const vector<U>& sorted, const U x, const U dx)
const {
    long first(0);
    while ((first<N())&&(sorted.at(first) < x))
    	++first;
	
    long last(first);
    while ((last<N())&&(sorted.at(last) <= x+dx))
    	++last;
    
    long i(first);
    T sum(0);
    while ((i<last)&&(i<N)){
    	sum += get(i);
	++i;
    }
    return sum;
}

/// Replaces value[windowSize][*] by its cumulative tail. Only second half.
template <class T> void SWMedianProbOddNoStore<T>::sum_tail(void){
    for (long i=1; i<current_storage_size(); ++i)
    	v.at(i) += v.at(i-1);
} 

//****************************************************************************
/// Prints sliding window median probability of WindowSize
template <class T> 
void SWMedianProbOddNoStore<T>::print_window(ostream& outFile, const string& s)
    const {
    long precision = outFile.precision();
    outFile.precision(3);
    std::ostringstream saux;
    saux << description() << ":mmAprob" << current_window_size();
    print_vector(outFile, window_content(), saux.str());
    outFile.precision(precision);    
} 

template <class T> 
void SWMedianProbOddNoStore<T>::write_window(ostream& outStream,const string& s)
    const {
    std::ostringstream saux;
    saux << description() << ":mmAprob" << current_window_size();
    print_vector(outStream, window_content(), saux.str());
} 

 

///prints the sliding window median prob of an ordered sequence with duplicates
//W = vector or Rank
template <class T> template <typename U, template<typename> class W>
void SWMedianProbOddNoStore<T>::print_prob(const long nNoDupl,
    const W<U>& sequence, ostream& outFile){
    
    LOG_PTAG(Debug)<<"Printing probabilities. "<<flush;
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
    	set_current_window_size(windowSize);
	long count = print_current_prob(nNoDupl, sequence, outFile);
	if (nNoDupl != count)
	    LOG_PTAG(Error) << "Wrong list size!(" << count <<"). " << flush;
    }
    outFile << endl;
       
} 

///writes the sliding window median prob of an ordered sequence with duplicates
template <class T> template <typename U, template<typename> class W>
void SWMedianProbOddNoStore<T>::write_prob(const long nNoDupl,
    const W<U>& sequence, ostream& outStream){
    
    LOG_PTAG(Debug)<<"Writing probabilities: " << definition() << " " << flush;
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
     	set_current_window_size(windowSize);
   	LOG_PTAG(Debug)<<"windowSize= "<<windowSize <<" "<<flush;
	long count = write_current_prob(nNoDupl, sequence, outStream);
	if (nNoDupl != count)
	    LOG_PTAG(Error) << "Wrong list size! " << flush;
    }
    outStream.flush();
}

///prints the sliding window median prob of an ordered sequence with duplicates
template <class T> template <typename U, template<typename> class W>
long SWMedianProbOddNoStore<T>::print_current_prob(const long nNoDupl,
    const W<U>& sequence, ostream& outFile){
    
    //int precision = outFile.precision();
    //outFile.precision(3);

    vector<T> prob = get_current_prob(sequence);

    std::ostringstream s;
    s << description << ":mmprob" << current_window_size();
    print_vector(outFile, prob, s.str());
    return prob.size();    
/*
    median_probability(windowSize);
    long count(0);
    U oldItem(sequence.at(0));
    T prob(get(windowSize, 0));
    outFile <<"# "<< nNoDupl <<" windowSize="<< windowSize << endl<<flush;
    for (long j=1; j<N(); ++j){
	U item(sequence.at(j));
	if (item != oldItem){//print probability
	    //outFile << "(" << oldItem << ", " << prob  << ") ";
	    outFile <<  oldItem << " " << prob  << "\n";
	    prob = 0;
	    oldItem=item;
	    ++count;
	}
	prob += get(windowSize, j);
    }    
    //outFile << "(" << oldItem << ", " << prob  << ") ";
    outFile <<  oldItem << " " << prob  << "\n";
    outFile << endl; 
    //outFile.precision(precision); 
    return ++count;	
*/
} 



///writes the sliding window median probabilities of a sequence with duplicates
template <class T> template <typename U, template<typename> class W>
long SWMedianProbOddNoStore<T>::write_current_prob(const long nNoDupl,
    const W<U>& sequence, ostream& outStream){

    vector<T> prob = get_current_prob(sequence);

    std::ostringstream s;
    s << description() << ":mmprob" << current_window_size();
    write_vector(outStream, prob, s.str());
    return prob.size();

/*
    median_probability(windowSize);

    long count(0);
    U oldItem(sequence.at(0));
    T prob(get(windowSize, 0));
    outStream <<"# "<< nNoDupl <<" descr=" << descr <<":swprob:ws-"
    	    	    << windowSize << endl<<flush;
    for (long j=1; j<N(); ++j){
	U item(sequence.at(j));
	if (item != oldItem){//print probability
	    write_struct(outStream, prob);
	    prob = 0;
	    oldItem=item;
	    ++count;
	}
	prob+=get(windowSize, j);
    }    
    write_struct(outStream, prob);
    outStream.flush();
    return ++count;//?check this
*/
} 



	
template<class T> 
bool SWMedianProbOddNoStore<T>::read_parameters(istream& inStream){  
    if (!SlideWindowBaseNoStore<T>::read_parameters(inStream)) return false;
    Ceil_N_2=ceil_div_2(definition().size()); 
    return true;
}

//****************************************************************************
/** \brief Computes the pdf of the moving medians in a sequence of size N 
 *  	    at window size 2k+1.
 *
 *  \see{SWProbMedian}
 */
template <class T>
void SWMedianProbOddNoStore<T>::median_probability(const long k, 
    const T logFirst){
    set_current_window_size(2*k+1);
    
    T logValue(logFirst);
    v.at(0) = exp(logFirst);
    
    for (long i=1, element=k+1; i<storage_size_k(k); ++i, ++element){
   	logValue += log_next_element_factor(element-1, k);
     	v.at(i) = exp(logValue);
    }

}
	

///computes the sliding window median probabilities of a sequence with ties
template <class T> template <typename U, template<typename> class W>
vector<T> 
SWMedianProbOddNoStore<T>::get_current_prob(
const W<U>& sequence){

    median_probability();

    vector<T> outVec;
    outVec.reserve(N());
    U oldItem(sequence.at(0));
    T prob(get(0));
    for (long j=1; j<N(); ++j){
	U item(sequence.at(j));
	if (item != oldItem){
	    outVec.push_back(prob);
	    prob = 0;
	    oldItem=item;
	}
	prob+=get(j);
    }    
    outVec.push_back(prob);
    return outVec;
} 


//****************************************************************************
/// Checks if probability sum of windowSize is acceptable (~1). 
template <class T>
bool SWMedianProbOddNoStore<T>::check_sum(const T MaxErr,const bool print)const
{
    T error = fabs(integrate()-1);
    if (error > MaxErr){
    	if (print) cout <<"Error("<<current_window_size()<< ")= "<<error<<endl;
	return false;
    }
    return true;
}	

/// Computes the probability sum of window size. 
template <class T>
T SWMedianProbOddNoStore<T>::integrate(const bool print) const {
    long k(current_window_size()/2);
    T sum(0);
    for (long j=0; j<N(); ++j)
    	sum += get_raw(k,j);
    if (print)
    	cout << "Sum(" << current_window_size << ")= " << sum << endl;
    return sum;
}


///renormalizes probability of windowSize 
template <class T>
void SWMedianProbOddNoStore<T>::renormalize(const T MaxErr){
    T sum(integrate());
    if (fabs(sum-1.0)>MaxErr){
    	for (long j=0; j<current_storage_size(); ++j)
            v.at(j)/=sum;
    }
}

///Returns pdf of WindowSize
template <class T> 
vector<T> SWMedianProbOddNoStore<T>::window_content(void) const {
    vector<T> aux;
    for (long i=0; i<N(); i++){
    	aux.push_back(get(i));
    }
    return aux;
}

//****************************************************************************
/// Computes the probability of element k for window size=2k+1
/**
 *
 *  If \f$2k+1\f$ is the window size then \f$j=k\f$ is the first element such  
 *  that \f$\sigma_{2k+1,N}(j) \neq 0\f$. If \f$N\f$ is the sequence size then 
 *  \f[
 *   {\rm first\_element\_prob}(k) = \frac{(N-k-1)! (2k+1)!}{N!k!}
 *                           = \frac{(2k+1)\dots(k+1)}{N \dots (N-k)}
 *  \f]
 *   Notes: 
 *  - Only valid for odd window size.
 *  - Ranks start counting at 0.
 *  - Only works fo small N. 
 *
*/
template <class T>
inline T SWMedianProbOddNoStore<T>::first_element_prob(const long k) const {
    long i(2*k+1);
    long l(N());
    T fraction(1.0);

    while (i>=k+1){
    	fraction *= T(i)/l;
    	--i;
	--l;
    }
    return fraction;
    
}

/**
 *  \brief Computes log(\f$\sigma_{2k+1,N}(k)\f$).
 *
 *   Notes: 
 *  - Only valid for odd window size.
 *  - Ranks start counting at 0.
 *  - For large N.
 *  \see: SWMedianProbOddNoStore<N>::first_element_prob
 *
*/
template <class T>
inline T SWMedianProbOddNoStore<T>::log_first_element_prob(const long k) const {
    long i(2*k+1);
    long l(N());
    T sumLog(0);

    while (i>=k+1){
    	T fraction(T(i)/l);
	sumLog += log(fraction);
    	--i;
	--l;
    }
    return sumLog;
    
}
    
//****************************************************************************
/** \brief Computes the factor that should be multiplied by
 *  	    \f$\sigma_{2k+1,N}(k)\f$ to obtain \f$\sigma_{2(k+1)+1,N}(k+1)\f$
 *
 *  If \f$2k+1\f$ is the window size and \f$N\f$ is the sequence size, then  
 *  \f[
 *  	sigma_{2(k+1)+1,N}(k+1) = 
 *  	    	\frac{(2k+2)(2k+3)}{(k+1)(N-k-1) \sigma_{2k+1,N}(k)}. 
 *  \f]
 *  And
 *  \f[
  *   {\rm first\_element\_prob\_factor}(k) = \frac{(2k+2)(2k+3)}{(k+1)(N-k-1)}
 *  \f]
 *   Notes: 
 *  - Only valid for odd window size.
 *  - Ranks start counting at 0.
 *  \see{SWMedianProbOddNoStore<N>::first_element_prob}
 *
*/
template <class T>
inline T SWMedianProbOddNoStore<T>::first_element_prob_factor(const long k) const {
    long k1(k+1);
    long aux(2*k1+1);
    return T(aux*(aux+1))/(k1*(N()-k1));
    
}
    

//****************************************************************************
/** \brief Computes the factor that should be multiplied by the probability of 
 *         the j-th element to obtain the probability of the j+1-th element.
 *
 *  If \f$2k+1\f$ is the window size and \f$N\f$ is the sequence size then
 *  \f[
 *   {\rm next\_element\_factor}(j,k) = \frac{(j+1) (N-j-k-1)}{(N-j-1)(j-k+1)}
 *  \f]
 *   Notes: 
 *   - Only valid for odd window size, 
 *   - and for \f$j \geq k\f$, and for \f$j < N-k\f$.
 *   - Ranks start counting at 0.
 *
*/
template <class T>
inline T SWMedianProbOddNoStore<T>::next_element_factor(const long element, 
    const long k) const {
    long j1(element+1);
    long aux(N()-element-1);
    return T(j1 * (aux-k))/(aux*(j1-k));
}

/** \brief Computes log(\f$[(j+1) (N-j-k-1)][(N-j-1)(j-k+1)]\f$).
 *
 *   Notes: 
 *   - Only valid for odd window size, 
 *   - and for \f$j \geq k\f$, and for \f$j < N-k\f$.
 *   - Ranks start counting at 0.
 *   \see: SWMedianProbOddNoStore<N>::next_element_factor
 *
*/
template <class T>
inline T SWMedianProbOddNoStore<T>::log_next_element_factor(const long element,
    const long k) const {
    T j1(element+1);
    T aux(N()-element-1);
    return log( j1*(aux-k) / (aux*(j1-k)) );
}    




#endif //SWMEDIANLOGPROBNOSTORE_
