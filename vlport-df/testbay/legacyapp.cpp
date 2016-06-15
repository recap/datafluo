#include<stdio.h>
#include<stdlib.h>
#include<malloc.h>
#include<string.h>
#include<fstream>
#include<iostream>

using namespace std;

int fd;
char* filein;
int parse_args(int argc, char* argv[]);
int main(int argc, char* argv[])
{
		parse_args(argc,argv);

		ifstream file(filein);
		string line;
		while(!file.eof()){
				getline(file,line);
				cout << line << endl;
		}
		file.close();
}
int parse_args(int argc, char *argv[])
{
    int largc = 0;

    char *p;
    for(largc=1;largc < argc;largc++)
    {
        p = argv[largc];
        if(!strcmp(p,"-i"))
        {
            filein = argv[largc+1];

        }//if
	}
}//parse args
