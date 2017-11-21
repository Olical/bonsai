(ns bonsai.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonsai.dom-test]
            [bonsai.tree-test]
            [bonsai.state-test]))

(doo-tests 'bonsai.dom-test
           'bonsai.tree-test
           'bonsai.state-test)
