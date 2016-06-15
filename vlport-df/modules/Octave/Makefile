
SUBDIRS=DirectoryReader ImageCollector RGB2rgb rgb2c1c2c3 rgb2I1I2I3 HistogramDifference Results Parameters
MAKE=make
GFLAGS=-fPIC

all:
	@for i in $(SUBDIRS); do \
	echo "make all in $$i..."; \
	(cd $$i; $(MAKE)); done
