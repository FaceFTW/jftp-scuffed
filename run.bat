@echo off
rem Please remember to use java-conform paths (no ! etc)

rem put "rem" in front of "goto jar" if you want to compile the source and run a patched version
goto jar
rem goto compile



:jar
echo === running from jar ====
cd .\build\jars

if "%1" == ""      java -jar jftp.jar 
if not "%1" == ""  java -jar jftp.jar %1

cd..
cd..

goto end



:compile
cd .\src\java

echo === compiling ====
javac net\sf\jftp\JFtp.java

echo === starting from new classes (remember: not all classes will be rebuilt!) ====
rem ressources like images have to be included, jar is easiest way
if "%1" == ""      java -cp %classpath%;..\..\build\jars\jftp.jar  net.sf.jftp.JFtp
if not "%1" == ""  java -cp %classpath%;..\..\build\jars\jftp.jar  net.sf.jftp.JFtp

cd..
cd..

goto end



:end