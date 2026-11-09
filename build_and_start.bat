@echo off

echo Build...
call mvn clean install -U

echo Build end. Start app...
java -jar .\target\jTorrTui-1.0-SNAPSHOT-jar-with-dependencies.jar -T "D:\Cache\3. TORRENTs\trnt_files" -D "D:\Cache\3. TORRENTs"

echo End all.