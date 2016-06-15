#include <boost/ptr_container/ptr_list.hpp>
#include <iostream>

using namespace std;

class ABC
{
public:
   int i;
   float j;
};

main()
{
   boost::ptr_list<ABC> intList;
   boost::ptr_list<ABC>::iterator iterIntList;

   ABC *a= new ABC;
   ABC *b= new ABC;
   ABC *c= new ABC;

   a->i = 1;
   b->i = 2;
   c->i = 3;

   intList.push_back(a);
   intList.push_back(b);
   intList.push_back(c);

   for (iterIntList = intList.begin();
        iterIntList != intList.end();
        iterIntList++)
   {
      cout << iterIntList->i << endl;
   }

}
