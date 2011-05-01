# clean the XML file so that it can be cleanly
# parsed by dom4j

import sys
import re

infile = open(sys.argv[1])
outfile = open(sys.argv[2], 'w')

for line in infile :
	arr = re.split("[^a-zA-Z0-9<>!?/ ]", line)
	newLine = "".join(arr)
	outfile.write(newLine+"\n")

