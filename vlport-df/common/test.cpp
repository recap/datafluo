#include <iostream>
#include <vector>
#include <string>
#include "TokenString.h"

using namespace std;


int main()
{
	string s;
	s  = "My string:string2:string3";
	string t;
	t = ":";
	
	cout << s << endl;
	TokenString::TokenString *tok = new TokenString::TokenString(s,t);

	s = "";
	vector<string>::iterator itok;
	for(itok = tok->tokens.begin(); itok != tok->tokens.end(); ++itok)
		cout << (*itok) << endl;
//	s.split("deli");	
	return 0;
}
