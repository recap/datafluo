#! /bin/sh
cp vlport2 myModule2/
cp InputFile.so myModule2/
cp OutputFile.so myModule2/
cp myModule2.so myModule2/
cp in_norm.txt myModule2/
cp mymodule2.conf myModule2/
tar -zcvf myModule2.tar.gz myModule2/
