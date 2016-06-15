/* 
 * File:   AbstractModule.h
 * Author: reggie
 *
 * Created on April 28, 2011, 1:39 PM
 */

#ifndef ABSTRACTMODULE_H
#define	ABSTRACTMODULE_H

#include "IModule.hpp"
#include "includes/logging/log4cplus/logger.h"
#include <activemq/library/ActiveMQCPP.h>
#include <decaf/lang/Thread.h>
#include <decaf/lang/Runnable.h>
#include <decaf/util/concurrent/CountDownLatch.h>
#include <decaf/lang/Integer.h>
#include <decaf/lang/Long.h>
#include <decaf/lang/System.h>
#include <activemq/core/ActiveMQConnectionFactory.h>
#include <activemq/util/Config.h>
#include <cms/Connection.h>
#include <cms/Session.h>
#include <cms/TextMessage.h>
#include <cms/BytesMessage.h>
#include <cms/MapMessage.h>
#include <cms/ExceptionListener.h>
#include <cms/MessageListener.h>
#include <cms/QueueBrowser.h>

using namespace log4cplus;
using namespace std;
using namespace vle;

namespace vle{

class AbstractModule{
public:
    class PortStats {
    private:
        std::string brokerURI;
        std::string portName;
        Logger logger;
        cms::Connection *connection;
        cms::Session *session;
        cms::Queue *queue;
        cms::Destination *destination;
        cms::QueueBrowser *browser;
        long numberOfMessages;
        long meanMessageSize;

    public:
        PortStats(const std::string& brokerURI, const std::string& portName):
        brokerURI(brokerURI),
        portName(portName){
            logger = Logger::getInstance("PortStats");
            LOG4CPLUS_DEBUG(logger,"starting portstat for "+portName);
        }

        long getCummulativeDataSize(){            
            long long size = 0;
            numberOfMessages = 0;
            
            if(connect() == true){
                cms::MessageEnumeration* enumeration = browser->getEnumeration();

                while(enumeration->hasMoreMessages() == true){                    
                    cms::Message* message = enumeration->nextMessage();
                    size += message->getLongProperty("size");
                    numberOfMessages++;
                }
                if(numberOfMessages > 0)
                    meanMessageSize = size / numberOfMessages;
            }
            disconnect();           

            return size;
        }

        long getMeanMessageSize(){
            return meanMessageSize;
        }

        long getNumberOfMessages(){
            return numberOfMessages;
        }

       
    private:
        bool connect(){
            auto_ptr<cms::ConnectionFactory> connectionFactory(
                cms::ConnectionFactory::createCMSConnectionFactory( brokerURI ) );

            connection = connectionFactory->createConnection();
            connection->start();            
            session = connection->createSession(cms::Session::AUTO_ACKNOWLEDGE);            
            queue = session->createQueue(portName);
            browser = session->createBrowser(queue);


            return true;
        }

        void disconnect(){
            browser->close();
            session->close();
            connection->close();
        }

    };
public:
    struct Port{
        std::string name;
        std::string direction;
        std::string id;
        std::string type;
        std::string partition;
        PortStats* stats;

        bool isAutoPartitioned(){
            if(partition.compare("auto")==0)
                return true;
            else
                return false;
        }

        PortStats* getPortStats(){
            return stats;
        }
    };
public:
    std::string name;
    std::string id;
    std::string context;
    std::string instance;
    std::string type;
    std::string version;
    bool cloned;



private:
    Logger logger;
    map<std::string, Port*, less<std::string> > inputPorts;
    map<std::string, Port*, less<std::string> > outputPorts;
    Port* partitionPort;
    IModule* concreteModule;
    bool cloneable;

public:

    AbstractModule(){
        logger = Logger::getInstance("AmstractModule");
        cloned = false;
        cloneable = false;
    }

    void setPartitionPort(Port* port){
        this->partitionPort = port;
    }

    Port* getPartitionPort(){
        return this->partitionPort;
    }

    bool isCloneable(){
        return this->cloneable;
    }

    Port* getInputPort(const std::string& portName){
        return inputPorts[portName];
    }

    Port* getOutputPort(const std::string& portName){
        return outputPorts[portName];
    }

    void addPort(Port* port){
        if(port->direction.compare("input") == 0){
            if(inputPorts[port->name] == NULL){
                inputPorts[port->name] = port;
                if(port->isAutoPartitioned() == true){
                    this->partitionPort = port;
                    this->cloneable = true;
                }
            }
        }
        if(port->direction.compare("output") == 0){
            if(outputPorts[port->name] == NULL)
                outputPorts[port->name] = port;
        }
    }//addPort

    bool registerPorts(){
        if(concreteModule == NULL)
            return false;

        map<std::string, Port*, less<std::string> >::iterator itr;
        for(itr = inputPorts.begin(); itr != inputPorts.end(); ++itr){
            Port *port = itr->second;
            assert(concreteModule->registerInputPort(port->name) == true);
            if(concreteModule->existsMessageListener(port->name) == false){
                LOG4CPLUS_WARN(logger,"port "+port->name+" for module " + concreteModule->getName() + " has no associated listener.");
            }
        }

        for(itr = outputPorts.begin(); itr != outputPorts.end(); ++itr){
            Port *port = itr->second;
            assert(concreteModule->registerOutputPort(port->name) == true);
        }

        list<std::string> names = concreteModule->getMessageListenerNames();
        list<std::string>::iterator itr_names;
        for(itr_names = names.begin(); itr_names != names.end(); ++itr_names){
            std::string name = *itr_names;
            if(inputPorts[name] == NULL){
                LOG4CPLUS_ERROR(logger,"registerd message listener "+name+" has no associated port.");
            }
        }
        return true;
    }

    bool isClone(){
        return this->cloned;
    }

    void setConcreteModule(IModule* module){
        this->concreteModule = module;
    }

    IModule* getConcreteModule(){
        return this->concreteModule;
    }

    long long predictComputationTime(const std::string& portName){
        IModule *m = this->getConcreteModule();
        PortStats *ps = inputPorts[portName]->getPortStats();
        long data = ps->getCummulativeDataSize();
        float rate = m->getPortProcessingRate(portName);
        if(rate > 0){
            long long predictTime = data / rate;
            return predictTime;
        }else
            return 0;
    }

    map<std::string, Port*, less<std::string> >* getInputPorts(){
        return &inputPorts;
    }

    map<std::string, Port*, less<std::string> >* getOutputPorts(){
        return &outputPorts;
    }

};
}

#endif	/* ABSTRACTMODULE_H */

