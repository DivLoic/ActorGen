#!/bin/bash

# The directory in which the app is installed
APPLICATION_DIR="/opt/actor-generator"
# The fat jar containing the app
APPLICATION_JAR="actorgen-assembly.jar"
# The class path with all dependencies
CLASS_PATH="$APPLICATION_DIR/conf/"
# The name of the class to run
CLASS_NAME="org.lmd.xke.kudu.ActorKudu"
# The app arguments such as <Class> foo bar ...
APPLICATION_ARGS=""
# The system properties (-Dfoo=bar).
ENV_OPTS=""
# The Java command to use to launch the application (must be java 8+)
CMD=/usr/bin/java

# ***********************************************
OUT_FILE="${APPLICATION_DIR}"/out.log
RUNNING_PID="${APPLICATION_DIR}"/RUNNING_PID
# ***********************************************

# colors
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }

# Check whether the application is running.
isrunning() {
  # Check for running app
  if [ -f "$RUNNING_PID" ]; then
    proc=$(cat $RUNNING_PID);
    if /bin/ps --pid $proc 1>&2 >/dev/null;
    then
      return 0
    fi
  fi
  return 1
}

start() {
  if isrunning; then
    echoYellow "The application is running."
    return 0
  fi

  pushd $APPLICATION_DIR > /dev/null
  echo "$CMD -cp $CLASS_PATH $ENV_OPTS -jar $APPLICATION_JAR $CLASS_NAME $APPLICATION_ARGS"
  nohup $CMD $ENV_OPTS -cp $CLASS_PATH:$APPLICATION_JAR $CLASS_NAME $APPLICATION_ARGS > $OUT_FILE 2>&1 &
  echo $! > ${RUNNING_PID}
  popd > /dev/null

  if isrunning; then
    echoGreen "The application started"
    exit 0
  else
    echoRed "The application has not started - check log"
    exit 3
  fi
}

restart() {
  echo "Restarting the app."
  stop
  start
}

stop() {
  echoYellow "Stopping the app."
  if isrunning; then
    kill `cat $RUNNING_PID`
    rm $RUNNING_PID
  fi
}

status() {
  if isrunning; then
    echoGreen "the app is running."
  else
    echoRed "the app is either stopped or inaccessible."
  fi
}

case "$1" in
start)
    start
;;

status)
   status
   exit 0
;;

stop)
    if isrunning; then
	stop
	exit 0
    else
	echoRed "The app is not running"
	exit 3
    fi
;;

restart)
    stop
    start
;;

*)
    echo "Usage: $0 {status|start|stop|restart}"
    exit 1
esac