#######################################
# test_export.sh
#
# Unit tests for the export
# module of CamScan.
#
# Uses test data from tests/xml/
# because the export module reads its
# data from the XML on disk.
#
#######################################

echo EXPORT UNIT TESTS
echo

# keep the convention of running everything
# from the root project directory
cd ../

# export test 1
echo TEST 1
python managers/export/jpg2pdf.py \
	tests/export/exporttest1/doc.xml \
	tests/export/test1.pdf

# export test 2
echo TEST 2
python managers/export/jpg2pdf.py \
	tests/export/exporttest2/doc.xml \
	tests/export/test2.pdf

# a couple of newlines!
echo
echo
