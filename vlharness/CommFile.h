/* 
 * File:   CommFile.h
 * Author: reggie
 *
 * Created on May 3, 2011, 11:11 AM
 */

#ifndef COMMFILE_H
#define	COMMFILE_H

#include <malloc.h>
using namespace std;

namespace vle{


class CommFile{
public:
    static std::string* getAsciiData(const std::string& path){
        std::ifstream st(path.c_str(), ios::in);
        std::string *msg = new std::string();
        st.seekg(0,std::ios::end);
        msg->reserve(st.tellg());
        st.seekg(0,std::ios::beg);
        msg->assign((std::istreambuf_iterator<char>(st)), std::istreambuf_iterator<char>());
        st.close();       
        return msg;
    }

    //void* getBinaryData(const std::string& path){
     //   std::ifstream st(path.c_str(), ios::in | ios::binary);
    //    st.seekg(0, std::ios::end);
    //    void* ptr = (char*)malloc(st.tellg()+1);
    //    st.seekg(0,std::ios::beg);
    //    st.read((char *)ptr,st.tellg());
   //     st.close();
    //    return ptr;
   // }
};

}

#endif	/* COMMFILE_H */

