SPARK_HOME=$1
MASTER=$2
RUN_CLASS=com.vinecom.rec.app.GetRecommendFromFileV2
APP_NAME="GetRecommendFromFileV2"
MODE=client
EXECUTOR_MEMORY=20g

COUNT=0
for f in lib/*.jar; do
        if [ $COUNT == 0 ]; then
                JARS="$f"
        else
            	JARS="$f,$JARS"
        fi
	COUNT=$(($COUNT+1))
done
$SPARK_HOME/bin/spark-submit --class $RUN_CLASS \
--jars $JARS \
--master $MASTER \
--name $APP_NAME \
--deploy-mode $MODE \
--conf "spark.executor.memory="$EXECUTOR_MEMORY \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer" \
--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" \
--conf "spark.storage.memoryFraction=0.4" \
--conf "spark.cores.max=32" \
vin-recommend-*.jar $3 $4