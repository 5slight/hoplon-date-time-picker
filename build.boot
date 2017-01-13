(set-env!
 :dependencies '[[adzerk/boot-cljs          "1.7.228-2"]
                 [adzerk/boot-reload        "0.4.13"]
                 [hoplon                    "6.0.0-alpha17"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]

                 [org.clojure/clojure       "1.8.0"]
                 [org.clojure/tools.nrepl   "0.2.12"]
                 [pandeiro/boot-http "0.7.6"]]

  :source-paths   #{"src"}
  :resource-paths #{"assets"})

(require
  '[adzerk.boot-cljs    :refer [cljs]]
  '[adzerk.boot-reload  :refer [reload]]
  '[hoplon.boot-hoplon  :refer [hoplon prerender]]
  '[pandeiro.boot-http  :refer [serve]])

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
   (serve :dir "target" :port 8083)
   (target :dir #{"target"} :no-link true)))

(deftask prod
  "Build frontend for production deployment."
  []
  (comp
   (hoplon)
   (cljs :optimizations :advanced)
   (prerender)
   (target :dir #{"target"})))
