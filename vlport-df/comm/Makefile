SUBDIRS=files gsiftp pgsiftp net pipes raw
MAKE=make
GFLAGS=-fPIC

all:
	@for i in $(SUBDIRS); do \
	echo "make all in $$i..."; \
	(cd $$i; $(MAKE)); done
