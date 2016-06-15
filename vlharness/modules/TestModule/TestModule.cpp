#include <iostream>
#include "Message.h"
#include "MessageListener.h"
#include "IModule.hpp"

using namespace std;
using namespace vle;



class PortOneListener: public MessageListener{
    void onMessage(Message* message){
        cout << "received message on port: "<< message->destination << " text: " << message->toText() << endl;
        sleep(10);
    }
};

class TestModule: public IModule{

    /*called before onLoad and expects to return true*/
    bool onTest(){
        cout << name << " in onTest " << endl;
        return true;
    }

    /*called if onTest returns true. Any MessageListeners should be registered here*/
    bool onLoad(){
        cout << name << " in onLoad" << endl;
        PortOneListener* p = new PortOneListener();

        //TestModule::registerMessageListener("parameters_in", p);
        //TestModule::registerMessageListener("c1c2c3_file", p);
        //TestModule::registerMessageListener("l1l2l3_file", p);

        return true;
    }

    /*called to clean up stuff*/
    bool onUnload(){
        cout << name << " in onUnload" << endl;
        return true;
    }

    /*moduleMain is run concurrently in a thread with any defined MessageListener*/
    void moduleMain(){
        cout << name << " in moduleMain" << endl;
        while(1){
        Message* message = readMessageFromPort("parameters_in",true);
        cout << "port processing rate: " << getPortProcessingRate("parameters_in") << endl;

        if(message == NULL)
            cout << "message NULL" << endl;
        else{
            cout << "received message on port: "<< message->destination << " text: " << message->toText() << endl;
            sleep(100);
            unLatchPort("parameters_in");
        }
        }

        //Message* message2 = readMessageFromPort("c1c2c3_file",true);
        //cout << "received message on port: "<< message2->destination << " text: " << message2->toText() << endl;
    }
    
};



REGISTER_MODULE(TestModule);
