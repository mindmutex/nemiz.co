#!/bin/bash

# remove the android and and sounds
rm -fr nemiz-android-app/ nemiz-sounds/

# move nemiz-server to root
shopt -s dotglob
mv nemiz-server/* .
rm -fr nemiz-server
shopt -u dotglob

exit 0
