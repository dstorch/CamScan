################################
# test_ocr.sh
#
# OCR unit tests
# This script is meant to be
# invoked by testall.sh
#
# @author dstorch
################################

# move into the bin in order
# to execute the jvm bytecode
# from the expected directory
cd ../

# get the tesseract path via "which"
# and get the path to python similarly
TESS=`which tesseract`
PYTHON=`which python`

# run the test suite via the
# appropriate main method
java -classpath bin/ ocr/ocrManager $TESS $PYTHON

