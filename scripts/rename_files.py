import os
import sys
import shutil
from datetime import datetime
from datetime import timedelta

from PIL import Image
from PIL import ImageFont
from PIL import ImageDraw

def renameFiles(src_path, skip_factor=1, first_file_name='19951212_093600'):
    skip_factor = int(skip_factor)
    #First filename const = 19951212_093600
    file_name = datetime.strptime(first_file_name, '%Y%m%d_%H%M%S')

    src_path = os.path.join(src_path)

    if not os.path.exists(src_path):
        sys.exit(1)
    dest_path = src_path[0:-1]+"_renamed"
    dest_path = os.path.join(dest_path)
    if os.path.exists(dest_path):
        shutil.rmtree(dest_path)
        os.mkdir(dest_path)
    else:
        os.mkdir(dest_path)

    #Copying the required files
    i = 0
    for root, dirs, files in os.walk(src_path):
        files.sort()
        for f in files:
            if(f.endswith('.jpg')):
                if(i%skip_factor == 0):
                    shutil.copy(src_path+'/'+f, dest_path + '/' + f)
                i+=1

    #Renaming files
    for root, dirs, files in os.walk(dest_path):
        files.sort()
        for f in files:

            file_name_str = file_name.strftime('%Y%m%d_%H%M%S')

            date_time_strs = file_name_str.split('_')
            img = Image.open(dest_path+'/'+f)
            draw = ImageDraw.Draw(img)
            font = ImageFont.truetype("Arial.ttf", 96)
            draw.text((0, 0),date_time_strs[0],(255,255,255),font=font)
            draw.text((950, 0),date_time_strs[1],(255,255,255),font=font)
            img.save(dest_path+'/'+f)

            os.rename(dest_path+'/'+f, dest_path+ '/' + file_name_str+'.jpg')
            file_name = file_name + timedelta(seconds=20*skip_factor)

    print "All files renamed successfully..."
