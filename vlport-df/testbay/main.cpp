#include<string>
#include<utility>
#include<iostream>
#include<vector>
#include <string.h>
#include<stdio.h>
#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main()
{

  string line;
  ifstream myfile ("vlport2.conf");
  if (myfile.is_open())
  {
    while (! myfile.eof() )
    {
      getline (myfile,line);
      cout << line << endl;
    }
    myfile.close();
  }

  else cout << "Unable to open file"; 

	/*	char* sc = "my flimsy cahr*";
	string scs(sc);
	string cpy(scs);
	string s = "test string";
	scs.at(0) = 116;
	cout << scs << endl;
	cout << cpy << endl;
	int p = 10;
	p = p ^ 123;
	cout << "p: " << p << endl;

	for(int i =0;i<s.size();i++)
		s.at(i) = (s.at(i) ^ 123) + 32;
	for(int k =0; k<s.size();k++)
		cout << (int)s.at(k) << endl;
	
//	int l = s.at(0);
	cout << s << endl;*/

	/*char s[200] = "This is a test string!";
	for(vector<char>::iterator it = s; s != (s+strlen(s)); ++it)
	{
		cout << *it <<endl;
	}*/
return 0;
}
