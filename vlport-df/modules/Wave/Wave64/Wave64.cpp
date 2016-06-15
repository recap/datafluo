#include <string>
#include <fstream>
#include <sstream>
#include "LogManager.H"
#include "TimeLag.H"
#include "CommonDefines.h"
#include "IModule.h"
#include <string.h>
#include <stdio.h>


using namespace std;

class Wave64 : public IModule {
  private:
	string dataurl;
	string current_dir;
	pid_t mypid;
	string working_dir;
	


  public:
    Wave64(){ 
		putenv("SPHOME=/home/rcushing/local/sepran/sepran0106");
		putenv("LD_LIBRARY_PATH=/home/rcushing/local/sepran/sepran0106/lib:$LD_LIBRARY_PATH");
		//char* pwd_env = getenv("PWD");
		//current_dir.assign((char*)pwd_env);
		 mypid = getpid();
		stringstream wdir_s;
		wdir_s << "/tmp/VL_Wave64." << mypid << flush;
		working_dir.assign(wdir_s.str());
		
		//pthread_cond_init (&condSync, NULL);
		//pthread_mutex_init(&condMutex, NULL);
		INIT_SYNCHRONIZE();

	//	printf("Pointer 1: 0x%08x\n", &condSync);	

		for(int i = 0; i < MAXPORTS; i++){
			rx_ports[i] = NULL;
			tx_ports[i] = NULL;
		}		
		rx_ports[1] = new MessageQueue("tasknumber");
		tx_ports[1] = new MessageQueue("result");
	    };
    
    virtual ~Wave64() throw() { }
	void init(vector<string>* rParam);
	void start();
	void stop();

};


void Wave64::stop(){}

void Wave64::init(vector<string>* rParam){
	//1-dataurl
	dataurl.assign(rParam->at(1));
	LOG_PTAG(Info) << "DATA_URL: " << dataurl << endl;
}

void Wave64::start()  {

	string tmp_dir(working_dir);

	char* pwd_env = NULL;
	size_t size;
	pwd_env = getcwd(pwd_env,size);
	current_dir.assign((char*)pwd_env);

	string url("http://staff.science.uva.nl/~adam/wave/DATA/");
	string dataurl(url);

	string bndcnd_dir(tmp_dir+"/bndcnd_files/");
	string mesh_dir(tmp_dir+"/mesh_files/");
	string flow_dir(tmp_dir+"/flow/");
	string problem_dir(tmp_dir+"/problem_files/");
	string result_dir(tmp_dir+"/sol_folders/");



    TimeLag timing;
    timing.start("Wave64");    

	while(1){
	MessageQueue::Message* im1 = rx_ports[1]->Read();
	 if(im1 == NULL)
            break;
    Log::ptag(Info) << "Reading file from port"<<flush;     
	char* sp = (char*) im1->mpData;
	sp[im1->mDataLength] = '\0';
    string tasknumber((char*)sp);

	stringstream mkdir_cmd;
	mkdir_cmd << "mkdir -p " << bndcnd_dir << " " << mesh_dir << " "
		<< flow_dir << " " << problem_dir << " " << result_dir << "sol_simu" << setfill('0') << setw(4) << tasknumber << flush;	

	//create directories
	system(mkdir_cmd.str().c_str());

	stringstream file_end_s;
	file_end_s  << setfill('0') << setw(4) << tasknumber << flush;
	string file_end(file_end_s.str());

	stringstream cmd;
	cmd << "wget " << dataurl <<  "bndcnd_files/bndcnd" << file_end <<  ".dat" 	<< " -O " << bndcnd_dir << "bndcnd" << file_end << ".dat" <<flush;

	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");
	cmd << "wget " << dataurl <<  "mesh_files/artSEPRAN" << file_end <<  ".dat" << " -O " << mesh_dir << "artSEPRAN" << file_end << ".dat" << flush;


	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");

	cmd << "wget " << dataurl <<  "mesh_files/meshoutput" << file_end <<  ".dat" << " -O " << mesh_dir << "meshoutput" << file_end << flush;

	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");
	cmd << "wget " << dataurl <<  "problem_files/tree" << file_end <<  ".prb" 	<< " -O " << problem_dir << "tree" << file_end << ".prb" << flush;

	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");

	cmd << "wget " << dataurl <<  "flow/f" << file_end <<  ".dat" 	<< " -O " << flow_dir << "f" << file_end  << flush;


	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");

	stringstream output_tar_s;
	output_tar_s << "sol_simu" << file_end << ".tar.gz" << flush;
	//LOG_PTAG(Info) << "WORKING DIR: " << tmp_dir << endl;


	cmd << "cp /home/rcushing/local/wave64/FFT_flow.dat " << tmp_dir << "/" << flush;
	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");

	//system("cp /home/rcushing/local/wave64/FFT_flow.dat /tmp/VL_WAVE64");
	chdir(tmp_dir.c_str());
	
	cmd << "/home/rcushing/local/wave64/bin/wave " << "<" << problem_dir << "tree" << file_end << ".prb" << flush;

	system(cmd.str().c_str());

	cmd.clear();
	cmd.str("");

	chdir(current_dir.c_str());

	cmd << "tar -zcf " << output_tar_s.str() << " " << tmp_dir <<"/sol_folders/sol_simu" << file_end  << flush;

	//LOG_PTAG(Info) "TAR CMD: " << cmd.str();
	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");

	//char* pwd_env = getenv("PWD");
	
//	LOG_PTAG(Info) << "CURRENT DIR: " << current_dir;
	
	//sleep(10);
	cmd << "rm -rf " << tmp_dir << flush;

	system(cmd.str().c_str());
	cmd.clear();
	cmd.str("");


	string newfile(current_dir + "/" + output_tar_s.str());

	
	MessageQueue::Message* om1 = new MessageQueue::Message();

    om1->mDataLength = newfile.size();
    om1->mpData = (void*)malloc(om1->mDataLength);
    memcpy(om1->mpData,newfile.c_str(),om1->mDataLength);

    tx_ports[1]->Write(om1);




	//pthread_mutex_lock(&condMutex);
	//pthread_cond_signal(&condSync);
	//pthread_mutex_unlock(&condMutex);

	SYNCHRONIZE();

}//while
	 tx_ports[1]->Write(NULL);
    
    timing.finish("Wave64");

};    
    

REGISTER_MODULE(Wave64);
//end_of_file
