#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.
AC_PREREQ(2.50)
AC_INIT([TessAutoMake],[1.0],[minhhai3b@gmail.com])
AC_PREFIX_DEFAULT(/usr/local)

# AC_CONFIG_SRCDIR([src/hello.c])
# AC_CONFIG_HEADER([config.h])
 
# Checks for programs.
AC_PROG_CXX
AC_PROG_CC
 
# Checks for libraries.
 
# Checks for header files.
 
# Checks for typedefs, structures, and compiler characteristics.
 
# Checks for library functions.

# Checks for programs.
AC_CANONICAL_SYSTEM
AC_PROG_LIBTOOL
AM_INIT_AUTOMAKE([-Wall -Werror foreign])

AC_SUBST(GENERIC_MAJOR_VERSION)
AC_SUBST(GENERIC_MINOR_VERSION)
AC_SUBST(GENERIC_MICRO_VERSION)
AC_SUBST(GENERIC_LIBRARY_VERSION)

GENERIC_MAJOR_VERSION=1
GENERIC_MINOR_VERSION=0
GENERIC_MICRO_VERSION=0

GENERIC_LIBRARY_VERSION=$GENERIC_MAJOR_VERSION:$GENERIC_MINOR_VERSION

LT_INIT
#AC_PROG_CXX

# Check location of jni.h/jni_md.h headers.
AC_MSG_CHECKING(for jni.h)
AC_ARG_VAR(JAVA_HOME_INCLUDE,[JAVA_HOME headers directory])

if test "$JAVA_HOME_INCLUDE" = "" ; then
  JAVA_HOME_INCLUDE="$JAVA_HOME/include"
fi

if test -r "$JAVA_HOME_INCLUDE/jni.h" ; then
      CPPFLAGS="$CPPFLAGS -I$JAVA_HOME_INCLUDE"
      AC_MSG_RESULT(yes)
else
	  AC_MSG_ERROR([jni.h not found])
fi

AC_MSG_CHECKING(for jni_md.h)
if test -r "$JAVA_HOME_INCLUDE/jni_md.h" ; then
      AC_MSG_RESULT(yes)
else
	if test -r "$JAVA_HOME_INCLUDE/linux/jni_md.h" ; then
		CPPFLAGS="$CPPFLAGS -I$JAVA_HOME_INCLUDE/linux"
      	AC_MSG_RESULT(yes)
    else  	
	  AC_MSG_ERROR([jni_md.h not found])
	fi  
fi

# Check location of leptonica/liblept headers.
AC_MSG_CHECKING(for leptonica)
AC_ARG_VAR(LIBLEPT_HEADERSDIR,[Leptonica headers directory])

have_lept=no
if test "$LIBLEPTONICA_HEADERSDIR" = "" ; then
  LIBLEPTONICA_HEADERSDIR="/usr/local/include /usr/include"
fi
for incd in $LIBLEPTONICA_HEADERSDIR
do
  for lept in . leptonica liblept
  do
    if test -r "$incd/$lept/allheaders.h" ; then
      CPPFLAGS="$CPPFLAGS -I$incd/$lept"
      have_lept=yes
    fi
  done
done
if test "$have_lept" = yes ; then
  AC_MSG_RESULT(yes)
  AC_CHECK_LIB(lept,pixCreate,[], AC_MSG_ERROR([leptonica library missing]))
else
  AC_MSG_ERROR([leptonica not found])
fi


# Check location of tesseract/libtesseract headers.
AC_MSG_CHECKING(for tesseract)
AC_ARG_VAR(LIBTESS_HEADERSDIR,[Tesseract headers directory])

have_tess=no
if test "$LIBTESSERACT_HEADERSDIR" = "" ; then
  LIBTESSERACT_HEADERSDIR="/usr/local/include /usr/include"
fi
for incd in $LIBTESSERACT_HEADERSDIR
do
  for tess in . tesseract libtesseract
  do
    if test -r "$incd/$tess/baseapi.h" ; then
      CPPFLAGS="$CPPFLAGS -I$incd/$tess"
      have_tess=yes
    fi
  done
done
if test "$have_tess" = yes ; then
  AC_MSG_RESULT(yes)
  AC_CHECK_LIB(tesseract,main,[], AC_MSG_ERROR([tesseract library missing]))
else
  AC_MSG_ERROR([tesseract not found])
fi


AC_CONFIG_FILES(Makefile src/Makefile)
AC_OUTPUT

