from parse_tracks import getTrackData
from bs4 import BeautifulSoup
import os
import argparse
import copy
import sys
import shutil
from pack_function import packFunction

sys.setrecursionlimit(15000)

parser = argparse.ArgumentParser(description='Script to plot gpx data on pptx')
parser.add_argument('--unpack_path', help='Path to sample unpack pptx file', required=True)
parser.add_argument('--tracks_path', help='Path to gpx tracks file', required=True)

args = parser.parse_args()

unpack_path = args.unpack_path
tracks_path = args.tracks_path

temps = tracks_path.split("/")
ppt_name = temps[len(temps)-1]

temp_unpack_path = ppt_name.split(".")[0]+"_temp"
shutil.copytree(unpack_path, temp_unpack_path)

slide_path = temp_unpack_path+"/ppt/slides/slide1.xml"

def createPptxFromTrackData(trackData):
    soup = BeautifulSoup(open(slide_path, 'r').read(), 'xml')
    shape_tag = soup.find('sp')
    shape_tag.extract()
    trackCount = 0
    # trackData = [trackData[0]]
    for track in trackData:
        temp_shape_tag = copy.deepcopy(shape_tag)
        animation_path = ""
        path_tag = temp_shape_tag.find('path')
        # if(trackCount!=0):
            # temp_shape_tag.find('off')['x'] = str(int(temp_shape_tag.find('off')['x']) + 2000000)
            # temp_shape_tag.find('off')['y'] = str(int(temp_shape_tag.find('off')['y']) + 2000000)
        #
        # w = path_tag['w']
        # h = path_tag['h']

        #Adding coordinates
        coordinates_detail = track['coordinates']
        coordinates = []
        for coordinate_detail in coordinates_detail:
            coordinates.append(coordinate_detail['coor_set'])

        num_coordinate = 1
        for coordinate in coordinates:
            (x,y) = coordinate
            if(num_coordinate==1):
                animation_path += "M "+str(float(x)/1000)+" "+str(float(y)/1000)+" "
            else:
                animation_path += "L "+str(float(x)/1000)+" "+str(float(y)/1000)+" "
            x = round(float(x))
            x = x*10000
            x = int(x)
            x = str(x)
            y = round(float(y))
            y = y*10000
            y = int(y)
            y = str(y)
            if(num_coordinate==1):
                coordinate_soup = BeautifulSoup("<a:moveTo><a:pt x='"+x+"' y='"+y+"'/></a:moveTo>", 'xml')
                path_tag.append(coordinate_soup.find('moveTo'))
                num_coordinate+=1
            else:
                coordinate_soup = BeautifulSoup("<a:lnTo><a:pt x='"+x+"' y='"+y+"'/></a:lnTo>", 'xml')
                path_tag.append(coordinate_soup.find('lnTo'))
                num_coordinate+=1

        #Adding color to the track
        colors = track['color']
        temp = colors.split("[")[1]
        temp = temp[0:-1]
        temp = temp.split(",")
        r = int(temp[0].split("=")[1])
        b = int(temp[1].split("=")[1])
        g = int(temp[2].split("=")[1])

        hex_value = "{:02x}{:02x}{:02x}".format(r,g,b)
        #
        temp_shape_tag.find('srgbClr')['val'] = hex_value.upper()

        soup.find('spTree').append(temp_shape_tag)

        # #adding animation path
        # anim_motion = soup.find('animMotion')
        # anim_motion['path'] = animation_path

        trackCount+=1

    soup_text = soup.prettify()
    soup_text = soup_text.replace("<body>","")
    soup_text = soup_text.replace("<html>","")
    soup_text = soup_text.replace("</body>","")
    soup_text = soup_text.replace("</html>","")
    soup_text = soup_text.replace("<lnTo>","<a:lnTo>")
    soup_text = soup_text.replace("</lnTo>","</a:lnTo>")
    soup_text = soup_text.replace("<moveTo>","<a:moveTo>")
    soup_text = soup_text.replace("</moveTo>","</a:moveTo>")
    soup_text = soup_text.replace("<pt","<a:pt")
    soup_text = soup_text.replace("</pt","</a:pt")
    soup_text = soup_text.strip()
    # print soup_text
    text_file = open(slide_path, "w")
    text_file.write(soup_text)
    text_file.close()

    packFunction(None, temp_unpack_path)
    shutil.rmtree(temp_unpack_path)



trackData = getTrackData(tracks_path)
createPptxFromTrackData(trackData)
