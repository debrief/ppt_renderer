from parse_tracks import getTrackData
from parse_tracks import getNarratives
from parse_tracks import getInterval
from unpack_function import unpackFunction
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

#Helper function declarations -
def checkPathandInitialization(donor, tracks_path):
    if(not os.path.exists(donor)):
        print "donor file does not exist"
        sys.exit(1)
    if(not os.path.exists(tracks_path)):
        print "tracks file does not exist"
        sys.exit(1)

    unpack_path = donor.split('.')[0]
    if(os.path.exists(unpack_path)):
        shutil.rmtree(unpack_path)

    temps = tracks_path.split("/")
    ppt_name = temps[len(temps)-1]

    temp_unpack_path = ppt_name.split(".")[0]+"_temp"
    unpackFunction(donor, temp_unpack_path)

    slide_path = temp_unpack_path+"/ppt/slides/slide1.xml"

    return slide_path, temp_unpack_path

def cleanSoup(soup):
    soup_text = str(soup)
    soup_text = soup_text.replace("<extLst1/>","<a:extLst/>")
    soup_text = soup_text.replace("<lnTo>","<a:lnTo>")
    soup_text = soup_text.replace("</lnTo>","</a:lnTo>")
    soup_text = soup_text.replace("<moveTo>","<a:moveTo>")
    soup_text = soup_text.replace("</moveTo>","</a:moveTo>")
    soup_text = soup_text.replace("<pt","<a:pt")
    soup_text = soup_text.replace("</pt","</a:pt")
    soup_text = soup_text.strip()
    return soup_text

def writeSoup(slide_path, soup):
    text_file = open(slide_path, "w")
    text_file.write(cleanSoup(soup))
    text_file.close()

def createTimeNarrativeShapes(spTreeobj, intervalDuration, trackData, time_tag, time_anim_tag_first, anim_insertion_tag_upper, time_anim_tag_big, time_anim_tag_big_insertion, narrativeEntries, narrative_tag):
    #Create parent animation object for all time box animationss
    time_shape_objs = []
    coord_num = 0
    time_delay = intervalDuration
    current_time_id = int(time_tag.find('cNvPr')['id'])
    print("Last Time Id:::::",current_time_id)
    #we will get the timestamps from the first track
    for coordinate in trackData[0]['coordinates']:
        timestamp=coordinate['time']
        #timestamp = timestamp.time()
        timestamp = timestamp.strftime("%y %b %d%H%M")
        temp_time_tag = copy.deepcopy(time_tag)
        temp_time_tag.find('cNvPr')['id'] = str(current_time_id)
        temp_time_tag.find('txBody').find('p').find('r').find('t').string = str(timestamp)
        time_shape_objs.append(temp_time_tag)

        # handle animation objs for time
        if(coord_num==0):
            temp_time_anim = copy.deepcopy(time_anim_tag_first)
            temp_time_anim.find('spTgt')['spid'] = str(current_time_id)
            temp_time_anim.find('cond')['delay'] = "0"
            temp_time_anim.find('cTn')['nodeType'] = "withEffect"
            anim_insertion_tag_upper.append(temp_time_anim)
        else:
            temp_time_anim = copy.deepcopy(time_anim_tag_big)
            temp_time_anim.find('spTgt')['spid'] = str(current_time_id)
            temp_time_anim.find('cond')['delay'] = str(time_delay)
            time_delay+=intervalDuration
            temp_time_anim.find('cTn')['nodeType'] = "afterEffect"
            temp_time_anim.find('par').find('cond')['delay'] = str(intervalDuration)
            time_anim_tag_big_insertion.append(temp_time_anim)

        if(coord_num == 0):
            current_time_id = 300
        current_time_id+=1
        coord_num+=1


    for timeshape in time_shape_objs:
        spTreeobj.append(timeshape)

    #Adding narratives -
    narrative_objects = []
    time_delay = 0
    current_narrative_id = int(narrative_tag.find('cNvPr')['id'])
    print("Last Narrative Id:::::",current_narrative_id)

    #Blank narrative box
    blank_narrative = copy.deepcopy(narrative_tag)
    blank_narrative.find('cNvPr')['id'] = current_narrative_id
    blank_narrative.find('txBody').find('p').find('r').find('t').string = ""
    narrative_objects.append(blank_narrative)
    current_narrative_id = 400
    num_narrative = 0
    for narrative in narrativeEntries:
        time_delay+=(int(narrative['elapsed']) - time_delay)
        time_str = narrative['dateStr']
        #time_str = time_str.split('.')[0]
        #time_str = time_str[0:2]+":"+time_str[2:4]+":"+time_str[4:6]
        temp_narrative_tag = copy.deepcopy(narrative_tag)
        temp_narrative_tag.find('cNvPr')['id'] = current_narrative_id
        temp_narrative_tag.find('txBody').find('p').find('r').find('t').string = time_str+" "+narrative['Text']
        narrative_objects.append(temp_narrative_tag)
        if(num_narrative==0):
            temp_narrative_anim = copy.deepcopy(time_anim_tag_first)
            temp_narrative_anim.find('spTgt')['spid'] = str(current_narrative_id)
            temp_narrative_anim.find('cond')['delay'] = str(time_delay)
            temp_narrative_anim.find('cTn')['nodeType'] = "withEffect"
            anim_insertion_tag_upper.append(temp_narrative_anim)
        else:
            temp_narrative_anim = copy.deepcopy(time_anim_tag_big)
            temp_narrative_anim.find('spTgt')['spid'] = str(current_narrative_id)
            temp_narrative_anim.find('cond')['delay'] = str(time_delay)
            temp_narrative_anim.find('cTn')['nodeType'] = "afterEffect"
            temp_narrative_anim.find('par').find('cond')['delay'] = str(intervalDuration)
            time_anim_tag_big_insertion.append(temp_narrative_anim)
        current_narrative_id+=1
        num_narrative+=1

    for narrative in narrative_objects:
        spTreeobj.append(narrative)


def addAnimationObjects(all_animation_objs, anim_tag_upper, anim_insertion_tag_upper):
    track_num = 1
    for track_anim_objs in all_animation_objs:
        anim_tag_upper_temp = copy.deepcopy(anim_tag_upper)
        anim_tag_upper_temp.name = "seq"
        parent_temp = anim_tag_upper_temp.find('animMotion').parent
        anim_tag_upper_temp.find('animMotion').extract()
        for anim in track_anim_objs:
            parent_temp.append(anim)

        del anim_tag_upper_temp.find('cTn')['accel']
        del anim_tag_upper_temp.find('cTn')['decel']
        anim_tag_upper_temp.find('cTn')['id'] = track_num
        track_num+=1
        anim_insertion_tag_upper.append(anim_tag_upper_temp)

def addShapeMarkerFootPrintsObjects(spTreeobj, shape_objs, arrow_objs, all_footprints_objs):
    for shape in shape_objs:
        spTreeobj.append(shape)
    for arrow in arrow_objs:
        spTreeobj.append(arrow)
    for footprintsTracks in all_footprints_objs:
        for footPrint in footprintsTracks:
            spTreeobj.append(footPrint)

def getColorinHex(track):
    colors = track['color']
    temp = colors.split("[")[1]
    temp = temp[0:-1]
    temp = temp.split(",")
    r = int(temp[0].split("=")[1])
    g = int(temp[1].split("=")[1])
    b = int(temp[2].split("=")[1])

    hex_value = "{:02x}{:02x}{:02x}".format(r,g,b)
    return hex_value

def getShapes(soup):
    shape_tag = None
    arrow_tag = None
    time_tag = None
    narrative_tag = None
    footprint_tag = None
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
        if(name=='narrative'):
            narrative_tag = shape
        if(name=='footprint'):
            footprint_tag = shape

    shape_tag.extract()
    arrow_tag.extract()
    time_tag.extract()
    narrative_tag.extract()
    footprint_tag.extract()
    return shape_tag, arrow_tag, time_tag, narrative_tag, footprint_tag

def fixCreationId(soup):
    #creationIdsoup = soup.find('creationId')
    #creationIdsoup.name = "p14:creationId"
    # creationIdsoup['xmlns:p14']="http://schemas.microsoft.com/office/powerpoint/2010/main"
    #save the p:extLst
    mainExt = None
    mainExtParent = None

    mainExt = soup.find('p14:creationId').parent.parent
    mainExtParent = mainExt.parent

    mainExt.extract()
    #remove extlst
    for ext in soup.find_all('extLst'):
        ext.parent.append(BeautifulSoup('<extLst1/>', 'xml').find('extLst1'))
        ext.extract()

    #Readding the main ext
    mainExtParent.append(mainExt)

def findTimeAnimationObjects(soup, time_tag):
    time_id_original = time_tag.find('cNvPr')['id']
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
    return time_anim_tag_first, time_anim_tag_big, time_anim_tag_big_insertion

def findAnimationTagObjects(soup):
    anim_tag = soup.find('animMotion')
    anim_tag_upper = anim_tag.parent.parent.parent
    anim_insertion_tag_upper = anim_tag_upper.parent
    anim_tag_upper.extract()
    return anim_tag, anim_tag_upper, anim_insertion_tag_upper

def getArrowPointerCoordinates(temp_arrow_tag):
    gds = temp_arrow_tag.find('spPr').find('prstGeom').find('avLst').find_all('gd')
    arrow_pointer_x = gds[0]['fmla']
    arrow_pointer_y = gds[1]['fmla']
    arrow_pointer_x = int(arrow_pointer_x[4:])
    arrow_pointer_y = int(arrow_pointer_y[4:])
    return arrow_pointer_x, arrow_pointer_y

def arrowCoordinates(temp_arrow_tag):
    arrow_off_x = float(temp_arrow_tag.find('off')['x'])
    arrow_off_y = float(temp_arrow_tag.find('off')['y'])
    arrow_ext_cx = float(temp_arrow_tag.find('ext')['cx'])
    arrow_ext_cy = float(temp_arrow_tag.find('ext')['cy'])
    return arrow_off_x, arrow_off_y, arrow_ext_cx, arrow_ext_cy

def coordinateTransformation(x, y, dimensionWidth, dimensionHeight, rectX, rectY, rectWidth, rectHeight, invertY = 1):
    x = rectX + x*(rectWidth/dimensionWidth)
    if invertY==1:
        y = y - dimensionHeight
        y = rectY + y*(rectHeight/-dimensionHeight)
    else:
        y = rectY + y*(rectHeight/dimensionHeight)
    return x,y

def getDimensionsFromGPXData(GPXData):
    return int(GPXData['dimensionWidth']), int(GPXData['dimensionHeight'])

def getMapDimesions(mapDetails):
    return int(mapDetails['x']), int(mapDetails['y']), int(mapDetails['cx']), int(mapDetails['cy'])

def findLastShapeId(soup):
    cNvPrs = soup.find_all('cNvPr')
    last_id = 0
    for cNvPr in cNvPrs:
        if(int(cNvPr['id'])>last_id):
            last_id = int(cNvPr['id'])
    return last_id

def cleanSpTree(soup):
    for child in soup.find('spTree').findChildren():
        child.extract()


def addAnimationFootPrints(time_anim_tag_first, anim_insertion_tag_upper, time_anim_tag_big, time_anim_tag_big_insertion):

    #for arrow in arrow_objs:
    #    spTreeobj.append(arrow)
    # Create parent animation object for all time box animationss
    time_shape_objs = []
    # coord_num = 0
    # time_delay = intervalDuration
    # current_time_id = int(time_tag.find('cNvPr')['id'])
    # print("Last Time Id:::::", current_time_id)
    # # we will get the timestamps from the first track
    # for coordinate in trackData[0]['coordinates']:
    #     timestamp = coordinate['time']
    #     # timestamp = timestamp.time()
    #     timestamp = timestamp.strftime("%y %b %d%H%M")
    #     temp_time_tag = copy.deepcopy(time_tag)
    #     temp_time_tag.find('cNvPr')['id'] = str(current_time_id)
    #     temp_time_tag.find('txBody').find('p').find('r').find('t').string = str(timestamp)
    #     time_shape_objs.append(temp_time_tag)
    #
    #     # handle animation objs for time
    #     if (coord_num == 0):
    #         temp_time_anim = copy.deepcopy(time_anim_tag_first)
    #         temp_time_anim.find('spTgt')['spid'] = str(current_time_id)
    #         temp_time_anim.find('cond')['delay'] = "0"
    #         temp_time_anim.find('cTn')['nodeType'] = "withEffect"
    #         anim_insertion_tag_upper.append(temp_time_anim)
    #     else:
    #         temp_time_anim = copy.deepcopy(time_anim_tag_big)
    #         temp_time_anim.find('spTgt')['spid'] = str(current_time_id)
    #         temp_time_anim.find('cond')['delay'] = str(time_delay)
    #         time_delay += intervalDuration
    #         temp_time_anim.find('cTn')['nodeType'] = "afterEffect"
    #         temp_time_anim.find('par').find('cond')['delay'] = str(intervalDuration)
    #         time_anim_tag_big_insertion.append(temp_time_anim)
    #
    #     if (coord_num == 0):
    #         current_time_id = 300
    #     current_time_id += 1
    #     coord_num += 1
    #
    # for timeshape in time_shape_objs:
    #     spTreeobj.append(timeshape)
    #
    # # Adding narratives -
    # narrative_objects = []
    # time_delay = 0
    # current_narrative_id = int(narrative_tag.find('cNvPr')['id'])
    # print("Last Narrative Id:::::", current_narrative_id)
    #
    # # Blank narrative box
    # blank_narrative = copy.deepcopy(narrative_tag)
    # blank_narrative.find('cNvPr')['id'] = current_narrative_id
    # blank_narrative.find('txBody').find('p').find('r').find('t').string = ""
    # narrative_objects.append(blank_narrative)
    # current_narrative_id = 400
    # num_narrative = 0
    # for narrative in narrativeEntries:
    #     time_delay += (int(narrative['elapsed']) - time_delay)
    #     time_str = narrative['dateStr']
    #     # time_str = time_str.split('.')[0]
    #     # time_str = time_str[0:2]+":"+time_str[2:4]+":"+time_str[4:6]
    #     temp_narrative_tag = copy.deepcopy(narrative_tag)
    #     temp_narrative_tag.find('cNvPr')['id'] = current_narrative_id
    #     temp_narrative_tag.find('txBody').find('p').find('r').find('t').string = time_str + " " + narrative['Text']
    #     narrative_objects.append(temp_narrative_tag)
    #     if (num_narrative == 0):
    #         temp_narrative_anim = copy.deepcopy(time_anim_tag_first)
    #         temp_narrative_anim.find('spTgt')['spid'] = str(current_narrative_id)
    #         temp_narrative_anim.find('cond')['delay'] = str(time_delay)
    #         temp_narrative_anim.find('cTn')['nodeType'] = "withEffect"
    #         anim_insertion_tag_upper.append(temp_narrative_anim)
    #     else:
    #         temp_narrative_anim = copy.deepcopy(time_anim_tag_big)
    #         temp_narrative_anim.find('spTgt')['spid'] = str(current_narrative_id)
    #         temp_narrative_anim.find('cond')['delay'] = str(time_delay)
    #         temp_narrative_anim.find('cTn')['nodeType'] = "afterEffect"
    #         temp_narrative_anim.find('par').find('cond')['delay'] = str(intervalDuration)
    #         time_anim_tag_big_insertion.append(temp_narrative_anim)
    #     current_narrative_id += 1
    #     num_narrative += 1
    pass


def createPptxFromTrackData(GPXData, narrativeEntries, intervalDuration, slide_path, temp_unpack_path):

    trackData = GPXData['trackData']
    print 'Number of tracks:::', len(trackData)

    #Dimension data
    dimensionWidth, dimensionHeight = getDimensionsFromGPXData(GPXData)

    # Get slide size from presentation.xml file
    slide_dimen_x, slide_dimen_y = parsePresentation(temp_unpack_path)

    soup = BeautifulSoup(open(slide_path, 'r').read(), 'xml')

    #Fix creation id tag
    fixCreationId(soup)

    #Get Map shape details
    mapDetails = getMapDetails(temp_unpack_path)
    mapX, mapY, mapCX, mapCY = getMapDimesions(mapDetails)

    #Calculating TL and BR
    TLx, TLy = coordinateTransformation(float(mapX), float(mapY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY = 0)
    BRx, BRy = coordinateTransformation(float(mapX+mapCX), float(mapY+mapCY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY = 0)

    #Calculating rectangle representated as animated target values
    animX = TLx
    animY = TLy
    animCX = BRx - TLx
    animCY = BRy - TLy

    #getting shape tags
    shape_tag, arrow_tag, time_tag, narrative_tag, footprint_tag = getShapes(soup)
    # Remove all the remaining shapes
    # cleanSpTree(soup)
    #Find time_animation objs -
    time_anim_tag_first, time_anim_tag_big, time_anim_tag_big_insertion = findTimeAnimationObjects(soup, time_tag)
    #Find anim_tags -
    anim_tag, anim_tag_upper, anim_insertion_tag_upper = findAnimationTagObjects(soup)

    # Get Footprint ellipse size
    footprint_x_size = int(footprint_tag.find('ext')['cx'])
    footprint_y_size = int(footprint_tag.find('ext')['cy'])

    trackCount = 0
    current_shape_id = int(shape_tag.find('cNvPr')['id'])
    current_arrow_id = int(arrow_tag.find('cNvPr')['id'])
    print("Last Shape Id::::: ", current_shape_id)
    print("Last Arrow Id::::: ", current_arrow_id)

    shape_ids = []
    arrow_ids = []

    shape_objs = []
    arrow_objs = []
    all_animation_objs = []
    all_footprints_objs = []

    for track in trackData:
        temp_arrow_tag = None
        temp_shape_tag = None

        temp_arrow_tag = copy.deepcopy(arrow_tag)
        temp_shape_tag = copy.deepcopy(shape_tag)

        #getting coordinates arrow pointer
        arrow_pointer_x, arrow_pointer_y = getArrowPointerCoordinates(temp_arrow_tag)

        #Get arrow shape off and ext
        arrow_off_x, arrow_off_y, arrow_ext_cx, arrow_ext_cy = arrowCoordinates(temp_arrow_tag)

        #Get middle point of arrow
        arrow_center_x = (arrow_off_x+arrow_ext_cx/2)
        arrow_center_y = (arrow_off_y+arrow_ext_cy/2)

        #TailX and TailY contains the offset(relative distance from the centre and not the absolute)
        TailX = float(arrow_ext_cx)*(float(float(arrow_pointer_x)/100000.0))
        TailY = float(arrow_ext_cy)*(float(float(arrow_pointer_y)/100000.0))

        # Scaling TailX and TailY (difference in tail's position from centre) to 0...1
        TailX, TailY = coordinateTransformation(float(TailX), float(TailY), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY=0)

        #Scaling centre coordinatens of callout values to 0...1
        arrow_center_x_small, arrow_center_y_small = coordinateTransformation(float(arrow_center_x), float(arrow_center_y), float(slide_dimen_x), float(slide_dimen_y), 0, 0, 1, 1, invertY=0)
        #Adding text to arrow shape -
        trackName = track['name']
        #trimming the trackname -
        trackName = trackName[0:4]
        temp_arrow_tag.find('txBody').find('p').find('r').find('t').string = trackName

        shape_ids.append(current_shape_id)
        arrow_ids.append(current_arrow_id)
        #Assign ids to arrow shape and path shape
        temp_arrow_tag.find('cNvPr')['id'] = current_arrow_id
        temp_shape_tag.find('cNvPr')['id'] = current_shape_id

        #Get Shape offsets and exts
        temp_shape_x = int(temp_shape_tag.find('off')['x'])
        temp_shape_y = int(temp_shape_tag.find('off')['y'])

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

        #footprints
        footprint_count = 1

        colorHexValue = getColorinHex(track).upper()

        track_anim_objs = []
        footprints_objs = []
        for coordinate in coordinates:
            (x,y) = coordinate

            temp_anim_tag = copy.deepcopy(anim_tag)
            anim_x, anim_y = coordinateTransformation(float(x), float(y), float(dimensionWidth), float(dimensionHeight), float(animX), float(animY), float(animCX), float(animCY), invertY=1)
            anim_x = anim_x - TailX  - arrow_center_x_small
            anim_y = anim_y - TailY - arrow_center_y_small

            animation_path = "M "+ ('%.4f' % prev_anim_x) + " " + ('%.4f' % prev_anim_y) + " L " + ('%.4f' % anim_x) + " " + ('%.4f' % anim_y)
            prev_anim_x = anim_x
            prev_anim_y = anim_y

            temp_anim_tag['path'] = animation_path
            temp_anim_tag.find('spTgt')['spid'] = current_arrow_id
            temp_anim_tag.find('cTn')['id'] = int(temp_anim_tag.find('cTn')['id'])+trackCount+coord_count
            temp_anim_tag.find('cTn')['dur'] = str(intervalDuration)
            track_anim_objs.append(temp_anim_tag)
            coord_count+=1

            x = round(float(x))
            y = round(float(y))
            x,y = coordinateTransformation(int(x), int(y), int(dimensionWidth), int(dimensionHeight), int(mapX), int(mapY), int(mapCX), int(mapCY),invertY=1)

            temp_footprint_tag = copy.deepcopy(footprint_tag)
            # We substract the ellipse size from the coordinate.
            temp_footprint_tag.find('off')['x'] = x - footprint_x_size / 2
            temp_footprint_tag.find('off')['y'] = y - footprint_y_size / 2
            # Adding color to the footprint
            temp_footprint_tag.find('srgbClr')['val'] = colorHexValue
            temp_footprint_tag.find('cNvPr')['id'] = int(temp_anim_tag.find('cTn')['id'])+trackCount+coord_count+footprint_count
            footprints_objs.append(temp_footprint_tag)
            footprint_count += 1

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
        all_footprints_objs.append(footprints_objs)
        #Adding color to the track
        temp_shape_tag.find('srgbClr')['val'] = colorHexValue
        #changing arrow to rect callout -
        temp_arrow_tag.find('prstGeom')['prst'] = "wedgeRectCallout"
        #Adding border color to marker
        temp_arrow_tag.find('spPr').find('ln').find('solidFill').find('srgbClr')['val'] = colorHexValue
        #We will add the shape and arrow objects in arrays for now
        shape_objs.append(temp_shape_tag)
        arrow_objs.append(temp_arrow_tag)

        if(trackCount==0):
            current_shape_id = 500
            current_arrow_id = 600
        current_shape_id += 1
        current_arrow_id += 1
        trackCount+=1

    #Adding all shape and arrow objects
    spTreeobj = soup.find('spTree')
    addShapeMarkerFootPrintsObjects(spTreeobj, shape_objs, arrow_objs,all_footprints_objs)
    addAnimationObjects(all_animation_objs, anim_tag_upper, anim_insertion_tag_upper)
    addAnimationFootPrints(time_anim_tag_first, anim_insertion_tag_upper, time_anim_tag_big, time_anim_tag_big_insertion)
    createTimeNarrativeShapes(spTreeobj, intervalDuration, trackData, time_tag, time_anim_tag_first, anim_insertion_tag_upper, time_anim_tag_big, time_anim_tag_big_insertion, narrativeEntries, narrative_tag)
    writeSoup(slide_path, soup)
    packFunction(None, temp_unpack_path)

#Main -
parser = argparse.ArgumentParser(description='Script to plot gpx data on pptx')
parser.add_argument('--donor', help='Path to donor pptx file', required=True)
parser.add_argument('--tracks_path', help='Path to gpx tracks file', required=True)

args = parser.parse_args()
tracks_path = args.tracks_path
donor = args.donor

#check paths check
slide_path, temp_unpack_path = checkPathandInitialization(donor, tracks_path)
GPXData = getTrackData(tracks_path)
narrativeEntries = getNarratives(tracks_path)
interval = getInterval(tracks_path)
createPptxFromTrackData(GPXData, narrativeEntries, interval, slide_path, temp_unpack_path)