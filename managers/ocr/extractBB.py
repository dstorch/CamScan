# extract bounding box information with regular expressions,
# without using a parsing library

import sys
import re

infile = open(sys.argv[1])

regex = re.compile(r"<span class='ocr_word'([^<>]*) title=\"([^<>]*)\"([^<>]*)><([^<>]*)>([^<>]*)</span></span>")

# build up a string containing all
fileString = ""
for line in infile :
	fileString += line


matches = regex.findall(fileString)

regex2 = re.compile("[^A-Za-z0-9\-,.:;!? ]")

for g1, g2, g3, g4, g5 in matches :

        g5 = re.sub(regex2, "", g5)
        g2 = re.sub(regex2, "", g2)
        
	sys.stdout.write(g5+" "+g2+"\n")
