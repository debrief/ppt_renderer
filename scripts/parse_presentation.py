from bs4 import BeautifulSoup

def parsePresentation(unpack_path):
    presentation_path = unpack_path+"/ppt/presentation.xml"
    soup = BeautifulSoup(open(presentation_path, 'r').read(), 'xml')
    slide_size_tag = soup.find('sldSz')
    return slide_size_tag['cx'], slide_size_tag['cy']
