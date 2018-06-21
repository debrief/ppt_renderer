from parse_tracks import getTrackData
from bs4 import BeautifulSoup
import os
import argparse
import copy
import sys
import shutil
from pack_function import packFunction
from findmap import getMapDetails
from parse_presentation import parsePresentation

#
# Screen Coordinates to PPTX Coordinates for path -
# X     Y	     x    y
# 0	  0	    100	200
# 1000  800	500	500
# 500	  400	300	350
#
# BaseX = 100
# BaseY = 200
#
# X in ppt = x*(4/10) + BaseX
# Y in ppt = y*(4/10) + BaseY
#

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
if(os.path.isdir(temp_unpack_path)):
    shutil.rmtree(temp_unpack_path)
shutil.copytree(unpack_path, temp_unpack_path)

slide_path = temp_unpack_path+"/ppt/slides/slide1.xml"

def coordinateTransformation(x, y, dimensionWidth, dimensionHeight, rectX, rectY, rectWidth, rectHeight, invertY = 1):
    x = rectX + x*(rectWidth/dimensionWidth)
    if invertY==1:
        y = y - dimensionHeight
        y = rectY + y*(rectHeight/-dimensionHeight)
    else:
        y = rectY + y*(rectHeight/dimensionHeight)
    return x,y

def createPptxFromTrackData(GPXData):
    trackData = GPXData['trackData']
    #Dimension data
    dimensionWidth = int(GPXData['dimensionWidth'])
    dimensionHeight = int(GPXData['dimensionHeight'])

    # Get slide size from presentation.xml file
    slide_dimen_x, slide_dimen_y = parsePresentation(temp_unpack_path)

    soup = BeautifulSoup(open(slide_path, 'r').read(), 'xml')

    #Fix creation id tag
    creationIdsoup = soup.find('creationId')
    creationIdsoup.name = "p14:creationId"
    creationIdsoup['xmlns:p14']="http://schemas.microsoft.com/office/powerpoint/2010/main"

    #Get Map shape details
    mapDetails = getMapDetails(temp_unpack_path)
    mapX = int(mapDetails['x'])
    mapY = int(mapDetails['y'])
    mapCX = int(mapDetails['cx'])
    mapCY = int(mapDetails['cy'])

    # print 'Map::', mapX, mapY, mapCX, mapCY

    #Calculating TL and BR
    TLx, TLy = coordinateTransformation(float(mapX), float(mapY), float(mapCX), float(mapCY), 0, 0, 1, 1, invertY = 0)
    BRx, BRy = coordinateTransformation(float(mapX+mapCX), float(mapY+mapCY), float(mapCX), float(mapCY), 0, 0, 1, 1, invertY = 0)

    # print "TL::", TLx, TLy
    # print "BR::", BRx, BRy

    #Calculating rectangle representated as animated target values
    animX = TLx
    animY = TLy
    animCX = BRx - TLx
    animCY = BRy - TLy
    # print "anim values: ",animX, animY
    # print "animC values: ", animCX, animCY

    shape_tag = None
    arrow_tag = None
    anim_tag = None
    anim_insertion_tag = None

    #retrive the sample arrow and path tag
    all_shape_tags = soup.find_all('sp')
    for shape in all_shape_tags:
        name = shape.find('cNvPr')['name']
        if(name=='track'):
            shape_tag = shape
        if(name=='marker'):
            arrow_tag = shape

    #Marker offsets
    # marker_x_off, marker_y_off = coordinateTransformation(float(arrow_tag.find('off')['x']), float(arrow_tag.find('off')['y']), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1)
    # print "Marker off::",marker_x_off, marker_y_off
    shape_tag.extract()
    arrow_tag.extract()

    #Finding anim_tag
    for cTn in soup.find_all('cTn'):
        if(cTn.has_attr('nodeType') and cTn['nodeType']=='mainSeq'):
            anim_tag = cTn.find('par')
            break

    anim_tag = anim_tag.find('cTn').find('cTn').find('par')
    anim_insertion_tag = anim_tag.parent

    anim_tag.extract()
    trackCount = 3

    shape_ids = []
    arrow_ids = []

    for track in trackData:
        temp_arrow_tag = None
        temp_shape_tag = None
        temp_anim_tag = None

        temp_arrow_tag = copy.deepcopy(arrow_tag)
        temp_shape_tag = copy.deepcopy(shape_tag)
        temp_anim_tag = copy.deepcopy(anim_tag)

        current_shape_id = trackCount+1
        shape_ids.append(current_shape_id)
        current_arrow_id = trackCount+2
        arrow_ids.append(current_arrow_id)

        #Assign ids to arrow shape and path shape
        temp_arrow_tag.find('cNvPr')['id'] = current_arrow_id
        # temp_arrow_tag.find('cNvPr')['name'] = "sample_arrow"+current_arrow_id
        temp_shape_tag.find('cNvPr')['id'] = current_shape_id
        # temp_shape_tag.find('cNvPr')['name'] = "sample_shape"+current_shape_id

        #Get Shape offsets and exts
        temp_shape_x = int(temp_shape_tag.find('off')['x'])
        temp_shape_y = int(temp_shape_tag.find('off')['y'])
        # temp_shape_cx = int(temp_shape_tag.find('ext')['cx'])
        # temp_shape_cy = int(temp_shape_tag.find('ext')['cy'])

        #Set off and ext properties of shape equal to that of map
        # temp_shape_tag.find('off')['x'] = mapX
        # temp_shape_tag.find('off')['y'] = mapY
        # temp_shape_tag.find('ext')['cx'] = mapCX
        # temp_shape_tag.find('ext')['cy'] = mapCY

        animation_path = ""
        path_tag = temp_shape_tag.find('path')
        for child in path_tag.findChildren():
            child.extract()
        # print soup

        #Adding coordinates
        coordinates_detail = track['coordinates']
        coordinates = []
        for coordinate_detail in coordinates_detail:
            coordinates.append(coordinate_detail['coor_set'])

        num_coordinate = 0
        # print coordinates
        for coordinate in coordinates:
            (x,y) = coordinate

            anim_x, anim_y = coordinateTransformation(float(x), float(y), float(dimensionWidth), float(dimensionHeight), float(animX), float(animY), float(animCX), float(animCY), invertY=1)
            anim_x = (anim_x)/1.25  - 0.22
            anim_y = (anim_y)/1.25  - 0.18
            # print "Anim coords: ", anim_x, anim_y
            if(num_coordinate==0):
                animation_path += "M "+str(anim_x)+" "+str(anim_y)+" "
            else:
                animation_path += "L "+str(anim_x)+" "+str(anim_y)+" "
            x = round(float(x))
            y = round(float(y))
            x,y = coordinateTransformation(int(x), int(y), int(dimensionWidth), int(dimensionHeight), int(mapX), int(mapY), int(mapCX), int(mapCY),invertY=1)

            # remove the offsets for the track object
            x = x - temp_shape_x
            y = y - temp_shape_y

            x = int(x)
            x = str(x)
            y = int(y)
            y = str(y)

            if(num_coordinate==0):
                coordinate_soup = BeautifulSoup("<a:moveTo><a:pt x='"+x+"' y='"+y+"'/></a:moveTo>", 'xml')
                path_tag.append(coordinate_soup.find('moveTo'))
                # temp_arrow_tag.find('off')['x'] = x
                # temp_arrow_tag.find('off')['y'] = y
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

        temp_shape_tag.find('srgbClr')['val'] = hex_value.upper()

        soup.find('spTree').append(temp_shape_tag)
        soup.find('spTree').append(temp_arrow_tag)

        anim_motion = temp_anim_tag.find('animMotion')
        anim_motion['path'] = animation_path
        anim_motion['ptsTypes'] = 'A'*(num_coordinate+1)
        anim_motion.find('spTgt')['spid'] = current_arrow_id

        anim_insertion_tag.append(temp_anim_tag)
        # print anim_motion
        soup.find('bldP')['spid'] = current_shape_id

        trackCount+=2

    soup_text = str(soup)
    #all the xml content in one line.
    # soup_text = soup_text.replace("\n","")

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
    # shutil.rmtree(temp_unpack_path)



GPXData = getTrackData(tracks_path)
createPptxFromTrackData(GPXData)
