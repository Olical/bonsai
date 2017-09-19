(defproject olical/bonsai "2.0.0"
  :description "Minimal state management and rendering for ClojureScript."
  :url "https://github.com/Olical/bonsai"
  :license {:name "Unlicense"
            :url "https://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/spec.alpha "0.1.123"]]
  :profiles {:dev {:plugins [[cider/cider-nrepl "0.15.1"]
                             [lein-doo "0.1.7"]]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "resources/public/js/testable.js"
                                   :main bonsai.test-runner
                                   :optimizations :none
                                   :target :nodejs}}]}
  :aliases {"test-cljs" ["doo" "node" "test" "once"]
            "test-all" ["do" ["test"] ["test-cljs"]]})
