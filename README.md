# overtone-compositions

Collection of pieces written in clojure and overtone by Aaron Zimmerman (https://twitter.com/apzimmerman).

## Windows:

https://soundcloud.com/aaron-zimmerman/windows-melody
https://soundcloud.com/aaron-zimmerman/windows-speech


 lein repl
 (use 'com.zimmermusic.windows)
 (windows "/path/to/sample.aiff" [2 3 4] 10000 400)

 the first argument is an audio file.  It needs to be sampled at 44100 and stereo.
 second argument is a vector of scalars at which to create additional windows past the one created with:
 third argument:  the base window size, in samples.
 fourth argument:  the base step size, how far to move the window after each repetition



## Spheres:


https://www.youtube.com/watch?v=8Y7hDDBCd2M
https://soundcloud.com/aaron-zimmerman/spheres

    lein repl
    (use 'overtone.live)
    (use 'com.zimmermusic.spheres)

    (start)
    .. music plays ..
    (end)
    .. music fades away into oblivion ..

NOTE that you will not be able to run as is, as the piece references a series of samples that I cannot include due to licensing.

So to run, you need to create samples under overtone-compositions/resources:
   "m38.aiff"
   "m41.aiff"
   "m44.aiff"
   "m47.aiff"
   "f50.aiff"
   "f53.aiff"
   "f56.aiff"
   "f59.aiff"
   "f62.aiff"
   "f65.aiff"
   "f68.aiff"
   "f71.aiff"
   "f74.aiff"
   "f77.aiff"
   "f80.aiff"
   "f83.aiff"

 These should all be around 60 seconds of an instrument playing the midi note represented in the file name.
 I can help you figure out how to get these do this if you are interested. Maybe I can find some on freesound but I haven't been able to yet.



## Diamonds:

https://soundcloud.com/aaron-zimmerman/diamonds

    lein repl
    (use 'com.zimmermusic.diamonds)
    (diamonds 10 14000)  ; 10 = maximum number of voices to build to, 14000 = duration of each section
    ... piece is bootstrapped and a screen pops up
    press return in the new console screen




Copyright © 2015

Distributed under the Eclipse Public License, the same as Clojure.
