(ns offcourse.appstate.react
  (:require [offcourse.appstate.check :as ck]
            [shared.protocols.actionable :as ac]
            [shared.protocols.loggable :as log]
            [shared.protocols.queryable :as qa]
            [shared.protocols.eventful :as ef]
            [shared.protocols.specced :as sp]
            [shared.models.query.index :as query]))

(defmulti react (fn [_ event] (sp/resolve event)))

(defmethod react [:granted :data] [{:keys [state] :as as} [_ payload]]
  (let [proposal (ac/perform @state [:add payload])
        user     (:user proposal)
        token    (:auth-token user)]
    (reset! state proposal)
    (when token
      (ef/respond as [:not-found (query/create user)]))))

(defmethod react [:revoked :data] [{:keys [state] :as as} [_ payload]]
  (let [proposal (ac/perform @state [:add payload])]
    (when (sp/valid? proposal)
      (reset! state proposal)
      (ef/respond as [:requested [:go :home]]))))

(defmethod react [:requested :action] [as [_ action]]
  (ac/perform as action))

(defmethod react [:found :data] [as [_ payload]]
  (ac/perform as [:add payload]))

;; should be [:not-found :query]
(defmethod react [:not-found :data] [{:keys [state] :as as} [_ query]]
  (when (= :user (sp/resolve query))
    (ef/respond as [:requested [:create :new-user]])))

#_(defmethod react [:not-found :data] [{:keys [state] :as as} [_ payload]]
  (log/error payload "missing-data")
  as
  #_(when-not (-> @state :user :user-name)
      (rd/redirect as :signup)))
