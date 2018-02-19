(ns bonsai.test-runner
  (:require [clojure.test :as t]
            [bonsai.diff-test]))

(defn -main []
  (t/run-tests 'bonsai.diff-test))
