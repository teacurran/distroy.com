#!/bin/sh

# -k: convert links to relative
# -E: name all files .html
# -r: recursive
# -l: recursive depth
# -p: download all supporting files
# -N: no timestamps
# -F: force files to HTML
# -nH: put everything in this directory

wget -k -E -r -l 10 -p -N -F -nH http://distroy.com/



