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
    export crossdata_driver_config_cluster_hosts=[$1:$2]
}

function standaloneConfig() {
    AKKAIP="akka.tcp://CrossdataServerCluster@${DOCKER_HOST}:13420"
    #TODO: Test instead of XD_SEED : CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES
    if [ -z ${XD_SEED} ]; then
     export CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES=${AKKAIP}
     sed -i "s|<member>127.0.0.1</member>|<member>${DOCKER_HOST}</member>|" /etc/sds/crossdata/server/hazelcast.xml
    else
     SEED_IP="akka.tcp://CrossdataServerCluster@${XD_SEED}:13420"
     export CROSSDATA_SERVER_AKKA_CLUSTER_SEED_NODES=${SEED_IP},${AKKAIP}
     sed -i "s|<member>127.0.0.1</member>|<member>${XD_SEED}</member>|" /etc/sds/crossdata/server/hazelcast.xml
    fi

    #TODO: Check environment vars for hostname and bind hostname & ports
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_HOSTNAME=${DOCKER_HOST}
    export CROSSDATA_SERVER_AKKA_REMOTE_NETTY_TCP_BIND_HOSTNAME=${DOCKER_HOST}
    if [ -z ${XD_SEED} ]; then
         crossdata_driver_config_cluster_hosts=[${DOCKER_HOST}:13420]
    else
         crossdata_driver_config_cluster_hosts=[${DOCKER_HOST}:13420, ${XD_SEED}]
    fi
}

function marathonConfig() {

    ####################################################
    #Memory
    ####################################################
    RAM_AVAIL=$(echo $MARATHON_APP_RESOURCE_MEM | cut -d "." -f1)
    CROSSDATA_JAVA_OPT="-Xmx${RAM_AVAIL}m -Xms${RAM_AVAIL}m"
    sed -i "s|# CROSSDATA_LIB|#CROSSDATA_JAVA_OPTS\nCROSSDATA_JAVA_OPTS=\"${CROSSDATA_JAVA_OPT}\"\n# CROSSDATA_LIB|" /etc/sds/crossdata/server/crossdata-env.sh

    #Spark UI port
    export CROSSDATA_SERVER_CONFIG_SPARK_UI_PORT=${PORT_4040}


    ########################################################################################################
    #If XD_EXTERNAL_IP and MARATHON_APP_LABEL_HAPROXY_1_PORT are not specified assume we are working in HTTP mode
    #Scenary: HAProxy exposing Akka http port, and creating an internal cluster using netty and autodiscovery through Zookeeper
    ########################################################################################################
    if [ -z ${XD_EXTERNAL_IP} ] && [ -z ${MARATHON_APP_LABEL_HAPROXY_1_PORT} ]; then
        setCrossdataDir ${HOST} ${PORT_13420}
        setCrossdataBindHost ${HOST} ${PORT_13420}
        setHazelcastConfig ${HOST} ${PORT_5701}
        setDriverConfig ${HOST} ${PORT_13420}
        # CROSSDATA_SERVER_CONFIG_HTTP_SERVER_PORT is set with the port provided by Marathon-LB
        export CROSSDATA_SERVER_CONFIG_HTTP_SERVER_PORT=$PORT_13422
    else
        #Scenary: HAProxy exposing the akka netty port with the external IP. Supported only for one instance of Crossdata
        if [ -z ${XD_EXTERNAL_IP} ] || [ -z ${MARATHON_APP_LABEL_HAPROXY_1_PORT} ]; then
            echo "ERROR: Env var XD_EXTERNAL_IP and label HAPROXY_1_PORT must be provided together using Marathon&Haproxy in TCP mode" 1>&2
            exit 1 # terminate and indicate error
        else
            #Hostname and port of haproxy
            setCrossdataDir ${XD_EXTERNAL_IP} ${MARATHON_APP_LABEL_HAPROXY_1_PORT}
            #Bind address for local
            setCrossdataBindHost ${DOCKER_HOST} ${PORT_13420}
            #Driver
            setDriverConfig ${XD_EXTERNAL_IP} ${MARATHON_APP_LABEL_HAPROXY_1_PORT}
        fi
        # When using ClusterClient External IP, the hosts-files get updated in order to keep a consistent
        # binding address in AKKA.
        NAMEADDR="$(hostname -i)"
        if [ -n "$HAPROXY_SERVER_INTERNAL_ADDRESS" ]; then
          NAMEADDR=$HAPROXY_SERVER_INTERNAL_ADDRESS
        fi
        echo -e "$NAMEADDR\t$XD_EXTERNAL_IP" >> /etc/hosts
    fi
}

####################################################
## Main
####################################################

if [ -z ${MARATHON_APP_ID} ]; then
    standaloneConfig
else
    marathonConfig
fi