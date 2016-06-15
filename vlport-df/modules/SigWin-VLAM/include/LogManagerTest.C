/*****************************************************************************
/** \file LogManagerTEST.C
 *  \brief Test LogManager. 
 */
/*---------------------------------------------------------------------------
 *  Created:  November 11, 2005      Last Update: November 11, 2005
 *---------------------------------------------------------------------------
 *  Author: Marcia Alves de Inda                                             
 *****************************************************************************/
#include "LogManagerNew.H"

int main()
{

    Log::print(NoWarn) << "NoWarn: This shouldn't be printed ??????????\n";
    Log::print(Fatal) << "Fatal: This should be printed\n";
    Log::print(Info) << "Info: This should be printed\n";
    Log::print(Debug) << "Debug: This shouldn't be printed ????????????????\n";

    Log::set_warn_level(NoWarn);
    Log::print(NoWarn) << "NoWarn: This shouldn't be printed ??????????\n";
    Log::print(Fatal) << "Fatal: This shouldn't be printed ??????????\n";
    Log::print(Info) << "Info: This shouldn't be printed ??????????\n";
    Log::print(Debug) << "Debug: This shouldn't be printed ????????????????\n";

    ofstream logFile("LogFile0.log");
    
    Log::init(&logFile, Debug);
    
    Log::print(NoWarn) << "NoWarn: This shouldn't be printed ??????????\n";
    Log::print(Fatal) << "Fatal: This should be printed\n";
    Log::print(Info) << "Info: This should be printed\n";
    Log::print(Debug) << "Debug: This should be printed\n";
    
    logFile.close();
    
    Log::open("LogFile1.log", Error);
    Log::ptag(Info)<< "????????????????????\n";
    Log::ptag(Error) << "This is not realy an error\n";
    
    Log::set_warn_level(Debug);
    Log::ptag(Info) << "The next shouldn't work\n";
    Log::open("LogFile2.log", Debug);
    Log::print(Info)<<"Log file is now LogFile2\n";
    Log::close();
    Log::ptag(Info) << "Now things should work\n";
    Log::open("LogFile3.log", Debug);
    Log::ptag(Info) << "Printing to logfile 3\n";
    Log::close();
    
    Log::init(&cout, Info);
    Log::ptag(Info) << "Back to cout\n";
    
}
