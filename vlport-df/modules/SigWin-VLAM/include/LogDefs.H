//*****************************************************************************
/** \File LogDefs.H
 *  \brief Defines loging level for different modules. 
 * 
 *<!------------------------------------------------------------------------->
 * \version 1.0 
 * \since September 15, 2006.
 * \author  Marcia A. Inda\n
 *  	   Integrative Bioinformatics Unit, UvA\n
 *         http://staff.science.uva.nl/~inda
 */
 /*---------------------------------------------------------------------------
 *  History:
 *  Created: September 15, 2006
 *---------------------------------------------------------------------------
 *  	This work was carried out in the context of the Virtual Laboratory for
 *  e-Science project (http://www.vl-e.nl/) and of the BioRange program of the
 *  Netherlands Bioinformatics Centre (NBIC, http://www.nbic.nl/). VL-e is 
 *  supported by a BSIK grant from the Dutch Ministry of Education, Culture and
 *  Science (OC&W) and the ICT innovation program of the Ministry of Economic
 *  Affairs (EZ) of the Netherlands. BioRange is supported by a BSIK grant
 *  through the Netherlands Genomics Initiative (NGI).
 ****************************************************************************/
#ifndef LOGDEFSH_
#define LOGDEFSH_

#define DEFAULTLOGLEVEL Timing

namespace Log{
    namespace {const std::string DefaultLogDir="myvlamlogs";}
}
#endif /*LOGDEFSH_*/
