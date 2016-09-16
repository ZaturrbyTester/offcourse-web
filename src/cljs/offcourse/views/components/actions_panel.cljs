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
  [:li.button
   {:key [action-name]
    :data-button-type "textbar"}
   [:a {:href url} (button-title action-name)]])

(defn handler-button [action-name respond]
  [:li.button {:data-button-type "textbar"
               :key [action-name]
               :on-click #(respond [action-name])}
   (button-title action-name)])

(rum/defc actions-panel [{:keys [user-name auth-token]        :as data}
                         respond]
  [:ul.actions-panel
   (when user-name [:li.button  
                    {:key "create"
                     :data-button-type "textbar"}
                    "Create Course"])
   (when user-name [:li.button  
                    {:key "profile"
                     :data-button-type "textbar"}
                    "My Profile"])
   (when user-name (url-button user-name "/"))
   (when-not user-name [:li.button  
                        {:key "profile"
                         :data-button-type "textbar"}
                        "Sign Up"])
   (if auth-token
     (handler-button :sign-out respond)
     (handler-button :sign-in respond))])
