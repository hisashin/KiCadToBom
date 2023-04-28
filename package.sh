#export JAVA_HOME=`/usr/libexec/java_home -v 7`;
export JAVA_HOME=`/usr/libexec/java_home -v 11`;

#gradle compileJava
gradle --warning-mode all distZip

