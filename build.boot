(def +version+ "0.2.0-SNAPSHOT")

(set-env!
 :version +version+
 :dependencies '[[adzerk/boot-cljs          "1.7.228-2"]
                 [adzerk/boot-reload        "0.4.13"]
                 [adzerk/bootlaces          "0.1.13"]
                 [hoplon                    "6.0.0-alpha17"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]

                 [org.clojure/clojure       "1.8.0"]
                 [org.clojure/tools.nrepl   "0.2.12"]
                 [pandeiro/boot-http "0.7.6"]]

  :source-paths   #{"src"}
  :resource-paths #{"assets"})

(require
 '[adzerk.bootlaces    :refer :all]
 '[adzerk.boot-cljs    :refer [cljs]]
 '[adzerk.boot-reload  :refer [reload]]
 '[hoplon.boot-hoplon  :refer [hoplon prerender]]
 '[pandeiro.boot-http  :refer [serve]])

(bootlaces! +version+)

(deftask dev
  "Build frontend for local development."
  []
  (comp
   (watch)
   (speak)
   (hoplon :pretty-print true)
   (reload)
   (cljs :optimizations :none
         :source-map true)
   (serve :dir "target" :port 8000)
   (target :dir #{"target"} :no-link true)
   (build-jar)))

(deftask prod
  "Build frontend for production deployment."
  []
  (comp
   (hoplon)
   (cljs :optimizations :advanced)
   (prerender)
   (target :dir #{"target"})))

(task-options!
 pom    {:project     'lightscale/datepicker
         :version     +version+
         :description "A small widget to represent date and time in hoplon
                      and cljs-time."
         :url         "https://github.com/lightscaletech/hoplon-date-time-picker"
         :scm         {:url "https://github.com/lightscaletech/hoplon-date-time-picker"}
         :license     {"Eclipse Public License"
                       "http://www.eclipse.org/legal/epl-v10.html"}})
