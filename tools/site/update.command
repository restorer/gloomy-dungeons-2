#!/bin/bash

SITE=/Users/restorer/Sites/mobile-zamedev.local/gloomy-ii
SELF=`dirname "$0"`

if command -v realpath > /dev/null ; then
	SELF=`realpath "$SELF"`
else
	SELF="`pwd`/$SELF"
fi

pushd "$SELF/source" > /dev/null
[ "`ls * 2> /dev/null`" != "" ] && rm -r *
popd > /dev/null
pushd "$SITE" > /dev/null
[ "`ls * 2> /dev/null`" != "" ] && cp -r * "$SELF/source"
popd > /dev/null
