#include <stdio.h>

int main(){
	char string[100];
	
	printf("Child Process\n");
	printf("-------------\n");
	do{
		printf("Enter Command: ");
		fflush(stdout);				/* Must flush to see command prompt */
		fgets(string,100,stdin);
		printf("%s\n",string);		/* No flush necessary because new line flushes */
	}while(!strstr(string,"exit"));
	
	return 0;
}
