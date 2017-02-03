(ns datepicker.utils
  (:require [cljs-time.core :as tc]))

(defn range-inc
  ([inc] (range-inc (tc/at-midnight (tc/now)) inc))
  ([start inc]
   {:start start
    :end (-> start (tc/plus inc) tc/at-midnight)}))
