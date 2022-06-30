#!/bin/sh
ffmpeg -f concat -safe 0 -i $1 -c copy $2
