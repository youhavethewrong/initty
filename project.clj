(defproject initty "0.1.0-SNAPSHOT"
  :description "A D20 initiative tracker."
  :url "https://github.com/youhavethewrong/initty"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.660"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.4"]
                 [re-com "2.1.0"]
                 [secretary "1.2.3"]]

  :plugins [[lein-cljsbuild "1.1.4"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]]
    :plugins      [[lein-figwheel "0.5.8"]
                   [lein-doo "0.1.7"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "initty.core/mount-root"}
     :compiler     {:main                 initty.core
                    :parallel-build       true
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}
    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            initty.core
                    :parallel-build  true
                    :output-to       "resources/public/js/compiled/app.js"
                    :asset-path      "js/compiled/out"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}
    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main             initty.runner
                    :parallel-build   true
                    :output-to        "resources/public/js/compiled/test.js"
                    :output-dir       "resources/public/js/compiled/test/out"
                    :optimizations :none}}]}
  )
