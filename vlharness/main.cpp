/*
 * File:   main.cpp
 * Author: reggie
 *
 * Created on April 25, 2011, 1:34 PM
 */

#include <log4cplus/logger.h>
#include <log4cplus/configurator.h>
#include <log4cplus/fileappender.h>
//#include <activemq/library/ActiveMQCPP.h>
//#include <decaf/lang/Thread.h>
//#include <decaf/lang/Runnable.h>
//#include <decaf/util/concurrent/CountDownLatch.h>
//#include <decaf/lang/Integer.h>
//#include <decaf/lang/Long.h>
#include <decaf/lang/System.h>
//#include <activemq/core/ActiveMQConnectionFactory.h>
//#include <activemq/util/Config.h>
//#include <cms/Connection.h>
//#include <cms/Session.h>
//#include <cms/TextMessage.h>
//#include <cms/BytesMessage.h>
//#include <cms/MapMessage.h>
//#include <cms/ExceptionListener.h>
//#include <cms/MessageListener.h>

#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <memory>
#include <uuid/uuid.h>
#include <dlfcn.h>
#include <sys/stat.h>

//#include "tinyxml.h"
//#include "Server.h"
//#include "IModule.h"
//#include "Message.h"
//#include "LibraryManager.h"
//#include "AbstractModule.h"
//#include "main.h"
//#include "QueueConsumer.h"
//#include "QueueConsumer.h"
#include "Harness.hpp"




using namespace log4cplus;
//using namespace activemq::core;
//using namespace decaf::util::concurrent;
//using namespace decaf::util;
//using namespace decaf::lang;
//using namespace cms;
using namespace std;
using namespace vle;


#define TIXML_USE_STL
map<string, maker_t *, less<string> > gModuleFactory;
Logger logger;

int main(int argc AMQCPP_UNUSED, char* argv[] AMQCPP_UNUSED) {   
    
#ifdef FILELOG
    SharedAppenderPtr myAppender(new FileAppender("myLogFile.log"));
    myAppender->setName("myAppenderName");
    std::auto_ptr<Layout> myLayout = std::auto_ptr<Layout>(new log4cplus::TTCCLayout());
    myAppender->setLayout( myLayout );
    Logger logger= Logger::getInstance("main");
    logger.addAppender(myAppender);
#else
    BasicConfigurator config;
    config.configure();
    logger = Logger::getInstance("main");
#endif   

    LOG4CPLUS_TRACE(logger,"starting vlharness");
    //get command line parameters
    char* p;
    int param_itr = 0;
    std::string serverURL;
    std::string harnessUID;
    long long quantumTime;
    for(param_itr=1; param_itr < argc;param_itr++)
    {
        p = argv[param_itr];
        if(!strcmp(p,"-s"))
            serverURL = std::string(argv[param_itr+1]);
        if(!strcmp(p,"-i"))
            harnessUID = std::string(argv[param_itr+1]);
        if(!strcmp(p,"-t"))
            quantumTime = atol(argv[param_itr+1]);
    }//for

    if(serverURL.empty() == true){        
        LOG4CPLUS_FATAL(logger,"no serverURL defined");
    }

    if(harnessUID.empty() == true){
        uuid_t uuid;
        uuid_generate_random(uuid);
        char ch[36];
        memset(ch,0,36);
        uuid_unparse(uuid,ch);
        harnessUID = std::string(ch);
    }
    LOG4CPLUS_DEBUG(logger,"server URL: " << serverURL << " harness UID: " << harnessUID);
    
    long long startTime = decaf::lang::System::currentTimeMillis();
    LOG4CPLUS_INFO(logger,"harness:" << harnessUID << " start time: " << startTime);

    Harness *harness = new Harness(serverURL, harnessUID);
    harness->setDuration(quantumTime);
    assert(harness->getConfiguration() == true);
    assert(harness->installModules() == true);
    harness->runModules();
    sleep(10000);
    //harness->writeEvent("got config");
    delete harness;

    long long endTime = decaf::lang::System::currentTimeMillis();
    double totalTime = (double)(endTime - startTime) / 1000.0;

    

    std::cout << "Time to completion = " << totalTime << " seconds." << std::endl;
    std::cout << "-----------------------------------------------------\n";
    std::cout << "Finished with the vlharness." << std::endl;
    std::cout << "=====================================================\n";
    
}

IModule* loadModuleLib(const char* path, const char* name){
    void* dlib;
    dlib = dlopen(path,RTLD_NOW);
    if(dlib == NULL){
        char *errstr;
        errstr = dlerror();
        if (errstr != NULL){
            std::string ex("a dynamic linking error occurred: ");
            ex.append(errstr);
            LOG4CPLUS_FATAL(logger,ex.c_str());
        }
        return NULL;
    }
    IModule* module = gModuleFactory[name]();  
    return module;
}

bool fileExists(std::string strFilename) {
  struct stat stFileInfo;
  bool blnReturn;
  int intStat;

  // Attempt to get the file attributes
  intStat = stat(strFilename.c_str(),&stFileInfo);
  if(intStat == 0) {
    // We were able to get the file attributes
    // so the file obviously exists.
    blnReturn = true;
  } else {
    // We were not able to get the file attributes.
    // This may mean that we don't have permission to
    // access the folder which contains this file. If you
    // need to do that level of checking, lookup the
    // return values of stat which will give you
    // more details on why stat failed.
    blnReturn = false;
  }

  return(blnReturn);
}