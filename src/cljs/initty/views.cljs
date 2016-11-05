(ns initty.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [reagent.core :as reagent]))


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

;; home
(defn home-title []
  [re-com/title
   :label "initty"
   :level :level1])

(defn link-to-add-character-page []
  [re-com/hyperlink-href
   :label "Add character"
   :href "#/character/add"])

(defn link-to-remove-character-page []
  [re-com/hyperlink-href
   :label "Remove character"
   :href "#/character/remove"])

(defn link-to-add-status-page []
  [re-com/hyperlink-href
   :label "Add status"
   :href "#/status/add"])

(defn link-to-remove-status-page []
  [re-com/hyperlink-href
   :label "Remove status"
   :href "#/status/remove"])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "back to Home"
   :href "#/"])

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
                 :children [[re-com/button
                             :label "Next"
                             :on-click #(re-frame/dispatch [:forward-round])]
                            [link-to-add-character-page]
                            [link-to-remove-character-page]
                            [link-to-add-status-page]
                            [link-to-remove-status-page]]]]]))

(defn home-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[home-title]
              [home-body]]])

;; add character
(defn add-character-body
  []
  (let [base-actor (reagent/atom {:name ""
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
                              :on-change #(swap! base-actor (fn [ba n] (assoc ba :name n)) %)]
                             [re-com/title
                              :label "Initiative"]
                             [re-com/input-text
                              :model init-val
                              :on-change #(swap! base-actor (fn [ba n] (assoc ba :init (js/parseInt n))) %)
                              :validation-regex #"^(\d{0,2})$"]]]
                 [re-com/v-box
                  :gap "2em"
                  :children [[re-com/button
                              :label "Add actor"
                              :on-click #(re-frame/dispatch [:add-actor @base-actor])]
                             [link-to-home-page]]]]]]))

(defn add-character-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[re-com/title :label "Add character to the encounter" :level :level1]
              [add-character-body]]])

;; add status
(defn add-status-body
  []
  (let [encounter (re-frame/subscribe [:encounter])
        base-status (reagent/atom {:name "" :duration 0})
        character-val (reagent/atom nil)
        name-val ""
        duration-val ""]
    [re-com/h-box
     :gap "2em"
     :children [[re-com/v-box
                 :children [[re-com/title
                             :label "Character"]
                            [re-com/single-dropdown
                             :width "14em"
                             :choices (:actors @encounter)
                             :id-fn :name
                             :label-fn :name
                             :model character-val
                             :on-change #(reset! character-val %)]
                            [re-com/title
                             :label "Name"]
                            [re-com/input-text
                             :model name-val
                             :on-change #(swap! base-status (fn [ba n] (assoc ba :name n)) %)]
                            [re-com/title
                             :label "Rounds"]
                            [re-com/input-text
                             :model duration-val
                             :on-change #(swap! base-status (fn [ba n] (assoc ba :duration (js/parseInt n))) %)
                             :validation-regex #"^(\d{0,3})$"]]]
                [re-com/v-box
                 :gap "2em"
                 :children [[re-com/button
                             :label "Add status"
                             :on-click #(re-frame/dispatch [:add-status {:character @character-val
                                                                         :status @base-status}])]
                            [link-to-home-page]]]]]))

(defn add-status-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[re-com/title
               :label "Add status effect to a character"
               :level :level1]
              [add-status-body]]])

;; remove actor
(defn remove-character-body
  []
  (let [encounter (re-frame/subscribe [:encounter])
        character-val (reagent/atom nil)]
    [re-com/h-box
     :gap "2em"
     :children [[re-com/v-box
                 :children [[re-com/title
                             :label "Character"]
                            [re-com/single-dropdown
                             :width "14em"
                             :choices (:actors @encounter)
                             :id-fn :name
                             :label-fn :name
                             :model character-val
                             :on-change #(reset! character-val %)]]]
                [re-com/v-box
                 :gap "2em"
                 :children [[re-com/button
                             :label "Remove character"
                             :on-click #(re-frame/dispatch [:remove-actor @character-val])]
                            [link-to-home-page]]]]]))

(defn remove-character-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[re-com/title
               :label "Select a character to remove"
               :level :level1]
              [remove-character-body]]])

;; remove status
(defn remove-status-body
  []
  (let [encounter (re-frame/subscribe [:encounter])
        character-val (reagent/atom nil)
        char-statuses (reagent/atom [])
        status-val (reagent/atom nil)]
    [re-com/h-box
     :gap "2em"
     :children [[re-com/v-box
                 :children [[re-com/title
                             :label "Character"]
                            [re-com/single-dropdown
                             :width "14em"
                             :choices (:actors @encounter)
                             :id-fn :name
                             :label-fn :name
                             :model character-val
                             :on-change (fn [name]
                                          (let [_ (reset! character-val name)
                                                a (filter #(= @character-val (:name %)) (:actors @encounter))
                                                statuses (or (:status (first a)) [])]
                                            (reset! char-statuses statuses)))]
                            [re-com/title
                             :label "Status"]
                            [re-com/single-dropdown
                             :width "14em"
                             :choices char-statuses
                             :id-fn :name
                             :label-fn :name
                             :model status-val
                             :on-change #(reset! status-val %)]]]
                [re-com/v-box
                 :gap "2em"
                 :children [[re-com/button
                             :label "Remove status"
                             :on-click #(re-frame/dispatch [:remove-status {:character @character-val
                                                                            :status @status-val}])]
                            [link-to-home-page]]]]]))

(defn remove-status-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[re-com/title
               :label "Select status to remove from a character"
               :level :level1]
              [remove-status-body]]])

;; main

(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :add-character-panel [] [add-character-panel])
(defmethod panels :add-status-panel [] [add-status-panel])
(defmethod panels :remove-character-panel [] [remove-character-panel])
(defmethod panels :remove-status-panel [] [remove-status-panel])
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
