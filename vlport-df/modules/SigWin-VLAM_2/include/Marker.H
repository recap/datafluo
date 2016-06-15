//****************************************************************************
/** \file Marker.H 
 *  \brief A class to navigate over unmarked items of an "array"
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since September 28, 2005.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: August 08, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/

#ifndef MARKER_
#define MARKER_
#include "LogManager.H"

/// A class to navigate over unmarked items of an "array"
class Marker
{
  private:
    int N; //> Number of items.
    bool *marked; //> Marked items.
    int pointer;    //> Position of interst: n-th unmarked item.
    int index;      //> Index of position of interest.
    int nMarked;
    
  public:
    Marker(int n, int position=0) : N(n), pointer(position), index(position){
    	marked = new bool[N+1];//The last element is a controller
    	for (int i=0; i<N+1; i++)
	    marked[i]=false;
	nMarked = 0;
    }
    Marker(Marker& m) : N(m.pointer), pointer(m.pointer), index(m.index),   
    	nMarked(m.nMarked){
    	marked = new bool[N+1];
    	for (int i=0; i<N+1; i++)
	    marked[i] = m.marked[i];
    }
    ~Marker(){delete[] marked;}
	
	
    //functions	
    bool isMarked(const int i) const {return marked[i];}
    int marked_size(void) const {return nMarked;}
    int position(void) const {return pointer;}
    int position_index(void) const {return index;}
    
    int prev_position_index(void) const {
    	int aux = index-1;
	while (marked[aux])
	    --aux;
	return aux;
    }
    void mark(const int i){
	if (!marked[i]){
    	    marked[i]=true;
	    nMarked++; 
	    if (i<=index)
	    	increment_index();
    	} 
	//print_resume();
    };
    void unmark(const int i){
    	if (marked[i]){
	    marked[i]=false; 
	    nMarked--;
	    if (i<index) 
	    	decrement_index();
	}
 	//print_resume();
   };

    //replaces isMarked (enters the window) by notMarked (lefts the list)
    void replace(const int isMarked, const int notMarked){
	
	if (marked[isMarked]){
	    marked[isMarked]=false;
	    nMarked--;
	}
	if (!marked[notMarked]){
	    marked[notMarked]=true;
	    nMarked++;
	}
    	if ((notMarked <= index)&&(isMarked>index))
	    increment_index();
	else if ((notMarked>=index)&&(isMarked<index))
	    decrement_index();
    }

    ///Increments position of interest by <n>.
    /** Does not check bonds. */
    void increment_position(const int n=1){	
     	increment_index(n);
	pointer+=n;
    }
    ///Decrements position of interst by <n>.
    /** Does not check bonds. */
    void decrement_position(const int n=1){
      	decrement_index(n);
	pointer-=n;		
    }

    ///Sets position of interest.
    /** Does not check bonds. */
    void set_position(int n){
    	int diff = n-pointer;
    	if (diff>0)
	    increment_position(diff);
	else if (diff<0)
	    decrement_position(-diff);
    }
    void print(void) const {
    	cout<<"pos["<< pointer <<" ]="<< index <<" nMarked="<< nMarked << endl;
	for (int i=0; i<N; i++)
	    cout << marked[i] << " ";
	cout << endl;
	
    }
    void print_resume(void) const{
     	cout<<"pos["<< pointer <<" ]="<< index <<" nMarked="<< nMarked << endl;
    }
  private:
    ///Increments index by <n>.
    /** Assumes marked[0]=0;*/
    void increment_index(const int n=1){	
     	for (int i=0; i<n; i++)
	    do ++index;
	    while (marked[index]);
	    if (index>=N){
    	    	LOG_PTAG(Error)<<"index="<<index<<" >= N ="<<N<<"."
		    <<flush;
	    }
    }
    ///Decrements index by <n>.
    /** Check bonds partially. */
    void decrement_index(const int n=1){
      	for (int i=0; i<n; i++)
	    do --index;
	    while (marked[index]);
	if (index<0)
    	    LOG_PTAG(Error)<<"Index smaller than 0. " << flush;
    }

};



#endif //MARKER_
