/* 
 * File:   QueueProducer.cpp
 * Author: reggie
 * 
 * Created on May 6, 2011, 1:16 PM
 */

#include "QueueProducer.hpp"
#include "Harness.hpp"

using namespace log4cplus;
using namespace activemq::core;
using namespace decaf::util::concurrent;
using namespace decaf::util;
using namespace decaf::lang;
using namespace cms;
using namespace vle;
using namespace std;

QueueProducer::QueueProducer(const std::string& brokerURI, const std::string& queueName, vle::AbstractModule* module):
    brokerURI(brokerURI),
    queueName(queueName),
    connection(NULL),
    session(NULL),
    destination(NULL),
    producer(NULL){
        logger = Logger::getInstance("QueueProducer");
        this->module = module;
}

void QueueProducer::run(){
    IModule *cmodule = module->getConcreteModule();

    auto_ptr<ConnectionFactory> connectionFactory(
                ConnectionFactory::createCMSConnectionFactory( brokerURI ) );

    connection = connectionFactory->createConnection();
    connection->start();
    connection->setExceptionListener(this);
    session = connection->createSession(Session::AUTO_ACKNOWLEDGE);
    destination = session->createQueue(queueName);
    producer = session->createProducer(destination);

    while(1){
        vle::Message* message = cmodule->readMessageFromPort(queueName, true);
        if(message != NULL){
            if(message->getPayloadSize() < Harness::MAX_PAYLOAD){
                cms::Message *msg = session->createTextMessage(message->toText());
                producer->send(msg);
            }
            if(message->isLast() == true)
                break;
        }
    }

    connection->close();
}

void QueueProducer::setThread(decaf::lang::Thread* thread){
    this->thread = thread;
}

Thread* QueueProducer::getThread(){
    return this->thread;
}

vector<std::string>  QueueProducer::split(std::string str,std::string delimiter){
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

void QueueProducer::onException(const cms::CMSException& ex){

}

QueueProducer::QueueProducer(const QueueProducer& orig) {
}

QueueProducer::~QueueProducer() {
}

