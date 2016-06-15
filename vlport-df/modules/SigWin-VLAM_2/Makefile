
SUBDIRS=ColumnReader Sample2Freq SigWinSelect SWMedianProb FDRTHreshold Rank SigWinPlotGrace SWMedian
MAKE=make

all:
	@for i in $(SUBDIRS); do \
	echo "make all in $$i..."; \
	(cd $$i; $(MAKE)); done
