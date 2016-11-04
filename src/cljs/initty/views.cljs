(ns initty.views
    (:require [re-frame.core :as re-frame]
              [re-com.core :as re-com]))

;; home

(defn home-title []
  [re-com/title
   :label (str "Welcome to initty")
   :level :level1])

(defn home-body
  []
  (let [encounter (re-frame/subscribe [:encounter])]
    [:div {:id "content"}
     [:p {:class "text-left text-info bg-primary"}
      "text in content"]
     [re-com/button
      :label "Advance"
      :on-click #(re-frame/dispatch [:forward-round])]
     [:ul (str "Round " (:rounds @encounter))
      (map #(vec [:li (str (:name %) " - " (:init %))]) (:actors @encounter))]]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   ;; :style {:background-color "white"}
   :label "go to About Page"
   :href "#/about"])

(defn home-panel []
  [re-com/v-box
   ;; :style {:background-color "yellow"}
   :gap "1em"
   :children [[home-title]
              [home-body]
              [link-to-about-page]]])


;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title] [link-to-home-page]]])


;; main

(defmulti panels identity)
(defmethod panels :home-panel [] [home-panel])
(defmethod panels :about-panel [] [about-panel])
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
