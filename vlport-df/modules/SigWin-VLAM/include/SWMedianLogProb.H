//****************************************************************************
/** \file SWMedianLogProb.H 
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
 *  Created:  August 31, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SWMEDIANLOGPROB_
#define SWMEDIANLOGPROB_

#include <cmath>
#include "SlideWindowBase.H"
#include "Rank.H"
#include <vector>


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
class SWMedianProbOdd : public SlideWindowBase<T> {
  public:
    SWMedianProbOdd(const long n, const long min, const long max, 
    	const long step=2, const string d="") :
	SlideWindowBase<T>(n,min,max,step,ProbSym,d), Ceil_N_2(ceil_div_2(n)){ 	
	init(); 
    }

    SWMedianProbOdd(const long n=0, const long step=2, const string d="") :
    	SlideWindowBase<T>(n,step,ProbSym,d), Ceil_N_2(ceil_div_2(n)) { init();}

    SWMedianProbOdd(const SlideWindowParameters p) :
    	SlideWindowBase<T>(p), Ceil_N_2(ceil_div_2(p.size())) { init();}
    
    
    //functions for getting information about parameters
    SlideWindowParameters definition(void) const {return this->def;}
    long N(void) const {return definition().size();}
    Serie<long> winDef(void) const {return definition().scope();}
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

    // Sliding window median probability for rank at windowSize 
    inline T get(const long windowSize, const long rank) const;

    /// Sliding window median probability of an interval for windowSize
    template <class U>
    T get(const Rank<U>& sequence, const long windowSize, const U x, 
    	const U dx=0) const;
   
    /// Sliding window median probability of an interval for windowSize
    template <class U>
    T get(const vector<U>& sequence, const long windowSize, const U x, 
    	const U dx=0) const;

    //Sliding window probability of the median at windowSize.
    void median_probability(const long windowSize){ 	    	
    	median_probability(windowSize/2, log_first_element_prob(windowSize/2));
    }

    //Sliding window probability of the median.
    void median_probability(void);
    
    /// Checks if probability sum of windowSize is acceptable (~1). 
    bool check_sum(const T MaxErr, const long windowSize, 
    	const bool print=false) const;
	
    /// Checks if probability sum of all window sizes is acceptable (~1). 
    bool check_sum(const T MaxErr, const bool print=false) const;	
    
    ///computes probability sum of windowSize
    T integrate(const long windowSize, const bool print=false) const;
    void print_integrate(void) const;

    /// Replaces value[*][*] by its cumulative tail. Only second half of tail.
    void sum_tail(void);
    
    ///renormalizes probability of windowSize if error larger than MaxErr
    void renormalize(const T MaxErr, const long windowSize);	
    ///renormalizes probability of windowSize
    void renormalize(const T MaxErr);	

    // PDF of WindowSize
    vector<T> window_content(const long WindowSize) const;
    
    /// Print sliding window median probability
    void print_window(const long WindowSize, ostream& outFile=cout, 
    	const string &s="") const;
    void print_raw(ostream& outFile=cout) const;
    
    void write_window(const long WindowSize, ostream& outStream, 
    	const string &s="") const ;
   
    //print probabilities of a list with duplicates
    template <typename U, template<typename> class W> //W = Rank or vector
    void print_prob(const long nNoD, const W<U>& seq, ostream& file=cout) const;

    template <typename U, template<typename> class W> //W = Rank or vector
    long print_prob(const long nNoD, const W<U>& seq, const long windowSize, 
    	ostream& file=cout) const;
   
    //write probabilities of a list with duplicates
    template <typename U, template<typename> class W> //W = Rank or vector
    void write_prob(const long nNoD, const W<U>& seq, ostream& outStream) const;
#ifdef _LOCAL
    template <typename U, class K, template<typename, class> class W> //W = Rank or vector
    void write_prob(const long nNoD, const W<U,K>& seq, ostream& outStream) const;
    template <typename U, class K, template<typename, class> class W> //W = Rank or vector
    long write_prob(const long nNoD, const W<U,K>& seq, const long windowSize,
    	ostream& outStream) const;
#endif

    template <typename U, template<typename> class W> //W = Rank or vector
    long write_prob(const long nNoD, const W<U>& seq, const long windowSize,
    	ostream& outStream) const;



    bool read_parameters(istream& inStream);

   
  private:
    long Ceil_N_2; //> Ceil(N div 2)
    
    long window_size(const long index) const {return winDef().value(index);}
    long window_index(const long wSize)const{return winDef().index(wSize);}
    long window_index_test(const long wSize) const{
    	return winDef().index_test(wSize);
    }
    
    long storage_size(const long wSize) const {return Ceil_N_2-wSize/2;}
    long storage_size_k(const long k) const {return Ceil_N_2-k;}
    long ceil_div_2(const long num) const {return (num+1)/2;}
    T get_raw(const long k, const long rank, const long wIndex) const;
    
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
    void sum_tail(const long windowsize);
    

    void init(){
    	if ((N()!=0)&&((wMin()%2==0)||(wStep()%2!=0))){
	    LOG_PTAG(Fatal) << "Window size not odd: "<< definition() << endl;
	    exit(1);
    	}  
    	alloc();  
    }
    
    void alloc(void);
    void realloc(const SlideWindowParameters& newDef);

  private:
    vector<vector<T> > v;  //> structure where property values are stored.

};

//****************************************************************************
//functions
//****************************************************************************
template<class T> 
void SWMedianProbOdd<T>::alloc(void){
    v.resize(nWSizes());
    for (long i=0; i<nWSizes(); i++){
    	v.at(i).resize(storage_size(window_size(i)));
    }
}
template<class T> 
void SWMedianProbOdd<T>::realloc(const SlideWindowParameters& newDef){
    this->def = newDef;
    LOG_PTAG(High) << "New parameters=" << definition() << flush;
    alloc();	
}


//****************************************************************************
/// Sliding window median probability for the element ranked rank at windowSize 
template <class T>
inline T SWMedianProbOdd<T>::get(const long windowSize, const long rank) const {
    long k(windowSize/2);
    if ((rank < k) || (rank > N()-k-1))
    	return 0;
    else if (rank-k < storage_size_k(k))
    	return v.at(window_index(windowSize)).at(rank-k);
    else
    	return v.at(window_index(windowSize)).at(N()-rank-1-k);
}

/// Sliding window median probability for element ranked rank at windowSize=2k+1
template <class T>
inline T SWMedianProbOdd<T>::get_raw(const long k, const long rank, 
    const long wIndex) const {
    if ((rank < k) || (rank > N()-k-1))
    	return 0;
    else if (rank-k < storage_size_k(k))
    	return v.at(wIndex).at(rank-k);
    else
    	return v.at(wIndex).at(N()-rank-1-k);
}

/// Sliding window median probability of a interval for windowSize
// Simple algorithm
template <class T> template <class U>
T SWMedianProbOdd<T>::get(const Rank<U>& sequence, const long windowSize,
    const U x, const U dx) const {
    
    long first(0);
    while ((first<N)&&(sequence.get_ranked(first) < x))
    	++first;
	
    long last(first);
    while ((last<N)&&(sequence.get_ranked(last) <= x+dx))
    	++last;
    
    long i(first);
    T sum(0);
    while ((i<last)&&(i<N)){
    	sum += get(windowSize, i);
	++i;
    }
    return sum;
}				    

/// Sliding window median probability of a interval for windowSize
// Simple algorithm
template <class T> template <class U>
T SWMedianProbOdd<T>::get(const vector<U>& sorted, const long windowSize, 
    const U x, const U dx) const {
    
    long first(0);
    while ((first<N)&&(sorted.at(first) < x))
    	++first;
	
    long last(first);
    while ((last<N)&&(sorted.at(last) <= x+dx))
    	++last;
    
    long i(first);
    T sum(0);
    while ((i<last)&&(i<N)){
    	sum += get(windowSize, i);
	++i;
    }
    return sum;
}

/// Replaces value[windowSize][*] by its cumulative tail. Only second half.
template <class T> void SWMedianProbOdd<T>::sum_tail(const long windowSize){
    long wIndex=window_index(windowSize);
    for (unsigned i=1; i<v.at(wIndex).size(); ++i)
    	v.at(wIndex).at(i) += v.at(wIndex).at(i-1);
} 
/// Replaces value[*][*] by its cumulative tail.
template <class T> void SWMedianProbOdd<T>::sum_tail(void){
    SlideWindowBase<T>::sum_tail();
    set_type(NormTailSym);
}
//****************************************************************************
/// Prints sliding window median probability of WindowSize
/// Prints sliding window median probability of WindowSize
template <class T> 
void SWMedianProbOdd<T>::print_window(const long WindowSize, 
    ostream& outFile, const string& s) const {
    long precision = outFile.precision();
    outFile.precision(3);
    std::ostringstream saux;
    saux << description() << ":mmAprob" << WindowSize;
    print_vector(outFile, window_content(WindowSize), saux.str());
    outFile.precision(precision);    
} 


template <class T> void SWMedianProbOdd<T>::print_raw(ostream& outFile) const {
    int precision = outFile.precision();
    outFile.precision(3);
    for (long i=0; i<nWSizes(); i++){
    	std::ostringstream saux;
    	saux << description() << ":mmAprob" << window_size(i);
    	print_vector(outFile, v.at(i), saux.str());
    }
    outFile.precision(precision);    
} 
 
 
template <class T> 
void SWMedianProbOdd<T>::write_window(const long WindowSize, 
    ostream& outStream,const string& s) const {
    std::ostringstream saux;
    saux << description() << ":mmAprob" << WindowSize;
    print_vector(outStream, window_content(WindowSize), saux.str());
} 


///prints the sliding window median prob of a ordered sequence with duplicates
//W = vector or Rank
template <class T> template <typename U, template<typename> class W>
void SWMedianProbOdd<T>::print_prob(
    const long nNoDupl,
    const W<U>& sequence, 
    ostream& outFile) const {
    
    LOG_PTAG(Debug)<<"Printing probabilities. "<<flush;
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
	long count = print_prob(nNoDupl, sequence, windowSize, outFile);
	if (nNoDupl != count)
	    LOG_PTAG(Error) << "Wrong list size!(" << count <<"). " << flush;
    }
    outFile << endl;
       
} 

//template <class T> template <typename U, class K, template<typename, class> class W> //W = Rank or vector
//void SWMedianProbOdd<T>::test_prob(const W<U,K>&seq) const{
		
    //LOG_PTAG(Debug)<<"Writing probabilities: " << definition() << " " << flush;
//}
#ifdef _LOCAL
///writes the sliding window median prob of a ordered sequence with duplicates
template <class T> template <typename U, class K, template<typename, class> class W>
void SWMedianProbOdd<T>::write_prob(
    const long nNoDupl,
    const W<U,K>& sequence, 
    ostream& outStream) const {
    
    LOG_PTAG(Debug)<<"Writing probabilities: " << definition() << " " << flush;
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
    	LOG_PTAG(Debug)<<"windowSize= "<<windowSize <<" "<<flush;
	long count = write_prob(nNoDupl, sequence, windowSize, outStream);
	if (nNoDupl != count)
	    LOG_PTAG(Error) << "Wrong list size! " << flush;
    }
    outStream.flush();
}
#endif   
///writes the sliding window median prob of a ordered sequence with duplicates
template <class T> template <typename U, template<typename> class W>
void SWMedianProbOdd<T>::write_prob(
    const long nNoDupl,
    const W<U>& sequence, 
    ostream& outStream) const {
    
    LOG_PTAG(Debug)<<"Writing probabilities: " << definition() << " " << flush;
    for (long windowSize=wMin(); windowSize<=wMax(); windowSize+=wStep()){
    	LOG_PTAG(Debug)<<"windowSize= "<<windowSize <<" "<<flush;
	long count = write_prob(nNoDupl, sequence, windowSize, outStream);
	if (nNoDupl != count)
	    LOG_PTAG(Error) << "Wrong list size! " << flush;
    }
    outStream.flush();
}

///prints the sliding window median prob of a ordered sequence with duplicates
template <class T> template <typename U, template<typename> class W>
long SWMedianProbOdd<T>::print_prob(
    const long nNoDupl,
    const W<U>& sequence, 
    const long windowSize,
    ostream& outFile) const {
    
    //int precision = outFile.precision();
    //outFile.precision(3);
    
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
} 



///writes the sliding window median probabilities of a sequence with duplicates
template <class T> template <typename U, template<typename> class W>
long SWMedianProbOdd<T>::write_prob(    
    const long nNoDupl,
    const W<U>& sequence, 
    const long windowSize,
    ostream& outStream) const {

    long count(0);
    U oldItem(sequence.at(0));
    T prob(get(windowSize, 0));
    outStream <<"# "<< nNoDupl <<" windowSize="<< windowSize << endl<<flush;
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
    return ++count;
}
#ifdef _LOCAL
///writes the sliding window median probabilities of a sequence with duplicates
template <class T> template <typename U, class K, template<typename,class> class W>
long SWMedianProbOdd<T>::write_prob(    
    const long nNoDupl,
    const W<U,K>& sequence, 
    const long windowSize,
    ostream& outStream) const {

    long count(0);
    U oldItem(sequence.at(0));
    T prob(get(windowSize, 0));
    outStream <<"# "<< nNoDupl <<" windowSize="<< windowSize << endl<<flush;
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
    return ++count;
}
#endif
	
template<class T> 
bool SWMedianProbOdd<T>::read_parameters(istream& inStream){  
    SlideWindowParameters newDef;
    if (!newDef.read(inStream)) return false;
    if (this->def!=newDef){
    	Ceil_N_2=ceil_div_2(newDef.size()); 
    	realloc(newDef);
    } 
    return true;
}

//****************************************************************************
/** \brief Computes the pdf of the moving medians in a sequence of size N 
 *  	    at window size 2k+1.
 *
 *  \see{SWProbMedian}
 */
template <class T>
void SWMedianProbOdd<T>::median_probability(const long k, const T logFirst){
    long wIndex(window_index(2*k+1));
    
    T logValue(logFirst);
    v.at(wIndex).at(0) = exp(logFirst);
    
    for (long i=1, element=k+1; i<storage_size_k(k); ++i, ++element){
   	logValue += log_next_element_factor(element-1, k);
     	v.at(wIndex).at(i) = exp(logValue);
    }

}
	
//****************************************************************************
/** \brief Computes the sliding window probability of the median of 
 *         a sequence of size N.
 *
 *  \see{SWProbMedian}
 */
template <class T>
void SWMedianProbOdd<T>::median_probability(void){
    for (long k=wMin()/2; k<=wMax()/2; k+=wStep()/2)
    	median_probability(k, log_first_element_prob(k));
}



//****************************************************************************
/// Checks if probability sum of all window sizes is acceptable. 
template <class T>
bool SWMedianProbOdd<T>::check_sum(const T MaxErr, const bool print) const {
    for (long w=wMin(); w<=wMax(); w+=wStep())
    	if (!check_sum(MaxErr, w, print))
	    return false;
    return true;
}

/// Checks if probability sum of windowSize is acceptable (~1). 
template <class T>
bool SWMedianProbOdd<T>::check_sum(const T MaxErr, const long windowSize,
    const bool print) const {
    T error = fabs(integrate(windowSize)-1);
    if (error > MaxErr){
    	if (print) cout << "Error(" << windowSize << ")= " << error << endl;
	return false;
    }
    return true;
}	

/// Computes the probability sum of window size. 
template <class T>
T SWMedianProbOdd<T>::integrate(const long windowSize, const bool print) const {
    long k(windowSize/2);
    long wIndex=window_index(windowSize);
    T sum(0);
    for (long j=0; j<N(); ++j)
    	sum += get_raw(k,j,wIndex);
    if (print)
    	cout << "Sum(" << windowSize << ")= " << sum << endl;
    return sum;
}

template <class T>
void SWMedianProbOdd<T>::print_integrate(void) const {
    for (long w=wMin(); w<=wMax(); w+=wStep())
    	integrate(w, true);
}


///renormalizes probability of windowSize 
template <class T>
void SWMedianProbOdd<T>::renormalize(const T MaxErr, const long windowSize){

    T sum(integrate(windowSize));
    if (fabs(sum-1.0)>MaxErr){
    	long wIndex=window_index(windowSize);
    	for (long j=0; j<storage_size(windowSize); ++j)
            v.at(wIndex).at(j)/=sum;
    }
}

///renormalizes probability of all windowSizes 
template <class T>
void SWMedianProbOdd<T>::renormalize(const T MaxErr){
    for (long w=wMin(); w<=wMax(); w+=wStep())
    	renormalize(MaxErr,w);
}

///Returns pdf of WindowSize
template <class T> 
vector<T> SWMedianProbOdd<T>::window_content(const long WindowSize) const {
    vector<T> aux;
    for (long i=0; i<N(); i++){
    	aux.push_back(get(WindowSize,i));
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
inline T SWMedianProbOdd<T>::first_element_prob(const long k) const {
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
 *  \see: SWMedianProbOdd<N>::first_element_prob
 *
*/
template <class T>
inline T SWMedianProbOdd<T>::log_first_element_prob(const long k) const {
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
 *  \see{SWMedianProbOdd<N>::first_element_prob}
 *
*/
template <class T>
inline T SWMedianProbOdd<T>::first_element_prob_factor(const long k) const {
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
inline T SWMedianProbOdd<T>::next_element_factor(const long element, 
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
 *   \see: SWMedianProbOdd<N>::next_element_factor
 *
*/
template <class T>
inline T SWMedianProbOdd<T>::log_next_element_factor(const long element,
    const long k) const {
    T j1(element+1);
    T aux(N()-element-1);
    return log( j1*(aux-k) / (aux*(j1-k)) );
}    




#endif //SWMEDIANLogPROB_
