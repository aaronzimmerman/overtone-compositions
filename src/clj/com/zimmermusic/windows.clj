(ns com.zimmermusic.windows
  (:require [lanterna.screen :as s]
            [clojure.string :as str])
  (:use overtone.live))

(def scr (s/get-screen :swing  {:cols 120
                                :rows 45}))

(s/start scr)

(defn update-ui[id start-at]
  (let [x   (inc id)
        val (str start-at)]
    (doseq [i (range (count val))]
      (let [c (nth val i)
            y (+ 0 i)]
        (s/put-string scr y x (str c) {}))))
  (s/redraw scr))


(defn bp2[])

(defn duration-for-num-samples[num-samples]
  (/ num-samples 44100.0))


(defn play-window[sample window-length adjust start-at pan level index]
  (let [duration (duration-for-num-samples window-length) ;convert samples to seconds
        next-start-samp (+ adjust start-at) ; add the adjust to the sample start for the next iteration
        next-start-time (+ (now) (* 1000 duration))
        pan-adjust (/ (- (rand-int 10) 5) 100.0)
        new-pan (+ pan pan-adjust)]
    (bp2 start-at duration pan level)
    (update-ui index start-at)
    (if (<= (+ window-length next-start-samp) (:size sample))
      (apply-at next-start-time #'play-window[sample window-length adjust next-start-samp new-pan level index])
      (println "window length " window-length " done"))))



(defn duration-for-window-size[window adjustment total]
  (let [iterations (- (/ total adjustment) (/ window adjustment))
        total-samples (* window iterations)
        length (/ total-samples 44100)]
    length))


;; long sampler stuff
(defn index-of-breaks[data sample-rate threshold window-size]
  (let [ downsampled  (vec (take-nth sample-rate data))
         x-vals (vec  (map #(* sample-rate %)  (range (count downsampled))))
         with-index (zipmap x-vals downsampled)
         total (count downsampled)]
    (filter identity        ;gets rid of nils
      (for [i (range 0 (- total window-size) window-size)] ; loop through all samples and fine each place where begins a sequence of window-size samples less than threshold
        (let [current (with-index i)
              total  (reduce + (filter identity (for [j (range i (+ i window-size))] (with-index j))))
              average-samp (/ total window-size)]  ; builds the sum all all samples starting at i
          (when (<= average-samp threshold) (* i sample-rate)))))))



(defn build-bounds[indexes max-length]
  (filter identity
    (apply concat
      (for [i (range (dec (count indexes)))]
        (let [subvec (nthnext indexes i)
              start (first subvec)
              remaining (rest subvec)]
          (for [n remaining]
            (when (<= (- n start) max-length)
              [start n])))))))


(defn duration-for-num-samples[num-samples]
  (/ num-samples 44100.0))

(defn play-for-duration[start stop total-duration level]
  (let [ sample-dur (duration-for-num-samples (- stop start))
         iter-dur (if (<= total-duration sample-dur) total-duration sample-dur)
         next-start (+ (now) (* 1000 iter-dur))
         remaining (- total-duration iter-dur)]
    (bp2 start iter-dur 0.0 level)
    (when (> remaining 0)
      (apply-at next-start #'play-for-duration[start stop remaining level]))))


(defn long-sampler[s duration-remaining bounds level]
  (let [duration (min (+ (* (rand-int 10) 3) 10) duration-remaining) ; play up to 30 seconds
        avail (count bounds)
        chosen (rand-int avail)
        [start stop] (nth bounds chosen)
        next-start (+ (now) (* 1000 duration))
        remaining-time (- duration-remaining duration)]
    (println "playing bounds " start stop " for " duration " with total time remaining " remaining-time)

    (play-for-duration start stop duration level)

    (if (> remaining-time 0)
      (apply-at next-start #'long-sampler[s remaining-time bounds level])
      (println "long sampler done"))))




(defn go [s scalars base-window base-adjust]

  (definst bp2[start 0 duration 1.0 position 0.5 volume 0.5]
    (let [buf (:id s)
          env (env-gen (envelope [0.0 0.8 0.8 0.0] [0.1 duration 0.1]) :action FREE)]
      (pan2
        (*
          volume
          env
          (play-buf 2 :bufnum buf :action FREE :loop 0 :start-pos start))
        position)))

  (definst play-sample[]
    (play-buf 1 s :action FREE))

  (let [sample-count (:size s)
        sample-duration (duration-for-num-samples sample-count)
        base-duration (duration-for-window-size base-window base-adjust sample-count)]
    (println "Sample duration is " sample-duration)
    (println "The destructured piece will last " (int base-duration)  " seconds")

    (play-sample)
    (Thread/sleep (* 950 sample-duration))    ; starts right as the sample is ending

    ; base iteration
    (play-window s base-window base-adjust 0 0.5 0.3 1)


    (doseq [x scalars]
      (let [window (* x base-window)
            adjust (* x base-adjust)
            duration (duration-for-window-size window adjust sample-count)
            start-at (long (+ (now) (* 1000 (- base-duration duration))))]
        (println x " will start at " start-at " and last " + duration " seconds, with window size of " window)

        (apply-at start-at #'play-window[s window adjust 0 0.0 0.3 x])))


    ;    (let [all-data (vec (buffer-data s))
    ;          indexes (index-of-breaks all-data 300 0.0 100)
    ;          bounds (build-bounds indexes 100000)
    ;          start-at (+ (now) (* 2000 sample-duration))
    ;          second-start-at (+ (now) (/ base-duration 2))]
    ;      (apply-at start-at #'long-sampler[s (- base-duration sample-duration) bounds 1.0])
    ;      (apply-at second-start-at #'long-sampler[s (- base-duration sample-duration) bounds 1.3]))


    ;play whole sample once at the end
    (let [coda-start (+ (now) (* base-duration 1000))]
      (apply-at coda-start #'play-sample))))


(defn windows [file scalars base-window base-adjust]
  (go (load-sample file) scalars base-window base-adjust))

(defn end[]
  (stop))



;(windows "resources/guitar.aiff" (range 1 20 2) 20000 1000)
(windows "resources/oliver.aiff" (range 1 30) 20000 1000)

