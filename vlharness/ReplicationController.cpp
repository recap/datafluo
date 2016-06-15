/* 
 * File:   ReplicationController.cpp
 * Author: reggie
 * 
 * Created on May 12, 2011, 2:37 PM
 */

#include "ReplicationController.hpp"
#include "Harness.hpp"
using namespace log4cplus;
using namespace activemq::core;
using namespace decaf::util::concurrent;
using namespace decaf::util;
using namespace decaf::lang;
using namespace cms;
using namespace vle;
using namespace std;

ReplicationController::ReplicationController(vle::AbstractModule* module, int interval)
{
    this->module = module;
    this->sleepTimer = interval;
    this->elapsedTime = 0;
    this->predictionTimes.clear();
    logger = Logger::getInstance("ReplicationController");
}

void ReplicationController::run(){    
    Harness* harness = Harness::getInstance();

    while(1){
        long ssd = module->getPartitionPort()->getPortStats()->getCummulativeDataSize();
        long nm = module->getPartitionPort()->getPortStats()->getNumberOfMessages();
        long ms = module->getPartitionPort()->getPortStats()->getMeanMessageSize();

        PredPoint pt;
        pt.time = module->predictComputationTime(module->getPartitionPort()->name);
        pt.elapsed = elapsedTime;
        predictionTimes.push_back(pt);
        std::stringstream* sevent = new std::stringstream();
        *sevent << pt.time << " " << pt.elapsed << endl;
        harness->writeEvent(sevent->str());
        delete sevent;

        std::stringstream* ss = new stringstream();
        
        *ss << "module: " << module->name << " port stats: " << module->getPartitionPort()->name << endl
                << "    no messages: " << nm << endl
                << "    all data: " << ssd << endl
                << "    predicted completion time: " << pt.time << endl
                << "    remaining harness time: " << harness->getRemainingTime() << endl
                << endl;

        ss->flush();
        LOG4CPLUS_DEBUG(logger, ss->str());
        delete ss;
        sleep(this->sleepTimer);
        elapsedTime += sleepTimer;
    }
}

void ReplicationController::setThread(Thread* thread){
    this->thread = thread;
}

Thread* ReplicationController::getThread(){
    return this->thread;
}

void ReplicationController::setInterval(int interval){
    this->sleepTimer = interval;
}

ReplicationController::ReplicationController(const ReplicationController& orig) {
}

ReplicationController::~ReplicationController() {
}

