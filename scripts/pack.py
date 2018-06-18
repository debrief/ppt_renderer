#Script for packing a pptx file
#Sample run - python pack.py Desktop/sample.pptx Desktop/unpack

import argparse
from pack_function import packFunction

parser = argparse.ArgumentParser(description='Script to pack a unpacked pptx file')
parser.add_argument('--pptx_path', help='Path to destination packed pptx file', required=False)
parser.add_argument('--unpack_path', help='Path to unpacked pptx folder', required=False)
args = parser.parse_args()

pptx_path = args.pptx_path
unpack_path = args.unpack_path

packFunction(pptx_path, unpack_path)
