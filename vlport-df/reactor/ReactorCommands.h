#ifndef _REACTORCOMMANDS_H_
#define _REACTORCOMMANDS_H_
///////////CLIENT->SERVER////////////
#define REACTOR_IM_ALIVE 		0x7F
#define REACTOR_CHECK_MAIL 		0x7E
#define REACTOR_POST_MAIL 		0x7D
#define REACTOR_POST_EVENT 		0x7C
#define REACTOR_GET_CONFIG 		0x7B
#define REACTOR_HEART_BEAT 		0x7A
#define REACTOR_COMPLETE		0x70
///////////SERVER->CLIENT////////////
#define REACTOR_PORT_DESTROYED	0x6F
#define REACTOR_SENDING_MAIL	0x6E
#define REACTOR_NO_MAIL			0x6D
///////////BOTH//////////////////////
#define REACTOR_NO_COMMAND 		0x00

#endif
