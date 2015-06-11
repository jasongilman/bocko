(ns bocko.core)

(def ^:private color-map
  {:black       [0 0 0]
   :red         [157 9 102]
   :dark-blue   [42 42 229]
   :purple      [199 52 255]
   :dark-green  [0 118 26]
   :dark-gray   [128 128 128]
   :medium-blue [13 161 255]
   :light-blue  [170 170 255]
   :brown       [85 85 0]
   :orange      [242 94 0]
   :light-gray  [192 192 192]
   :pink        [255 137 229]
   :light-green [56 203 0]
   :yellow      [213 213 26]
   :aqua        [98 246 153]
   :white       [255 255 254]})

(defonce ^:private create-canvas-fn (atom nil))

(defn set-create-canvas
  "Sets a function that creates a 'canvas'. The function will
  be passed the color-map, the raster atom, and raster width and
  height and desires pixel-width and pixel-height."
  [f]
  (reset! create-canvas-fn f))

(def create-canvas-defaults
  {:width 40
   :height 40
   :pixel-width 28
   :pixel-height 16
   :clear-color :black
   :default-color :white})

(defn create-view
  [options]
  (let [view (merge create-canvas-defaults options)
        {:keys [width
                height
                pixel-width
                pixel-height
                clear-color
                default-color]} view
        clear-screen (vec (repeat height (vec (repeat width clear-color))))
        raster (atom clear-screen)]
    (assoc view
           :clear-screen clear-screen
           :raster raster
           :canvas (@create-canvas-fn color-map raster width height pixel-width pixel-height))))

(defn close-view
  [{:keys [canvas]}]
  (.dispatchEvent canvas (java.awt.event.WindowEvent. canvas java.awt.event.WindowEvent/WINDOW_CLOSING)))

;; If we are in Clojure, set up a Swing canvas
#?(:clj
    (set-create-canvas
      (fn [color-map raster width height pixel-width pixel-height]
        (require 'bocko.swing)
        (let [make-panel (eval 'bocko.swing/make-panel)]
          (make-panel color-map raster width height pixel-width pixel-height)))))

(defn clear
  "Clears this screen."
  [{:keys [raster clear-screen]}]
  (reset! raster clear-screen)
  nil)

(defn color
  "Sets the color for plotting.
  
  The color must be one of the following:
  
  :black        :red        :dark-blue    :purple
  :dark-green   :dark-gray  :medium-blue  :light-blue
  :brown        :orange     :light-gray   :pink
  :light-green  :yellow     :aqua         :white"
  [view c]
  {:pre [(keyword? c)
         (c #{:black :red :dark-blue :purple
              :dark-green :dark-gray :medium-blue :light-blue
              :brown :orange :light-gray :pink
              :light-green :yellow :aqua :white})]}
  (assoc view :default-color c))

(defn- plot*
  [r x y c]
  (assoc-in r [x y] c))

(defn plot
  "Plots a point at a given x and y.
  
  Both x and y must be between 0 and 39."
  [{:keys [raster height width default-color]} x y]
  {:pre [(integer? x) (integer? y) (<= 0 x (dec width)) (<= 0 y (dec height))]}
  (swap! raster plot* x y default-color)
  nil)

(defn- lin
  [r a1 a2 b c f]
  (if (< a2 a1)
    (lin r a2 a1 b c f)
    (reduce (fn [r x]
              (assoc-in r (f [x b]) c))
            r
            (range a1 (inc a2)))))

(defn- hlin*
  [r x1 x2 y c]
  (lin r x1 x2 y c identity))

(defn hlin
  "Plots a horizontal line from x1 to x2 at a given y.
  
  The x and y numbers must be between 0 and 39."
  [{:keys [raster height width default-color]} x1 x2 y]
  {:pre [(integer? x1) 
         (integer? x2)
         (integer? y)
         (<= 0 x1 (dec width))
         (<= 0 x2 (dec width))
         (<= 0 y (dec height))]}
  (swap! raster hlin* x1 x2 y default-color)
  nil)

(defn- vlin*
  [r y1 y2 x c]
  (lin r y1 y2 x c reverse))

(defn vlin
  "Plots a vertical line from y1 to y2 at a given x.
  
  The x and y numbers must be between 0 and 39."
  [{:keys [raster height width default-color]} y1 y2 x]
  {:pre [(integer? y1) 
         (integer? y2) 
         (integer? x) 
         (<= 0 y1 (dec height)) 
         (<= 0 y2 (dec height)) 
         (<= 0 x (dec width))]}
  (swap! raster vlin* y1 y2 x default-color)
  nil)

(defn- scrn*
  [r x y]
  (get-in r [x y]))

(defn scrn
  "Gets the color at a given x and y.
  
  Both x and y must be between 0 and 39."
  [{:keys [raster height width default-color]} x y]
  {:pre [(integer? x) (integer? y) (<= 0 x (dec width)) (<= 0 y (dec height))]}
  (scrn* @raster x y))