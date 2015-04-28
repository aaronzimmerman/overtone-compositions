(ns com.zimmermusic.fractals
  (:use overtone.live))

(use 'overtone.inst.sampled-piano)

(def numbers (take 12 (repeatedly #(rand-int 12))))

(def geekfest '(1 5 1 6 7 11 4 2 6 2))

(def minimalism [0 1 5 2 8 1 10 1 7 1])

(def control (atom 0))

(defn play[time-left notes octave o-var label]
  (let [ n (first notes)
         pitches (n :p)
         beat-length (scale-range @control 0 11 0.450 0.170)
         next-beat-at (+ (now) (mod (now) beat-length))
         duration (* (n :d) beat-length)
         next-start (+ (now) (* 1000 duration))
         next-notes (concat (rest notes) [n])
         plus-pitches (* 12 (+ octave (rand-int o-var)))
         time-remaining (- time-left (* 1000 duration))
         level (scale-range @control 0 12 0.7 0.25)]
    (doseq [pitch pitches]
        (sampled-piano (+ plus-pitches pitch @control) :sustain (* 0.85 duration) :level level :release 0.1))

    (if (< time-remaining 0)
      (println label " done")
      (apply-at next-start #'play[time-remaining next-notes octave o-var label]))))


; Helper functions

(defn audit[numbers]
  "Turn numbers into melody and play"
  (let [melody (for [[a b] (partition 2 numbers)] {:p [a] :d b})]
    (println melody)
    (play 100000 melody 5 0 "audit")))

;(defn play[time-left notes octave o-var label]
(defn taudit[numbers t]
  "Turn numbers into melody, transform, and play"
  (let [melody (for [[a b] (partition 2 numbers)] {:p [a] :d b})
        transformed (t melody)]
    (println transformed)
    (play 100000 transformed 5 0 "audit")))


; **********
; Transforms
; **********


(defn dist-from-first[motif]
  (let [first-pitch (first (get (first motif) :p))]
    (map #(- (first (%1 :p)) first-pitch)
      (rest motif))))

(defn apply-intervals[note interval]
  ;(println "interval " interval " note " note)
  {:p [(- (first (note :p)) (* 2 interval))]
   :d (get note :d)})


(defn inversion[motif]
  "Inverts each interval, moving the same steps in the opposite direction"
  (let [dists (dist-from-first motif)]
    (conj
      (map apply-intervals
        (rest motif)
        dists)
      (first motif))))


(defn transpose[motif steps]
  "Moves each pitch up or down by steps"
  (for [elem motif]
    {:p (flatten (for [pitch (elem :p)] (+ pitch steps)))
     :d (elem :d)}))

(defn up-second[motif]
  (transpose motif 2))

(defn lengthen[motif]
  "Doubles the length of every note"
  (for [elem motif]
    {:p  (elem :p)
     :d  (* 2 (elem :d))}))

(defn shorten[motif]
  "Halves the length of every note"
  (for [elem motif]
    {:p  (elem :p)
     :d  (* 0.5 (elem :d))}))

(defn add-interval[motif interval]
  (for [elem motif]
    {:p  (conj (elem :p) (+ interval (first (elem :p))))
     :d  (elem :d)}))


(defn change-pitch[motif note]
  (for [elem motif]
    {:p  [note]
     :d  (elem :d)}))


(defn add-duration[motif num]
  (for [elem motif]
    {:p  (elem :p)
     :d  (+ num  (elem :d))}))





(defn build-vocab [numbers]
  "construct a large vector of playable functions that take a duration and a name"

  (let [melodic (for [[a b] (partition 2 numbers)] {:p [a] :d b})
        harmonic (for [[a b c] (partition 3 numbers)] {:p [a b] :d c})]
    [ [#(play %1 melodic 6 0 %2), "melodic6"]
      [#(play %1 (inversion (shorten melodic)) 5 2 %2), "melodic-n7-short"]
      [#(play %1 harmonic 3 5 %2), "harmonic"]
      [#(play %1 (shorten (transpose melodic @control)) 4 4 %2), "melodic-n0"]
      [#(play %1 (add-interval melodic @control) 3 1 %2), "melodic3-w5"]
      [#(play %1 (inversion melodic) 4 0 %2), "melodic3"]
      [#(play %1 (lengthen melodic) 6 0 %2), "melodic-long"]
      [#(play %1 (lengthen harmonic) 3 5 %2), "harmonic-lengthen"]
      [#(play %1 (add-interval (change-pitch melodic 0) 7) 3 1 %2), "melodic-n7"]
      [#(play %1 melodic 5 1 %2), "melodic5-1"]
      [#(play %1 (shorten (add-interval (reverse (inversion melodic)) @control)) 3 1 %2), "mel-rev-inv"]
      [#(play %1 (shorten (shorten melodic)) 6 2 %2), "melodic-supershort"]]))


(defn tv[numbers]
  "Test the vocab"
  (let [vocab (build-vocab numbers)]
    (for [[f name] vocab]
      (do
        (println "testing " name)
        (f 10000 name)
        (Thread/sleep 10000)))))


(defn play-shape[numbers vocab]
  "Take the first number from numbers and play a section with that as the key"

  (println "*************************")
  (println "****** " (first numbers) "*******")
  (println "*************************")
  
  (let [n (first numbers)
        num-to-play (inc n)
        chosen (take num-to-play vocab)
        duration (scale-range n 0 11 6000 12000) ; no 0 durations
        next-start (+ (now) duration)
        remaining (rest numbers)]

    (println "Playing " num-to-play " until " next-start " using " (for [[c name] chosen] name))
    (reset! control n)

    (if (not-empty remaining)
      (apply-at next-start #'play-shape[remaining vocab])
      (println "Done"))

    (doseq [[f name] chosen]
      (f duration name))))


(defn fractals[numbers]
  (let [vocab (build-vocab numbers)]
    (play-shape (conj numbers 0) vocab)))


; (fractals numbers)
