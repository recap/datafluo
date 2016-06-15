#ifndef L_TOKENSTRING_H_
#define L_TOKENSTRING_H_

#include <string>
#include <vector>

using namespace std;


class TokenString{
public:
	TokenString();
	TokenString(string str,string delimiter);
	void PrintTokens();
	~TokenString();	
	vector<string> tokens;
};
#endif
