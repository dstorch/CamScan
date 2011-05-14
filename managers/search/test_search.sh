#########################################
# test_search.sh
#
# Unit tests for the search module.
# Calls SearchManager.test()
#
# The test data used by this script
# can be found in tests/search-tests
#
#########################################


# make sure that the java is invoked
# from the root directory of the repo
cd ../

# run the java tests, including dom4j for
# parsing the test documents
java -classpath bin/:libraries/jar/dom4j-1.6.1.jar search/SearchManager

