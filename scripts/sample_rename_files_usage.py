#python scripts/sample_rename_files_usage.py --src_path honolulu_engagment/ --skip_factor 5 --start_date 19951212_093600

import argparse
from rename_files import renameFiles

parser = argparse.ArgumentParser(description='Script to rename files')
parser.add_argument('--src_path', help='Path to original folder containing files that are to be renamed', required=True)
parser.add_argument('--skip_factor', help='Skip Factor', required=False)
parser.add_argument('--start_date', help='First file name', required=False)
args = parser.parse_args()

src_path = args.src_path
skip_factor = args.skip_factor
if(skip_factor is None):
    skip_factor = 1
start_date = args.start_date
if(start_date is None):
    start_date = '19951212_093600'

renameFiles(src_path, skip_factor, start_date)
