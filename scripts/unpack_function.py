import zipfile
import os
import shutil
import sys

def unpackFunction(pptx_path=None, unpack_path=None):
    if(pptx_path is None):
        print("Provide pptx_path (path to packed pptx file)")
        sys.exit(1)

    if(unpack_path is None):
        unpack_path = pptx_path[0:-5]

    #check if unpack_path is directory or not
    if(not os.path.isfile(pptx_path) or not pptx_path.endswith("pptx")):
        print "pptx_path provided is not a pptx file"
        sys.exit(1)

    #Unpack the pptx file
    print("Unpacking pptx file...")
    if not os.path.exists(unpack_path):
        os.makedirs(unpack_path)
    zip_ref = zipfile.ZipFile(pptx_path, 'r')
    zip_ref.extractall(unpack_path)
    zip_ref.close()
    print("File unpacked at "+unpack_path)
