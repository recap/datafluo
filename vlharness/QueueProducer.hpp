/* 
 * File:   QueueProducer.hpp
 * Author: reggie
 *
 * Created on May 6, 2011, 1:16 PM
 */

#ifndef QUEUEPRODUCER_HPP
#define	QUEUEPRODUCER_HPP

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

#include "Message.h"
#include "CommFile.h"
#include "AbstractModule.h"


namespace vle{


class QueueProducer : public cms::ExceptionListener,
                      public decaf::lang::Runnable {
private:
    std::string brokerURI;
    std::string queueName;
    log4cplus::Logger logger;
    cms::Connection* connection;
    cms::Session* session;
    cms::Destination* destination;
    cms::MessageProducer* producer;
    vle::AbstractModule* module;
    decaf::lang::Thread* thread;

public:
    QueueProducer(const std::string& brokerURI, const std::string& queueName, vle::AbstractModule* module);
    QueueProducer(const QueueProducer& orig);
    virtual ~QueueProducer();
    void close();
    void setThread(decaf::lang::Thread* thread);
    decaf::lang::Thread* getThread();

private:
    virtual void run();
    virtual void onException(const cms::CMSException& ex AMQCPP_UNUSED);
    std::vector<std::string>  split(std::string str,std::string delimiter);

};
}
#endif	/* QUEUEPRODUCER_HPP */

