from reportlab.pdfgen import canvas
from reportlab.platypus import SimpleDocTemplate, Spacer, Paragraph, Image
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.units import inch
import sys
import xml.dom.minidom
    
# represents a single word within a single page
# consists of the word and its bounding box
class Word:
	def __init__(self, xmin, ymin, xmax, ymax, word) :
		self.xmin = xmin
		self.ymin = ymin
		self.xmax = xmax
		self.ymax = ymax
		self.word = word

	def draw(self, c, height) :
        
		# get descriptions of the bounding box based on OCR data
		bbheight = self.ymax - self.ymin
		bbwidth = self.xmax - self.xmin
		trueY = height - self.ymin - bbheight
 
 		# create the text object, and make it render as invisible
		text = c.beginText(self.xmin, trueY)
		text.setTextRenderMode(3)

		# use the font size that gives the words the proper height
		text.setFont('Courier', bbheight)
		text.textOut(self.word)

		c.drawText(text)

	def xmin(self) :
		return self.xmin
	def ymin(self) :
		return self.ymin
	def xmax(self) :
		return self.xmax
	def ymax(self) :
		return self.ymax
	def word(self) :
		return self.word
	


# a page is essentially a wrapper around a
# list of word objects
class Page:
	def __init__(self, raw, processed) :
		self.words = []
		self.raw = raw
		self.processed = processed

	def draw(self, c) :
		
		# draw the image on the canvas
		width, height = c.drawImage(self.processed, 0, 0, width=None, height=None, mask=None)
		
		# draw the words
		for w in self.words :
			w.draw(c, height)
		
		# determine pdf page size based on the image dimensions
		c.setPageSize((width, height))

	def addWord(self, word) :
		self.words.append(word)
	def getWords(self) :
		return self.words
	def getRawImg(self) :
		return self.raw
	def getProcessedImg(self) :
		return self.processed

class Document:
	def __init__(self, name) :
		self.name = name
		self.pages = []

	def draw(self, c) :
		for p in self.pages :
			p.draw(c)
			c.showPage()

	def numberOfPages(self) :
		return len(self.pages)

	def addPage(self, p) :
		self.pages.append(p)
	def getName(self) :
		return self.name

def parsePage(pagefile) :
	doc = xml.dom.minidom.parse(pagefile)
	rootElement = doc.documentElement
	imgEl = rootElement.getElementsByTagName("IMG")[0]
	textEl = rootElement.getElementsByTagName("TEXT")[0]

	# pathnames of the raw and processed image files
	raw = imgEl.getAttribute("path")
	processed = imgEl.getAttribute("processed")

	# list of bounding boxes
	positions = textEl.getElementsByTagName("POSITIONS")[0]
	words = positions.getElementsByTagName("WORD")

	# construct the output data structure
	page = Page(raw, processed)
	for w in words :
		xmin = int(w.getAttribute("xmin"))
		ymin = int(w.getAttribute("ymin"))
		xmax = int(w.getAttribute("xmax"))
		ymax = int(w.getAttribute("ymax"))
		value = w.getAttribute("value")
		page.addWord(Word(xmin, ymin, xmax, ymax, value))

	return page


def parseXML(documentfile) :

     # read the document xml
	doc = xml.dom.minidom.parse(documentfile)
	rootElement = doc.documentElement
	name = rootElement.getAttribute("name")
	pagesEl = rootElement.getElementsByTagName("PAGES")[0]
	pagesList = pagesEl.getElementsByTagName("PAGE")

	document = Document(name)
	for p in pagesList :
	 	order = int(p.getAttribute("order"))
	 	metafile = p.getAttribute("metafile")

		# pasrse the individual page to
		# get bounding box information
		document.addPage(parsePage(metafile))

	if document.numberOfPages() == 0:
		raise EmptyDocumentError("")
	return document

def main() :
	
	# get command line arguments
	documentfile = sys.argv[1]
	outfile = sys.argv[2]

	doc = parseXML(documentfile)

	# use the DOM to draw on the canvas
	c = canvas.Canvas(outfile, bottomup=1)
	doc.draw(c)
	c.save()

# use to print error messages to the stdout
def error(message) :
	sys.stdout.write("ERROR\n")
	sys.stdout.write(message+"\n")

class EmptyDocumentError(Exception) :
	def __init__(self, message) :
		self.message = message
	def message() :
		return self.message


# Entry point of the program. Calls the main() method above,
# and handles errors.
if __name__ == "__main__" :
	
	if len(sys.argv) != 3 :
		print "Usage: python jpg2pdf.py <doc.xml> <out.pdf>"
	else :
		try :
			main()
			sys.stdout.write("OK\n")
		except IOError :
			error("I/O problem encountered when exporting to PDF.")
		except xml.parsers.expat.ExpatError :
			error("Corrupted document data. Export failed!")
		except EmptyDocumentError :
			error("Document is empty; aborting export.")


