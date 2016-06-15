#ifndef VL_COMMDEFS_H_
#define VL_COMMDEFS_H_

#define REGISTER_LIB(NAME) \
extern "C" { IComm *cmaker(){  return new NAME; } \
class cproxy { \
public: \
   cproxy() { \
      gCommFactory[#NAME] = cmaker;  } \
}; \
cproxy cp; }
 
#endif /*VL_COMMDEFS_H_*/
