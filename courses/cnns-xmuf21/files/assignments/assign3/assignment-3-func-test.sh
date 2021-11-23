#!/bin/bash

#Get Method
curl --http1.0 http://192.168.0.120:80

#ServerName
curl --http1.0 --resolve ApacheServer:80:192.168.0.120 Http://ApacheServer/cgi-enabled/

#Last-Modified
curl --http1.0 -I http://baidu.com

#If-Modified-Since
curl --http1.0 --header 'If-Modified-Since: Wed, 27 Oct 2021 19:06:23 GMT' http://192.168.0.120:6789

#User-Agent
curl --http1.0 --user-agent "Mozilla/5.0(iPhone)" http://192.168.0.120:6789/youfile

#CGI
curl --http1.0 http://192.168.0.120:80/cgi-enabled/get_cgi_env.py
curl --http1.0 http://192.168.0.120:80/cgi-enabled/get_cgi_env.py?111

#Heartbeat Monitoring

errorCode=503
for line in $(cat requests.txt)
do
        echo "$line"
	content="$(curl -sLI http://192.168.0.120:80/doc-root/$line | grep HTTP/1.1 | tail -1 | awk {'print $2'})"  #Get Response Code (200 or 503)

        echo "$content"
        if [[ $content == $errorCode ]]
        then
                echo "503"
                break
        fi
done


#Performance Benchmarking using requests.txt(request-patterns)
< requests.txt xargs -r -L 1 -P 1 curl -O --http1.0

