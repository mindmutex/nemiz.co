#!/usr/bin/python

import glob
import hashlib
import json

from os import path

# base url where to find the audio file
defaultUrl = 'http://a.nemiz.co'

files = [{'name': path.basename(f),'md5': hashlib.md5(open(f, 'rb').read()).hexdigest(),'url': defaultUrl + '/' + path.basename(f) } for f in glob.glob('audio/*.mp3')]
with open('audio/audio.json', 'w') as o:
	json.dump({'count': len(files), 'files': files}, o)

