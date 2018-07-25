# ppt_renderer
Development project to produce body of scripts for manipulating PPT slide

## unpack_pack script -
To run the script -

## Unpack:

python scripts/unpack.py Desktop/sample.pptx Desktop/unpack

python scripts/unpack.py --pptx_path Desktop/sample.pptx

## Pack:

python scripts/pack.py --pptx_path Desktop/sample.pptx --unpack_path Desktop/unpack

python scripts/pack.py --unpack_path unpacked_pptx/

## FindMap:

python scripts/findmap.py --unpack_path map_rect

## plot_gpy.py:

python scripts/plot_gpx.py --tracks_path track_data/long_tracks.txt --unpack_path donor

## Run Help -

python pack.py -h

python pack.py -h

# Documentation for PPT RENDERER

## Overall Idea – 
We have created a bunch of scripts using Python to render information obtained from a GPX data file onto a PowerPoint slide. The output of the scripts would be a .pptx file which would contain animations and renders of the tracks contained in the GPX file. We do this by unzipping a “donor” .pptx file (.pptx file is just a zipped file)

## Components – 
1.	Donor.pptx – This is a PowerPoint presentation containing a single slide. The slide contains components such as “map” (rectangular shape), “marker” (rectangular callout), “track” (freehand shape)”, “time” (rectangular shape which will containing the current time of the ongoing track animation (derived from GPX data))
2.	Donor – this is an unzipped version of donor.pptx
3.	Scripts – This is a folder containing all the scripts related to the project

## Scripts – 
1.	findmap.py – This script contains functions and logic code to retrieve dimensions, offset and other details about a rectangular shape named “map” from donor folder. It analyses the file named “slide.xml”.
2.	pack_function.py – This script contains functions and logic code to create a pptx file from a folder of xml files. It copies the folder and renames it to give a .pptx extension. 
3.	parse_presentation.py – This script contains functions and logic code to retrieve the dimensions of the slide by parsing the file named – “presentation.xml”
4.	parse_tracks.py – This script parses a GPX data file and retrieves all the information about tracks from the file.
5.	plot_gpx.py – This script is the core of the overall project. It takes donor folder and gpx data file as input parameters. It extracts information about the map and appends one track(freehand shape) tag, one time(shape containing current time) tag, marker (callout) tag, and animation tags (<p:seq> per track, <animMotion> tag per coordinate in the track).
6.	unpack.py – This script is used to unzip a pptx file.

## Things derived from GPX data file 

Track Information:
1.	Color representing the track
2.	Track coordinates
3.	Timestamps for each coordinates
4.	Name of the track
