(ns example.core
  (:require [reagent.core :as reagent]
            [bonsai.core :as bonsai]
            [clojure.string :as str]))

;; Define your state atom, in this case, a Reagent atom.
(defonce state! (reagent/atom {:text "This reverses text!"}))

(defn text-changed
  "Action that updates the text within the state."
  [state text]
  (-> state
      (assoc :text text)))

(defn reverser
  "Component that renders the current text reversed. Dispatches changes to text-changed."
  [state!]
  (let [{:keys [text]} @state!]
    [:div
     [:input {:type "text"
              :value text
              :on-change #(bonsai/step! state! text-changed (-> % .-target .-value))}]
     [:p (str/reverse text)]]))

(defn root
  "Component at the top of our app, notice how we always pass state! through."
  [state!]
  [reverser state!])

(defn init!
  "Starts up the application and mounts it into the DOM."
  []
  (reagent/render [root state!] (.getElementById js/document "app")))
