#include <iostream>
#include <string>
#include <stdio.h>
#include <stdlib.h>

#define HOME "/home/reggie/VLPORT2"
using namespace std;

static string get_message_id()
{
	static int i = 8;

	i++;
	
	char si[20];
	sprintf(si,"file_%d.rst",i);

	string sk(si);
	string s(HOME + sk);

	return s;
}

int main(){

	

	string s=get_message_id();
	
	if(s == "")
		cout << "s is empty" << endl;
	else
		{
		cout << s << endl;
	s = get_message_id();	
		cout << s << endl;
	s = get_message_id();	
		cout << s << endl;
}

return 0;
}
