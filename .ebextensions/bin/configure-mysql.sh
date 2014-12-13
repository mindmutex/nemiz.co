#!/bin/bash

echo `pwd` >> /tmp/x

MYSQL_STATUS=`/sbin/service mysqld status | grep stopped | wc -l`
if [ "$MYSQL_STATUS" = "1" ]; then
	/sbin/service mysqld start

	echo "create database symfony" | /usr/bin/mysql -u root

	
	/usr/bin/mysql -u root symfony < .ebextensions/sql/init.sql
	/usr/bin/mysqladmin -u root password "$SYMFONY__DATABASE__PASSWORD"
fi

exit 0
