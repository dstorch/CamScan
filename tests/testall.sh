###############################################
# testall.sh --
#  CamScan unit tests.
#
# This script calls all of the unit tests
# in sequence.
#
# Author:
#   David Storch (dstorch)
#
###############################################


##############
# PARAMETERS

VISION_ENABLED="0"
SEARCH_ENABLED="1"
EXPORT_ENABLED="1"
OCR_ENABLED="1"

VISION="test_vision.sh"

SEARCH="test_search.sh"

EXPORT="test_export.sh"

OCR="test_ocr.sh"

##############

# 1. vision unit tests
if [ $VISION_ENABLED = "1" ]; then
	../managers/vision/$VISION
fi

# 2. test search
if [ $SEARCH_ENABLED = "1" ]; then
	../managers/search/$SEARCH
fi

# 3. test export
if [ $EXPORT_ENABLED = "1" ]; then
	../managers/export/$EXPORT
fi

# 4. test ocr
if [ $OCR_ENABLED = "1" ]; then
	../managers/ocr/$OCR
fi

