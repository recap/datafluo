#include "IModule.hpp"

using namespace std;
using namespace vle;

IModule* IModule::classInstance = NULL;


bool IModule::deliverMessage(const std::string& portName, Message* message){
    if(messageListeners[portName] != NULL){
        MessageListener* messageListener = messageListeners[portName];
        markStartOfMessage(portName, message);
        messageListener->onMessage(message);
        unLatchPort(portName);
        return true;
    }
    return false;
}

void IModule::markStartOfMessage(const std::string& portName, Message* message){
    Message* currentMessage = lastMessages[portName];
    if(currentMessage != NULL){
        currentMessage->stopProcessingTime();
        float rate = (float)currentMessage->getActualDataSize() / (float)currentMessage->getProcessDuration();
        float currentRate = portProcessingRate[portName];
        float newRate = ((rate*1.7) + (currentRate*0.3)) / 2;
        cout << "RATES: " << rate << " " << currentRate << " " << newRate << " : "
                <<  currentMessage->getActualDataSize() << " " << currentMessage->getProcessDuration() << endl;
        portProcessingRate[portName] = newRate;
        //portProcessingRate[portName] = currentMessage->getProcessDuration();
    }
    lastMessages[portName] = message;
    message->startProcessingTime();
    
}

void IModule::unLatchPort(const std::string& portName){
    sem_t *sem = inputPortLatch[portName];
    assert(sem != NULL);
    sem_post(sem);
}

IModule::IModule(){
    IModule::classInstance = this;
}

void IModule::waitOnPort(const std::string& portName){
    sem_t *sem = inputPortLatch[portName];
    assert(sem != NULL);
    sem_wait(sem);
}

void IModule::startThreadedMain(){
    assert(pthread_create(&thread_t,NULL,&IModule::threadMain,NULL) == 0);
}

void IModule::setName(const std::string& name){
    this->name = name;
}

void IModule::setContext(const std::string& context){
    this->context = context;
}

void IModule::setInstance(const std::string& instance){
    this->instance = instance;
}

void IModule::setVersion(const std::string& version){
    this->version = version;
}

std::string IModule::getName(){
    return this->name;
}

std::string IModule::getContext(){
    return this->context;
}

std::string IModule::getInstance(){
    return this->instance;
}

void IModule::registerMessageListener(const std::string& portName, MessageListener* messageListener){
    messageListeners[portName] = messageListener;
}

bool IModule::existsMessageListener(const std::string& portName){
    if(messageListeners[portName] == NULL)
        return false;
    else
        return true;
}

std::list<std::string> IModule::getMessageListenerNames(){
    list<std::string> names;
    map<std::string, MessageListener*, less<std::string> >::iterator itr;
    for(itr=messageListeners.begin(); itr!=messageListeners.end(); ++itr){
        names.push_back(itr->first);
    }
    return names;
}

float IModule::getPortProcessingRate(const std::string& portName){
    if((portProcessingRate.count(portName) > 0) && (portProcessingRate[portName] > 0)){
        return portProcessingRate[portName];
    }else{
        if(lastMessages[portName] != NULL){
            Message* message = lastMessages[portName];
            float rate = (float)message->getActualDataSize() / (float)message->getProcessDuration();
            return rate;
        }
    }
    return 0;
}

void IModule::writeMessageToPort(Message* message){
    std::string portName = message->destination;   
    if(inputPorts[portName] != NULL){
        if(deliverMessage(portName, message) == false){
            MessageQueue<Message> *queue = inputPorts[portName];
            queue->enqueue(message);
        }
    }
    if(outputPorts[portName] != NULL){
        MessageQueue<Message> *queue = outputPorts[portName];
        queue->enqueue(message);
    }
}

Message* IModule::readMessageFromPort(const std::string& portName, bool blocking){
    cout << "reading message from " << portName << endl;
    if(inputPorts[portName] != NULL){
        cout << "found port "<< portName << endl;
        MessageQueue<Message> *queue = inputPorts[portName];
        Message* message;
        if(blocking == false)
            message = (Message*)queue->trydequeue();
        else
            message = (Message*)queue->dequeue();

        if(message != NULL){
            markStartOfMessage(portName,message);
        }

        return message;
    }
    if(outputPorts[portName] != NULL){
        MessageQueue<Message> *queue = outputPorts[portName];
        Message* message;
        if(blocking == false)
            message = (Message*)queue->trydequeue();
        else
            message = (Message*)queue->dequeue();
        return message;
    }
    return NULL;
}

bool IModule::registerInputPort(const std::string& portName){
    if(inputPorts[portName] == NULL){
        MessageQueue<Message> *queue = new MessageQueue<Message>();
        inputPorts[portName] = queue;
        sem_t *sem  = (sem_t*)malloc(sizeof(sem_t));
        sem_init(sem,NULL,1);
        inputPortLatch[portName] = sem;
        return true;
    }
    return false;
}

bool IModule::registerOutputPort(const std::string& portName){
    if(outputPorts[portName] == NULL){
        MessageQueue<Message> *queue = new MessageQueue<Message>();
        outputPorts[portName] = queue;
        return true;
    }
    return false;
}

void IModule::close(){
    map<std::string, MessageQueue<Message>*, less<std::string> >::iterator itr;
    for(itr = inputPorts.begin(); itr != inputPorts.end(); ++itr){
        MessageQueue<Message> *queue = itr->second;
        delete queue;
        itr->second = NULL;
    }
    for(itr = outputPorts.begin(); itr != outputPorts.end(); ++itr){
        MessageQueue<Message> *queue = itr->second;
        delete queue;
        itr->second = NULL;
    }
    onUnload();
}

IModule::~IModule() throw(){
    close();
}
