#!/bin/bash
# Stratio Crossdata Deployment script

function usage {

  echo "Usage: ./make-distribution-crossdata.sh [OPTION]... [SPARK_BUILD_OPTIONS] "
  echo "Tool for build binary distributions of Spark with the Stratio Crossdata Pluggins"
  echo "Example: ./make-distribution-crossdata.sh --profile crossdata-cassandra --skip-java-test -Dhadoop.version=2.4.0"
  echo ""
  echo "--profile            Crossdata Build Profile, Default: crossdata-cassandra. Options: crossdata-core, crossdata-all, crossdata-hive, crossdata-cassandra"
  echo "--sparkRepo          Github repository used to download Official Spark Distribution. Default: https://github.com/apache/spark.git"
  echo "--sparkBranch        Github branch or tag used to build the Spark Distribution. Default: tags/v1.4.1"
  echo ""
  echo "[SPARK_BUILD_OPTIONS]"
  echo "Spark's specific Build options."
  echo "See Spark's \"Building Spark\" doc for correct Maven options at http://spark.apache.org/docs/latest/building-spark.html"
  echo ""
  exit 1
}

if [ -z "$JAVA_HOME" ]; then
    echo Error: JAVA_HOME is not set, cannot proceed.
    exit 1
fi

# Keep all the arguments, then remove the XD specific ones and only keep the Spark arguments.
SPARK_BUILD_OPTIONS="$@"

while [[ $# > 0 ]]
do
key="$1"

case $key in
    --profile)
    PROFILE="$2"
    SPARK_BUILD_OPTIONS=${SPARK_BUILD_OPTIONS/"--profile $PROFILE"/}
    shift # past argument
    ;;
    --sparkRepo)
    SPARK_REPO="$2"
    SPARK_BUILD_OPTIONS=${SPARK_BUILD_OPTIONS/"--sparkRepo $SPARK_REPO"/}
    shift # past argument
    ;;
    --sparkBranch)
    SPARK_BRANCH="$2"
    SPARK_BUILD_OPTIONS=${SPARK_BUILD_OPTIONS/"--sparkBranch $SPARK_BRANCH"/}
    shift # past argument
    ;;
    --help)
    usage
    ;;
    *)
            # unknown option
    ;;
esac
shift # past argument or value
done

#Default Arguments
if [ -z "$SPARK_REPO" ]; then
    SPARK_REPO="https://github.com/apache/spark.git"
fi

if [ -z "$SPARK_BRANCH" ]; then
    SPARK_BRANCH="tags/v1.4.1"
fi

if [ -z "$PROFILE" ]; then
    PROFILE="crossdata-cassandra"
fi

if [ -z "$SPARK_BUILD_OPTIONS" ]; then
    SPARK_BUILD_OPTIONS="--skip-java-test -Dhadoop.version=2.4.0 -Pyarn -Phive -Pnetlib-lgpl -Pscala-2.10"
fi

TMPDIR=/tmp/stratio-crossdata-distribution

rm -rf ${TMPDIR}
mkdir -p ${TMPDIR}
export JAVA_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

export SCALA_HOME=$SCALA_HOME
export PATH=$SCALA_HOME/bin:$PATH



LOCAL_EDITOR=$(which vim)

if [ -z "$LOCAL_EDITOR" ]; then
    $LOCAL_EDITOR=$(which vi)
fi

if [ -z "$LOCAL_EDITOR" ]; then
    echo "Cannot find any command line editor, ChangeLog.txt won't be edited interactively"
fi

echo "SPARK_REPO: ${SPARK_REPO}"
echo "SPARK_BRANCH: ${SPARK_BRANCH}"
echo " >>> STRATIO CROSSDATA MAKE DISTRIBUTION <<< "

LOCAL_DIR=`pwd`

echo "LOCAL_DIR=$LOCAL_DIR"

mvn -version >/dev/null || { echo "Cannot find Maven in path, aborting"; exit 1; }

cd ..
RELEASE_VER=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[') || { echo "Cannot obtain project version, aborting"; exit 1; }
echo "RELEASE_VER: ${RELEASE_VER}"

if [ "$RELEASE_VER" = "" ]; then
   echo "Release version empty, aborting"; exit 1;
fi

#### Create Crossdata jars from github (master tag) through maven release plugin

echo "################################################"
echo "Compiling Crossdata"
echo "################################################"
echo "$(pwd)"
mvn clean package -DskipTests -P"$PROFILE" || { echo "Cannot build Crossdata project, aborting"; exit 1; }

mkdir -p ${TMPDIR}/lib || { echo "Cannot create output lib directory"; exit 1; }

cp -u ./*/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy target jars to output lib directory, aborting"; exit 1; }
###cp -u ./*/target/alternateLocation/*.jar ${TMPDIR}/lib || { echo "Cannot copy alternate jars to output lib directory, aborting"; exit 1; }

git fetch --tags
latest_tag=$(git describe --tags `git rev-list --tags --max-count=1`)

echo -e "[${RELEASE_VER}]\n\n$(git log ${latest_tag}..HEAD)\n\n$(cat ChangeLog.txt)" > ${TMPDIR}/ChangeLog.txt

#if [ -n "$LOCAL_EDITOR" ]; then
#    $LOCAL_EDITOR ${TMPDIR}/ChangeLog.txt
#fi

echo "################################################"
echo "Copy Crossdata scripts"
echo "################################################"
mkdir -p ${TMPDIR}/bin || { echo "Cannot create output bin directory"; exit 1; }
cp crossdata-scripts/stratio-xd-init.scala ${TMPDIR}/bin || { echo "Cannot copy stratio-xd-init.scala"; exit 1; }
cp crossdata-scripts/stratio-xd-shell ${TMPDIR}/bin || { echo "Cannot copy stratio-xd-shell"; exit 1; }

chmod +x ${TMPDIR}/bin/stratio-xd-shell || { echo "Cannot modify stratio-xd-shell"; exit 1; }

echo "################################################"
echo "Creating Spark distribuition"
echo "With this options $SPARK_BUILD_OPTIONS"
echo "################################################"
cd ${TMPDIR}

STRATIOSPARKDIR=stratiospark

git clone "$SPARK_REPO" ${STRATIOSPARKDIR} || { echo "Cannot clone Spark project from repository: ${SPARK_REPO}"; exit 1; }

cd ./${STRATIOSPARKDIR}/
git checkout "$SPARK_BRANCH" || { echo "Cannot checkout branch: ${SPARK_BRANCH}"; exit 1; }




#--hadoop 2.0.0-mr1-cdh4.4.0
./make-distribution.sh $SPARK_BUILD_OPTIONS || { echo "Cannot make Spark distribution"; exit 1; }

cd ..

DISTDIR=spark-crossdata-distribution-${RELEASE_VER}
DISTFILENAME=${DISTDIR}.tgz

cp ${TMPDIR}/lib/*.jar ${STRATIOSPARKDIR}/dist/lib/
cp ${TMPDIR}/bin/stratio-xd-init.scala ${STRATIOSPARKDIR}/dist/bin/
cp ${TMPDIR}/bin/stratio-xd-shell ${STRATIOSPARKDIR}/dist/bin/

rm -f ${STRATIOSPARKDIR}/dist/lib/*-sources.jar
rm -f ${STRATIOSPARKDIR}/dist/lib/*-javadoc.jar
rm -f ${STRATIOSPARKDIR}/dist/lib/*-tests.jar

mv ${STRATIOSPARKDIR}/dist/ ${DISTDIR}
cp ${TMPDIR}/ChangeLog.txt ${DISTDIR}/

echo "DISTFILENAME: ${DISTFILENAME}"

tar czf ${DISTFILENAME} ${DISTDIR} || { echo "Cannot create tgz"; exit 1; }

mv ${DISTFILENAME} ${LOCAL_DIR}

echo "################################################"
echo "Finishing process"
echo "################################################"
cd ${LOCAL_DIR}
rm -rf ${TMPDIR}




