(ns particles.smoke
  (:require
    [quil.core :as q]
    [particles.util :refer :all]))

(def rand-pos #(vector (+ 300 (rand (- (q/width) 600))) (q/height)))
(def rand-speed #(max 0.1 (rand 3)))
(def rand-life #(inc (rand-int 9)))
(def rand-gray #(let [c (+ 5 (rand-int 10))] [c c c]))

(defn old? [p] (-> p :life pos? not))

(defn particle []
  (let [start-life (rand-life), life (min start-life (rand-life)), speed (rand-speed)]
    {:pos (rand-pos)
     :velocity [(* speed (Math/cos (radians 90)))
                (* speed (Math/sin (radians 90)) -1)] 
     :color (rand-gray)
     :size 64
     :start-life start-life
     :life life
     :alpha 255}))

(defn emit-particle [p]
  (if (old? p) (particle) p))

(defn age-particle
  [{:keys [velocity life start-life] :as m}]
  (-> m
      (update :pos map+ velocity)
      (assoc :alpha (* 255 (/ life start-life)))
      (update :life #(- % (rand 0.07)))))

;; -----------------------------------------------------------------------------

(defonce image (atom nil))

(defn setup []
  (q/frame-rate 60)
  (q/blend-mode :add)
  (reset! image (q/load-image "particleTexture16.png"))
  (q/resize @image 64 0)
  (repeatedly 1000 particle))

(defn step [particles]
  (map (comp emit-particle age-particle) particles))

(defn draw [particles]
  (q/background 0 0 0)
  (q/no-stroke)
  (doseq [{:keys [pos color alpha size life start-life]} (remove old? particles)] 
    (q/push-matrix)
    (apply q/translate pos)
    (q/begin-shape)
    (q/texture @image)
    (let [[r g b] color]
      (q/tint r g b alpha))
    (q/vertex 0 0 0 0)
    (q/vertex size 0 size 0)
    (q/vertex size size size size)
    (q/vertex 0 size 0 size)
    (q/end-shape :close)
    (q/pop-matrix)))
