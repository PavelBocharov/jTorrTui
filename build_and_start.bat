@echo off

echo Build...
call mvn clean install -U

echo Build end. Start app...
java -jar .\target\jTorrTui-1.0-SNAPSHOT-jar-with-dependencies.jar

echo End all.