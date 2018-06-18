#Script for unpacking a pptx file
#Sample run - python unpack.py Desktop/sample.pptx Desktop/unpack
import zipfile
import argparse
import os
import shutil
import sys

from unpack_function import unpackFunction

parser = argparse.ArgumentParser(description='Script to unpack pptx file')
parser.add_argument('--pptx_path', help='Path to original pptx file',  required=False)
parser.add_argument('--unpack_path', help='Path to destination folder for unpacked pptx file', required=False)
args = parser.parse_args()

pptx_path = args.pptx_path
unpack_path = args.unpack_path

unpackFunction(pptx_path, unpack_path)
