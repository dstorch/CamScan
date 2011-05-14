# test_ocr.sh
#
# OCR unit tests
#
# @author dstorch

cd ../../bin/

java -classpath bin/:libraries/jar/:libraries/jar/javacpp.jar:libraries/jar/javacv.jar:libraries/jar/javacv-macosx-x86_64.jar:libraries/jar/dom4j-1.6.1.jar \ 
ocr/ocrManager 
