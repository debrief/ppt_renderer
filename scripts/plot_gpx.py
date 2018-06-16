from parse_tracks import getTrackData
from bs4 import BeautifulSoup
import os
import argparse
import copy
import sys

sys.setrecursionlimit(15000)

parser = argparse.ArgumentParser(description='Script to plot gpx data on pptx')
parser.add_argument('--unpack_path', help='Path to sample unpack pptx file', required=True)
parser.add_argument('--tracks_path', help='Path to gpx tracks file', required=True)

args = parser.parse_args()

unpack_path = args.unpack_path
tracks_path = args.tracks_path
slide_path = unpack_path+"/ppt/slides/slide1.xml"

def createPptxFromTrackData(trackData):
    soup = BeautifulSoup(open(slide_path, 'r').read(), 'xml')
    shape_tag = soup.find('sp')
    shape_tag.extract()
    trackCount = 0
    for track in trackData:
        temp_shape_tag = copy.deepcopy(shape_tag)

        path_tag = temp_shape_tag.find('path')
        if(trackCount!=0):
            temp_shape_tag.find('off')['x'] = str(int(temp_shape_tag.find('off')['x']) + 2000000)
            # temp_shape_tag.find('off')['y'] = str(int(temp_shape_tag.find('off')['y']) + 2000000)

        w = path_tag['w']
        h = path_tag['h']

        #Scaling --
        # x = 1 ---> 297649
        # y = 1 ---> 1051897
        coordinates_detail = track['coordinates']
        coordinates = []
        for coordinate_detail in coordinates_detail:
            coordinates.append(coordinate_detail['coor_set'])

        scale = 1
        for coordinate in coordinates:
            (x,y) = coordinate
            x = round(float(x))
            x = x*128*scale
            x = int(x)
            x = str(x)
            y = round(float(y))
            y = y*128*scale
            y = int(y)
            y = str(y)
            coordinate_soup = BeautifulSoup("<a:lnTo><a:pt x='"+x+"' y='"+y+"'/></a:lnTo>", 'xml')
            path_tag.append(coordinate_soup.find('lnTo'))
            scale+=1

        soup.find('spTree').append(temp_shape_tag)
        trackCount+=1

    soup_text = soup.prettify()
    soup_text = soup_text.replace("<body>","")
    soup_text = soup_text.replace("<html>","")
    soup_text = soup_text.replace("</body>","")
    soup_text = soup_text.replace("</html>","")
    soup_text = soup_text.replace("<lnTo>","<a:lnTo>")
    soup_text = soup_text.replace("</lnTo>","</a:lnTo>")
    soup_text = soup_text.replace("<pt","<a:pt")
    soup_text = soup_text.replace("</pt","</a:pt")
    soup_text = soup_text.strip()
    # print soup_text
    text_file = open(slide_path, "w")
    text_file.write(soup_text)
    text_file.close()



trackData = getTrackData(tracks_path)
createPptxFromTrackData(trackData)
