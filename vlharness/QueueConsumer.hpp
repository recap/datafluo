/* 
 * File:   QueueConsumer.hpp
 * Author: reggie
 *
 * Created on May 4, 2011, 11:37 AM
 */

#ifndef QUEUECONSUMER_HPP
#define	QUEUECONSUMER_HPP

#include <log4cplus/logger.h>
#include <log4cplus/configurator.h>
#include <log4cplus/fileappender.h>
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

#include "Message.h"
#include "CommFile.h"
#include "AbstractModule.h"

namespace vle {

class QueueConsumer :   public cms::ExceptionListener,
                        public decaf::lang::Runnable {
private:
    enum types {   asciifile,
                   binaryfile,
                   reffile,
                   raw,
                   gridftp,
                   none };

    std::map<std::string, types> s_mapTypes;

    std::string brokerURI;
    std::string queueName;
    log4cplus::Logger logger;
    cms::Connection* connection;
    cms::Session* session;
    cms::Destination* destination;
    cms::MessageConsumer* consumer;
    cms::QueueBrowser* browser;
    cms::MessageEnumeration* enumeration;
    cms::Queue* queue;
    vle::AbstractModule* module;
    decaf::lang::Thread *thread;
    bool browse;

public:
    QueueConsumer(const std::string& brokerURI, const std::string& queueName, vle::AbstractModule* module);
    QueueConsumer(const QueueConsumer& orig);
    virtual ~QueueConsumer();
    void close();
    decaf::lang::Thread* getThread();
    void setThread(decaf::lang::Thread* thread);
    

private:
    std::vector<std::string>  split(std::string str,std::string delimiter);
    virtual void run();
    virtual void onException(const cms::CMSException& ex AMQCPP_UNUSED);


};
}

#endif	/* QUEUECONSUMER_HPP */

