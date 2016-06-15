/* 
 * File:   main.h
 * Author: reggie
 *
 * Created on April 28, 2011, 10:56 AM
 */

#ifndef MAIN_H
#define	MAIN_H
#include "IModule.hpp"

#ifdef	__cplusplus
extern "C" {
#endif
vle::IModule* loadModuleLib(const char* path, const char* name);
bool fileExists(std::string strFilename);


#ifdef	__cplusplus
}
#endif

#endif	/* MAIN_H */

