(ns example.core
  (:require [reagent.core :as reagent]
            [bonsai.core :as bonsai]
            [clojure.string :as str]))

(defonce state! (reagent/atom {:text "This reverses text!"}))

(defn text-changed [state text]
  (-> state
      (assoc :text text)))

(defn reverser [state!]
  (let [{:keys [text]} @state!]
    [:div
     [:input {:type "text"
              :value text
              :on-change #(bonsai/next! state! text-changed (-> % .-target .-value))}]
     [:p (str/reverse text)]]))

(defn root [state!]
  [reverser state!])

(defn init! []
  (reagent/render [root state!] (.getElementById js/document "app")))
