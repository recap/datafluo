#ifndef _MESSAGEQUEUE_H_
#define _MESSAGEQUEUE_H_

#include <semaphore.h>
#include <pthread.h>
#include <string>
#include <string.h>
#include <list>

using namespace	std;

class MessageQueue{
public:
	class Message{
	public:
		string mMessageId;
//		char mSource[25];
//		char mDestination[25];
//		int  mDestinationPort;
//		int  mSourcePort;
		int  mSequenceNumber;
//		int  mStructureID;
		size_t  mDataLength;
//		int  mState;
		void *mpData;		

		~Message();
	};
	string mName;
	int state;
	int condCounter;
	pthread_cond_t condSync;
    pthread_mutex_t condMutex;

	MessageQueue(string rName);
	~MessageQueue();
	void Write(Message *n);
	Message *Read();
	void Signal();
	void Wait();
	Message *PeekHead();
	Message *PeekTail();
	string getName();
	void setName(string rName);
	size_t size();
	void setState(int rState);
	int getState();


private:

	list<MessageQueue::Message*> vQueue;
	pthread_mutex_t mGuard;
	sem_t mSem;
};


#endif
