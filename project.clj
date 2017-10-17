(defproject olical/bonsai "2.0.0"
  :description "Render your state and manage the changes over time."
  :url "https://github.com/Olical/bonsai"
  :license {:name "Unlicense"
            :url "https://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [org.clojure/clojurescript "1.9.946"]]
  :profiles {:dev {:source-paths ["src" "dev"]
                   :dependencies [[com.cemerick/piggieback "0.2.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[cider/cider-nrepl "0.15.1"]
                             [lein-doo "0.1.7"]]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "resources/public/js/testable.js"
                                   :main bonsai.test-runner
                                   :optimizations :none
                                   :npm-deps {:jsdom "11.3.0"}
                                   :install-deps true
                                   :target :nodejs}}]}
  :aliases {"test-cljs" ["doo" "node" "test" "once"]
            "test-all" ["do" ["test"] ["test-cljs"]]})
