# ppt_renderer
Development project to produce body of scripts for manipulating PPT slide

# unpack_pack script -
To run the script -

Unpack:

python scripts/unpack.py Desktop/sample.pptx Desktop/unpack

python scripts/unpack.py --pptx_path Desktop/sample.pptx

Pack:

python scripts/pack.py --pptx_path Desktop/sample.pptx --unpack_path Desktop/unpack

python scripts/pack.py --unpack_path unpacked_pptx/

FindMap:

python scripts/findmap.py --unpack_path map_rect

Run Help -

python pack.py -h

python pack.py -h
