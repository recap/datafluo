#include <iostream>
#include "TokenString.h"

TokenString::TokenString(string str,string delimiter){
    // Skip delimiter at beginning.
    string::size_type lastPos = str.find_first_not_of(delimiter, 0);
    // Find first "non-delimiter".
    string::size_type pos     = str.find_first_of(delimiter, lastPos);

    while (string::npos != pos || string::npos != lastPos)
    {
        // Found a token, add it to the vector.
        tokens.push_back(str.substr(lastPos, pos - lastPos));
        // Skip delimiter.  Note the "not_of"
        lastPos = str.find_first_not_of(delimiter, pos);
        // Find next "non-delimiter"
        pos = str.find_first_of(delimiter, lastPos);
    }
}
void TokenString::PrintTokens()
{
	vector<string>::iterator it;
	for(it = tokens.begin(); it < tokens.end(); ++it)
		cout << (*it) << endl;
}
