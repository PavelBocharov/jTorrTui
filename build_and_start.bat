@echo off

echo Build...
call ./mvnw clean install -U

echo Build end. Start app...
java -jar .\target\jTorrTui-1.0-SNAPSHOT-jar-with-dependencies.jar -T "C:\Users\marolok\Downloads" -D "C:\Users\marolok\Downloads\Torrent"

echo End all.