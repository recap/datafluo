#include <string>
#include <fstream>
#include <sstream>
#include <string.h>
#include <stdio.h>
#include <iostream>

//octave
#include <octave/oct.h>
#include <octave/octave.h>
#include <octave/parse.h>
#include <octave/toplev.h>

using namespace std;

int main()  {

  	const char* argvv[] ={"","--silent"};
        octave_main(2,(char **)argvv,true);
        octave_value_list functionArguments;

	
	string c1c2c3_file_str("/home/rcushing/c1c2c3.png");
	string i1i2i3_file_str("/home/rcushing/i1i2i3.png");
	string bin_size("16");

	 functionArguments(0) = c1c2c3_file_str.c_str();
    functionArguments(1) = i1i2i3_file_str.c_str();
    functionArguments(2) = 16;// bin_size.c_str();

    const octave_value_list result = feval("HistogramDifference",functionArguments,0);

    cout << c1c2c3_file_str << '\t' << i1i2i3_file_str << '\t' << result(0).int_value() << endl;


	do_octave_atexit();	
    

}    
