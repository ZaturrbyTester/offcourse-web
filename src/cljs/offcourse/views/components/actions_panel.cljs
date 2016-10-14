(ns offcourse.views.components.actions-panel
  (:require [rum.core :as rum]
            [cuerdas.core :as str]
            [shared.protocols.loggable :as log]))

(defn button-title [string]
  (-> string
      name
      str/humanize
      str/titleize))

(defn url-button [action-name url]
  [:li.actions-panel--link
   {:key [action-name]}
   [:a {:href url} (button-title action-name)]])

(defn handler-button [action-name respond]
  [:li.actions-panel--link {:key [action-name]
                            :on-click #(respond [action-name])}
   (button-title action-name)])

(rum/defc actions-panel [{:keys [user-name auth-token] :as data}
                         respond]
  [:ul.actions-panel
   (when user-name [:li.actions-panel--link
                     {:key "create"}
                     "Create Course"])
   (when user-name (url-button user-name "/"))
   (if auth-token
     (handler-button :sign-out respond)
     (handler-button :sign-in respond))])
