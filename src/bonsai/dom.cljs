(ns bonsai.dom
  (:require [bonsai.tree :as tree]))

(defn patch! [node diff]
  (aset node "innerHTML" (tree/->html (get-in diff [0 2]))))
