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
    print 'Number of tracks:::', len(trackData)

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

    #Calculating TL and BR
    TLx, TLy = coordinateTransformation(float(mapX), float(mapY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY = 0)
    BRx, BRy = coordinateTransformation(float(mapX+mapCX), float(mapY+mapCY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY = 0)

    #Calculating rectangle representated as animated target values
    animX = TLx
    animY = TLy
    animCX = BRx - TLx
    animCY = BRy - TLy

    shape_tag = None
    arrow_tag = None
    anim_tag = None
    time_tag = None

    #retrive the sample arrow and path tag
    all_shape_tags = soup.find_all('sp')
    for shape in all_shape_tags:
        name = shape.find('cNvPr')['name']
        if(name=='track'):
            shape_tag = shape
        if(name=='marker'):
            arrow_tag = shape
        if(name=='time'):
            time_tag = shape

    shape_tag.extract()
    arrow_tag.extract()
    time_tag.extract()

    #Find time_animation objs -
    time_id_original = time_tag.find('cNvPr')['id']
    print "time_id_original:::::", time_id_original
    spTgts = soup.find_all('spTgt')
    time_anim_tag_big = None
    time_anim_tag_first = None
    for spTgt in spTgts:
        if(spTgt['spid']==time_id_original):
            time_anim_tag_first = spTgt.parent.parent.parent.parent.parent.parent
            time_anim_tag_big = time_anim_tag_first.parent.parent.parent
            break
    time_anim_tag_big_insertion = time_anim_tag_big.parent
    time_anim_tag_big.extract()

    anim_tag = soup.find('animMotion')
    anim_tag_upper = anim_tag.parent.parent.parent
    anim_insertion_tag_upper = anim_tag_upper.parent

    anim_tag_upper.extract()

    # time_anim_tag.extract()
    # #we will get the timestamps from the first track
    # for coordinate in trackData[0]['coordinates']:
    #     timestamp=coordinate['time']
    #     temp_time_tag = copy.deepcopy(time_tag)
    #     temp_time_anim_tag = copy.copy(time_anim_tag)
    #     temp_time_tag.find('cNvPr')['id'] = str(time_tag_id+1)
    #     temp_time_tag.find('txBody').find('p').find('r').find('t').string = str(timestamp)
    #     temp_time_anim_tag.find('spTgt')['spid'] = str(time_tag_id+1)
    #     soup.find('spTree').append(temp_time_tag)
    #     time_anim_tag_parent.append(time_anim_tag)
    #     time_tag_id+=1

    trackCount = 3 #TODO: set equal to existing id.

    shape_ids = []
    arrow_ids = []

    shape_objs = []
    arrow_objs = []
    all_animation_objs = []

    for track in trackData:
        temp_arrow_tag = None
        temp_shape_tag = None
        # temp_anim_tag = None ---> one anim per track

        temp_arrow_tag = copy.deepcopy(arrow_tag)
        temp_shape_tag = copy.deepcopy(shape_tag)
        # temp_anim_tag = copy.deepcopy(anim_tag) ---> one anim per track

        #getting coordinates arrow pointer
        gds = temp_arrow_tag.find('spPr').find('prstGeom').find('avLst').find_all('gd')
        arrow_pointer_x = gds[0]['fmla']
        arrow_pointer_y = gds[1]['fmla']
        arrow_pointer_x = int(arrow_pointer_x[4:])
        arrow_pointer_y = int(arrow_pointer_y[4:])

        #Get arrow shape off and ext
        arrow_off_x = float(temp_arrow_tag.find('off')['x'])
        arrow_off_y = float(temp_arrow_tag.find('off')['y'])
        arrow_ext_cx = float(temp_arrow_tag.find('ext')['cx'])
        arrow_ext_cy = float(temp_arrow_tag.find('ext')['cy'])

        #Get middle point of arrow
        arrow_center_x = (arrow_off_x+arrow_ext_cx/2)
        arrow_center_y = (arrow_off_y+arrow_ext_cy/2)

        #TailX and TailY contains the offset(relative distance from the centre and not the absolute)
        TailX = float(arrow_ext_cx)*(float(float(arrow_pointer_x)/100000.0))
        TailY = float(arrow_ext_cy)*(float(float(arrow_pointer_y)/100000.0))

        # Scaling TailX and TailY (difference in tail's position from centre) to 0...1
        TailX, TailY = coordinateTransformation(float(TailX), float(TailY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY=0)

        #Scaling centre coordinates of callout values to 0...1
        arrow_center_x_small, arrow_center_y_small = coordinateTransformation(float(arrow_center_x), float(arrow_center_y), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY=0)
        #Adding text to arrow shape -
        trackName = track['name']
        #trimming the trackname -
        trackName = trackName[0:4]
        temp_arrow_tag.find('txBody').find('p').find('r').find('t').string = trackName

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

        #Adding coordinates
        coordinates_detail = track['coordinates']
        coordinates = []
        for coordinate_detail in coordinates_detail:
            coordinates.append(coordinate_detail['coor_set'])

        num_coordinate = 0

        #multiple anim per tracks
        coord_count = 1
        (first_x, first_y) = coordinates[0]
        prev_anim_x, prev_anim_y = coordinateTransformation(float(first_x), float(first_y), float(dimensionWidth), float(dimensionHeight), float(animX), float(animY), float(animCX), float(animCY), invertY=1)
        prev_anim_x = prev_anim_x  - TailX - arrow_center_x_small
        prev_anim_y = prev_anim_y  - TailY - arrow_center_y_small

        track_anim_objs = []
        for coordinate in coordinates:
            (x,y) = coordinate

            temp_anim_tag = copy.deepcopy(anim_tag)
            anim_x, anim_y = coordinateTransformation(float(x), float(y), float(dimensionWidth), float(dimensionHeight), float(animX), float(animY), float(animCX), float(animCY), invertY=1)
            anim_x = anim_x - TailX  - arrow_center_x_small
            anim_y = anim_y - TailY - arrow_center_y_small

            animation_path = "M "+str(prev_anim_x)+" "+str(prev_anim_y)+" L "+str(anim_x)+" "+str(anim_y)
            prev_anim_x = anim_x
            prev_anim_y = anim_y

            temp_anim_tag['path'] = animation_path
            temp_anim_tag.find('spTgt')['spid'] = current_arrow_id
            temp_anim_tag.find('cTn')['id'] = int(temp_anim_tag.find('cTn')['id'])+trackCount+coord_count
            # temp_anim_tag.find('cTn')['nodeType'] = "afterEffect"
            #
            # del temp_anim_tag.find('cTn')['accel']
            # del temp_anim_tag.find('cTn')['decel']
            temp_anim_tag.find('cTn')['dur'] = "100"
            track_anim_objs.append(temp_anim_tag)
            coord_count+=1

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
            else:
                coordinate_soup = BeautifulSoup("<a:lnTo><a:pt x='"+x+"' y='"+y+"'/></a:lnTo>", 'xml')
                path_tag.append(coordinate_soup.find('lnTo'))
            num_coordinate+=1

        all_animation_objs.append(track_anim_objs)

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
        #changing arrow to rect callout -
        temp_arrow_tag.find('prstGeom')['prst'] = "wedgeRectCallout"

        #Adding border color to marker
        temp_arrow_tag.find('spPr').find('ln').find('solidFill').find('srgbClr')['val'] = hex_value.upper()

        #We will add the shape and arrow objects in arrays for now
        shape_objs.append(temp_shape_tag)
        arrow_objs.append(temp_arrow_tag)

        # anim_motion = temp_anim_tag.find('animMotion')
        # anim_motion['path'] = animation_path
        # anim_motion['ptsTypes'] = 'A'*(num_coordinate+1)
        # anim_motion.find('spTgt')['spid'] = current_arrow_id

        #Adjust ids
        # temp_anim_tag.find('cTn')['id']=int(temp_anim_tag.find('cTn')['id'])+trackCount
        # anim_motion.find('cTn')['id'] = int(anim_motion.find('cTn')['id'])+trackCount

        #Adjust accel and decel
        # del temp_anim_tag.find('cTn')['accel']
        # del temp_anim_tag.find('cTn')['decel']

        #Adjust duration of the animation
        # anim_motion.find('cTn')['dur'] = "10000"

        # anim_insertion_tag.append(temp_anim_tag)
        # print anim_motion
        # soup.find('bldP')['spid'] = current_shape_id

        trackCount+=2

    #Adding all shape and arrow objects
    spTree_obj = soup.find('spTree')
    for shape in shape_objs:
        spTree_obj.append(shape)
    for arrow in arrow_objs:
        spTree_obj.append(arrow)

    track_num = 1
    for track_anim_objs in all_animation_objs:
        anim_tag_upper_temp = copy.deepcopy(anim_tag_upper)
        anim_tag_upper_temp.name = "seq"
        parent_temp = anim_tag_upper_temp.find('animMotion').parent
        anim_tag_upper_temp.find('animMotion').extract()
        for anim in track_anim_objs:
            # anim.name="par"
            parent_temp.append(anim)

        del anim_tag_upper_temp.find('cTn')['accel']
        del anim_tag_upper_temp.find('cTn')['decel']
        anim_tag_upper_temp.find('cTn')['id'] = track_num
        track_num+=1
        anim_insertion_tag_upper.append(anim_tag_upper_temp)

    time_id_start = arrow_ids[len(arrow_ids) - 1]+1
    print "ID::::::", time_id_start

    #Create parent animation object for all time box animationss
    time_shape_objs = []
    num_coords = len(trackData[0]['coordinates'])
    coord_num = 0
    time_delay = 100
    #we will get the timestamps from the first track
    for coordinate in trackData[0]['coordinates']:
        timestamp=coordinate['time']
        timestamp = timestamp.time()
        temp_time_tag = copy.deepcopy(time_tag)
        temp_time_tag.find('cNvPr')['id'] = str(time_id_start)
        temp_time_tag.find('txBody').find('p').find('r').find('t').string = str(timestamp)
        time_shape_objs.append(temp_time_tag)

        # handle animation objs for time
        if(coord_num==0):
            temp_time_anim = copy.deepcopy(time_anim_tag_first)
            temp_time_anim.find('spTgt')['spid'] = str(time_id_start)
            temp_time_anim.find('cond')['delay'] = "0"
            temp_time_anim.find('cTn')['nodeType'] = "withEffect"
            anim_insertion_tag_upper.append(temp_time_anim)
        else:
            temp_time_anim = copy.deepcopy(time_anim_tag_big)
            temp_time_anim.find('spTgt')['spid'] = str(time_id_start)
            temp_time_anim.find('cond')['delay'] = str(time_delay)
            time_delay+=100
            temp_time_anim.find('cTn')['nodeType'] = "afterEffect"
            temp_time_anim.find('par').find('cond')['delay'] = "100"
            time_anim_tag_big_insertion.append(temp_time_anim)

        coord_num+=1
        time_id_start+=1

    spTreeobj = soup.find('spTree')
    for timeshape in time_shape_objs:
        spTreeobj.append(timeshape)

    #Adding anim objects
    # max_length = max([len(x) for x in all_animation_objs])
    # i = 0
    # while(i<max_length):
    #     anim_tag_upper_temp = copy.deepcopy(anim_tag_upper)
    #     flag = 0
    #     for track_anim_objs in all_animation_objs:
    #         if(i<len(track_anim_objs)):
    #             if flag==0:
    #                 parent_temp = anim_tag_upper_temp.find('cTn').find('cTn').find('par').parent
    #                 anim_tag_upper_temp.find('cTn').find('cTn').find('par').extract()
    #                 parent_temp.append(track_anim_objs[i])
    #                 flag=1
    #             else:
    #                 anim_tag_upper_temp.find('cTn').find('cTn').find('par').parent.append(track_anim_objs[i])
    #     anim_insertion_tag_upper.append(anim_tag_upper_temp)
    #     i+=1



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
    text_file = open(slide_path, "w")
    text_file.write(soup_text)
    text_file.close()

    packFunction(None, temp_unpack_path)
    # shutil.rmtree(temp_unpack_path)



GPXData = getTrackData(tracks_path)
createPptxFromTrackData(GPXData)
