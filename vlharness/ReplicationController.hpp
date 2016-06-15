/* 
 * File:   ReplicationController.hpp
 * Author: reggie
 *
 * Created on May 12, 2011, 2:37 PM
 */

#ifndef REPLICATIONCONTROLLER_HPP
#define	REPLICATIONCONTROLLER_HPP

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

#include "AbstractModule.h"
namespace vle{
    
class ReplicationController : public decaf::lang::Runnable{
private:
    struct PredPoint{
        long long time;
        long long elapsed;        
    };

private:
    AbstractModule* module;
    decaf::lang::Thread* thread;
    int sleepTimer;
    long long elapsedTime;
    log4cplus::Logger logger;
    list<PredPoint> predictionTimes;

public:
    ReplicationController(vle::AbstractModule* module,int interval);
    ReplicationController(const ReplicationController& orig);
    virtual ~ReplicationController();
    virtual void run();
    decaf::lang::Thread* getThread();
    void setThread(decaf::lang::Thread* thread);
    void setInterval(int interval);
private:

};
}
#endif	/* REPLICATIONCONTROLLER_HPP */

