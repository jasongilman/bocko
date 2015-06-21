# Bocko Fun(ctional)

A forked version of [Bocko](https://github.com/mfikes/bocko). It provides a slightly more functional API. 

## Caveats

This version will likely not be maintained in the future. I encourage you to use the original library. 

Tested with Clojure 1.7.0-RC1. Requires Clojure 1.7.0 or greater. The original Bocko is Clojurescript compatible. That capability has not been tested with Bock Fun.

# Usage

Add bocko-fun to your project dependencies.

```
[bocko-fun "0.3.0"]
```

# Detailed Usage

The view is immediately visible after it is created.

```clojure
(require '[bocko-fun.core :as b])

(def view (b/create-view
            ;; All of these are optional.
            ;; These are the default values if not specified.
            {:width 40
             :height 40
             :pixel-width 28
             :pixel-height 16
             :clear-color :black
             :default-color :white}))
```             

The view contains the keys: 

* `:clear-screen` - contains a copy of a raster as a blank screen.
* `:raster` - The immutable raster that's modified when the view is manipulated.
* `:raster-atom` - A mutable reference to the currently displayed raster.
* `:canvas` - A Swing JFrame that displays what's in the raster-atom. It watches the raster atom for changes and applies them as the raster atom is updated.


Rasters are matrices (vector of vectors) representing the displayed screen. The matrix is a vector of columns where each column is a vector containing keywords representing the displayed color in a square.

Functions that manipulate the view take the view as an argument. The updated view is returned.

```clojure
;; Draws a point at 1,2
(def updated-view (b/plot view 1 2))
```

Changes to a view create a new immutable version of the view

```clojure
(not= updated-view view)
```

Changes are not visible until they are applied to the underlying mutable raster atom and canvas.

```clojure
(b/apply-raster! updated-view)
```


```clojure
(-> view
    
    ;; plots a point on the screen
    (b/plot 2 3)

    ;; changes the color to pink
    (b/color :pink)
    (b/plot 5 5)
    
    ;; draws a horizontal line
    (b/hlin 3 9 10)
    
    ;; Mutation!: Applies the view changes to the display
    b/apply-raster!)
  
  
;; Close the view
(b/close-view view)
```

The commands comprise `create-view`, `apply-raster!`, `close-view`, `color`, `plot`, `scrn`, `hlin`, `vlin`, and `clear`.

# Examples

Draw an American flag:
```clojure
(require '[bocko-fun.core :as b])

(def view (b/create-view))

(let [;; Draw 13 stripes cycling over red/white
      view-with-stripes (reduce (fn [view [n c]]
                                  (-> view
                                      (b/color c)
                                      (b/hlin 10 25 (+ 10 n))))
                                view
                                (take 13 
                                      (map vector (range) (cycle [:red :white]))))
      ;; Fill in a dark blue field in the corner
      view-with-dark-blue-field (reduce #(apply b/plot %1 %2)
                                        (b/color view-with-stripes :dark-blue)
                                        (for [x (range 10 19)
                                              y (range 10 17)]
                                          [x y]))
      ;; Add some stars to the field by skipping by 2
      flag-view (reduce #(apply b/plot %1 %2)
                        (b/color view-with-dark-blue-field :white)
                        (for [x (range 11 19 2)
                              y (range 11 17 2)]
                          [x y]))]
  
  (b/apply-raster! flag-view))
```

Animated bouncing ball using `loop`/`recur`:
```clojure
(require '[bocko-fun.core :as b])
  
(loop [view (b/create-view) x 5 y 23 vx 1 vy 1]
  ; First determine new location and velocity,
  ; reversing direction if bouncing off edge.
  (let [x' (+ x vx)
        y' (+ y vy)
        vx' (if (< 0 x' 39) vx (- vx))
        vy' (if (< 0 y' 39) vy (- vy))
        updated-view (-> view
                         ; Erase drawing at previous location
                         (b/color :black)
                         (b/plot x y)
                         ; Draw ball in new location
                         (b/color :dark-blue)
                         (b/plot x' y')
                         b/apply-raster!)]
    
    ; Sleep a little and then loop around again
    (Thread/sleep 50)
    (recur updated-view x' y' vx' vy')))
```

# License

Distributed under the Eclipse Public License, which is also used by Clojure.
