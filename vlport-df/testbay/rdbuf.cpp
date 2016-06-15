#include <iostream>
#include <sstream>
using namespace std;

int main () {
  stringbuf *pbuf;
  void** p;
  void  *l;
  stringstream ss;
  int b[100];
  void *o;
	
	for(int i = 0; i < 100; i++)
		b[i] = i;

	o = (void*)&b;

	stringstream sb;
	//char mybuffer [512];
  //fstream filestr;
  //filestr.rdbuf()->pubsetbuf(mybuffer,512);
	sb.rdbuf()->pubsetbuf((char*)o,sizeof(b));
	//sb.rdbuf()->pubseekpos(sizeof(b));
	sb.rdbuf()->pubseekoff(0,ios_base::end);
	int q = 0;
//	sb << q;
//	q = 0;
//	sb >> q;
	cout << q << endl;

	/*

ss << "HELLO" << "SDAFASDFASDASDASFDVCSDSSSDF" << "asddsafasdfdsasa" << "ARMY";
  //p = reinterpret_cast<void **>(&(ss.str()));
  p = (void **)(&(ss.str()));
l = *p;
//	l = **p;
 // pbuf=ss.rdbuf();
 // p = (void* ) &pbuf->str();
  //pbuf->sputn ("Sample string",13);
  cout << (char *)*p <<endl;
  cout << (char *)l <<endl;
*/
  return 0;
}
