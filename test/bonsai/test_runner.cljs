(ns bonsai.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonsai.dom-test]
            [bonsai.tree-test]))

(doo-tests 'bonsai.dom-test
           'bonsai.tree-test)
