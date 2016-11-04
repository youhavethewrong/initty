(ns initty.events
    (:require [re-frame.core :as re-frame]
              [initty.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :forward-round
 (fn [db [_ _]]
   (let [t (:actors (:encounter db))
         last-actor (first (sort-by #(:init %) t))
         new-order (conj (butlast t) (last t))
         db (if (= (last new-order) last-actor)
              (update-in db [:encounter :rounds] inc)
              db)]
     (assoc-in db [:encounter :actors] new-order))))
