#!/usr/bin/python

import glob
import hashlib
import json

# base url where to find the audio file
defaultUrl = 'http://nemiz.co'

files = [{'name': f,'md5': hashlib.md5(open(f, 'rb').read()).hexdigest(),'url': defaultUrl + '/' + f } for f in glob.glob('audio/*.mp3')]
with open('audio/audio.json', 'w') as o:
	json.dump({'count': len(files), 'files': files}, o)

