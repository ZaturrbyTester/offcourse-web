(ns offcourse.protocol-extensions.decoratable
  (:require [cuerdas.core :as str]
            [plumbing.core :refer-macros [fnk]]
            [plumbing.graph :as graph]
            [shared.models.checkpoint.index :refer [Checkpoint]]
            [shared.models.course.index :as co :refer [Course]]
            [shared.protocols.convertible :as cv]
            [shared.protocols.decoratable :as dc :refer [Decoratable]]
            [shared.protocols.queryable :as qa]
            [shared.protocols.loggable :as log]))

<<<<<<< HEAD
(defn compute [graph graph-data]
  ((graph/compile graph) graph-data))

(def affordances-graph
  {:browsable? (fnk [] true)
   :forkable?  (fnk [current-user user-is-curator? user-is-forker?]
                    (and current-user (not user-is-forker?) (not user-is-curator?)))
   :editable?  (fnk [user-is-curator?] user-is-curator?)
   :trackable? (fnk [user-is-curator?] user-is-curator?)})

(def course-meta-graph
  {:tags             (fnk [course] (qa/get course {:tags :all}))
   :fork-curators      (fnk [course] (->> (:forks course)
                                          (map (fn [id]
                                                 (let [[org curator hash] (str/split id "::")]
                                                   curator)))
                                          (into #{})))
   :course-url       (fnk [course routes]
                          (cv/to-url course routes))
   :current-user     (fnk [appstate]
                          (when-let [user (:user appstate)] (:user-name user)))
   :course-curator   (fnk [course] (:curator course))
   :user-is-forker?  (fnk [current-user course fork-curators] (contains? fork-curators current-user))
   :user-is-curator? (fnk [current-user course-curator]
                          (and current-user (= course-curator current-user)))
   :affordances      (fnk [user-is-curator? current-user user-is-forker?]
                          (compute affordances-graph {:user-is-curator? user-is-curator?
                                                      :user-is-forker? user-is-forker?
                                                      :current-user     current-user}))})
=======

(defn compute-affordances [course appstate]
  {:browsable? true})  
>>>>>>> f23194e... add affordances to decoratable extention

(extend-protocol Decoratable
  Checkpoint
  (-decorate [{:keys [task] :as checkpoint} {:keys [selected course]} routes]
    (let [checkpoint-url  (cv/to-url checkpoint course routes)
          checkpoint-slug (str/slugify task)]
      (if (= selected checkpoint-slug)
        (with-meta checkpoint {:selected       true
                               :checkpoint-url checkpoint-url})
        (with-meta checkpoint {:checkpoint-url checkpoint-url}))))
  Course
<<<<<<< HEAD
  (-decorate [{:keys [checkpoints forks curator] :as course} appstate routes]
    (let [course-meta (compute course-meta-graph {:course   course
                                                  :routes   routes
                                                  :appstate appstate})]
      (some-> course
              (assoc :checkpoints (map #(dc/decorate %1 {:selected (:checkpoint-slug %)
                                                         :course course} routes)
                                       checkpoints))
              (with-meta course-meta)))))
=======
  (-decorate [{:keys [checkpoints curator] :as course} user-name selected routes]
    (let [tags (-> (qa/get course {:tags :all}))
          course-url (cv/to-url course routes)
          affordances (compute-affordances course nil)]
      (some-> course
              (assoc :checkpoints (map #(dc/decorate %1 (:checkpoint-slug selected) course routes) checkpoints))
              (with-meta {:tags       tags
                          :affordances affordances
                          :course-url course-url})))))
>>>>>>> f23194e... add affordances to decoratable extention
