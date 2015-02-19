
(ns com.zimmermusic.diamonds
  (:require [lanterna.screen :as s]
            [clojure.string :as str])
  (:use overtone.live))


(def homeScreenText '( "Diamonds"
                       "By Aaron Zimmerman *"
                       ""
                       ""
                       "(defn diamond-iter[inst section-length stanzas]"
                       "  (if (not (empty? stanzas))"
                       "    (do"
                       "       (draw-diamond (first stanzas))"
                       "       (play-melodies (first stanzas) inst section-length)"
                       "       (def iter-count (inc iter-count))"
                       "       (apply-at (+ section-length (now)) #'diamond-iter [inst section-length (rest stanzas)]))))"
                       " "
                       "(defn diamonds[inst num section-length]"
                       "   (init-graphics num)"
                       "   (let [stanzas (map inc (flatten (concat (range num) (reverse (range (dec num))))))]"
                       "   (diamond-iter inst section-length stanzas)))"
                       ))




(defn safe-dec[num]
  "Decremenents if greater than one, else one"
  (if (= 1 num)
    num
    (dec num)))

(defn get-key [num div]
  (str num "/" div))


;initially, set all values
(def ratio-count-map
  (zipmap (flatten (for [i (range 1 30)]
                     [(get-key i (safe-dec i))
                      (get-key (safe-dec i) i)]))
    (repeat 100 0)))


(defn interval-y-pos[num div]
  ;figures out the index along y axis to print a given interval's counts
  (let [adjuster (if (>= num div) 1 2)]
    (+
      -3
      (* 2 num)
      adjuster)))


(def scr (s/get-screen :swing  {:cols 120
                                :rows 45}))

(defn screen-size[] (s/get-size scr))

(defn inc-and-get-count [num div]
  (def ratio-count-map (update-in ratio-count-map [(get-key num div)] inc))
  (ratio-count-map (get-key num div)))  ;return new value


(defn center-x[]
  (/ ((screen-size) 0) 2))


(defn start-pos-for-centering[width]
  (let [offset (int (/ width 2))]
    (println "calculating center for width " width " offset is " offset)
    (- (center-x) offset)))


(defn drawstr [scr x y text opts]
  (s/put-string scr x y text opts))

(def last-update
  (System/currentTimeMillis))

(defn refresh [s]
  ;  (locking s
  ;    (let [elapsed (- (System/currentTimeMillis) last-update)]
  ;      (if (> elapsed 15)
  ;        (do
  (s/redraw s))
; (def last-update (System/currentTimeMillis)))))))


(defn init-graphics[num]

  (def iter-count 0)

  (s/start scr)

  (def total-iter num)


  (def sq-height 1)
  ;  (def sq-height (/ ((screen-size) 1) (* total-iter 3.0)))
  (def sq-width 1);(/ ((screen-size) 0) (* total-iter 2.5)))

  ;display the title in the middle of the screen
  (doseq [i (range (count homeScreenText))]
    (let [s (nth homeScreenText i)
          y (+ i 3)
          x 15]
      (drawstr scr x y s {})))


  ;add the key for interval counting
  (doseq [k (keys ratio-count-map )]
    (let [[num div] (str/split k #"/")
          y (interval-y-pos (Integer/parseInt num) (Integer/parseInt div))]
      (drawstr scr 0 y k {})))
  ;(s/put-string scr 0 y k)))

  (refresh scr)
  ;wait for any key to begin
  (s/get-key-blocking scr))


(defn draw-pitch[pitch pan]

  (let [max-x ((s/get-size scr) 0)
        min-x 20
        x-range (- max-x min-x)
        scalar (/ (+ 1 pan) 2.0) ;moves pan from 0 to 1
        x (+ min-x (* scalar x-range))
        max-y ((s/get-size scr) 1)
        y (int (- max-y (* (/ pitch 1800) max-y)))]
    ;   (println "drawing pitch at " x "," y)
    (drawstr scr x y " " {:bg :white})
    (refresh scr)

    )
  )

; track the num of times this ratio has occured and add an asterisk to that file or something
(defn draw-ratio[num div]
  ;get the count that this has occured and then add an asterisk aft the next spot, increment the count
  ;  (println "Drawing ratio " num ":" div)
  (let [x (+ (inc-and-get-count num div) 5)
        y (interval-y-pos num div)]
    (drawstr scr x y "*" {})
    (refresh scr)))


(defn draw-shape[x1 y1 x2 y2]
  ;  (println "drawing shape from " x1 y1 " to " x2 y2)
  (let [numx (- x2 x1)
        numy (- y2 y1)]
    (doseq [x (range x1 x2)]
      (doseq [y (range y1 y2)]
        (drawstr scr x y "#" {})))
    (refresh scr)))


(def iter-count 0)
; create a layer in the diamond.  The size should be screenheight
; create diamond - screen height / num iter - padding = height.
; Create a row of sqaures with cur count and center it on the screen on the cur count row
(defn draw-diamond [cur]

  (let [  start-y iter-count
          ;start-y (int (* iter-count (inc sq-height)))    ;this adds a space between lines
          end-y (+ start-y sq-height)
          total-row-width (* cur (inc sq-width))  ;row width is the total number of squares + 1 space between
          start-x-offset (start-pos-for-centering total-row-width)]
    (println "total row width " total-row-width)
    (println "start x offset " start-x-offset)

    (doseq [i (range cur)]
      (println "drawing square" i)
      (let [startx (+
                     start-x-offset         ; the overall start for the row of squares
                     (* i sq-width)  ; the number of squares that have already come
                     (* 1 i))               ; the space between each square
            endx (+ startx sq-width)]
        ; (println "calculated start and end x of " startx endx)

        (draw-shape startx start-y endx end-y)))))


;; instrument stuff

;; every iteration is n notes away from the fundamental, the entire piece is n such trips
(definst plop
  [note  400 pan 0.0 atk 0.1 dcy 0.1]
  (let [snd (sin-osc note)
        env (env-gen (perc atk dcy) :action FREE)]
    (out 0
      (pan2
        (*
          0.8 ; volume
          env ; envelope=
          snd )
        pan))))


(def choices
  (flatten
    (conj
      (repeat 70 1)
      (repeat 60 2)
      (repeat 45 3)
      (repeat 35 4)
      (repeat 25 5)
      (repeat 20 6)
      (repeat 15 7)
      (repeat 10 8)
      (repeat 6 9)
      (repeat 4 10)
      (repeat 3 11)
      (repeat 2 12)
      (repeat 1 13)
      (repeat 1 14)
      (repeat 1 15)
      (repeat 1 16))))


(defn get-next-note[pitch dur]
  (let [numerator (first (shuffle choices))
        allowed-to-go-higher (>= 1800 pitch)
        up? (and allowed-to-go-higher (> 0.5 (rand)))
        divisor (if up? (safe-dec numerator) (inc numerator))]
    (draw-ratio numerator divisor)
    [ (* pitch (/ numerator divisor))
      (* dur (/ divisor numerator))]))


(defn melody-player[inst pitch duration total-runtime allowed-runtime pan a d]
  "Plays the pitch and sleeps for the duration before selecting another pitch and playing it"
  (inst :note pitch :pan pan :atk a :dcy d)
  (draw-pitch pitch pan)
  (let [[nextp nextd] (get-next-note pitch duration)
        elapsed-after (+ nextd total-runtime)
        next-start (+ (now) nextd)]
    (if (< elapsed-after allowed-runtime)
      (apply-at next-start #'melody-player[inst nextp nextd elapsed-after allowed-runtime pan a d]))))

(def max-env 0.5)
(def min-env 0.05)
(def env-range (- max-env min-env))

(defn scale-within-range[cur cur-min cur-max new-min new-max]
  (let [cur-range (- cur-max cur-min)
        cur-perc (/ cur cur-range)
        new-range (- new-max new-min)
        new-base (* cur-perc new-range)]
    (+ new-base new-min)))


(defn play-melodies [num inst dur]
  "Plays a number of melodies at once, all starting from the same pitch
  (play-melodies 3 plop 5000)"
  (println "Playing " num " melodies")
  (doseq [i (range num)]
    (let [pan (- (* 2 (rand)) 1)
          start-duration (+ 500 (rand-int 1000))
          start-scalar (inc (rand-int 4))
          start-pitch (* start-scalar 110)
          a (scale-within-range num 1 20 max-env min-env)   ; num goes from 1 -> 20, at 20 I want this to be 0.01, at 1 I want it to be 0.5
          d (scale-within-range num 1 20 max-env min-env)]
      (melody-player inst start-pitch start-duration 0 dur pan a d))))


(defn diamond-iter[inst section-length stanzas]
  "(diamond-iter plop 5000 '(1))"
  (if (not (empty? stanzas))
    (do
      (println "***************************")
      (println "********* " (first stanzas) " ***********")
      (println "***************************")

      (draw-diamond (first stanzas))
      (play-melodies (first stanzas) inst section-length)
      (def iter-count (inc iter-count))
      (apply-at (+ section-length (now)) #'diamond-iter [inst section-length (rest stanzas)]))))



(defn diamonds[num section-length]
  "Bootstrap the first iteration and begin the piece"
  (init-graphics num)
  (let [stanzas (map inc (flatten (concat (range num) (reverse (range (dec num))))))]
    (diamond-iter plop section-length stanzas)))




