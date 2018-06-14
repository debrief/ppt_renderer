#Script for unpacking a pptx file
#Sample run - python unpack.py Desktop/sample.pptx Desktop/unpack
import zipfile
import argparse
import os
import shutil
import sys

parser = argparse.ArgumentParser(description='Script to unpack pptx file')
parser.add_argument('--pptx_path', help='Path to original pptx file',  required=False)
parser.add_argument('--unpack_path', help='Path to destination folder for unpacked pptx file', required=False)
args = parser.parse_args()

pptx_path = args.pptx_path
unpack_path = args.unpack_path

if(pptx_path == None):
    print("Provide pptx_path (path to packed pptx file)")
    sys.exit(1)

if(unpack_path==None):
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
