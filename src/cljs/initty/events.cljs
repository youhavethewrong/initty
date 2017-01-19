(ns initty.events
  (:require [re-frame.core :as re-frame]
            [initty.db :as db]))

;;;;;;;;;;;;;;;;;;;
;; local helpers ;;
;;;;;;;;;;;;;;;;;;;

(defn- local-store
  [store key value]
  (.setItem store (clj->js key) (clj->js value)))

(defn- local-load
  [store key]
  (if-let [stored-val (.getItem store (clj->js key))]
    (js->clj stored-val :keywordize-keys true)))

(defn- store-actors
  [{:keys [encounter] :as db}]
  (if-let [actors (:actors encounter)]
    (local-store js/localStorage :actors actors)))

(defn- load-actors
  []
  (local-load js/localStorage :actors))

(defn- update-status
  [actor]
  (-> actor
      (update :status #(map (fn [s] (update s :duration dec)) %))
      (update :status #(filter (fn [s] (> (:duration s) 0)) %))))

(defn- clear-status-from-actor
  [status actor]
  (update actor :status (fn [statuses] (filter #(not= status (:name %)) statuses))))

;;;;;;;;;;;;
;; events ;;
;;;;;;;;;;;;

(re-frame/reg-event-db
 :initialize-db
 (fn [_ _]
   (let [d db/default-db]
     (update-in d [:encounter :actors]
      (fn [a] (reverse (sort-by (fn [x] [(:init x) (:name x)]) a)))))))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :forward-round
 (fn [db _]
   (let [actors (:actors (:encounter db))
         {me true them false} (group-by #(= "🎲" (:turn %)) actors)
         last-actor (first (sort-by :init actors))
         current-actor (first me)
         grouped (group-by #(compare (:init current-actor) (:init %)) them)
         same-init (group-by #(compare (:name current-actor) (:name %)) (get grouped 0))
         next-actor (or (first (get same-init 1))
                        (first (get grouped 1))
                        (first (get grouped -1)))
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
 :load
 (fn [db _]
   (if-let [actors (load-actors)]
     (assoc db :actors actors)
     (assoc db :actors []))))

(re-frame/reg-event-db
 :store
 (fn [db _]
   (store-actors (:actors db))))

(re-frame/reg-event-db
 :add-actor
 (fn [db [_ ba]]
   (-> db
       (update-in [:encounter :actors] #(conj % ba))
       (update-in [:encounter :actors] #(reverse (sort-by (fn [x] [(:init x) (:name x)]) %))))))

(re-frame/reg-event-db
 :save-actor
 (fn [db [_ on ba]]
   (-> db
       (update-in [:encounter :actors] #(map (fn [a] (if (= on (:name a)) ba a)) %))
       (update-in [:encounter :actors] #(reverse (sort-by (fn [x] [(:init x) (:name x)]) %))))))

(re-frame/reg-event-db
 :add-status
 (fn [db [_ {:keys [character status]}]]
   (update-in db [:encounter :actors] (fn [actors]
                                        (map #(if (= character (:name %))
                                                (update % :status conj status)
                                                %)
                                             actors)))))

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
