#ifndef VL_COMPDEFS_H_
#define VL_COMPDEFS_H_
#define MAXPORTS 32

#define REGISTER_MODULE(NAME) \
extern "C" { IModule *maker(){  return new NAME; } \
class proxy { \
public: \
   proxy() { \
      gModuleFactory[#NAME] = maker;  } \
}; \
proxy p; }

#define REGISTER_PORT_IN(OBJCLASS,OBJ,PN) \
OBJ = new OBJCLASS; \
rx_ports[PN] = OBJ

/*#define INIT_SYNCHRONIZE()\
		condCounter = 0;\
 		pthread_cond_init (&condSync, NULL);\
        pthread_mutex_init(&condMutex, NULL)


#define SYNCHRONIZE() \
 	pthread_mutex_lock(&condMutex);\
	condCounter++;\
    pthread_cond_signal(&condSync);\
    pthread_mutex_unlock(&condMutex)*/

#define INIT_PORTS()\
 for(int i = 0; i < MAXPORTS; i++){\
            rx_ports[i] = NULL;\
            tx_ports[i] = NULL;  }

#define MAP_TX_PORT(NUM,NAME)\
	tx_ports[NUM] = new MessageQueue(#NAME)

#define MAP_RX_PORT(NUM,NAME)\
	rx_ports[NUM] = new MessageQueue(#NAME)

#define WRITE_PORT(NUM,MSG)\
	tx_ports[NUM]->Write(MSG)

#define READ_PORT(NUM)\
	rx_ports[NUM]->Read()

#define SIGNAL_RX_PORT(NUM)\
	rx_ports[NUM]->Signal()

#define WAIT_RX_PORT(NUM)\
	rx_ports[NUM]->Wait()

#define SET_RX_PORT_STATE(NUM,STATE)\
	rx_ports[NUM]->state = STATE


 
#endif /*VL_COMPDEFS_H_*/
