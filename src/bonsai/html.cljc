(ns bonsai.html
  "Turn parsed Bonsai trees into HTML strings."
  (:require [clojure.spec.alpha :as s]))

(s/fdef render
        :args (s/cat :t :bonsai.tree/parsed-tree)
        :ret string?)
(defn render [t]
  "")
