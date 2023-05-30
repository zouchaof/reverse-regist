#!/bin/bash

appName=register-server-web/target/register-server-web.jar
pid=`ps -ef|grep $appName|grep -v grep|awk '{print $2}'`
if [ -n "$pid" ]
then
	    echo "kill -9 pid:" $pid
	        kill -9 $pid
		    sleep 1
fi
nohup java -jar -Dserver.port=8080 $appName >nohup.out 2>&1 &

