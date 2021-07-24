#!/bin/bash
for file in /home/pi/pixelcade/mp4marquees1920/*
do
  if ffprobe $file 2>&1 | grep -q 'Invalid'; then
    echo "$file is bad"
    #sudo rm $file
  else
    echo "$file is good"
  fi
    #whatever you need with "$file"
done
