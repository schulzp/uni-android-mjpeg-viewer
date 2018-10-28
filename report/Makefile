DOCUMENT = report

all:
	xelatex $(DOCUMENT).tex
	biber $(DOCUMENT)
	xelatex $(DOCUMENT).tex
	xelatex $(DOCUMENT).tex
	gs -sDEVICE=pdfwrite -dCompatibilityLevel=1.4 -dPDFSETTINGS=/printer -dNOPAUSE -dQUIET -dBATCH -sOutputFile=$(DOCUMENT)-small.pdf $(DOCUMENT).pdf

clean:
	rm -f *.out *.bbl *.blg *.toc *.aux *.pdf *.log text/*.aux
