#!/usr/bin/env python3

import json
import speech_recognition as sr
import urllib
import urllib2

# obtain path to "test.wav" in the same folder as this script
from os import path
WAV_FILE = path.join(path.dirname(path.realpath(__file__)), "test.wav")

# use "test.wav" as the audio source
r = sr.Recognizer()
with sr.WavFile(WAV_FILE) as source:
    audio = r.record(source) # read the entire WAV file

# recognize speech using Google Speech Recognition
try:
    # for testing purposes, we're just using the default API key
    # to use another API key, use `r.recognize_google(audio, key="GOOGLE_SPEECH_RECOGNITION_API_KEY")`
    # instead of `r.recognize_google(audio)`
    value = r.recognize_google(audio)
    if str is bytes: # this version of Python uses bytes for strings (Python 2)
        print(u"{}".format(value).encode("utf-8"))
    else: # this version of Python uses unicode for strings (Python 3+)
        print("{}".format(value))
   
    userInput =  u"{}".format(value).encode("utf-8")
    data = {'text': userInput}
    dataSend = urllib.urlencode(data)
    url = "http://text-processing.com/api/sentiment/"
    #print dataSend
    headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}

    # build a request
    request = urllib2.Request(url, dataSend)
    # add any other information you want
    request.add_header("Content-Type",'application/x-www-form-urlencoded')

    res = urllib2.urlopen(request)
    jsonFile = res.read()
    jsonData = json.loads(jsonFile)
    print jsonData['label']
except sr.UnknownValueError:
    print("Google Speech Recognition could not understand audio")
except sr.RequestError as e:
    print("Could not request results from Google Speech Recognition service; {0}".format(e))
    
