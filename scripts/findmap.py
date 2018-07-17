#Script for fetching details about a rectangle named "map" from the provided unpacked pptx
#Run - python scripts/findmap.py --unpack_path map_rect
#Use getMapDetails function
# from findmap import getMapDetails
#
# print getMapDetails("map_rect")


# import argparse
from bs4 import BeautifulSoup
import os

# parser = argparse.ArgumentParser(description='Script to find details about rectangle named map')
# parser.add_argument('--unpack_path', help='Path to unpacked pptx folder', required=True)
# args = parser.parse_args()

# unpack_path = args.unpack_path
# slides_base_path = unpack_path+"/ppt/slides"

def getSlides(slides_base_path):
    temp = os.listdir(slides_base_path)
    slides_path = []
    for slide in temp:
        if(slide.endswith(".xml")):
            slides_path.append(slides_base_path+"/"+slide)
    return slides_path

# slides_path = getSlides(slides_base_path)
#Slides_path contains path to all the slide.xml files

#Now we need to check for every slide if it contains a rectangle named map

def checkForMap(slides_path):
    mapDetails = {}
    for slidePath in slides_path:
        flag=0
        soup = BeautifulSoup(open(slidePath, 'r').read(), 'lxml')
        # print "soup","\n\n\n", soup,"\n\n"
        shapes = soup.find_all("p:sp")
        cnvpr = "p:cnvpr"

        if(not shapes):
            soup = BeautifulSoup(open(slidePath, 'r').read(), 'lxml-xml')
            # print "soup","\n\n\n", soup,"\n\n"
            shapes = soup.find_all("p:sp")
            cnvpr = "p:cNvPr"

        for shape in shapes:
            shapeDetails = {}
            # for child in shape.find_all(cnvpr):
            #     shapeDetails['name'] = child['name']
            # for child in shape.find_all("a:off"):
            #     shapeDetails['x'] = child['x']
            #     shapeDetails['y'] = child['y']
            # for child in shape.find_all("a:ext"):
            #     shapeDetails['cx'] = child['cx']
            #     shapeDetails['cy'] = child['cy']
            shapeDetails['name'] = shape.find(cnvpr)['name']
            if(shapeDetails['name']=="map"):
                shapeDetails['x'] = shape.find('a:off')['x']
                shapeDetails['y'] = shape.find('a:off')['y']
                shapeDetails['cx'] = shape.find('a:ext')['cx']
                shapeDetails['cy'] = shape.find('a:ext')['cy']
                mapDetails = shapeDetails
                print "mapDetails - ", mapDetails
                flag=1
                break
        if(flag==1):
            break

    return mapDetails

def getMapDetails(unpack_path):
    slides_base_path = unpack_path+"/ppt/slides"
    slides_path = getSlides(slides_base_path)
    mapDetail = checkForMap(slides_path)
    return mapDetail
# print "mapDetails - ",mapDetail
#
# if(not 'name' in mapDetail):
#     print "No shape named 'Map' found..."
# else:
#     print "Top Left Corner: x=",mapDetail['x']," y=",mapDetail['y']," \nExtentions: cx:",mapDetail['cx']," cy:",mapDetail['cy']
