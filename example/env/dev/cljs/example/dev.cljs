(ns ^:figwheel-no-load example.dev
  (:require
    [example.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(core/init!)
