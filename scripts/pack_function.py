import zipfile
import os
import shutil
import sys

def packFunction(pptx_path=None, unpack_path=None):
    print pptx_path, unpack_path

    if(unpack_path is None):
        print("Provide unpack_path (path to directory containing unpacked pptx)")
        sys.exit(1)

    if(pptx_path is None):
        if(unpack_path[-1]=="/" or unpack_path[-1]=="\\"):
            pptx_path = unpack_path[0:-1]+".pptx"
        else:
            pptx_path = unpack_path[0:-1]+".pptx"

    #check if unpack_path is directory or not
    if(not os.path.isdir(unpack_path)):
        print "unpack_path provided is not a directory"
        sys.exit(1)

    #Pack the unpack_path folder to pptx_path pptx file
    shutil.make_archive(pptx_path, 'zip', unpack_path)
    os.rename(pptx_path+".zip", pptx_path)
    print("File packed at "+pptx_path)
