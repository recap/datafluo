#include "MessageQueue.h"
#include <semaphore.h>
#include <iostream>
#include <string.h>
#include <malloc.h>
#include "LogManager.H"

using namespace std;

MessageQueue::Message::~Message(){}

MessageQueue::MessageQueue(string rName)
{
//	string name((char*)rName);
	mName.assign(rName);
	state = 0;
	//mName = name.c_str();
//	mDummyHead.pNext = NULL;
//	mpTail = &mDummyHead;
//	mpHead = &mDummyHead;

	condCounter = 0;
	pthread_cond_init (&condSync, NULL);
    pthread_mutex_init(&condMutex, NULL);

	pthread_mutex_init(&mGuard,NULL);
	sem_init(&mSem,NULL,0);

	

	vQueue.push_back(NULL);
//	mSize = 0;
	LOG_PTAG(Debug) << "Started Queue " << mName << endl;
	
}

void MessageQueue::Signal(){
 	pthread_mutex_lock(&condMutex);
    condCounter++;
    pthread_cond_signal(&condSync);
    pthread_mutex_unlock(&condMutex);
}//signal

void MessageQueue::Wait(){
	pthread_mutex_lock(&condMutex);
	if(condCounter <= 0){
		pthread_cond_wait(&condSync,&condMutex);
		condCounter--;
	}
	else{
    	condCounter--;
	}
    pthread_mutex_unlock(&condMutex);
}//wait
void MessageQueue::Write(Message *rMessage)
{
	pthread_mutex_lock(&mGuard);
	//mpHead->pNext = rMessage;
	//mpHead = rMessage;
	//mSize++;
	MessageQueue::vQueue.push_back(rMessage);
	sem_post(&mSem);
	pthread_mutex_unlock(&mGuard);	
}

MessageQueue::Message* MessageQueue::Read()
{	
	sem_wait(&mSem);
	pthread_mutex_lock(&mGuard);
	//Message *tmp = mpTail->pNext;
	//mpTail = mpTail->pNext;
	//mSize--;
	//remove previosly read item
	vQueue.pop_front();
	//read next
	MessageQueue::Message* tmp = (MessageQueue::Message*)vQueue.front();
	pthread_mutex_unlock(&mGuard);
	return tmp;
}

string MessageQueue::getName(){
	return mName;
}
void MessageQueue::setName(string rName){
	mName = rName;
}
void MessageQueue::setState(int rState){
	state = rState;
}
int MessageQueue::getState(){
	return state;
}

MessageQueue::Message* MessageQueue::PeekHead()
{
	Message* tmp = (Message*)vQueue.back();
	return tmp;
}
MessageQueue::Message* MessageQueue::PeekTail()
{
	Message* tmp = (Message*)MessageQueue::vQueue.front();
	return tmp;
}

size_t MessageQueue::size(){
	return MessageQueue::vQueue.size();
}

MessageQueue::~MessageQueue()
{
	//MessageQueue::Message::~Message();
	MessageQueue::vQueue.clear();
	sem_destroy(&mSem);
}

