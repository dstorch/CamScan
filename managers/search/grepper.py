#############################################
# grepper.py
#
# Called from Java in order to grep through
# a file and return relevant search hits.
#
# A single hit is returned as 
# SNIPPET
# text of the snippet
#
# @author dstorch
##############################################

import sys

if __name__ == "__main__" :
	
	# read a line from the stdin until the input
	while true :
		try :
			document = sys.stdin.readline()
			print document
		except EOFError :
			print "exiting"

