#!/bin/bash
export props="app_dev.properties"
/usr/lib/jvm/java-8-oracle/bin/java -cp "/root/larditrans/lib/*:/root/larditrans/lorditransparser" com.yh.lt.Main > /root/larditrans/output.log