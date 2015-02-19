(ns com.zimmermusic.spheres
  (:require [clojure.string :as str])
  (:require [shadertone.tone :as t])
  (:use overtone.live))


(import '(com.zimmermusic TemporalVariable))




; ************
; Probability distributions
; ************

(def intervals [:down-fourth :down-min-third :up-fourth :root :down-octave :up-fifth :down-fifth :up-octave :up-maj-third])

(def roots [50 50 50 52 52 48 54 51 55 46 40])

(def durs [ 10000 10000 10000 10000 14000 14000 7000 20000 4000 4000 3000])

(def rests [-4000 -3000 -3000 -3000 -3000 -2000 -2000 -2000 -1000 -1000 -1000 0 0 500 1000 1000 2000 5000])

(def num-notes-to-use[5 4 4 4 3 3 3 3 3 3 2 2 2 1 1 1])


(def decisions [:new :alter :alter :alter :alter :alter])


(defn build-chord [intervals num]
  ;return a vector of pitches in midi note values
  (conj (take num (shuffle intervals)) :root)) ;include fundamental in vector





; ********
; Instrument config
; ********

(def interval->ratio
  {:root 1.0
   :up-octave 2.0
   :down-octave 0.5
   :up-fifth  (/ 3 2)
   :down-fifth  (/ 2 3)
   :up-fourth (/ 4 3)
   :down-fourth (/ 3 4)
   :up-maj-third (/ 5 4)
   :down-maj-third (/ 4 5)
   :up-min-third (/ 6 5)
   :down-min-third (/ 5 6)})

(def samples {
               "m38.aiff" 38
               "m41.aiff" 40
               "m44.aiff" 44
               "m47.aiff" 47
               "f50.aiff" 50
               "f53.aiff" 53
               "f56.aiff" 56
               "f59.aiff" 59
               "f62.aiff" 62
               "f65.aiff" 65
               "f68.aiff" 68
               "f71.aiff" 71
               "f74.aiff" 74
               "f77.aiff" 77
               "f80.aiff" 80
               "f83.aiff" 83
               })

(def file-lookup
  (into {} (map (fn [[a b]] [b a]) samples)))

(def CHOIR-SAMPLE-FILES
  (map #(str "resources/" %) (keys samples)))

(defonce choir-samples
  (doall (map load-sample CHOIR-SAMPLE-FILES)))

(defn note-from-filename[name]
  (Long/parseLong (subs name 1 3)))

(defn- buffer->midi-note [buf]
  (note-from-filename (:name buf)))

(defn- note-index
  "Returns a map of midi-note values [0-127] to buffer ids."
  [buffers]
  (reduce (fn [index buf]
            (let [note (buffer->midi-note buf)
                  id   (-> buf :id)]
              (assoc index note id)))
    {}
    buffers))

;; Silent buffer used to fill in the gaps.
(defonce ^:private silent-buffer (buffer 0))

(defonce index-buffer
  (let [tab (note-index choir-samples)
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))


(definst sampled-choir
  [note 68 duration 1 rate 1 level 1 loop? 0
   decay 1   reverb 20 curve -4 gate 1]
  (let [buf (index:kr (:id index-buffer) note)
        sustain (* 0.45 duration)
        attack (* 0.15 duration)
        release (* 0.3 duration)
        ; env (env-gen (adsr attack decay sustain release level curve)
        env (env-gen (envelope [0 0.8 0.6 0.15 0] [attack sustain release reverb])
              ;env (env-gen (perc (* 0.8 sustain) (* 0.2 sustain))
              :gate gate
              :action FREE)

        mover1 (sin-osc:kr 0.03)
        mover2 (lf-saw:kr 0.01)
        mover (* mover1 mover2)]
    ;mover (lin-rand)]
    (pan2
      (* env
        (scaled-play-buf 2 buf rate :level 0.8 :loop 0 :action FREE))
      mover)))


(defn choose-lesser[left right hz]
  (let [left-hz (midi->hz left)
        right-hz (midi->hz right)
        left-diff (Math/abs (- hz left-hz))
        right-diff (Math/abs (- hz right-hz))]
    (if (< left-diff right-diff)
      left
      right)))


(defn get-nearest-sample-of-hz[hz]
  ; find the midi note value whose base hz is closest to that passed in
  (let [available (vals samples)]
    (reduce #(choose-lesser %1 %2 hz)
      available)))



(use '[clojure.set :only [difference]])

(defn build-chord-with-octaves [num]
  ;return a vector of pitches in midi note values
  (for [i (conj (take num (shuffle intervals)) :root)]
    {:interval i,
     :octave (inc (rand-int 3))}))

(defn change-chord[chord]
  (let [current-intervals (for [c chord] (c :interval))
        not-used (difference (set intervals) (set current-intervals))
        to-keep (rest (shuffle chord))
        new-note {:interval (first (shuffle not-used))
                  :octave (inc (rand-int 3))}]
    (conj to-keep new-note)))


; Function to play a chord, figures out which sample to play and how to adjust it to get the correct interval from root
; invokes the instrument with the correct playback rate
(defn chr2[root notes duration]
  ;intervals is a verctor of maps :pitch :octave
  (let [root-freq (midi->hz root)]
    (doseq [note notes]
      (let [ octave (note :octave)
             ratio (interval->ratio (note :interval))
             pitch-hz (* (* octave root-freq) ratio) ; add 1-3 octaves and then scale to get an interval from there
             sample-to-play (get-nearest-sample-of-hz pitch-hz)
             hz-of-sample (midi->hz sample-to-play)
             adjust-rate (/ pitch-hz hz-of-sample)]
        (sampled-choir :note sample-to-play :duration duration :rate adjust-rate)))))


; **************
; viz stuff
; *******************


(defn line-between-in-x-samples[start end grain]
  (let [amount-to-increase ( / (- end start) (double grain))]
    (for [i (range grain)]
      (+ start (* i amount-to-increase)))))

(defn env-for-duration[duration]
  (let  [ num-samples (/ duration 20)
          sustain (* 0.65 num-samples)
          attack (* 0.05 num-samples)
          release (* 0.3 num-samples)
          reverb 1000
          first (line-between-in-x-samples 0 0.8 attack)
          second (line-between-in-x-samples 0.8 0.6 sustain)
          third (line-between-in-x-samples 0.6 0.15 release)
          fourth (line-between-in-x-samples 0.15 0 reverb)]
    (flatten [first second third fourth])))

;(def intervals [:down-fourth :down-min-third :up-fourth :root :down-octave :up-fifth :down-fifth :up-octave :up-maj-third])
(def interval-intensities
  {:unisons (TemporalVariable. "Unisons")
   :fifths (TemporalVariable. "Fifths" )
   :thirds (TemporalVariable. "Thirds")})



(def int->bucket {
                   :root :unisons
                   :down-octave :unisons
                   :up-octave :unisons
                   :up-fifth :fifths
                   :down-fifth :fifths
                   :up-fourth :fifths
                   :down-fourth :fifths
                   :up-maj-third :thirds
                   :down-min-third :thirds })

(defn get-strengths[intervals]
  (let [coll (for [i intervals]
               (int->bucket i))]
    (reduce #(assoc %1 %2 (inc (%1 %2 0))) {} coll)))


(defn visualize-intervals[intervals duration]
  ; this creates temporal additions to three variables
  ; track unison-intensity, fifth-intensity - third-intensity
  (let [envelope (env-for-duration duration)
        strengths (get-strengths intervals)]
    ; (println "visualizing " intervals " for duration " duration " which maps to strengths " strengths " with total samples " (count envelope))
    (doseq [[bucket strength] strengths]
      (let [intensities (map #(* %1 strength 0.3) envelope)]
        (.addEnvelope (interval-intensities bucket) intensities)))))



(def my-rgb (atom [0.0 0.0 0.0]))

(defn visualizer[]

  (let [unison-strength (.valueForNow (interval-intensities :unisons))
        fifth-strength (.valueForNow (interval-intensities :fifths))
        thirds-strength (.valueForNow (interval-intensities :thirds))
        now (System/currentTimeMillis)
        next (+ now 20)]
    (swap! my-rgb (fn [x] [unison-strength fifth-strength thirds-strength]))
    (apply-at next #'visualizer[])))



; ************
;  App logic
; **********


(defn play2 [chord fund]

  (let [ decision (first (shuffle decisions))

         next-chord (case decision
                      :new (build-chord-with-octaves 3)
                      :alter (change-chord chord))
         time (System/currentTimeMillis)
         fund-change? (< 0.8 (rand))
         new-fund (if fund-change?
                    (first (shuffle roots))
                    fund)
         dur (first (shuffle durs))
         rest (first (shuffle rests))]
    (println "playing chord " (sort (for [c chord] (c :interval))) " from fund " new-fund " dur " dur " rest " rest)
    (chr2 fund chord (/ dur 1000))

    (visualize-intervals (for [c chord] (c :interval)) dur)
    (let [next-time (+ time dur rest)]
      (apply-at next-time #'play2[next-chord new-fund]))))


;(t/start-fullscreen
(t/start "resources/rgb.glsl" :user-data { "iRGB" my-rgb})


(defn start[]
  (def interval-intensities
    {:unisons (TemporalVariable. "Unisons")
     :fifths (TemporalVariable. "Fifths" )
     :thirds (TemporalVariable. "Thirds")})
  (let [chord (build-chord-with-octaves 3)]
    (play2 chord 50)
    (visualizer)))


(defn end[]
  (defn play2[chord fund]
    (println "the end")))

