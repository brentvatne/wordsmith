(ns wordsmith.handlers
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent defcomponentk]]
            [om-tools.dom :as dom :include-macros true]
            [wordsmith.persistence :as p]
            [cljs.core.async :refer [put! chan <!]]
            [goog.events :as events])
  (:import [goog.events EventType]
           [goog.async Throttle]))

(enable-console-print!)

;; Title field

(defn handle-title-change
  "Updates the app state with the latest text from the title input."
  [event app]
  (let [new-title (.. event -target -value)]
    (om/update! app :title new-title)))

;; New document

(defn new-document-click
  "Sends a :new command on the app channel."
  [app]
  (put! (:channel @app) [:new nil]))


;; Save button


(defn save-button-click
  "Sends a :save command on the app channel."
  [app]
  (put! (:channel @app) [:save nil]))

;; Left menu

(defn change-current
  "Sends a :change command on the app channel."
  [event app]
  (let [title (.. event -target -textContent)]
    (put! (:channel @app) [:change title])))

(defn delete-click
  "Shows a prompt asking the user for confirmation. If confirmation
  is true, sends a :remove command on the app channel."
  [title app]
  (let [response (js/confirm "Are you sure?")]
    (when response
      (put! (:channel @app) [:remove title]))))

;; Main input

(defn handle-input
  "Handles input change and updates the input app state."
  [event input]
  (om/update! input (.. event -target -value)))

;; Hot keys

(defn listen-to-hotkeys
  "Listens to KEYDOWN events using goog.events and checks for Ctrl+S or 
  Cmd+S. When identified, sends a :save command on app channel."
  [app]
  (events/listen js/document EventType/KEYDOWN
    #(when
       (and (or (.-metaKey %) (.-ctrlKey %))
            (= 83 (.-keyCode %)))
       (.preventDefault %)
       (put! (:channel @app) [:save nil]))))
