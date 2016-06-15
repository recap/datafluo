#include<stdio.h>
#include<stdlib.h>
#include<time.h>
int main(){
	srand((unsigned)time(NULL));
	int rnd = int((double(rand())/RAND_MAX)*40);
	printf("Random number: %d\n",rnd);
return 0;
}
