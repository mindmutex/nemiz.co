#!/bin/bash


RELEASE=AWS-ElasticBeanstalk-CLI-2.6.4
ElasticBeanstalk=https://s3.amazonaws.com/elasticbeanstalk/cli/$RELEASE.zip

TARGET=.environment

if [ ! -d "$TARGET" ]; then
	mkdir $TARGET
	wget $ElasticBeanstalk -O $TARGET/elasticbeanstalk.zip
	unzip $TARGET/elasticbeanstalk.zip -d $TARGET
	mv $TARGET/$RELEASE/* $TARGET
	rm -fr $TARGET/$RELEASE/ $TARGET/elasticbeanstalk.zip
fi

ABSOLUTE_TARGET=`readlink -f $TARGET`

env PATH=$PATH:$ABSOLUTE_TARGET/eb/linux/python2.7/ bash


