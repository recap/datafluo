#include <stdint.h>
#include <iostream>
#include <boost/iostreams/device/array.hpp>
#include <boost/iostreams/stream.hpp>
#include <boost/archive/binary_iarchive.hpp>

int main()
{
    uint16_t data[] = {1234, 5678};
    char* dataPtr = (char*)&data;
	int dataI[32];
	int* ptr = (int *)&dataI;

    typedef boost::iostreams::basic_array_source<char> Device;
    boost::iostreams::stream_buffer<Device> buffer(dataPtr, sizeof(data));
    boost::archive::binary_iarchive archive(buffer, boost::archive::no_header);
    typedef boost::iostreams::basic_array_source<int> DeviceI;
    boost::iostreams::stream_buffer<DeviceI> bufferI(ptr, sizeof(dataI));
    boost::archive::binary_iarchive archiveI(bufferI, boost::archive::no_header);

    uint16_t word1, word2;
	archiveI << 2 << 3;
    archive >> word1 >> word2;
    std::cout << word1 << "," << word2 << std::endl;
    return 0;
}
