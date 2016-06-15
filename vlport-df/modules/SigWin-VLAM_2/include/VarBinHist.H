//****************************************************************************
/** \file VarBinHist.H 
 *  \brief A class handle variable size bin histograms. 
 *  
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since  January 06, 2006.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Jan 2008: Changed storage type from array to vector.
 *  Jan 2008: Modified to conform with new GNU C++.
 *  Created:  September 19, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/ 
#ifndef VARBINHIST_
#define VARBINHIST_ 

//#include <fstream>
#include <iostream>
#include <algorithm>
#include <vector>
#include "LogManager.H"
//#include "Serie.H"
#include "DataIO.H"
#include "ArrayIO.H"
#include "VecIO.H"


//****************************************************************************
// class declarations 
//****************************************************************************
template <class T, class U>  class VarBinHist;

template <class T, class U> 
void print(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    ostream& outFile=cout, const bool check=true);
template <class T, class U> 
void print(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    const U BinSize, ostream& outFile=cout, const bool check=true);
template <class T, class U> 
void print_sum(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    ostream& outFile=cout, const bool check=true);

template <class T, //> Data type 
    	  class U> //> Bin Type
class VarBinHist {
  private:
    //T min;   //> Minimum value in series
    //T max;   //> Maximum value in series
    //T step;  //> Space between values
    int number; //> Number of values in series
    
    const bool KeepCopy;
    vector<T> value;//> Series values 
    typename vector<T>::const_iterator value_it;
    //const int ID; //> Identifies Series values

    U *freq; // number of entries (or density) in bin 
    U sum;    // total number of entries  (or sum of density)
      
  public:
    //constructors
    VarBinHist<T,U>(const vector<T>& vec,  const bool save=false);

    //destructor
    ~VarBinHist<T,U>(){delete[] freq;}

    //functions
    double get_freq(int i) const {return freq[i];}
    double get_sum(void) const {return sum;}
    void reset(void){sum=0; for (int i=0; i<number+1; i++) freq[i]=0;}
    vector<U> frequencies(void) const {
    	vector<U> outVec;
	outVec.reserve(number);
	for (int i=0; i<number; ++i)
	    outVec.push_back(freq[i]);
    	return outVec;
    } 

    void set_from_sample(vector<T>& sample);
    void set_from_frequency(vector<T>& val, vector<U>& frequency);
    
    ///Normalizes the histogram. (Class U must be floating.)
    void normalize(void); 

    bool check_values(const typename vector<T>::const_iterator test_it) const;
    
    void print(ostream& outFile=cout, bool rows=true) const;
    
    void write_parameters(ostream& outStream) const;
    void write_values(ostream& outStream) const {
    	write_array(outStream, value_it, number);
    }
    void write_freq(ostream& outStream) const {
    	write_array(outStream, freq, number);
    }
    void write(ostream& out) const {
    	write_parameters(out), write_values(out); write_freq(out);
    }

    void read_parameters(istream& inStream);
    void read_values(istream& inStream);
    void read_freq(istream& inStream);
    //void read(istream& in) 
    //{read_parameters(in); read_values(in); read_freq(in);}
    
    //void read(istream& inStream, const vec_citer start);
    
    //???
    //friend void print<T,U>(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    //	ostream& outFile, const bool check); 
    //friend void print<T,U>(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    //	const U BinSize, ostream& outFile, const bool check);
    //???
    friend void print_sum<T,U>(const VarBinHist<T,U>& H1, 
    	const VarBinHist<T,U>& H2, ostream& outFile, const bool check); 
};

//*****************************************************************************	
template <class T, class U> 
VarBinHist<T,U>::VarBinHist(const vector<T>& vec, const bool save) :
    //min(vec.at(0)),
    //max(vec.at(vec.size()-1)),
    number(vec.size()),
    KeepCopy(save){
    
    if (KeepCopy){
	value.assign(vec.begin(), vec.end());
	value_it = value.begin();
    }
    else
    	value_it = vec.begin();
	
    freq = new U[number+1];
	    
    reset();
}

///Computes the frequency of each value of the sample.
//Sets the histogram from a sample of values.
//####### seems to sort the input vector sample ############################## 
template <class T, class U> 
void VarBinHist<T,U>::set_from_sample(vector<T>& sample) {
    sort(sample.begin(),sample.end());
    reset();
    
    int index=0;
    for (unsigned i=0; i<sample.size(); ++i){
    	while (value_it[index]<sample.at(i)){
	    index++;
	    if (index>=number){
	    	LOG_PTAG(Fatal) << "Index (" << index << ") >= number (" 
		    << number << ", max value = " <<  value_it[number-1] 
		    << ", sample = " << sample.at(i) << ", sample.size: " << sample.size() << endl<< flush;
	    	exit(10);
	    } 
	}
	if (value_it[index] != sample.at(i)){
	    cout<<"Error VarBinHist<T,U>::set_from_sample(vector<T>& sample): ";
	    cout << value_it[index] << " <> " << sample.at(i) << endl;
	    ++freq[number];
	}
	else{
    	    ++freq[index]; 
	    sum++;
	}
    }
}



template <class T, class U> 
void VarBinHist<T,U>::set_from_frequency(vector<T>& val, vector<U>& frequency){
    reset();
    int index=0;
    for (int i=0; i<val.size(); ++i){
    	while (value_it[index]<val.at(i))
	    index++;
	if (value_it[index] != val.at(i)){
	    cout << "Error VarBinHist<T,U>::set_from_frequency()\n";
	    freq[number]+=frequency.at(i);
	}
	else{
    	    freq[index]+=frequency.at(i); 
	    sum+=frequency.at(i);
	}
    }
}

template <class T, class U> 
void VarBinHist<T,U>::normalize(void){
    if (sum==0){
    	LOG_PTAG(Fatal)<<"Cannot normalize sum=0!"<<flush;
	print(cerr);
	exit(1);
    }
    for (int i=0; i<number; ++i){
    	freq[i] /= sum;
    }
}

template <class T, class U> 
bool VarBinHist<T,U>::check_values(const typename vector<T>::const_iterator
    test_it) const {
    if (test_it == value_it)
    	return true;
    for (int i=0; i<number; i++){
    	if (test_it[i] != value_it[i]){
	    return false;
	    cout << "VarHistBin check_value failed at " << i << ".\n";
	}
    }
    return true;
} 

///Prints histogram
template <class T, class U> 
void VarBinHist<T,U>::print(ostream& outFile, bool rows) const {
    outFile << "#Value \t Freq" << endl;
    
    for (int i=0; i<number; ++i)
    	outFile << value_it[i] << "\t" << freq[i] << "\n";
    outFile << "#sum= " << sum << ",  errors= " << freq[number] << "\n\n";
    outFile.flush();
	
}

template <class T, class U> 
void VarBinHist<T,U>::write_parameters(ostream& outStream) const {
    LOG_PTAG(Debug) << "Writing parameters...\n" << flush; 
    write_struct(outStream, number);
    outStream.flush();
    write_struct(outStream, KeepCopy);
    outStream.flush();
}




//*****************************************************************************
template <class T, class U> 
void print(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    ostream& outFile, const bool check){
    
    bool same = H1.check_values(H2.value_it);
    if (!same){
    	cout << "Values are not the same!\n";
	return;
    }
    
    outFile << "#Value\t H1 \t H2 \n";
    for (int i=0; i<H1.number; i++)
    	outFile << H1.value_it[i] << "\t" 
	        << H1.freq[i] << "\t" << H2.freq[i] << endl;
    outFile << "#sum= " << H1.sum << "\t" << H2.sum << endl;
    outFile << "#errors= " << H1.freq[H1.number] << "\t" 
                       << H2.freq[H2.number] <<"\n\n\n";
} 

template <class T, class U> 
void print(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    const U BinSize, ostream& outFile, const bool check){
	   
    bool same = H1.check_values(H2.value_it);
    if (!same){
    	cout << "Values are not the same!\n";
	return;
    }
    
    outFile << "#BinValue\t H1 \t H2 \n";
    U binStart(H1.value_it[0]);
    U binEnd(binStart+BinSize);
    U sum1(0);
    U sum2(0);
    for (int i=0; i<H1.number; i++){//########### check !!!! ##############
	if (H1.value_it[i] < binEnd){//accumulate
	    sum1+= H1.freq[i];
	    sum2+= H2.freq[i];
	}
	else{//print and reset
    	    while (H1.value_it[i] >= binEnd){
	    	outFile << binStart << "\t" << sum1 << "\t" << sum2 << endl;
	    	binStart = binEnd;
	    	binEnd += BinSize;
	    	sum1=0;
	    	sum2=0;
	    }
	}
    }	
    outFile << binStart << "\t" << sum1 << "\t" << sum2 << endl;
    
    outFile << "#sum= " << H1.sum << "\t" << H2.sum << endl;
    outFile << "#errors= " << H1.freq[H1.number] << "\t" 
                       << H2.freq[H2.number] <<"\n\n\n";
} 

template <class T, class U> 
void print_sum(const VarBinHist<T,U>& H1, const VarBinHist<T,U>& H2, 
    ostream& outFile, const bool check){
	       
    bool same = H1.check_values(H2.value_it);
    if (!same){
    	cout << "Values are not the same!\n";
	return;
    }
    
    U sum1(0);
    U sum2(0);
    outFile << "#Value\t sum(H1) \t sum(H2) \n";
    for (int i=0; i<H1.number; ++i){
    	sum1+= H1.freq[i];
	sum2+= H2.freq[i];
    	outFile << H1.value_it[i] << "\t"  << sum1 << "\t" << sum2 << endl;
    }
    outFile << "#errors= " << H1.freq[H1.number] << "\t" 
                       << H2.freq[H2.number] <<"\n\n\n";
} 

#endif //VarBinHist_
//end-of-file
