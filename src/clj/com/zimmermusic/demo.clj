(ns com.zimmermusic.demo
  (:use overtone.studio.scope)
  (:use overtone.live))

;▗▄▄          ▗                       ▗
; ▐  ▗▗▖  ▄▖ ▗▟▄  ▖▄ ▗ ▗ ▗▄▄  ▄▖ ▗▗▖ ▗▟▄  ▄▖
; ▐  ▐▘▐ ▐ ▝  ▐   ▛ ▘▐ ▐ ▐▐▐ ▐▘▐ ▐▘▐  ▐  ▐ ▝
; ▐  ▐ ▐  ▀▚  ▐   ▌  ▐ ▐ ▐▐▐ ▐▀▀ ▐ ▐  ▐   ▀▚
;▗▟▄ ▐ ▐ ▝▄▞  ▝▄  ▌  ▝▄▜ ▐▐▐ ▝▙▞ ▐ ▐  ▝▄ ▝▄▞


(definst simple[freq 440]
  (sin-osc freq))

(simple)

(simple 200)

(stop)




; ▗▄         ▐    ▝       ▝              ▗  ▖
;▗▘ ▘ ▄▖ ▗▄▄ ▐▄▖ ▗▄  ▗▗▖ ▗▄  ▗▗▖  ▄▄     ▐  ▌ ▄▄  ▄▖ ▗▗▖  ▄▖
;▐   ▐▘▜ ▐▐▐ ▐▘▜  ▐  ▐▘▐  ▐  ▐▘▐ ▐▘▜     ▐  ▌▐▘▜ ▐▘▐ ▐▘▐ ▐ ▝
;▐   ▐ ▐ ▐▐▐ ▐ ▐  ▐  ▐ ▐  ▐  ▐ ▐ ▐ ▐     ▐  ▌▐ ▐ ▐▀▀ ▐ ▐  ▀▚
; ▚▄▘▝▙▛ ▐▐▐ ▐▙▛ ▗▟▄ ▐ ▐ ▗▟▄ ▐ ▐ ▝▙▜     ▝▄▄▘▝▙▜ ▝▙▞ ▐ ▐ ▝▄▞
;                                 ▖▐          ▖▐
;                                 ▝▘          ▝▘


(definst less-simple[note 60 duration 1.0 amplitude 0.5 ]
  (let [freq  (midicps note)
        env  (env-gen (envelope [0.0 0.8 0.8 0.0] [0.1 duration 0.1]) :action FREE)]
    (*               
      (sin-osc freq) 
      env            
      amplitude )))  


(less-simple) 

(less-simple 70)

(less-simple :note 46 :duration 2.5 :amplitude 0.9)




(defsynth dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1 out-bus 0]
  (let [trig (impulse:kr (/ bpm 120))
        freq (midicps note)
        swr (demand trig 0 (dseq [wobble] INF))
        sweep (lin-exp (lf-tri swr) -1 1 40 3000)
        wob (apply + (saw (* freq [0.99 1.01])))
        wob (lpf wob sweep)
        wob (* 0.8 (normalizer wob))
        wob (+ wob (bpf wob 1500 2))
        wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

        kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
        kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
        kick (clip2 kick 1)

        snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
        snare (+ snare (bpf (* 4 snare) 2000))
        snare (clip2 snare 1)]

    (out out-bus    (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))

(dubstep)
(stop)





;▞▀▖          ▜
;▚▄ ▝▀▖▛▚▀▖▛▀▖▐ ▞▀▖▞▀▘
;▖ ▌▞▀▌▌▐ ▌▙▄▘▐ ▛▀ ▝▀▖
;▝▀ ▝▀▘▘▝ ▘▌   ▘▝▀▘▀▀

(def guitar (sample "resources/guitar.aiff"))

(guitar)

(def buf (load-sample "resources/guitar.aiff"));

(scope buf)

(definst play[]
  (scaled-play-buf 2 (:id buf) :action FREE))

(play)

(stop)






; ▄▄                          ▝
;▐▘ ▘ ▄▖  ▄▄ ▗ ▗  ▄▖ ▗▗▖  ▄▖ ▗▄  ▗▗▖  ▄▄
;▝▙▄ ▐▘▐ ▐▘▜ ▐ ▐ ▐▘▐ ▐▘▐ ▐▘▝  ▐  ▐▘▐ ▐▘▜
;  ▝▌▐▀▀ ▐ ▐ ▐ ▐ ▐▀▀ ▐ ▐ ▐    ▐  ▐ ▐ ▐ ▐
;▝▄▟▘▝▙▞ ▝▙█ ▝▄▜ ▝▙▞ ▐ ▐ ▝▙▞ ▗▟▄ ▐ ▐ ▝▙▜
;          ▐                          ▖▐
;          ▝                          ▝▘

(def nome (metronome 100))

(nome)     

(nome 140) 

(use 'overtone.inst.drum)

(kick2)
(closed-hat)

(apply-at (+ (now) 2000) #'closed-hat)

(defn looper[inst nome offset]
  (let [beat (nome)
        next-start (nome (inc beat))]
    (at (nome (+ offset beat)) (inst))
    (apply-by next-start #'looper[inst nome offset])))

(looper kick4 nome 0)
(looper closed-hat nome 0.5)
(looper closed-hat nome 0.75)
(stop)

;▗▄▄          ▗  ▐
;▐  ▌ ▄▖  ▄▖ ▗▟▄ ▐▗▖  ▄▖ ▗ ▗  ▄▖ ▗▗▖
;▐▄▄▘▐▘▐ ▐▘▐  ▐  ▐▘▐ ▐▘▜ ▝▖▞ ▐▘▐ ▐▘▐
;▐  ▌▐▀▀ ▐▀▀  ▐  ▐ ▐ ▐ ▐  ▙▌ ▐▀▀ ▐ ▐
;▐▄▄▘▝▙▞ ▝▙▞  ▝▄ ▐ ▐ ▝▙▛  ▐  ▝▙▞ ▐ ▐


(use 'overtone.inst.sampled-piano)
(sampled-piano)

(def ode-to-joy [[:e4 1][:e4 1][:f4 1][:g4 1][:g4 1][:f4 1][:e4 1][:d4 1][:c4 1][:c4 1][:d4 1][:e4 1][:e4 1.5][:d4 0.5][:d4 2]
                 [:e4 1][:e4 1][:f4 1][:g4 1][:g4 1][:f4 1][:e4 1][:d4 1][:c4 1][:c4 1][:d4 1][:e4 1][:d4 1.5][:c4 0.5][:c4 2]
                 [:d4 1][:d4 1][:e4 1][:c4 1][:d4 1][:e4 0.5][:f4 0.5][:e4 1][:c4 1][:d4 1][:e4 0.5][:f4 0.5][:e4 1][:d4 1][:c4 1][:d4 1][:g2 1][:e4 2]
                 [:e4 1][:f4 1][:g4 1][:g4 1][:f4 1][:e4 1][:d4 1][:c4 1][:c4 1][:d4 1][:e4 1][:d4 1.5][:c4 0.5][:c4 2]])

(def ode-bass   [[:c3 3]              [:c3 1][:g2 3]              [:g2 1][:c3 3]              [:c3 1][:g2 2][:g2 1][:g2 0.5][:g2 0.5]
                 [:c3 3]              [:c3 1][:f2 3]              [:f2 1][:g2 3]              [:c3 1][:g2 2][:c3 1][:c3 0.5][:c3 0.5]
                 [:g2 2]       [:g2 2]        [:g2 3]             [:g2 1][:g2 2]       [:g#3 1][:e3 1][:a3 1][:b3 2][:c3 1]
                 [:c3 3]              [:c3 1][:f2 3]              [:f2 1][:g2 3]              [:c3 1][:g2 1.5][:c3 0.5][:c3 1]])


(defn play-notes [inst time notes bl level]
  (let [[pitches dur] (first notes)
        duration-ms (* bl dur 0.95)
        duration-s (/ duration-ms 1000)
        remaining (rest notes)]
    (doseq [pitch (if (seq? pitches) pitches [pitches])]
      (at time (inst (note pitch) level :sustain duration-s)))
    (when (not-empty remaining)
      (let [next-time (+ duration-ms time)]
        (apply-by next-time #'play-notes[inst next-time remaining bl level])))))


(play-notes sampled-piano (now) ode-to-joy 500 0.9)
(stop)
(play-notes sampled-piano (now) ode-to-joy 100 0.9)

(def chord-scales {
                    :C [:c5 :e5 :g5 :c6]
                    :F [:f5 :a5 :c5 :f6]
                    :G [:g5 :b5 :d5 :f5]})


(defn build-chord [key scales]
  (choose-n 2 (key scales)))

;will pick 2 of the chords defined each time run
(build-chord :C chord-scales)

; chord progression as [chord beats]
(def ode-prog
  [[:C 4][:F 4][:C 4][:G 4]
   [:C 4][:F 4][:G 4][:C 4]
   [:G 4][:G 4][:G 4][:G 4]
   [:C 4][:F 4][:G 4][:C 4]])

; build chords for an entire progressions
(defn build-progression [chords chord-scales]
  (for [[name duration] chords]
    [ (build-chord name chord-scales) duration]))

;put it all together, start everything in 500 ms
(let [time (+ (now) 500)]
  (play-notes sampled-piano time ode-to-joy 500 0.9)
  (play-notes sampled-piano time ode-bass 500 0.8)
  (play-notes sampled-piano time (build-progression ode-prog chord-scales) 500 0.7))

(stop)




