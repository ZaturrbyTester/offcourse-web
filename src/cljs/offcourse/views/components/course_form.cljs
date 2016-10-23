(ns offcourse.views.components.course-form
  (:require [rum.core :as rum]
            [offcourse.views.components.button :refer [button]]
            [offcourse.views.components.item-list :refer [edit-list]]
            [offcourse.views.components.dropdown :refer [dropdown]]
            [shared.protocols.specced :as sp]
            [shared.protocols.loggable :as log]
            [shared.models.course.index :as co]))

(defn update-prop [prop-name event atom]
  (let [prop-value (.. event -target -value)]
    (swap! atom #(assoc % prop-name prop-value))))

(defn update-checkpoint [course-atom course checkpoint]
  (swap! course-atom #(co/update-checkpoint course checkpoint)))

(defn remove-checkpoint [course-atom course checkpoint]
  (swap! course-atom #(co/remove-checkpoint course checkpoint)))

(defn create-checkpoint [course-atom course]
  (swap! course-atom #(co/create-checkpoint course)))

(rum/defcs course-form < (rum/local {} ::course) [state {:keys [course]} respond]
  (let [course-atom (::course state)
        course      (merge course @course-atom)
        errors (:cljs.spec/problems (sp/errors course))
        error-paths (into #{} (map #(-> % :path first) errors))
        old-course  course
        dirty?      (not= course old-course)
        valid?      (sp/valid? course)]
    [:.card {:data-card-type :wide}
      [:.card--section
        [:h1.card--title {:data-title-indent true} "Curate a course"]]
      [:.card--section
        [:.card--row {:data-row-spaced true
                      :data-row-padded :small}
          [:p.card--subtitle {:data-subtitle-indent true}
                             "Title of the course"]
          [:p.card--link {:data-link-type :em} "How to write a catchy title"]
          (dropdown {:title "Titles"
                     :text "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                     :shown false})]
        (when (contains? error-paths :goal) [:.card--error "This field is not correct yet"])
        [:input.form--field {:key       "title"
                             :type      :text
                             :placeholder "Course Title"
                             :value     (:goal course)
                             :on-change #(update-prop :goal % course-atom)}]]
      [:.card--section
        [:.card--row {:data-row-spaced true
                      :data-row-padded :small}
          [:p.card--subtitle {:data-subtitle-indent true}
                             "Add or edit resources"]
          [:p.card--link {:data-link-type :em} "What are resources?"]
          (dropdown {:title "Titles"
                     :text "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                     :shown false})]
        (edit-list (:checkpoints course)
                   #(update-checkpoint course-atom course %1)
                   #(remove-checkpoint course-atom course %1))
        (when true
          (button {:button-text "Add Checkpoint"}
                  #(create-checkpoint course-atom course)))]
     [:.card--section
       [:.card--row {:data-row-spaced true}
         [:.card--row
           (when true ;(and valid? dirty?)
             (button {:button-text "Save Course"}
                     #(respond [:update course])))
           (when true
             (button {:button-text "Publish Course"
                      :button-color "blue"}
                     #(respond [:update course])))]
         [(when true
           (button {:button-text "Cancel"
                    :button-color "red"}
                   #(respond [:switch-to :view-mode])))]]]]))
