/* 
 * File:   MessageListener.h
 * Author: reggie
 *
 * Created on April 27, 2011, 1:34 PM
 */

#ifndef MESSAGELISTENER_H
#define	MESSAGELISTENER_H
#include "Message.h"

namespace vle{

class MessageListener{
public:
    virtual void onMessage(Message* message)=0;
};

}

#endif	/* MESSAGELISTENER_H */

