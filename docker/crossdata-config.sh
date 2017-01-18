#!/bin/bash -xe

function setCrossdataDir() {
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_HOSTNAME=$1
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_PORT=$2
    export CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES="akka.tcp://CrossdataServerCluster@$1:$2"
}

function setCrossdataBindHost() {
    #Bind address for host machine (In host is also the host machine. In bridge we need to put the internal of the docker: TODO)
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_BIND_HOSTNAME=$1
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_BIND_PORT=$2
}

function setHazelcastConfig() {
    sed -i "s|<member>127.0.0.1</member>|<member>$1:$2</member>|" /etc/sds/crossdata/server/hazelcast.xml
}

function setDriverConfig() {
    export crossdata_driver_config_cluster_hosts="[$1:$2]"
}

function standaloneConfig() {
    export AKKAIP="akka.tcp://CrossdataServerCluster@${DOCKER_HOST}:13420"
    #TODO: Test instead of XD_SEED : CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES
    if [ -z "$XD_SEED" ]; then
     export CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES=${AKKAIP}
    else
     export SEED_IP="akka.tcp://CrossdataServerCluster@${XD_SEED}:13420"
     export CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES=${SEED_IP},${AKKAIP}
     # TODO: Study whether it is worth of making hazelcast nodes available when auto discovery is disabled.
     #       If so, find a better way of editing hazelcast.xml. The method commented below is as flimsy as it gets.
     #sed -i "s|<member>127.0.0.1</member>|<member>${XD_SEED}</member>|" /etc/sds/crossdata/server/hazelcast.xml
    fi

    #TODO: Check environment vars for hostname and bind hostname & ports
    if [ -n "$CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_HOSTNAME" ]; then
        setHazelcastConfig ${CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_HOSTNAME} 5701
    else
        export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_HOSTNAME=${DOCKER_HOST}
    fi

    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_BIND_HOSTNAME=${DOCKER_HOST}

    if [ -z "$XD_SEED" ]; then
         export crossdata_driver_config_cluster_hosts="\[${DOCKER_HOST}:13420\]"
    else
         export crossdata_driver_config_cluster_hosts="\[${DOCKER_HOST}:13420, ${XD_SEED}\]"
    fi
}

function marathonConfig() {

    ####################################################
    #Memory
    ####################################################
    export RAM_AVAIL=$(echo $MARATHON_APP_RESOURCE_MEM | cut -d "." -f1)
    export CROSSDATA_JAVA_OPT="-Xmx${RAM_AVAIL}m -Xms${RAM_AVAIL}m"
    sed -i "s|# CROSSDATA_LIB|#CROSSDATA_JAVA_OPTS\nCROSSDATA_JAVA_OPTS=\"${CROSSDATA_JAVA_OPT}\"\n# CROSSDATA_LIB|" /etc/sds/crossdata/server/crossdata-env.sh

    #Spark UI port
    export CROSSDATA_SERVER_CONFIG_SPARK_UI_PORT=${PORT_4040}


    ########################################################################################################
    # Working in HTTP mode
    # Scenary: HAProxy exposing Akka http port, and creating an internal cluster using netty and
    #          autodiscovery through Zookeeper
    ########################################################################################################
    setCrossdataDir ${HOST} ${PORT_13420}
    setCrossdataBindHost ${HOST} ${PORT_13420}
    setHazelcastConfig ${HOST} ${PORT_5701}
    setDriverConfig ${HOST} ${PORT_13420}
    # CROSSDATA_SERVER_CONFIG_HTTP_SERVER_PORT is set with the port provided by Marathon-LB
    export CROSSDATA_SERVER_CONFIG_HTTP_SERVER_PORT=$PORT_13422

}

####################################################
## Main
####################################################
if [ -z ${MARATHON_APP_ID} ]; then
    standaloneConfig
else
    marathonConfig
fi