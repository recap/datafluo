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

#define HERE(NUM) cout << "HERE " << #NUM << endl
#define HERE2(VAR) cout<< "HERE " << #VAR  << ": " << VAR << endl


using namespace std;

class HistogramDifference : public IModule {

  private:
	int bin_size;
	bool file_exists(const char* file);
	vector<string> processed_files;
	//int hist_diff(string file1, string file2);


  public:
    HistogramDifference(){ 
		LOG_PTAG(Info) << "Setting LD_LIBRARY_PATH to  LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH";
		putenv("LD_LIBRARY_PATH=/home/rcushing/local/octave-3.2.4/lib/octave-3.2.4:$LD_LIBRARY_PATH");
		//INIT_SYNCHRONIZE();
		INIT_PORTS();

		MAP_RX_PORT(1,parameters_in);
		MAP_RX_PORT(2,c1c2c3_file);
		MAP_RX_PORT(3,I1I2I3_file);
		MAP_TX_PORT(1,results);

//		rx_ports[1] = new MessageQueue("parameters_in");
//		rx_ports[2] = new MessageQueue("c1c2c3_file");
//		rx_ports[3] = new MessageQueue("I1I2I3_file");
//		tx_ports[1] = new MessageQueue("results");
	    };
    
    virtual ~HistogramDifference() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();

};


void HistogramDifference::stop(){}

void HistogramDifference::init(vector<string>* rParam){
	//HERE(INIT);
}

void HistogramDifference::start()  {
	int port2 = 1;
	int port3 = 1;
	int pass = 0;
	//int counter = 0;

    TimeLag timing;
    timing.start("HistogramDifference");    
  	const char* argvv[] ={"","--silent","-q"};
      octave_main(2,(char **)argvv,true);
      octave_value_list functionArguments;
	//MessageQueue::Message* im1 = rx_ports[1]->Read();
while(1){
	MessageQueue::Message* im1 = READ_PORT(1);
	pass++;
	if(im1 == NULL)
		goto END;

	char* sp = (char*) im1->mpData;
	sp[im1->mDataLength] = '\0';
	bin_size = strtol((const char*)sp,NULL,10);
	LOG_PTAG(Debug) << "Bin size set to: " << bin_size << endl;
	//SET_RX_PORT_STATE(1,1);
	//SIGNAL_RX_PORT(1);
	//rx_ports[1]->state = 1;

	string* g_c1c2c3;
	string* g_i1i2i3;
if(pass > 1){

	vector<string>::iterator itr;
	for(itr = processed_files.begin(); itr < processed_files.end(); ++itr){

		string c1c2c3_file_str = (string)(*itr);

		size_t found = c1c2c3_file_str.find("_c1c2c3");
		string orig_file_str(c1c2c3_file_str.substr(0,found)+".png");
    	string i1i2i3_file_str(orig_file_str);
    	i1i2i3_file_str.insert(orig_file_str.length()-4,"_I1I2I3");
		if( (file_exists(c1c2c3_file_str.c_str()) == true) && (file_exists(i1i2i3_file_str.c_str()) == true) ){

	 		functionArguments(0) = c1c2c3_file_str.c_str();
    		functionArguments(1) = i1i2i3_file_str.c_str();
    		functionArguments(2) = bin_size;
			int lc = 0;
			octave_value_list result;
			while(lc < 5)
			{
				try{

					stringstream cmd1;
    				result = feval("HistogramDifference",functionArguments,1);
					lc = 5;
				}catch(char* str)
				{
					LOG_PTAG(Error) << str << endl;
					sleep(1);
					lc++;		
				}
			}//while

    		stringstream msg;
			size_t found = c1c2c3_file_str.find_last_of('/');
			string c1c2c3_file = c1c2c3_file_str.substr(found+1, c1c2c3_file_str.length());
			found = i1i2i3_file_str.find_last_of('/');
			string i1i2i3_file = i1i2i3_file_str.substr(found+1, i1i2i3_file_str.length());
    		msg << "Raw://" << c1c2c3_file << '\t' << i1i2i3_file << '\t' << result(0).int_value() << '\t' << bin_size << '\0' << '\n' << flush;

    		string message(msg.str() + '\0');

			LOG_PTAG(Info) << "RESULTS: " << message;

    		MessageQueue::Message* om1 = new MessageQueue::Message();
			om1->mMessageId = "NONE";
    		om1->mDataLength = message.size();
    		om1->mpData = (void*)malloc(om1->mDataLength);
    		memcpy(om1->mpData,message.c_str(),om1->mDataLength);

			WRITE_PORT(1,om1);

		}//if
	}//for
}//pass > 1
else{
	while(1){
	//sleep(10);
	//counter++;
	//if(counter > 10)
	//{
	//	counter = 0;
		//do_octave_atexit();	
        //octave_main(2,(char **)argvv,true);
        //octave_value_list functionArguments;
		
	//}
  	//const char* argvv[] ={"","--silent"};
      //  octave_main(2,(char **)argvv,true);
      //  octave_value_list functionArguments;
	MessageQueue::Message* im2 = NULL;
	MessageQueue::Message* im3 = NULL;

	if(port2 == 1){
	//HERE(1.0);
	//MessageQueue::Message* im2 = rx_ports[2]->Read();
	//MessageQueue::Message* im2 = READ_PORT(2);
	im2 = READ_PORT(2);
	if(port3 == 1){
		//MessageQueue::Message* im3 = READ_PORT(3);
		im3 = READ_PORT(3);
		if(im3 == NULL){
			SET_RX_PORT_STATE(3,1);
			port3 = 0;
		}
	}

	if(im2 == NULL){
		SET_RX_PORT_STATE(2,1);
		//rx_ports[2]->state = 1;
		port2 = 0;
	}
	else{
	char* sp = (char*)im2->mpData;
	sp[im2->mDataLength] = '\0';
	string c1c2c3_file_str((char*)sp);
	//HERE(1.1);
	size_t found = c1c2c3_file_str.find("_c1c2c3");
	string orig_file_str(c1c2c3_file_str.substr(0,found)+".png");
	string i1i2i3_file_str(orig_file_str);
	i1i2i3_file_str.insert(orig_file_str.length()-4,"_I1I2I3");
	g_c1c2c3 = &c1c2c3_file_str;
	g_i1i2i3 = &i1i2i3_file_str;
	//HERE(1.2);
	//HERE2(orig_file_str);
	//HERE2(c1c2c3_file_str);
	//HERE2(i1i2i3_file_str);
	vector<string>::iterator f1;
	vector<string>::iterator f2;
	//sleep(5);
	
	f1 = find(processed_files.begin(), processed_files.end(), c1c2c3_file_str);
	//f2 = find(processed_files.begin(), processed_files.end(), i1i2i3_file_str);
	if(f1 == processed_files.end())
	if( (file_exists(c1c2c3_file_str.c_str()) == true) && (file_exists(i1i2i3_file_str.c_str()) == true) ) {
	{	
	//HERE(1.3);

	 functionArguments(0) = c1c2c3_file_str.c_str();
    functionArguments(1) = i1i2i3_file_str.c_str();
    functionArguments(2) = bin_size;
	int lc = 0;
	octave_value_list result;
	while(lc < 5)
	{
		try{

			stringstream cmd1;
			cmd1 << "md5sum " << c1c2c3_file_str << " >> dump_md5";
			stringstream cmd2;
			cmd2 << "md5sum " << i1i2i3_file_str << " >> dump_md5";
			system(cmd1.str().c_str());
			system(cmd2.str().c_str());
    		result = feval("HistogramDifference",functionArguments,1);
			lc = 5;
	//		if(result(0).int_value() != 0)
	//			lc = 5;
	//		else
	//		{
	//			sleep(1);
	//			lc++;
	//			do_octave_atexit();	
    //  		octave_main(2,(char **)argvv,true);
    //			octave_value_list functionArguments;
	//		}

		}catch(char* str)
		{
			LOG_PTAG(Error) << str << endl;
			sleep(1);
			lc++;		
		}
	}

    stringstream msg;
	size_t found = c1c2c3_file_str.find_last_of('/');
	string c1c2c3_file = c1c2c3_file_str.substr(found+1, c1c2c3_file_str.length());
	found = i1i2i3_file_str.find_last_of('/');
	string i1i2i3_file = i1i2i3_file_str.substr(found+1, i1i2i3_file_str.length());
    msg << "Raw://" << c1c2c3_file << '\t' << i1i2i3_file << '\t' << result(0).int_value() << '\t' << bin_size << '\0' << '\n' << flush;

    string message(msg.str() + '\0');

	LOG_PTAG(Info) << "RESULTS: " << message;

    MessageQueue::Message* om1 = new MessageQueue::Message();
	om1->mMessageId = "NONE";
    om1->mDataLength = message.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,message.c_str(),om1->mDataLength);
	//HERE(1.4);

	processed_files.push_back(c1c2c3_file_str);
	//processed_files.push_back(i1i2i3_file_str);

	//tx_ports[1]->Write(om1);
	WRITE_PORT(1,om1);


	}//if
	}//else
	}//if port
	
	if(port3 == 1){
	//HERE(2.0);
	//MessageQueue::Message* im3 = rx_ports[3]->Read();
	//MessageQueue::Message* im3 = READ_PORT(3);
	//if(im3 == NULL){
	//	SET_RX_PORT_STATE(3,1);
	//	rx_ports[3]->state = 1;
	//	port3 = 0;
	//}
	//else{
	char* sp3= (char*)im3->mpData;
	sp3[im3->mDataLength] = '\0';
	//HERE(2.1);
	string i1i2i3_file_str3((char*)sp3);
	size_t found = i1i2i3_file_str3.find("_I1I2I3");
	string orig_file_str3(i1i2i3_file_str3.substr(0,found)+".png");
	string c1c2c3_file_str3(orig_file_str3);
	c1c2c3_file_str3.insert(orig_file_str3.length()-4,"_c1c2c3");
	//HERE(2.2);
	
	//HERE2(c1c2c3_file_str3);
	//HERE2(i1i2i3_file_str3);

	vector<string>::iterator f1;
	vector<string>::iterator f2;

	f1 = find(processed_files.begin(), processed_files.end(), c1c2c3_file_str3);
	//f2 = find(processed_files.begin(), processed_files.end(), i1i2i3_file_str3);
	if(f1 == processed_files.end())	
	if( (file_exists(c1c2c3_file_str3.c_str()) == true) && (file_exists(i1i2i3_file_str3.c_str()) == true) ){
	//HERE(2.3);
	functionArguments(0) = c1c2c3_file_str3.c_str();
	functionArguments(1) = i1i2i3_file_str3.c_str();
	functionArguments(2) = bin_size; 
	int lc = 0;
	octave_value_list result;
	while(lc < 5)
	{
		try{
			stringstream cmd1;
			cmd1 << "md5sum " << c1c2c3_file_str3 << " >> dump_md5";
			stringstream cmd2;
			cmd2 << "md5sum " << i1i2i3_file_str3 << " >> dump_md5";
			system(cmd1.str().c_str());
			system(cmd2.str().c_str());

    		result = feval("HistogramDifference",functionArguments,1);
			lc = 5;
		//	if(result(0).int_value() != 0)
		//		lc = 5;
		//	else
		//	{
		//		sleep(1);
		//		lc++;
		//		do_octave_atexit();	
        //		octave_main(2,(char **)argvv,true);
        //		octave_value_list functionArguments;
		//	}

		}catch(char* str)
		{
			LOG_PTAG(Error) << str << endl;
			sleep(1);
			lc++;		
		}
	}


	//const octave_value_list result = feval("HistogramDifference",functionArguments,1);
	
	stringstream msg;
	size_t found = c1c2c3_file_str3.find_last_of('/');
	string c1c2c3_file = c1c2c3_file_str3.substr(found+1, c1c2c3_file_str3.length());
	found = i1i2i3_file_str3.find_last_of('/');
	string i1i2i3_file = i1i2i3_file_str3.substr(found+1, i1i2i3_file_str3.length());
	msg << "Raw://" << c1c2c3_file << '\t' << i1i2i3_file << '\t' << result(0).int_value() << '\t' << bin_size << '\0' << flush;
	
	string message = msg.str();

	LOG_PTAG(Info) << "RESULTS: " << message;

	MessageQueue::Message* om2 = new MessageQueue::Message();
	om2->mMessageId = "NONE";
	om2->mDataLength = message.size();
	om2->mpData = (void*)malloc(om2->mDataLength);
	memcpy(om2->mpData,message.c_str(),om2->mDataLength);
	//HERE(2.4);

	processed_files.push_back(c1c2c3_file_str3);
	//processed_files.push_back(i1i2i3_file_str3);

	//tx_ports[1]->Write(om2);
	WRITE_PORT(1,om2);
	
	}//if
		
	}//else
	}//if port

//	sleep(3);

	//SYNCHRONIZE();

	/*pthread_mutex_lock(&condMutex);

	if(first_pass == 1){ 
		first_pass = 0;	
    	pthread_cond_signal(&condSync);
	}
    pthread_cond_signal(&condSync);
    pthread_cond_signal(&condSync);
    pthread_mutex_unlock(&condMutex);*/
	//system("rm 252_l_rgb_I1I2I3.png");
	//system("rm 252_l_rgb_c1c2c3.png");


	SIGNAL_RX_PORT(2);
	SIGNAL_RX_PORT(3);

	if ( (port2 == 0) && (port3 == 0) )
		break;

	//HERE(3.1);
	//sem_post(&mSync);
	//sem_post(&mSync);
}//whilea
}//else
	SIGNAL_RX_PORT(1);
}//outer while
	//HERE(3.2);a
END:
	//tx_ports[1]->Write(NULL);
	SET_RX_PORT_STATE(1,1);
	SET_RX_PORT_STATE(2,1);
	SET_RX_PORT_STATE(3,1);
	SIGNAL_RX_PORT(1);
	SIGNAL_RX_PORT(2);
	SIGNAL_RX_PORT(3);
	WRITE_PORT(1,NULL);

	do_octave_atexit();	
    
    timing.finish("HistogramDifference");

};    
bool HistogramDifference::file_exists(const char* filename){
	if(FILE* file = fopen(filename,"r")){
            fclose(file);
            return true;
     }
     return false;
}
 

REGISTER_MODULE(HistogramDifference);
//end_of_file
