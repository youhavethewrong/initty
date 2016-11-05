(ns initty.events
  (:require [re-frame.core :as re-frame]
            [initty.db :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   (let [d db/default-db]
     (update-in d [:encounter :actors]
      (fn [a] (reverse (sort-by :init a)))))))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(defn- update-status
  [actor]
  (-> actor
      (update :status #(map (fn [s] (update s :duration dec)) %))
      (update :status #(filter (fn [s] (> (:duration s) 0)) %))))

(re-frame/reg-event-db
 :forward-round
 (fn [db [_ _]]
   (let [actors (:actors (:encounter db))
         sorted (sort-by #(:init %) actors)
         last-actor (first sorted)
         current-actor (first (filter #(= "🎲" (:turn %)) actors))
         grouped (group-by #(> (:init current-actor) (:init %)) (reverse sorted))
         next-actor (first (get grouped true))
         next-actor (if-not next-actor (first (get grouped false)) next-actor)
         round-end (= current-actor last-actor)
         db (if round-end
              (-> db
                (update-in [:encounter :rounds] inc)
                (update-in [:encounter :actors] #(map update-status %)))
              db)]
     (-> db
         (update-in [:encounter :actors] #(map (fn [a] (if (= (:name current-actor) (:name a)) (assoc a :turn "") a)) %))
         (update-in [:encounter :actors] #(map (fn [a] (if (= (:name next-actor) (:name a)) (assoc a :turn "🎲") a)) %))))))

(re-frame/reg-event-db
 :add-actor
 (fn [db [_ ba]]
   (-> db
       (update-in [:encounter :actors] #(conj % ba))
       (update-in [:encounter :actors] #(reverse (sort-by :init %))))))

(re-frame/reg-event-db
 :add-status
 (fn [db [_ {:keys [character status]}]]
   (update-in db [:encounter :actors] (fn [actors]
                                        (map #(if (= character (:name %))
                                                (update % :status conj status)
                                                %)
                                             actors)))))

(defn- clear-status-from-actor
  [status actor]
  (update actor :status (fn [statuses] (filter #(not= status (:name %)) statuses))))

(re-frame/reg-event-db
 :remove-status
 (fn [db [_ {:keys [character status]}]]
   (update-in db [:encounter :actors] (fn [actors]
                                        (map
                                         #(if (= character (:name %)) (clear-status-from-actor status %) %)
                                         actors)))))

(re-frame/reg-event-db
 :remove-actor
 (fn [db [_ ba]]
   (let [a (get-in db [:encounter :actors])
         a' (filter #(not= ba (:name %)) a)]
     (assoc-in db [:encounter :actors] a'))))
