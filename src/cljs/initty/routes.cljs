(ns initty.routes
    (:require-macros [secretary.core :refer [defroute]])
    (:import goog.History)
    (:require [secretary.core :as secretary]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [re-frame.core :as re-frame]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (re-frame/dispatch [:set-active-panel :home-panel]))

  (defroute "/character/add" []
    (re-frame/dispatch [:set-active-panel :add-character-panel]))

  (defroute "/character/remove" []
    (re-frame/dispatch [:set-active-panel :remove-character-panel]))

  (defroute "/status" []
    (re-frame/dispatch [:set-active-panel :add-status-panel]))

  ;; --------------------
  (hook-browser-navigation!))
