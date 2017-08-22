(defproject olical/bonsai "1.0.0"
  :description "Minimalistic state management."
  :url "https://github.com/Olical/bonsai"
  :license {:name "Unlicense"
            :url "https://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:plugins [[lein-doo "0.1.7"]]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "resources/public/js/testable.js"
                                   :main bonsai.test-runner
                                   :optimizations :none
                                   :target :nodejs}}]})
