#include<stdio.h>
#include<stdlib.h>

int main(){

setenv("FLIPPY","testme",0);
system("echo FLIPPY=$FLIPPY");
return 0;
}
