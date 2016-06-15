#include<stdio.h>
//#include<unistd.h>

int main(int argc, char* argv[])
{
	int i = 0;
	for(i = 0;i<32;i++)
		printf("%d\n",i);
//	execl("legacyapp","legacyapp","-i","alltissues.txt",">","dump",NULL);
//	execl("sh","legacyapp","-i","alltissues.txt",">","dump",NULL);
	return 0;
}
