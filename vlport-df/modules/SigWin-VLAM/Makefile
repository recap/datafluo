
SUBDIRS=ColumnReader Sample2Freq SigWinSelect SWMedianProb FDRTHreshold Rank SigWinPlotGrace SWMedian
MAKE=make
GFLAGS=-fPIC

all:
	@for i in $(SUBDIRS); do \
	echo "make all in $$i..."; \
	(cd $$i; $(MAKE)); done
