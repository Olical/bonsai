(ns bonsai.core
  (:require [cljs.spec.alpha :as s]))

(defn dom? [e]
  (and e (string? (.-nodeName e))))

(s/def ::node vector?)

(s/fdef render
        :args (s/cat :mount dom?
                     :old ::node
                     :new ::node))

(defn render [])
