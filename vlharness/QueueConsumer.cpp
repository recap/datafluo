/* 
 * File:   QueueConsumer.cpp
 * Author: reggie
 * 
 * Created on May 4, 2011, 11:37 AM
 */

#include "QueueConsumer.hpp"

using namespace log4cplus;
using namespace activemq::core;
using namespace decaf::util::concurrent;
using namespace decaf::util;
using namespace decaf::lang;
using namespace cms;
using namespace std;
using namespace vle;

QueueConsumer::QueueConsumer(const std::string& brokerURI, const std::string& queueName, AbstractModule* module):
    brokerURI(brokerURI),
    queueName(queueName),
    connection(NULL),
    session(NULL),
    destination(NULL),
    consumer(NULL),
    browse(false){
        logger = Logger::getInstance("QueueConsumer");
        s_mapTypes["asciifile"] = asciifile;
        s_mapTypes["binaryfile"] = binaryfile;
        s_mapTypes["reffile"] = reffile;
        s_mapTypes["raw"] = raw;
        s_mapTypes["gridftp"] = gridftp;
        s_mapTypes["none"] = none;

        if((module->isCloneable() == true) && (module->getInputPort(queueName)->isAutoPartitioned() == false)){
            browse = true;
        }

        this->module = module;
    }

void QueueConsumer::run(){
     try{

            auto_ptr<ConnectionFactory> connectionFactory(
                ConnectionFactory::createCMSConnectionFactory( brokerURI ) );

            connection = connectionFactory->createConnection();
            connection->start();
            connection->setExceptionListener(this);
            session = connection->createSession(Session::AUTO_ACKNOWLEDGE);
            destination = session->createQueue(queueName);
            queue = session->createQueue(queueName);
            if(browser == false)
                consumer = session->createConsumer(destination);
            else{
                browser = session->createBrowser(queue);
                enumeration = browser->getEnumeration();
            }

            
            //if(queueName.compare(module->getPartitionPort()->name) == 0){
           //     long ssd = module->getPartitionPort()->getPortStats()->getCummulativeDataSize();
            //    long nm = module->getPartitionPort()->getPortStats()->getNumberOfMessages();
            //    long ms = module->getPartitionPort()->getPortStats()->getMeanMessageSize();
                
            //    std::stringstream ss;
            //    ss << "cummulative data size for " << module->name << " " << ssd << " no msg: " << nm << " mean size: "<<ms;
            //    ss.flush();
             //   LOG4CPLUS_DEBUG(logger, "portstat: " + ss.str());
            //}

            while(1){
                module->getConcreteModule()->waitOnPort(queueName);

               // if(queueName.compare(module->getPartitionPort()->name) == 0){
               // long long pt = module->predictComputationTime(queueName);
               // std::stringstream spt;
               // spt << "PREDICTION: " << pt;
                //LOG4CPLUS_DEBUG(logger, spt.str());
                //}
                
                cms::Message* message;
                if(browse == true)
                    message = consumer->receive();
                else{
                    bool gotMessage = false;
                    while(gotMessage == false){
                        if(enumeration->hasMoreMessages()){
                            gotMessage = true;
                            message = enumeration->nextMessage();
                        }else
                            sleep(2);
                   }
                }

                if(message != NULL){
                    
                    const TextMessage* textMessage = dynamic_cast< const TextMessage* >( message );
                    vector<std::string> tokens = split(textMessage->getText(),":");
                    std::string messageType = tokens.at(0);
                    LOG4CPLUS_DEBUG(logger,"protocol: "+messageType);

                    if(s_mapTypes.count(messageType) == 0)
                        messageType = "none";

                    switch(s_mapTypes[messageType]){
                        case asciifile:{
                            std::string *data = CommFile::getAsciiData(tokens.at(1));
                            LOG4CPLUS_DEBUG(logger, "asciifile data receivedd: " + *data);                           
                            vle::Message* vleMessage = new vle::Message();
                            vleMessage->setActualDataSize(message->getLongProperty("size"));
                            vleMessage->createTextMessage(queueName,*data);                            
                            module->getConcreteModule()->writeMessageToPort(vleMessage);                            
                            break;}
                        case reffile:{
                            std::string *data = new std::string();
                            data->assign(tokens.at(1).substr(1,tokens.at(1).length()));
                            LOG4CPLUS_DEBUG(logger, "refffile data received: " + *data);
                            vle::Message* vleMessage = new vle::Message();
                            vleMessage->setActualDataSize(message->getLongProperty("size"));
                            vleMessage->createTextMessage(queueName,*data);
                            module->getConcreteModule()->writeMessageToPort(vleMessage);                            
                            break;}
                        case raw:{
                            std::string *data = new std::string();                            
                            data->assign(tokens.at(1).substr(2,tokens.at(1).length()));
                            LOG4CPLUS_DEBUG(logger, "raw data received: " + *data);
                            vle::Message* vleMessage = new vle::Message();
                            vleMessage->setActualDataSize(message->getLongProperty("size"));
                            vleMessage->createTextMessage(queueName,*data);
                            module->getConcreteModule()->writeMessageToPort(vleMessage);                            
                            break;}
                        case none:{
                            vle::Message* vleMessage = new vle::Message();
                            vleMessage->setActualDataSize(message->getLongProperty("size"));
                            LOG4CPLUS_WARN(logger, "no protocol found in message " + tokens.at(0).substr(0,15) + "...");
                            vleMessage->createTextMessage(queueName,tokens.at(0));
                            module->getConcreteModule()->writeMessageToPort(vleMessage);                            
                            break;}
                        default:{
                            LOG4CPLUS_WARN(logger,"default protocol");
                        }
                    }//switch
                    delete message;
                }//if


            }

        }catch(CMSException& ex){
            LOG4CPLUS_ERROR(logger,ex.getStackTraceString());
        }

}

void QueueConsumer::onException( const CMSException& ex AMQCPP_UNUSED) {
        LOG4CPLUS_ERROR(logger, ex.getMessage());
}

void QueueConsumer::setThread(Thread* thread){
    this->thread = thread;
}

Thread* QueueConsumer::getThread(){
    return this->thread;
}

vector<std::string>  QueueConsumer::split(std::string str,std::string delimiter){
    vector<std::string> tokens;
    // Skip delimiter at beginning.
    string::size_type lastPos = str.find_first_not_of(delimiter, 0);
    // Find first "non-delimiter".
    string::size_type pos     = str.find_first_of(delimiter, lastPos);

    while (string::npos != pos || string::npos != lastPos)
    {
        // Found a token, add it to the vector.
        tokens.push_back(str.substr(lastPos, pos - lastPos));
        // Skip delimiter.  Note the "not_of"
        lastPos = str.find_first_not_of(delimiter, pos);
        // Find next "non-delimiter"
        pos = str.find_first_of(delimiter, lastPos);
    }
    return tokens;
}

QueueConsumer::QueueConsumer(const QueueConsumer& orig) {
}

QueueConsumer::~QueueConsumer() {
}

