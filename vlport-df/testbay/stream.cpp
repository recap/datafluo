#include<istream>
#include<ostream>
#include<iostream>
#include<fstream>
#include<sstream>
#include<malloc.h>
using namespace std;


int main()
{
	stringstream memoryStream;
 
int number = 10;
double dnubmer = 10.122;
string text = "alfa ";

memoryStream << text << number << ' ' << dnubmer ;
 
 
cout << memoryStream.str() << endl << "\nExtraction:\n";
 
//reset the values
number = 0;
dnubmer = 0;
text.clear();

//reassign
memoryStream >> text;
memoryStream >> number;
memoryStream >> dnubmer;
 
cout << number << endl << text << endl << dnubmer << endl;


	return 0;
}
