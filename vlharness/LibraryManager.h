/* 
 * File:   LibraryManager.h
 * Author: reggie
 *
 * Created on April 28, 2011, 11:39 AM
 */

#ifndef LIBRARYMANAGER_H
#define	LIBRARYMANAGER_H

#include "main.h"

using namespace std;
using namespace vle;

namespace vle{


class LibraryManager {
public:
    struct Library{
        std::string name;
        std::string url;
        std::string protocol;
        std::string param;
    };

private:
    map<std::string, Library*, less <std::string> > libraryMap;

public:
    //LibraryManager(){};
    //LibraryManager(const LibraryManager& orig);
    //virtual ~LibraryManager(){};

    void addLibrary(Library* library){
        libraryMap[library->name] = library;
    }

    Library* getLibrary(std::string name){
        if(libraryMap[name] != NULL){
            return libraryMap[name];
        }else
            return NULL;
    }

   /* Library* searchModule(std::string moduleName){
        map<std::string, Library*, less <std::string> >::iterator itr;
        for(itr = libraryMap.begin(); itr != libraryMap.end(); ++itr){
            Library* library = itr->second;

            if(library->protocol.compare("file") == 0){
                std::string path(library->param+"/lib"+moduleName+".so");
                if(fileExists(path) == true){
                    return library;
                }
            }
        }
    }//searchModule*/

    std::string* searchModule(std::string moduleName){
        map<std::string, Library*, less <std::string> >::iterator itr;
        for(itr = libraryMap.begin(); itr != libraryMap.end(); ++itr){
            Library* library = itr->second;

            if(library->protocol.compare("file") == 0){
                std::string *path = new std::string();
                path->assign(library->param+"/lib"+moduleName+".so");
                if(fileExists(*path) == true){
                    return path;
                }
            }
        }
        return NULL;
    }//searchModule

};
}
#endif	/* LIBRARYMANAGER_H */

