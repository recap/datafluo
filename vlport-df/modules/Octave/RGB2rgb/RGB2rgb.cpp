#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>

//octave
#include <octave/oct.h>
#include <octave/octave.h>
#include <octave/parse.h>
#include <octave/toplev.h>

using namespace std;

class RGB2rgb : public IModule {


  public:
    RGB2rgb(){ 
		LOG_PTAG(Info) << "Setting LD_LIBRARY_PATH to  LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH";
		putenv("LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH");
//		INIT_SYNCHRONIZE();
		INIT_PORTS();
		MAP_RX_PORT(1,input_file);
		MAP_TX_PORT(1,norm_rgb);
		//rx_ports[1] = new MessageQueue("input_file");
		//tx_ports[1] = new MessageQueue("norm_rgb");
	    };
    
    virtual ~RGB2rgb() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();

};


void RGB2rgb::stop(){}

void RGB2rgb::init(vector<string>* rParam){}

void RGB2rgb::start()  {

    TimeLag timing;
    timing.start("RGB2rgb");    
  	const char* argvv[] ={"","--silent"};
        octave_main(2,(char **)argvv,true);
        octave_value_list functionArguments;

	while(1){
	//MessageQueue::Message* im1 = rx_ports[1]->Read();
	MessageQueue::Message* im1 = READ_PORT(1);
	 if(im1 == NULL)
            break;
                

    	Log::ptag(Info) << "Reading file from port"<<flush;     
        //Expecting an image filname
	char* sp = (char*) im1->mpData;
	sp[im1->mDataLength] = '\0';
        //string s((char*) im1->mpData);
        string s((char*)sp);
	LOG_PTAG(Info) << "File: " << s;  
	string newfile(s);
	newfile.insert(s.length()-4,"_rgb");
	//*inStream << s;
	//cout << inStream->str() << endl;
	//char c=inStream->peek();
	//cout << (int)c << endl;
	
   	functionArguments(0) = s.c_str();
	const octave_value_list result = feval("imread",functionArguments,1);
	functionArguments(0)= result(0);
	LOG_PTAG(Info) << "imread() ready"; 
	const octave_value_list result2 = feval("RGB2rgb",functionArguments,1);
	//functionArguments(0)= result2(0);
	//LOG_PTAG(Info) << "RGB2rgb ready"; 
	//const octave_value_list result3 = feval("rgb2I1I2I3",functionArguments,1);
	functionArguments(0)= result2(0);
	functionArguments(1)= newfile.c_str(); 
	//functionArguments(2)= "PNG";
	//LOG_PTAG(Info) << "rgb2I1I2I3 ready"; 
	feval("imwrite",functionArguments,1);
	LOG_PTAG(Info) << "imwrite() ready"; 
   

	MessageQueue::Message* om1 = new MessageQueue::Message();

	om1->mDataLength = newfile.size();
	om1->mpData = (void*)malloc(om1->mDataLength);
	memcpy(om1->mpData,newfile.c_str(),om1->mDataLength);


	//tx_ports[1]->Write(om1);
	WRITE_PORT(1,om1);

	//SYNCHRONIZE();
	SIGNAL_RX_PORT(1);
	

}//while

	WRITE_PORT(1,NULL);
	//tx_ports[1]->Write(NULL);
	do_octave_atexit();	
    timing.finish("RGB2rgb");
};    
    

REGISTER_MODULE(RGB2rgb);
//end_of_file
