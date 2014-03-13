#!/bin/sh

#if [ ! -d m4 ]; then
#mkdir m4
#fi

bail_out()
{
	echo 
	echo "  Something went wrong, bailing out!" 
	echo 
	exit 1
}

echo "Installing GNU Auto Makefile"
 autoreconf --install || bail_out

echo "Running aclocal"
aclocal || bail_out


echo "Running libtoolize"
libtoolize -f -c || glibtoolize -f -c || bail_out
libtoolize --automake || glibtoolize --automake || bail_out


echo "Running automake --add-missing --copy"
automake --add-missing -c  -Wno-portability > /dev/null || bail_out


echo "Running autoconf"
autoconf || bail_out

echo ""
echo "All done."
echo "To build the software now, do something like:"
echo ""
echo "$ ./configure [--with-debug] [...other options]"
echo "$ make"
