#!/bin/bash

font=mono12
small=smblock

clear
toilet -f $font -t Audible
toilet -f $font -t Algorithms
toilet -f $font -t "   "
toilet -f $small -t "Aaron Zimmerman ( @apzimmerman )"
read

clear
toilet -f $font -t Pick a number
toilet -f $font -t 0-11
toilet -f $small -t "( inclusive )"


read
clear
toilet -f $font -t Sound as a
toilet -f $font -t stream of
toilet -f $font -t numbers


read
clear
toilet -f $font -t Sample Rate
toilet -f $font -t Bit Depth
toilet -f $font -t Frequency
toilet -f $font -t Amplitude

read

clear

toilet -f $font -t There is no reason
toilet -f $font -t to play beethoven
toilet -f $font -t with Overtone

read
clear

toilet -f $font -t Form
read
toilet -f $small -t Verse-Chorus
toilet -f $small -t 12 Bar Blues
toilet -f $small -t Sonata
toilet -f $small -t Canon

read
clear


toilet -f $font -t Form is a bridge
toilet -f $font -t between composer
toilet -f $font -t and listener


read
clear


toilet -f $font -t Without form
toilet -f $font -t music gets boring
toilet -f $font -t very quickly

read
clear

toilet -f $font -t Fractals
toilet -f $font "   "
toilet -f $small seed_numbers


read
clear

toilet -f $font -t there are 12
toilet -f $font  -t \"pitch classes\"


read
clear

toilet -f $font -t Represent a note
toilet -f $font -t as a map
toilet -f $small -t ":p   for pitch (vector)"
toilet -f $small -t ":d   for duration"


read
clear

toilet -f $font -t Then create melodies
toilet -f $font -t and transform them
toilet -f $small -t "reverse,   inversion,   lengthen,   shorten,   add-interval,   transpose"


read
clear

toilet -f $font -t Create a vocabulary
toilet -f $font -t of such functions


read
clear

toilet -f $font -t for each number n:
toilet -f $small -t "play   the   first   n   vocab   functions"
toilet -f $small -t "each   starting   from   pitch   class   n"
toilet -f $small -t "for   n   seconds"
toilet -f $small -t "at   n   speed"

#read
#clear
#
#toilet -f $font -t Windows
#toilet -f $font "   "
#toilet -f $small file_path
#toilet -f $small scalars
#toilet -f $small window_size
#toilet -f $small window_adjust


read
clear

toilet -f $small -t "Iannis   Xenakis"
toilet -f $small -t "John   Cage"
toilet -f $small -t "Karlheinz Stockhausen"
toilet -f $small -t "Philip   Glass   /   Steve   Reich"
toilet -f $small -t "Avro   Pärt"

toilet -f $small -t soundcloud.com/apzimmerman
toilet -f $small -t github.com/aaronzimmerman/overtone-compositions






