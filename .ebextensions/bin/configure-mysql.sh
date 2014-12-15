#!/bin/bash

echo `pwd` >> /tmp/x

MYSQL_STATUS=`/sbin/service mysqld status | grep stopped | wc -l`
if [ "$MYSQL_STATUS" = "1" ]; then
	/sbin/service mysqld start
	/usr/bin/mysqladmin -u root password "$SYMFONY__DATABASE__PASSWORD"
fi

exit 0
