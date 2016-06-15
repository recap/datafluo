#ifndef _IMODULE_H_
#define _IMODULE_H_

#include <map>
#include <vector>
#include <string>
#include <iostream>
#include <semaphore.h>

#include "MessageQueue.h"


#define MAX_IN_PORT 32
#define MAX_OUT_PORT 32

using namespace std;
class IModule {
public:
	MessageQueue* rx_ports[MAX_IN_PORT];
	MessageQueue* tx_ports[MAX_OUT_PORT];
	virtual void init(vector<string> *rParam)=0;
	virtual void start()=0;
	virtual void stop()=0;
	pthread_cond_t condSync;
	pthread_mutex_t condMutex;
	int condCounter;
};



typedef IModule *maker_t();
extern  map<string, maker_t *,less<string> > gModuleFactory;

#endif /*_IMODULE_H_*/
