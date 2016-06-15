#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=
AS=as

# Macros
CND_PLATFORM=GNU-Linux-x86
CND_CONF=Debug
CND_DISTDIR=dist

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/tinyxml.o \
	${OBJECTDIR}/main.o \
	${OBJECTDIR}/tinystr.o \
	${OBJECTDIR}/QueueProducer.o \
	${OBJECTDIR}/Harness.o \
	${OBJECTDIR}/tinyxmlerror.o \
	${OBJECTDIR}/Server.o \
	${OBJECTDIR}/ReplicationController.o \
	${OBJECTDIR}/QueueConsumer.o \
	${OBJECTDIR}/tinyxmlparser.o \
	${OBJECTDIR}/IModule.o


# C Compiler Flags
CFLAGS=

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=-Llib -Wl,-rpath /home/reggie/workspace/hg/datafluo/trunk/vlharness/lib -lactivemq-cpp -lapr-1 -laprutil-1 -lexpat -llog4cplus

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-Debug.mk dist/Debug/GNU-Linux-x86/vlharness

dist/Debug/GNU-Linux-x86/vlharness: ${OBJECTFILES}
	${MKDIR} -p dist/Debug/GNU-Linux-x86
	${LINK.cc} -lpthread -luuid -lrt -ldl -rdynamic -o ${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}/vlharness ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/tinyxml.o: tinyxml.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/tinyxml.o tinyxml.cpp

${OBJECTDIR}/main.o: main.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/main.o main.cpp

${OBJECTDIR}/tinystr.o: tinystr.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/tinystr.o tinystr.cpp

${OBJECTDIR}/QueueProducer.o: QueueProducer.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/QueueProducer.o QueueProducer.cpp

${OBJECTDIR}/Harness.o: Harness.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/Harness.o Harness.cpp

${OBJECTDIR}/tinyxmlerror.o: tinyxmlerror.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/tinyxmlerror.o tinyxmlerror.cpp

${OBJECTDIR}/Server.o: Server.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/Server.o Server.cpp

${OBJECTDIR}/ReplicationController.o: ReplicationController.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/ReplicationController.o ReplicationController.cpp

${OBJECTDIR}/QueueConsumer.o: QueueConsumer.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/QueueConsumer.o QueueConsumer.cpp

${OBJECTDIR}/tinyxmlparser.o: tinyxmlparser.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/tinyxmlparser.o tinyxmlparser.cpp

${OBJECTDIR}/IModule.o: IModule.cpp 
	${MKDIR} -p ${OBJECTDIR}
	${RM} $@.d
	$(COMPILE.cc) -g -Iincludes/activemq-cpp/main -Iincludes/apr -Iincludes/apr-util -Iincludes/logging -MMD -MP -MF $@.d -o ${OBJECTDIR}/IModule.o IModule.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r build/Debug
	${RM} dist/Debug/GNU-Linux-x86/vlharness

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
