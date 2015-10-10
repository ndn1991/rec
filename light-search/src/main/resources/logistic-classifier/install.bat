@echo off;

set CURR_DIR=%~dp0

mvn install:install-file -Dfile="%CURR_DIR%classifiers.jar" -DgroupId=com.adr.qvh -DartifactId=classifiers -Dversion=1.0 -Dpackaging=jar & mvn install:install-file -Dfile="%CURR_DIR%Document2Vector.jar" -DgroupId=com.adr.thanglq -DartifactId=doc2vec -Dversion=1.0 -Dpackaging=jar &