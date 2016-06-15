/* 
 * File:   IModule.hpp
 * Author: reggie
 *
 * Created on May 4, 2011, 12:40 PM
 */

#ifndef IMODULE_HPP
#define	IMODULE_HPP

#define REGISTER_MODULE(NAME) \
extern "C" { IModule *maker(){  return new NAME; } \
class proxy { \
public: \
   proxy() { \
      gModuleFactory[#NAME] = maker;  } \
}; \
proxy p; }

#include <list>
#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <map>
#include <pthread.h>
#include <semaphore.h>
#include <errno.h>
#include <assert.h>
#include "Message.h"
#include "MessageListener.h"

namespace vle {
class IModule {

protected:        
    std::string name;
    std::string context;
    std::string instance;
    std::string version;

private:
    pthread_t thread_t;
    static IModule* classInstance;

    template <class T> class MessageQueue{
    private:
	std::list<T*> queue;
        sem_t sem;
        pthread_mutex_t guard;
    public:
        MessageQueue(){
            pthread_mutex_init(&guard,NULL);
            sem_init(&sem,NULL,0);
        }

        void enqueue(T* item){
            pthread_mutex_lock(&guard);
            queue.push_back(item);
            //cout << "queued something" << endl;
            sem_post(&sem);
            pthread_mutex_unlock(&guard);
        }

        T* dequeue(){
            //cout << "in dequeue" << endl;
            sem_wait(&sem);
                //cout << "in semaphore" << endl;
                pthread_mutex_lock(&guard);
                T* item = queue.back();
                //if(item == NULL)
                   //cout << "item == NULL" << endl;
                queue.pop_back();
                pthread_mutex_unlock(&guard);
                return item;

        }

        T* trydequeue(){
            if(sem_trywait(&sem) != 0)
                //if(errno == EAGAIN)
                    return NULL;
            pthread_mutex_lock(&guard);
            T* item = queue.back();
            queue.pop_back();
            pthread_mutex_unlock(&guard);
            return item;
        }

        int size(){
            return queue.size();
        }

        ~MessageQueue() throw(){
            close();
        }

        void close(){
            pthread_mutex_destroy(&guard);
            sem_destroy(&sem);
        }
    };

    std::map<std::string, MessageQueue<Message>*, std::less<std::string> > inputPorts;
    std::map<std::string, MessageQueue<Message>*, std::less<std::string> > outputPorts;
    std::map<std::string, sem_t *, std::less<std::string> > inputPortLatch;
    std::map<std::string, Message*, std::less<std::string> > lastMessages;
    std::map<std::string, float, std::less<std::string> > portProcessingRate;
    std::map<std::string, MessageListener*, std::less<std::string> > messageListeners;

    static IModule* getClassInstance(){
        return IModule::classInstance;
    }

    static void* threadMain(void* ptr){
        IModule* module = IModule::getClassInstance();
        assert(module != NULL);
        module->moduleMain();
    }

    bool deliverMessage(const std::string& portName, Message* message);
    void markStartOfMessage(const std::string& portName, Message* message);
    
protected:
    void unLatchPort(const std::string& portName);

public:
    IModule();
    virtual bool onTest()=0;
    virtual bool onLoad()=0;
    virtual bool onUnload()=0;
    virtual void moduleMain()=0;

    void startThreadedMain();
    void setName(const std::string& name);
    void setContext(const std::string& context);
    void setInstance(const std::string& instance);
    void setVersion(const std::string& version);
    std::string getName();
    std::string getContext();
    std::string getInstance();    
    void registerMessageListener(const std::string& portName, MessageListener* messageListener);
    bool existsMessageListener(const std::string& portName);
    std::list<std::string> getMessageListenerNames();
    void writeMessageToPort(Message* message);
    Message* readMessageFromPort(const std::string& portName, bool blocking);
    bool registerInputPort(const std::string& portName);
    bool registerOutputPort(const std::string& portName);
    float getPortProcessingRate(const std::string& portName);
    void waitOnPort(const std::string& portName);
    void close();
    ~IModule() throw();
    };

}

typedef vle::IModule *maker_t();
extern  map<string, maker_t *, less<string> > gModuleFactory;

#endif	/* IMODULE_HPP */

