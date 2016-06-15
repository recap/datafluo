//****************************************************************************
/** \file Shape.H 
 *  \brief Implements simple shape structures.
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since March 31, 2006.(?)
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: October 31, 2005.
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef SHAPEH_
#define SHAPEH_

#include <iostream>
#include "LogManager.H"

enum Coord{x,y,z};

//*****************************************************************************
template <int N, class T> class Point; //T=numerical type; N=dimensionality

template <int M, int N, class T> class Shape; //M=number of vertices; 


template<int N, class T> ostream &operator<<(ostream &s, const Point<N,T> &a);
template<int N, class T> 
bool operator==(const Point<N,T> &x, const Point<N,T> &y);

//*****************************************************************************
// * Struct Point	                                                     
//*****************************************************************************
template <int N, class T>//T=numerical type;
class Point{//N=dimensionality of the point
  public:
    T coord[N];
    
    //constructors
    Point(){};
    Point(const T& X, const T& Y){set(X,Y);};
    Point(const T& X, const T& Y, const T& Z){set(X,Y,Z);};
    
    //functions
    void set(const T& X, const T& Y){coord[0]=X; coord[1]=Y;};
    void set(const T& X, const T& Y, const T& Z){
    	coord[0]=X; coord[1]=Y; coord[2]=Z;
    };
    T get_x(void) const {return coord[0];}
    T get_y(void) const {return coord[1];}
    T get_z(void) const {return coord[2];}
    
    void set_x(const T& X) {return coord[0]=X;}
    void set_y(const T& Y) {return coord[1]=Y;}
    void set_z(const T& Z) {return coord[2]=Z;}

    //operators
    Point<N,T> &operator+=(const Point<N,T> &a){
    	for (int i=0; i<N; ++i){
	    if (a.coord[i]!=0)
    	    	coord[i]+= a.coord[i];
	}
    	return *this;
    }
    
    Point<N,T> &operator+=(const T &a){
	if (a!=0){
    	    for (int i=0; i<N; ++i)
    	    	coord[i]+= a;
	}
    	return *this;
    }

    const Point<N,T> &operator+(const Point<N,T> &a){return Point(*this) += a;}
    const Point<N,T> &operator+(const T &a){return Point(*this) += a;}
    static void set_separator(const string& sep){separator=sep;}
    static void reset_separator(void){separator=" ";}
    friend ostream &operator<< <N,T>(ostream &s, const Point<N,T> &a);
    friend bool operator==<N,T>(const Point<N,T> &x,const Point<N,T> &y);
  
  private:
    static string separator;
};
template <int N, class T> string Point<N,T>::separator=" ";

template<int N, class T> 
inline ostream &operator<<(ostream &s, const Point<N,T> &a){
    for (int i=0; i<N-1; ++i)
    	s << a.coord[i] << a.separator;
     s << a.coord[N-1];
   return s;
};

template<int N, class T> 
inline bool operator==(const Point<N,T> &x,const Point<N,T> &y){
    for (int i=0; i<N; ++i)
    	if (x.coord[i] != y.coord[i]) return false;
    return true;
}

template<int N, class T> 
inline bool operator!=(const Point<N,T> &x, const Point<N,T> &y){
    if (!(x==y)) 
    	return true;
    else
    	return false;
}



//*****************************************************************************
// * Class Shape	                                                     
//*****************************************************************************
template <int M, int N, class T> //M=number of vertices; 
class Shape {//N=dimensionality (2-D or 3-D) of the shape; T=number type; 
  public:
    Point<N,T> vertex[M];

    //constructors
    Shape(){};
    Shape(const Shape<M,N,T>& s, const Point<N,T>& c){ (*this = s) + c; }
    
    //operators
    Shape<M,N,T> &operator+=(const Point<N,T> &a){
    	for (int i=0; i<M; ++i){
    	    vertex[i]+= a;
	}    
    	return *this;
    }

    const Shape<M,N,T> &operator+(const Point<N,T> &a){
    	return Shape(*this) += a;}

    //functions
    void gnuplot(ostream &s){
    	for (int j=0; j<M; ++j)
	    s << vertex[j] << endl;
	s << vertex[0] << endl;
    }
    
};

 
template<int M, int N, class T> 
inline ostream &operator<<(ostream &s, const Shape<M,N,T> &a){
    for (int i=0; i<M; ++i)
    	s << "(" << a.vertex[i] << ") ";
    return s;
};

template<int M, int N, class T> 
inline bool operator==(const Shape<M,N,T> &x, const Shape<M,N,T> &y){
    for (int i=0; i<M; ++i)
    	if (x.vertex[i] != y.vertex[i]) return false;
    return true;
}

template<int M, int N, class T> 
inline bool operator!=(const Shape<M,N,T> &x, const Shape<M,N,T> &y){
    if (!(x==y)) 
    	return true;
    else
    	return false;
}

#endif //SHAPEH_
//end-of-file
