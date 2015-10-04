(defproject jjAgglomerator "0.1.0-SNAPSHOT"
  :description "Combine multiple CSV files together with reference to a processed file."
  :url "http://example.com/FIXME"
  :license {:name "Apache 2 License"
            :url "http://www.example.com"}
  :dependencies [
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/data.json "0.2.6"]
                 [digest "1.4.4"]
                 ]
  :main jjAgglomerator.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
