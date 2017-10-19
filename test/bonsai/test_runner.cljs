(ns bonsai.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonsai.core-test]))

(doo-tests 'bonsai.core-test)
