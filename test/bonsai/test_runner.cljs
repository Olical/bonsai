(ns bonsai.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonsai.dom-test]))

(doo-tests 'bonsai.dom-test)
