#Script for unpacking/packing a pptx file
#Sample run - python pack_unpack.py 1 Desktop/sample.pptx Desktop/unpack
import zipfile
import argparse
import os
import shutil

parser = argparse.ArgumentParser(description='Script to unpack/pack pptx file----->1-unpack, 2-pack')
parser.add_argument('option', help='Unpack/Pack Mode, 1-unpack')
parser.add_argument('pptx_path', help='Path to original pptx file')
parser.add_argument('unpack_path', help='Path to destination folder for unpacked pptx file')
args = parser.parse_args()

option = int(args.option)
pptx_path = args.pptx_path
unpack_path = args.unpack_path

if(option==1):
    #Unpack the pptx file
    print("Unpacking pptx file...")
    if not os.path.exists(unpack_path):
        os.makedirs(unpack_path)
    zip_ref = zipfile.ZipFile(pptx_path, 'r')
    zip_ref.extractall(unpack_path)
    zip_ref.close()
    print("File unpacked at "+unpack_path)

if(option==2):
    #Pack the unpack_path folder to pptx_path pptx file
    shutil.make_archive(pptx_path, 'zip', unpack_path)
    os.rename(pptx_path+".zip", pptx_path)
    print("File packed at "+pptx_path)
