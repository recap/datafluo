//
// File: serializer.h
// Created by: dmitry <dvasunin@science.uva.nl>
// Created on: Tue May 13 19:10:01 2003
//

#include "serializer.h"

class Dynbuffer
{
	public:
		Dynbuffer(size_t size)
		{
			buf = new char[size];
		};
		~Dynbuffer()
		{
			delete[] buf;
		}
		char *buf;	
};


Serializer& operator <<(std::ostream& os, Serializer &xdrser)
{
    xdrser.os = &os;
    return xdrser;
};

Serializer& operator >>(std::istream& os, Serializer &xdrser)
{
    xdrser.is = &os;
    return xdrser;
};


//Long

Serializer& Serializer::operator << (long arg)
{
    char buf[XDRLongSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRLongSize, XDR_ENCODE);
    if(!xdr_long(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRLongSize); 
	if(!*os)
        throw EOFException();    
    return *this;
}

Serializer& Serializer::operator >> (long &arg)
{
    char buf[XDRLongSize];
    is->read(buf, XDRLongSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRLongSize, XDR_DECODE);
    if(!xdr_long(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

//Unsigned long 

Serializer& Serializer::operator << (unsigned long arg)
{
    char buf[XDRUnsignedLongSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRUnsignedLongSize, XDR_ENCODE);
    if(!xdr_u_long(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRUnsignedLongSize); 
	if(!*os)
        throw EOFException();
    return *this;
}

Serializer& Serializer::operator >> (unsigned long &arg)
{
    char buf[XDRUnsignedLongSize];
    is->read(buf, XDRUnsignedLongSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRUnsignedLongSize, XDR_DECODE);
    if(!xdr_u_long(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

// Integer

Serializer& Serializer::operator << (int arg)
{
    char buf[XDRIntSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRIntSize, XDR_ENCODE);
    if(!xdr_int(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRIntSize); 
	if(!*os)
        throw EOFException();   
    return *this;
}

Serializer& Serializer::operator >> (int &arg)
{
    char buf[XDRIntSize];
    is->read(buf, XDRIntSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRIntSize, XDR_DECODE);
    if(!xdr_int(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

//Unsigned int 

Serializer& Serializer::operator << (unsigned int arg)
{
    char buf[XDRUnsignedIntSize];
	if(!*os)
        throw EOFException();
        
    xdrmem_create(&xdrs, buf, XDRUnsignedIntSize, XDR_ENCODE);
    if(!xdr_u_int(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRUnsignedIntSize); 
	if(!*os)
        throw EOFException();    
    return *this;
}

Serializer& Serializer::operator >> (unsigned int &arg)
{
    char buf[XDRUnsignedIntSize];
    is->read(buf, XDRUnsignedIntSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRUnsignedIntSize, XDR_DECODE);
    if(!xdr_u_int(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}


//Short

Serializer& Serializer::operator << (short arg)
{
    char buf[XDRShortSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRShortSize, XDR_ENCODE);
    if(!xdr_short(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRShortSize); 
	if(!*os)
        throw EOFException();    
    return *this;
}

Serializer& Serializer::operator >> (short &arg)
{
    char buf[XDRShortSize];
    is->read(buf, XDRShortSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRShortSize, XDR_DECODE);
    if(!xdr_short(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

//Unsigned short

Serializer& Serializer::operator << (unsigned short arg)
{
    char buf[XDRUnsignedShortSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRUnsignedShortSize, XDR_ENCODE);
    if(!xdr_u_short(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRUnsignedShortSize); 
	if(!*os)
        throw EOFException();    
    return *this;
}

Serializer& Serializer::operator >> (unsigned short &arg)
{
    char buf[XDRUnsignedShortSize];
    is->read(buf, XDRUnsignedShortSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRUnsignedShortSize, XDR_DECODE);
    if(!xdr_u_short(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

//Double

Serializer& Serializer::operator << (double arg)
{
    char buf[XDRDoubleSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRDoubleSize, XDR_ENCODE);
    if(!xdr_double(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    	
    os->write(buf, XDRDoubleSize); 
	if(!*os)
        throw EOFException();
    return *this;
}

Serializer& Serializer::operator >> (double &arg)
{
    char buf[XDRDoubleSize];
    is->read(buf, XDRDoubleSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRDoubleSize, XDR_DECODE);
    if(!xdr_double(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}

//Float

Serializer& Serializer::operator << (float arg)
{
    char buf[XDRFloatSize];
	if(!*os)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRFloatSize, XDR_ENCODE);
    if(!xdr_float(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    os->write(buf, XDRFloatSize); 
	if(!*os)
        throw EOFException();    
    return *this;
}

Serializer& Serializer::operator >> (float &arg)
{
    char buf[XDRFloatSize];
    is->read(buf, XDRFloatSize);
    if(!*is)
        throw EOFException();
    xdrmem_create(&xdrs, buf, XDRFloatSize, XDR_DECODE);
    if(!xdr_float(&xdrs, &arg))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();
    }
    xdr_destroy(&xdrs);	    
    return *this;
}


//String

Serializer& Serializer::operator << (const std::string &arg)
{
	if(!*os)
        throw EOFException();
	
    unsigned int size = arg.length();
    
    char  *stringToSerializeP = const_cast<char*>(arg.c_str()); 
    unsigned int stringBufSize = xdr_sizeof((xdrproc_t)xdr_string, &stringToSerializeP);
	
	Dynbuffer buf(stringBufSize); 

    xdrmem_create(&xdrs, buf.buf, stringBufSize, XDR_ENCODE);
    if(!xdr_string(&xdrs, &stringToSerializeP, size))
    {
        xdr_destroy(&xdrs);		
        throw XDRException();
    }  
    operator <<(stringBufSize);
    os->write(buf.buf, stringBufSize); 
    xdr_destroy(&xdrs);	    
   	if(!*os)
        throw EOFException();
    return *this;
}


Serializer& Serializer::operator >> (std::string &arg)
{
    unsigned int stringBufSize;
    unsigned int stringSize;
     
    operator >>(stringBufSize);
    Dynbuffer buf(stringBufSize + 1); 
    is->read(buf.buf, stringBufSize);
    if(!*is)
    {
        throw EOFException();
    }

    xdrmem_create(&xdrs, buf.buf, stringBufSize, XDR_DECODE);
    if(!xdr_u_int(&xdrs, &stringSize))
    {
        xdr_destroy(&xdrs);	
        throw XDRException();  
    }
    buf.buf[XDRUnsignedIntSize + stringSize + 1] = 0;
    arg = (buf.buf + XDRUnsignedIntSize);
    
    xdr_destroy(&xdrs);	    
    return *this;
}

Serializer& Serializer::operator << (std::istream &argis)
{
/*
	if(!argis.is_open())
	{
		throw EOFException();
	}
*/
	if(!*os)
        throw EOFException();
	argis.seekg (0, std::ios::end);
	unsigned int length = argis.tellg();
	argis.seekg (0, std::ios::beg);

    operator <<(length);
	*os << argis.rdbuf();
   	if(!*os)
	{
/*	    std::cerr << "os.rdstate()=" << 
		((os->rdstate() == std::ostream::eofbit) ? "EOF": 
		((os->rdstate() == std::ostream::failbit) ? "failbit":
		((os->rdstate() == std::ostream::badbit) ? "badbit": "SOMETHING"
		))) << std::endl;
*/
    	    throw EOFException();
	}
	return *this;
}

Serializer& Serializer::operator >> (std::ostream &argos)
{
/*	if(!argos.is_open())
	{
		throw EOFException();
	}
*/	
	unsigned int length = 0;
	operator >>(length);
	char buf[PAGE_SIZE];
	do{
		unsigned int size = (length < PAGE_SIZE)?length:PAGE_SIZE;
		is->read(buf, size);
		if(!*is)
		{
			throw EOFException();
		}
		argos.write(buf, size);
		length -= size;
	}while(length != 0);
	
	return *this;
	
}

