(ns offcourse.adapters.github.send
  (:require [ajax.core :refer [GET]]
            [shared.models.appstate.paths :as paths]
            [cljs.core.async :as async :refer [<! chan]]
            cljsjs.js-yaml
            [clojure.walk :as walk]
            [shared.models.event.index :as event]
            [shared.protocols.loggable :as log]
            [shared.protocols.specced :as sp])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [com.rpl.specter.macros :refer [select-first]]))

(defn respond [{:keys [name]} res]
  (event/create [name :found res]))

(defn yaml-file? [path]
  (re-find #"\.yaml$" path))

(defn tree-url [{:keys [base-url repository]}]
  (let [{:keys [organization name sha]} repository]
    (str base-url "/repos/" organization "/" name "/git/trees/" sha)))

(defn content-url [{:keys [base-url repository]} path]
  (let [{:keys [organization name sha]} repository]
    (str base-url "/repos/" organization "/" name "/contents/" path)))

(defn handle-content [res]
  (->> res
       walk/keywordize-keys
       :content
       (.atob js/window)
       (.safeLoad js/jsyaml)
       js->clj
       walk/keywordize-keys))

(defn handle-tree [tree]
  (->> tree
       walk/keywordize-keys
       :tree
       (map :path)
       (filter yaml-file?)))

(defn handle-response [c res]
  (async/put! c res)
  (async/close! c))

(defn fetch [url]
  (let [c (chan)]
    (GET url
        {:format :json
         :headers {:Authorization "token efd7997fbcfc0aa8f93a92badf1c9d5f6f7007c4"}
         :handler #(handle-response c %)})
    c))


(defn fetch-all [{:keys [name repository base-url] :as adapter} [_ query :as event]]
  (let [c (chan)
        auth-token ""
        tree-url (tree-url adapter)]
    (go
      (let [tree (<! (fetch tree-url))
            paths (handle-tree tree)
            content-urls (map #(content-url adapter %) paths)
            query-chans (async/merge (map fetch content-urls))
            raw-content (<! (async/into [] query-chans))
            content (map handle-content raw-content)
            complete (map #(assoc %1
                                  :curator (or (:curator %1) (:curator repository))
                                  :organization (or (:organization %1) (:organization repository))) content)]
        (async/put! c complete)))
      c))

(defmulti send (fn [_ [_ query]] (sp/resolve query)))

(defmethod send :course [adapter event]
  (let [c (chan)]
    (go
      (let [courses   (<! (fetch-all adapter event))
            [_ query] event
            course    (select-first (paths/course query) courses)]
        (async/put! c (respond adapter course))))
      c))


(defmethod send :collection [adapter event]
  (let [c (chan)]
    (go
      (let [courses (<! (fetch-all adapter event))]
        (async/put! c (respond adapter courses))))
    c))
