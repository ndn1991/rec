kill -9 `ps -ef | grep -v grep | grep -i logreceiver | awk '{print $2}'`