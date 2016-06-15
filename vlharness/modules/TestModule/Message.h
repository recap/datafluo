/* 
 * File:   Message.h
 * Author: reggie
 *
 * Created on April 27, 2011, 1:37 PM
 */

#ifndef MESSAGE_H
#define	MESSAGE_H
#include <stdio.h>
#include <iostream>
#include <time.h>

using namespace std;

namespace vle{

class Message{
private:
    long timeStamp;
    long sequence;
    bool last;
    long long startProcessTime;
    long long processDuration;
    long actualDataSize;
    bool active;
    size_t  dataLenght;

public:
    std::string messageId;
    std::string source;
    std::string destination;    
    void *data;

    Message(){
        startProcessTime = 0;
        processDuration = 0;
        last = false;
        active = false;
    }

    void startProcessingTime(){
        time_t timeStamp = time(NULL);
        startProcessTime = (long long)timeStamp;
        active = true;
    }

    void stopProcessingTime(){
        time_t timeStamp = time(NULL);
        processDuration = (long long)timeStamp;
        processDuration -= startProcessTime;
        if(processDuration == 0)
            processDuration = 1;
        active = false;
    }

    bool isActive(){
        return this->active;
    }

    void setActualDataSize(long size){
        this->actualDataSize = size;
    }

    long getActualDataSize(){
        return this->actualDataSize;
    }

    void createTextMessage(const std::string& destination, const std::string& message){
        std::string *msg = new std::string;
        msg->assign(message);
        this->destination = destination;
        dataLenght = msg->length();
        data = (void *)msg->c_str();
    }

    std::string toText(){
        char* str = (char *)data;
        cout << "converting to text " << (char *)data << endl;
        str[dataLenght] = '\0';
        std::string dataAsString((char *)str);
        return dataAsString;
    }

    size_t getPayloadSize(){
        return this->dataLenght;
    }

    void setPayloadSize(size_t size){
        this->dataLenght = size;
    }

    long long getProcessDuration(){
        if(isActive() == false)
            return this->processDuration;
        else{
            time_t timeStamp = time(NULL);
            long long duration = (long long)timeStamp;
            duration -= startProcessTime;
            return duration;
        }
    }

    void markAsLast(){
        this->last = true;
    }

    bool isLast(){
        return this->last;
    }
    ~Message() throw(){

    }
};

}

#endif	/* MESSAGE_H */

