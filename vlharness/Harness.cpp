/* 
 * File:   Harness.cpp
 * Author: reggie
 * 
 * Created on May 4, 2011, 12:04 PM
 */

#include "Harness.hpp"
#include "ReplicationController.hpp"


using namespace log4cplus;
using namespace activemq::core;
using namespace decaf::util::concurrent;
using namespace decaf::util;
using namespace decaf::lang;
using namespace cms;
using namespace std;
using namespace vle;

Harness* Harness::instance = NULL;
int Harness::MAX_PAYLOAD = 100;

Harness::Harness(const std::string& serverURL, const std::string& harnessUID):
    eventConnection(NULL),
    eventSession(NULL),
    eventDestination(NULL),
    eventProducer(NULL),
    serverURL(serverURL),
    harnessUID(harnessUID),
    duration(0) {
        logger = Logger::getInstance("Harness");
        activemq::library::ActiveMQCPP::initializeLibrary();
        auto_ptr<ConnectionFactory> connectionFactory(
            ConnectionFactory::createCMSConnectionFactory( serverURL ) );
            eventConnection = connectionFactory->createConnection();
            eventConnection->start();
            eventSession = eventConnection->createSession( Session::AUTO_ACKNOWLEDGE );
            eventDestination = eventSession->createQueue(harnessUID + ".event" );
            eventProducer = eventSession->createProducer( eventDestination );
            eventProducer->setDeliveryMode(DeliveryMode::NON_PERSISTENT);
            harnessStartTime = decaf::lang::System::currentTimeMillis();
            Harness::instance = this;
}

bool Harness::getConfiguration() {

        try {
            // Create a ConnectionFactory
            auto_ptr<ConnectionFactory> connectionFactory(
                ConnectionFactory::createCMSConnectionFactory( serverURL ) );

            // Create a Connection
            Connection* connection = connectionFactory->createConnection();
            connection->start();
            Session* session = connection->createSession( Session::AUTO_ACKNOWLEDGE );
            Destination* destination = session->createQueue("global.config" );
            MessageConsumer* consumer = session->createConsumer( destination );

            LOG4CPLUS_DEBUG(logger, "started global.config listener");

            writeDummyConfig();

            cms::Message* message  = consumer->receive();
            connection->close();

            if(message != NULL){
                 const TextMessage* textMessage =
                    dynamic_cast< const TextMessage* >( message );

                 //LOG4CPLUS_DEBUG(logger, "received global.config message: " + textMessage->getText());

                TiXmlDocument doc;
                doc.Parse(textMessage->getText().c_str());
                TiXmlNode* node = doc.FirstChild();
                for(node = doc.FirstChild(); node; node=node->NextSibling()){
                    TiXmlElement* element;
                    std::string nodeString(node->Value());
                    LOG4CPLUS_DEBUG(logger,"parsing node: " + nodeString);
                    if(nodeString.compare("Module") == 0){
                        AbstractModule* module = new AbstractModule();
                        module->name.assign(node->FirstChildElement("Name")->GetText());
                        string clone = node->FirstChildElement("Name")->Attribute("cloned");
                        if(clone.compare("true"))
                            module->cloned = true;
                        else
                            module->cloned = false;
                        module->context.assign(node->FirstChildElement("Context")->GetText());
                        module->instance.assign(node->FirstChildElement("Instance")->GetText());
                        module->version.assign(node->FirstChildElement("Version")->GetText());
                        module->type.assign(node->FirstChildElement("Type")->GetText());

                        for(element = node->FirstChildElement("Port"); element; element=element->NextSiblingElement("Port")){
                            AbstractModule::Port *port = new AbstractModule::Port();
                            port->name.assign(element->GetText());
                            port->direction.assign(element->Attribute("direction"));
                            port->type.assign(element->Attribute("type"));
                            port->id.assign(element->Attribute("id"));
                            port->partition.assign(element->Attribute("partition"));
                            port->stats = new AbstractModule::PortStats(serverURL,port->name);
                            module->addPort(port);
                        }

                        abstractModules.push_back(module);
                    }
                    for(element = node->FirstChildElement(); element; element=element->NextSiblingElement()){
                        std::string elementString(element->Value());
                        //load dataserver entries
                        if(elementString.compare("Server") == 0){
                            std::string protocol(element->Attribute("protocol"));
                            std::string url(element->Attribute("url"));
                            std::string params(element->Attribute("param"));
                            std::string name(element->GetText());
                            std::string str_metric(element->Attribute("metric"));
                            int metric = Integer::parseInt(str_metric);

                            Server *server = new Server(name, protocol, url, params, metric);
                            serverMap[name] = server;
                        }//if Server

                        //load module store libraries
                        if(elementString.compare("Library") == 0){
                            std::string protocol(element->Attribute("protocol"));
                            std::string url(element->Attribute("url"));
                            std::string params(element->Attribute("param"));
                            LibraryManager::Library* library = new LibraryManager::Library();
                            library->name = element->GetText();
                            library->param = params;
                            library->protocol = protocol;
                            library->url = url;
                            libraryManager.addLibrary(library);
                        }//if Library

                        //load exchange servers
                        if(elementString.compare("ExServer") == 0){
                            std::string url(element->Attribute("url"));
                            std::string name(element->GetText());
                            exchangeServersMap[name] = url;
                        }
                    }//for
                }//for
            }//if

            map<std::string,Server *, less<std::string> >::iterator itr;
            for(itr = serverMap.begin(); itr != serverMap.end(); ++itr){
                Server *s = (itr)->second;
                std::stringstream is;
                is << "server entry:- name: " << s->getName() << " prot: " <<  s->getProtocol()
                        << " url: " <<  s->getUrl() << " param: " <<  s->getParams() << " metric: " <<  s->getMetric();
                LOG4CPLUS_DEBUG(logger,is.str());
            }
            map<std::string,std::string, less<std::string> >::iterator itrex;
            for(itrex = exchangeServersMap.begin(); itrex != exchangeServersMap.end(); ++itrex){
                std::stringstream is;
                is << "exchange server entry:- name: " << itrex->first << " url: " <<  itrex->second;
                LOG4CPLUS_DEBUG(logger,is.str());
            }
            list<AbstractModule*>::iterator itrmodules;
            for(itrmodules= abstractModules.begin(); itrmodules!=abstractModules.end(); ++itrmodules){
                AbstractModule *m = *itrmodules;
                std::stringstream is;
                is << "module entry:- name "<< m->name << endl;
                map<std::string, AbstractModule::Port*, less<std::string> >::iterator itr;
                for(itr=m->getInputPorts()->begin(); itr!=m->getInputPorts()->end(); ++itr){
                    AbstractModule::Port *p = (itr)->second;                   
                    is << " port:- name "<< p->name << " direction " << p->direction << endl;
                }
                for(itr=m->getOutputPorts()->begin(); itr!=m->getOutputPorts()->end(); ++itr){
                    AbstractModule::Port *p = (itr)->second;
                    is << " port:- name "<< p->name << " direction " << p->direction << endl;
                }
                LOG4CPLUS_DEBUG(logger,is.str());
            }

            return true;

        }catch ( CMSException& e ) {
            LOG4CPLUS_ERROR(logger,e.getMessage());
        }
 }

bool Harness::installModules(){
    list<AbstractModule*>::iterator itr;
    for(itr= abstractModules.begin(); itr!=abstractModules.end(); ++itr){
        AbstractModule* m = *itr;
        std::string *path = libraryManager.searchModule(m->name);
        if(path != NULL){
            vle::IModule* module = loadModuleLib(path->c_str(),m->name.c_str());
            if(module != NULL){
                module->setName(m->name);
                module->setContext(m->context);
                module->setInstance(m->instance);
                module->setVersion(m->version);

                if(module->onTest() != true){
                    LOG4CPLUS_WARN(logger,m->name + " failed to return true onTest()");
                }else{
                    assert(module->onLoad() == true);
                    m->setConcreteModule(module);
                    LOG4CPLUS_DEBUG(logger,"loaded module "+module->getName());
                }
                assert(m->registerPorts() == true);
                LOG4CPLUS_DEBUG(logger,"registerd ports with "+module->getName());              

            }//if
        }//if
    }//for
    return true;
}//installModules

void Harness::runModules(){
    list<AbstractModule*>::iterator itr;
    for(itr = abstractModules.begin(); itr != abstractModules.end(); ++itr){
        AbstractModule *m = *itr;
        IModule *module = m->getConcreteModule();
        assert(module != NULL);
        LOG4CPLUS_DEBUG(logger, "starting queue consumers for "+module->getName());
        map<std::string, AbstractModule::Port*, less<std::string> >::iterator itr;

        for(itr=m->getInputPorts()->begin(); itr!=m->getInputPorts()->end(); ++itr){
            AbstractModule::Port *p = (itr)->second;
            vle::QueueConsumer *qconsumer = new vle::QueueConsumer(serverURL,p->name, m);
            consumerMap[p->name] = qconsumer;
            Thread *consumerThread = new Thread(qconsumer);
            qconsumer->setThread(consumerThread);
            consumerThread->start();
        }

        for(itr=m->getOutputPorts()->begin(); itr!=m->getOutputPorts()->end(); ++itr){
            AbstractModule::Port *p = (itr)->second;
            vle::QueueProducer *qproducer = new vle::QueueProducer(serverURL,p->name, m);
            producerMap[p->name] = qproducer;
            Thread *producerThread = new Thread(qproducer);
            qproducer->setThread(producerThread);
            producerThread->start();
        }

        ReplicationController* rcontroller = new ReplicationController(m,2);
        Thread* rcontroller_t = new Thread(rcontroller);
        rcontroller->setThread(rcontroller_t);
        rcontroller->setInterval(2);
        rcontroller_t->start();

        LOG4CPLUS_DEBUG(logger, "starting module: " + module->getName());
        module->startThreadedMain();
    }
}

void Harness::setDuration(long duration){
    this->duration = duration;
}

long long Harness::getRemainingTime(){
    //in seconds
    long long startTime = harnessStartTime / (1000);
    long long currentTime = decaf::lang::System::currentTimeMillis() / (1000);
    long long diffTime = currentTime - startTime;
    long long lduration =  duration * 60;
    long long remaining = lduration - diffTime;
    
    return remaining;
}

bool Harness::isTimeElapsing(int timeToEnd){
    int diffTime = getRemainingTime();
    if(diffTime >= duration-timeToEnd)
        return true;
    else
        return false;
}

void Harness::close(){
    LOG4CPLUS_DEBUG(logger,"closing harness");
    eventConnection->close();
    activemq::library::ActiveMQCPP::shutdownLibrary();
}

bool Harness::writeEvent(const std::string& msg){
     cms::Message* message = eventSession->createTextMessage(msg);
     eventProducer->send(message);
     return true;
}

void Harness::dumpQueueData(const std::string& queueName){
    auto_ptr<ConnectionFactory> connectionFactory(
            ConnectionFactory::createCMSConnectionFactory( serverURL ) );
            Connection* connection = connectionFactory->createConnection();
            connection->start();
            Session* session = connection->createSession( Session::AUTO_ACKNOWLEDGE );
            Destination* destination = session->createQueue( queueName );
            MessageConsumer* consumer = session->createConsumer( destination );
            //consumer->setDeliveryMode(DeliveryMode::NON_PERSISTENT);
}

void Harness::writeDummyConfig(){
    std::ifstream st("/home/reggie/TRUNK/vlharness/configtemplate.xml");
    std::string msg;
    st.seekg(0,std::ios::end);
    msg.reserve(st.tellg());
    st.seekg(0,std::ios::beg);
    msg.assign((std::istreambuf_iterator<char>(st)), std::istreambuf_iterator<char>());

    auto_ptr<ConnectionFactory> connectionFactory(
            ConnectionFactory::createCMSConnectionFactory( serverURL ) );
            Connection* connection = connectionFactory->createConnection();
            connection->start();
            Session* session = connection->createSession( Session::AUTO_ACKNOWLEDGE );
            Destination* destination = session->createQueue( "global.config" );
            MessageProducer* producer = session->createProducer( destination );
            producer->setDeliveryMode(DeliveryMode::NON_PERSISTENT);

            cms::Message* message = session->createTextMessage(msg);
            producer->send(message);

            destination = session->createQueue("parameters_in");
            producer = session->createProducer( destination );

            cms::Message* message2 = session->createTextMessage("asciifile://tmp/dump");
            message2->setLongProperty("size",20);
            producer->send(message2);

           message2 = session->createTextMessage("reffile://tmp/flippy");
           message2->setLongProperty("size",20);
           producer->send(message2);

           for(int i =0; i< 100; i++){
               message2 = session->createTextMessage("raw://some raw data");
               message2->setLongProperty("size",20);
               producer->send(message2);
           }

           message2 = session->createTextMessage("122324njasdd338hfdsncvw89r8e8r8fhdjf8333332420394809380fdjkslnvkjsdgkjdsfnsocmfol4mr40");
           message2->setLongProperty("size",20);
           producer->send(message2);

            producer->close();
            connection->close();
    }

Harness::Harness(const Harness& orig) {
}

Harness::~Harness() {
    close();
}
