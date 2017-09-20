(ns bonsai.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonsai.tree-test]
            [bonsai.html-test]))

(doo-tests 'bonsai.tree-test
           'bonsai.html-test)
