(ns bonsai.diff
  (:require [orchestra.core #?(:clj :refer, :cljs :refer-macros) [defn-spec]]))

(defn-spec add integer?
  [a integer?, b integer?]
  (+ a b))

(defn-spec -main nil?
  []
  (println "Hello, World!"))
