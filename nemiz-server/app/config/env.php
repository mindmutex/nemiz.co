<?php

$container->setParameter('rds.port', getenv('RDS_PORT'));
$container->setParameter('rds.host', getenv('RDS_HOSTNAME'));
$container->setParameter('rds.database', getenv('RDS_DB_NAME'));

$container->setParameter('rds.username', getenv('RDS_USERNAME'));
$container->setParameter('rds.password', getenv('RDS_PASSWORD'));
