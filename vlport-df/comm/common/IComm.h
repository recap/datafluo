#ifndef _ICOMM_H_
#define _ICOMM_H_

#include <map>
#include <vector>
#include <string>
#include <iostream>

#include "MessageQueue.h"

#define MAX_IN_PORT 32
#define MAX_OUT_PORT 32

using namespace std;
class IComm {
public:
	char* s;		
	MessageQueue* mQueue;
	virtual void init(vector<string> rParam)=0;
	virtual void start()=0;
};



typedef IComm *cmaker_t();
extern  map<string, cmaker_t *,less<string> > 		gCommFactory;
extern  map<string, void *,less<string> > 			gSharedVariables;

#endif /*_ICOMM_H_*/
