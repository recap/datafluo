#ifndef _TSTRING_H_
#define _TSTRING_H_
#include<string>
#include<vector>

using namespace std;

class Tstring : public string, String
{
public:
	Tstring():String(){};
	vector<string>* split(string delimiter);
};
#endif
