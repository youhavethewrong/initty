(ns initty.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [reagent.core :as reagent]))

;; home

(defn home-title []
  [re-com/title
   :label "initty"
   :level :level1])

(defn- display-actor
  [{:keys [name init status turn]}]
  (let [a [:li (str name " (" init ") " turn)]]
    (if (and (comp not nil? status)
             (not-empty status))
      (conj a [:ul (map #(vec [:li (str (:name %) " for " (:duration %) " rounds") ]) status)])
      a)))

(defn- display-actors
  [actors]
  (map display-actor actors))

(defn link-to-add-page []
  [re-com/hyperlink-href
   :label "Add character"
   :href "#/add"])

(defn home-body
  []
  (let [encounter (re-frame/subscribe [:encounter])]
    [re-com/h-box
     :justify :center
     :children [[re-com/v-box
                 :min-width "14em"
                 :children [[:ul [:h3 (str "Round " (:rounds @encounter))]
                             (display-actors (:actors @encounter))]]]
                [re-com/v-box
                 :gap "2em"
                 :children [[re-com/button
                             :label "Next"
                             :on-click #(re-frame/dispatch [:forward-round])]
                            [link-to-add-page]]]]]))

(defn home-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[home-title]
              [home-body]]])

;; add

(defn add-title []
  [re-com/title
   :label "Add character to the encounter"
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "back to Home"
   :href "#/"])

(defn add-body
  []
  (let [encounter (re-frame/subscribe [:encounter])
        base-actor (reagent/atom {:name ""
                                  :turn ""
                                  :init 0
                                  :status []})
        name-val ""
        init-val ""]
    [:div {:id "content"}
     [re-com/h-box
      :gap "2em"
      :children [[re-com/v-box
                  :children [[re-com/title
                              :label "Name"]
                             [re-com/input-text
                              :model name-val
                              :on-change #(swap! base-actor (fn [ba n] (assoc ba :name n)) %)
                              :placeholder "Melkor"]
                             [re-com/title
                              :label "Initiative"]
                             [re-com/input-text
                              :model init-val
                              :on-change #(swap! base-actor (fn [ba n] (assoc ba :init (js/parseInt n))) %)
                              :validation-regex #"^(\d{0,2})$"
                              :placeholder "20"]]]
                 [re-com/v-box
                  :gap "2em"
                  :children [[re-com/button
                              :label "Add actor"
                              :on-click #(re-frame/dispatch [:add-actor @base-actor])]
                             [link-to-home-page]]]]]]))

(defn add-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[add-title]
              [add-body]]])

;; main

(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :add-panel [] [add-panel])
(defmethod panels :default [] [:div])

(defn show-panel
  [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [re-com/v-box
       :height "100%"
       :children [[panels @active-panel]]])))
