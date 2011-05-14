java -classpath bin/:libraries/jar/:libraries/jar/javacpp.jar:libraries/jar/javacv.jar:libraries/jar/javacv-macosx-x86_64.jar:libraries/jar/dom4j-1.6.1.jar \
	 -Xdock:name="CamScan" \
	 -Xdock:icon="libraries/icons/CamscanLogo.png"\
	 -Xms32m  \
	 -Xmx200m \
	 gui/App
