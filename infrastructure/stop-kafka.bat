@echo off
echo Stopping Kafka cluster...
docker compose -f kafka_cluster.yml down
docker compose -f zookeeper.yml down
echo Done. All containers stopped.
pause