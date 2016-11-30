(ns initty.db)

;; Silly die unicode
;; 🎲

(def default-db
  {:encounter {:rounds 1
               :actors [{:name "Yamamoto" :turn "" :init 21 :status [{:name "Blind" :duration 2}]}
                        {:name "Fayruz" :turn "🎲" :init 27 :status []}
                        {:name "Augustus" :turn "" :init 12 :status []}
                        {:name "Kortahl" :turn "" :init 13 :status []}
                        {:name "Galion" :turn "" :init 16 :status []}
                        {:name "Lochlin" :turn "" :init 22 :status []}
                        {:name "Goblin 1" :turn "" :init 19 :status []}]}})

(comment
(defn sort-actors
  [actors]
  (reverse (sort-by #([(:init %) (:name %)]) actors)))

(defn save-actors
  [actors]
  (.setItem js/localStorage "actors" actors))
  )
