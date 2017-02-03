(ns datepicker.core
  (:require-macros [javelin.core :refer [defc defc= cell= dosync]])
  (:require [hoplon.core :as h]
            [javelin.core :refer [cell]]
            [cljs-time.core :as tc]
            [cljs-time.format :as tf]
            [cljs-time.periodic :as tp]
            [goog.events :as gev]
            [goog.dom :as gdom]
            [goog.string :as gstring]))

(def days-in-week 7)
(def default-date-format :date)
(def default-time-format :hour-minute)

(defn set-dt-items [dt items]
  (let [dt-split {:year (tc/year dt) :month (tc/month dt) :day (tc/day dt)
                  :hour (tc/hour dt) :minute (tc/minute dt)
                  :second (tc/second dt) :milli (tc/milli dt)}
        {:keys [year month day hour minute second milli]} (merge dt-split items)]
    (tc/date-time year month day hour minute second milli)))

(defn merge-date-time [date time]
  (set-dt-items date {:hour (tc/hour time)
                      :minute (tc/minute time)}))

(defn year-month [d] (if d (tf/unparse (tf/formatter "MMM - yyyy") d)))

(defn formatter [f]
  (cond
    (string? f) (tf/formatter f)
    (keyword? f) (tf/formatters f)))

(defn date-lense [state f]
  (cell= (when (-> state empty? not)
           (tf/parse (formatter f) state))
         #(reset! state (tf/unparse (formatter f) (tc/to-default-time-zone %)))))

(defn date-with-time
  ([date time] (date-with-time date time {}))
  ([date time
    {:keys [date-format time-format]
     :or {date-format default-date-format
          time-format default-time-format}}]
   (cell= (when (and (-> date empty? not) (-> time empty? not))
            (let [d (tf/parse (tf/formatters date-format) date)
                  t (tf/parse (tf/formatters time-format) time)]
              (tf/unparse (tf/formatters :date-time) (merge-date-time d t)))))))

(defn make-weeks [fday lday]
  (let [fwday (tc/day-of-week fday)
        start-buffer (vec (map (fn [] nil) (range 1 fwday)))
        end-pre-weekdays (+ (dec fwday) (tc/day lday))
        weeks (.ceil js/Math (/ end-pre-weekdays days-in-week))
        end-buffer (map (fn [] nil)
                        (range 0 (- (* weeks days-in-week) end-pre-weekdays)))
        days (tp/periodic-seq fday (tc/plus lday (tc/days 1)) (tc/days 1))
        all-days (-> start-buffer (into days) (into end-buffer))]
    (partition days-in-week all-days)))

(h/defelem timep [])

(h/defelem month-select [{:keys [cur allowed-range]}]
  (let [allowed-start (cell= (:start allowed-range))
        allowed-end (cell= (:end allowed-range))
        next-allowed (cell= (if (nil? allowed-range)
                              true
                              (< (tc/month cur) (tc/month allowed-end))))
        prev-allowed (cell= (if (nil? allowed-range)
                              true
                              (> (tc/month cur) (tc/month allowed-start))))]
    (h/div
     :class "month-select"
     ((h/button
       :class "prev"
       :click (fn [] (when @prev-allowed (swap! cur #(tc/minus % (tc/months 1)))))
       (h/i :class "icon-left-arrow"))
      :class (cell= {:disabled (not prev-allowed)})
      :attr (cell= {:disabled (not prev-allowed)}))
     (h/span (cell= (year-month cur)))
     ((h/button
       :class "next"
       :click (fn [] (when @next-allowed (swap! cur #(tc/plus % (tc/months 1)))))
       (h/i :class "icon-right-arrow"))
      :class (cell= {:disabled (not next-allowed)})
      :attr (cell= {:disabled (not next-allowed)})))))

(h/defelem day [{:keys [state day selected! allowed-range]}]
  (let [selected (cell= (and (-> state nil? not) (-> day nil? not)
                             (tc/equal? (tc/at-midnight state) (tc/at-midnight day))))
        allowed-start (cell= (:start allowed-range))
        allowed-end (cell= (:end allowed-range))
        in-allowed (cell= (if (nil? allowed-range)
                            true
                            (and (-> day nil? not)
                                 (or (tc/after? day allowed-start)
                                     (tc/equal? day allowed-start))
                                 (or (tc/before? day allowed-end)
                                     (tc/equal? day allowed-end)))))]
    (h/td :class (cell= {:day (-> day nil? not)
                         :selected selected
                         :disabled (not in-allowed)})
          :click #(when @in-allowed
                      (do (reset! state @day)
                          (selected! @day)))
          (cell= (if day (-> day tc/day str))))))

(h/defelem datep [{:keys [cur state selected! allowed-range]
                   :or {selected (fn [d])}}]
  (let [days ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]
        fd (cell= (tc/first-day-of-the-month- cur))
        ld (cell= (tc/last-day-of-the-month- cur))
        weeks (cell= (make-weeks fd ld))]
    (h/div :class "date-picker"
           (month-select :cur cur :allowed-range allowed-range)
           (h/table
            (h/thead (h/tr (h/loop-tpl :bindings [d (cell= days)] (h/th d))))
            (h/tbody
             (h/loop-tpl :bindings [w (cell= weeks)]
                         (h/tr (h/loop-tpl :bindings [d (cell= w)]
                                           (day :day d :state state
                                                :allowed-range allowed-range
                                                :selected! selected!)))))))))

(defn hide-ev [ev exclude state]
  (let [t (.-target ev)]
    (if (not (gdom/findNode exclude #(= t %)))
      (reset! state false))))

(h/defelem date-picker [{:keys [identifier state
                                state-format
                                display-format
                                allowed-range]
                         :or {state-format default-date-format
                              display-format default-date-format}}]
  (let [showp (cell false)
        cur (cell (if (-> @state empty?)
                    (tc/now)
                    (tf/parse (tf/formatters state-format) @state)))
        state' (date-lense state state-format)
        dis-state (cell= (do
                           (when (-> state' nil? not)
                             (tf/unparse (formatter display-format) state'))))
        picker (h/div :class "date-time-picker"
                      (h/input :type "text" :id identifier
                               :name identifier :value dis-state
                               :click #(swap! showp not))
                      ((datep :state state' :cur cur
                              :allowed-range allowed-range
                              :selected! #(reset! showp false))
                       :class (cell= {:hidden (not showp)})))]
    (gev/listen js/document goog.events.EventType.CLICK #(hide-ev % picker showp))
    (when (nil? @state') (reset! state' (tc/now)))
    picker))

(defn time-item [state display modifier]
  (h/span :class "time-item"
          (h/button
           :click (fn [] (swap! state #(tc/plus % modifier)))
           (h/i :class "icon-up-arrow")) (h/br)
          (h/span (cell= (if state (gstring/format "%02d" (display state)))))
          (h/br)
          (h/button
           :click (fn [] (swap! state #(tc/minus % modifier)))
           (h/i :class "icon-down-arrow"))))

(defn round-mins [min inc] (* (.ceil js/Math (/ min inc)) inc))

(h/defelem time-picker [{:keys [identifier state time-format min-inc]
                         :or {time-format default-time-format
                              min-inc 15}}]
  (let [state' (date-lense state time-format)
        now (tc/now)]
    (when (nil? @state')
      (reset! state' (set-dt-items now {:minute (round-mins (tc/minute now) min-inc)})))
    (h/div :class "time-picker"
           (time-item state' tc/hour (tc/hours 1))
           (h/div :class "time-item" ":")
           (time-item state' tc/minute (tc/minutes min-inc)))))
