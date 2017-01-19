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

(defn- make-link
  [n v]
  [re-com/hyperlink-href
   :label (str v " " n)
   :href (str "#/" n "/" v)])

(defn- create-panel
  [l f]
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[re-com/title
               :label l
               :level :level1]
              [f]]])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "back to Home"
   :href "#/"])

(defn home-title []
  [re-com/title
   :label "initty"
   :level :level1])

(defn home-body
  []
  (let [encounter (re-frame/subscribe [:encounter])]
    [re-com/h-box
     :justify :center
     :gap "2em"
     :children [[re-com/v-box
                 :min-width "14em"
                 :children [[:ul [:h2 (str "Round " (:rounds @encounter))]
                             (display-actors (:actors @encounter))]]]
                [re-com/v-box
                 :children [[re-com/title :label "Turn" :level :level2]
                            [re-com/button
                             :label "Advance"
                             :on-click #(re-frame/dispatch [:forward-round])]
                            [re-com/title :label "Modify" :level :level2]
                            (make-link "character" "add")
                            (make-link "character" "remove")
                            (make-link "status" "add")
                            (make-link "status" "remove")
                            [re-com/button
                             :label "Load"
                             :on-click #(re-frame/dispatch [:load])]
                            [re-com/button
                             :label "Store"
                             :on-click #(re-frame/dispatch [:store])]]]]]))

(defn home-panel []
  [re-com/v-box
   :gap "2em"
   :align :center
   :children [[home-title]
              [home-body]]])

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

(defn edit-character-body
  []
  (let [encounter (re-frame/subscribe [:encounter])
        old-name (reagent/atom "")
        character-val (reagent/atom nil)
        name-val ""
        init-val ""]
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
                            [re-com/v-box
                             :children [[re-com/title
                                         :label "Name"]
                                        [re-com/input-text
                                         :model name-val
                                         :on-change (fn [new-n]
                                                      (swap! character-val (fn [ba n] (reset! old-name (:name ba)) (assoc ba :name n)) new-n))]
                                        [re-com/title
                                         :label "Initiative"]
                                        [re-com/input-text
                                         :model init-val
                                         :on-change #(swap! character-val (fn [ba n] (assoc ba :init (js/parseInt n))) %)
                                         :validation-regex #"^(\d{0,2})$"]]]]]
                [re-com/v-box
                 :gap "2em"
                 :children [[re-com/button
                             :label "Save character"
                             :on-click #(re-frame/dispatch [:save-actor @old-name @character-val])]
                            [link-to-home-page]]]]]))

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

(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :add-character-panel [] (create-panel "Add a character to the encounter" add-character-body))
(defmethod panels :add-status-panel [] (create-panel "Add a status effect to a character" add-status-body))
(defmethod panels :edit-character-panel [] (create-panel "Select a character to edit" edit-character-body))
(defmethod panels :remove-character-panel [] (create-panel "Select a character to remove" remove-character-body))
(defmethod panels :remove-status-panel [] (create-panel "Select a status to remove from a character" remove-status-body))
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
