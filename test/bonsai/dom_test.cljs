(ns bonsai.dom-test
  (:require [cljs.test :as t :include-macros true]
            [bonsai.dom :as sut]
            [jsdom]))

(defn body []
  (-> (new jsdom/JSDOM) .-window .-document .-body))
