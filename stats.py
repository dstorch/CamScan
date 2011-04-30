import os


def getlines(path):
	f = open(path, 'rb')
	c = 0
	for line in f.xreadlines():
		c += 1
	f.close()
	return c

types = {}

for root, dirs, files in os.walk(os.getcwd()):
	for file in files:
		path = os.path.join(root, file)
		if path.find('.') == -1: continue
		ext = path.rsplit(".",1)[1]
		if ext.lower() in ['java', 'py']:
			size = getlines(path)
			print path[len(os.getcwd())+1:], size
			types[ext.lower()] = types.get(ext.lower(), 0) + size


total = 0

for key in types:
	print "%s: %d" % (key, types[key])
	total += types[key]

print "Total: %d" % total
