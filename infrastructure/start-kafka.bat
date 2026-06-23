@echo off
echo ============================================
echo  Starting Kafka Infrastructure
echo ============================================

echo.
echo [1/4] Starting Zookeeper...
docker compose -f zookeeper.yml up -d

echo.
echo Waiting 10 seconds for Zookeeper to initialize...
timeout /t 10 /nobreak >nul

echo.
echo [2/4] Starting Kafka brokers, schema-registry, kafka-manager...
docker compose -f kafka_cluster.yml up -d

echo.
echo Waiting 20 seconds for brokers to elect leaders...
timeout /t 20 /nobreak >nul

echo.
echo [3/4] Checking container status...
echo ============================================
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ============================================

echo.
echo [4/4] Kafka cluster should be up now.
echo Verify all containers show "Up" above:
echo   - zookeeper
echo   - kafka-broker-1, kafka-broker-2, kafka-broker-3
echo   - schema-registry
echo   - kafka-manager
echo.
echo You can now start your Spring Boot services:
echo   - Eureka server
echo   - booking-service
echo   - payment-service
echo   - api-gateway
echo.
pause