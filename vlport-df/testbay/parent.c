#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

int main(){

	pid_t pid;
	int rv;
	int	commpipe[2];		/* This holds the fd for the input & output of the pipe */
	int fd;
	fd = fopen("dump2.txt","wb");

	/* Setup communication pipeline first */
	if(pipe(commpipe)){
		fprintf(stderr,"Pipe error!\n");
		exit(1);
	}

	/* Attempt to fork and check for errors */
	if( (pid=fork()) == -1){
		fprintf(stderr,"Fork error. Exiting.\n");  /* something went wrong */
		exit(1);        
	}

	if(pid){
		/* A positive (non-negative) PID indicates the parent process */
		//dup2(commpipe[1],1);	/* Replace stdout with out side of the pipe */
		//close(commpipe[0]);		/* Close unused side of pipe (in side) */
		//setvbuf(stdout,(char*)NULL,_IONBF,0);	/* Set non-buffered output on stdout */
		//sleep(2);
		//printf("Hello\n");
		//sleep(2);
		//printf("Goodbye\n");
		//sleep(2);
		//printf("exit\n");
		wait(&rv);				/* Wait for child process to end */
		fprintf(stderr,"Child exited with a %d value\n",rv);
	}
	else{
		/* A zero PID indicates that this is the child process */
		//dup2(commpipe[0],0);	/* Replace stdin with the in side of the pipe */
		dup2(3,1);	/* Replace stdin with the in side of the pipe */
		//close(commpipe[1]);		/* Close unused side of pipe (out side) */
		/* Replace the child fork with a new process */
		if(execl("legacyapp","legacyapp","-i","alltissues.txt",NULL) == -1){
			fprintf(stderr,"execl Error!");
			exit(1);
		}
	}
	return 0;
}
