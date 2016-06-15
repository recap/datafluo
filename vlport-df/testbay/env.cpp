#include <stdio.h>
#include <iostream>
#include <stdlib.h>

using namespace std;
void ttt();
string s;
int main(){

	
	char* env = getenv("VL_HOME");
	s.assign((char *)env);
	s.append("/");

	//cout << s << endl;
	ttt();
	
}
void ttt(){

	char b[1000];
	cout << s << endl;
}

