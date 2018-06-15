#Script for fetching details about a rectangle named "map" from the provided unpacked pptx
#Run - python scripts/findmap.py --unpack_path map_rect
import argparse
from bs4 import BeautifulSoup
import os

parser = argparse.ArgumentParser(description='Script to find details about rectangle named map')
parser.add_argument('--unpack_path', help='Path to unpacked pptx folder', required=True)
args = parser.parse_args()

unpack_path = args.unpack_path
slides_base_path = unpack_path+"/ppt/slides"

def getSlides(slides_base_path):
    temp = os.listdir(slides_base_path)
    slides_path = []
    for slide in temp:
        if(slide.endswith(".xml")):
            slides_path.append(slides_base_path+"/"+slide)
    return slides_path

slides_path = getSlides(slides_base_path)
#Slides_path contains path to all the slide.xml files

#Now we need to check for every slide if it contains a rectangle named map

def checkForMap(slides_path):
    mapDetails = {}
    for slidePath in slides_path:
        flag=0
        soup = BeautifulSoup(open(slidePath, 'r').read(), 'lxml')
        shapes = soup.find_all("p:sp")
        for shape in shapes:
            shapeDetails = {}
            for child in shape.find_all("p:cnvpr"):
                shapeDetails['name'] = child['name']
            for child in shape.find_all("a:off"):
                shapeDetails['x'] = child['x']
                shapeDetails['y'] = child['y']
            for child in shape.find_all("a:ext"):
                shapeDetails['cx'] = child['cx']
                shapeDetails['cy'] = child['cy']

            if(shapeDetails['name']=="map"):
                mapDetails = shapeDetails
                flag=1
                break
        if(flag==1):
            break

    return mapDetails

mapDetail = checkForMap(slides_path)
print "Top Left Corner: x=",mapDetail['x']," y=",mapDetail['y']," \nExtentions: cx:",mapDetail['cx']," cy:",mapDetail['cy']
