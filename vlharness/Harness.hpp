/* 
 * File:   Harness.hpp
 * Author: reggie
 *
 * Created on May 4, 2011, 12:04 PM
 */

#ifndef HARNESS_HPP
#define	HARNESS_HPP

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

#include "LibraryManager.h"
#include "Server.h"
#include "AbstractModule.h"
#include "IModule.hpp"
#include "QueueConsumer.hpp"
#include "QueueProducer.hpp"
#include "tinyxml.h"
#include "main.h"

namespace vle {


class Harness {
public:
    static int MAX_PAYLOAD;

private:
    log4cplus::Logger logger;
    static Harness* instance;
    cms::Connection* eventConnection;
    cms::Session* eventSession;
    cms::Destination* eventDestination;
    cms::MessageProducer* eventProducer;
    std::string serverURL;
    std::string harnessUID;
    int duration;
    long long harnessStartTime;

    LibraryManager libraryManager;
    std::map<std::string, Server *, std::less <std::string> > serverMap;
    std::map<std::string, std::string, std::less <std::string> > exchangeServersMap;
    std::list<AbstractModule*> abstractModules;
    std::map<std::string, QueueConsumer* , std::less <std::string> > consumerMap;
    std::map<std::string, QueueProducer* , std::less <std::string> > producerMap;

    friend class QueueConsumer;

public:
    Harness(const std::string& serverURL, const std::string& harnessUID);
    Harness(const Harness& orig);
    virtual ~Harness();
    void close();
    bool getConfiguration();
    bool writeEvent(const std::string& message);
    bool installModules();
    void runModules();
    void setDuration(long duration);
    long long getRemainingTime();
    bool isTimeElapsing(int timeToEnd);

    static Harness* getInstance(){
        return Harness::instance;
    }

private:
    void writeDummyConfig();
    void dumpQueueData(const std::string& queueName);

};

}

#endif	/* HARNESS_HPP */

