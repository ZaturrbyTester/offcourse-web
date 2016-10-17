(ns offcourse.views.components.card
  (:require [offcourse.views.components.button :refer [button]]
            [offcourse.views.components.card-meta :refer [card-meta]]
            [offcourse.views.components.item-list :refer [item-list]]
            [offcourse.views.components.card-social :refer [card-social]]
            [rum.core :as rum]))

(rum/defc card [{:keys [course-id goal course-slug checkpoints curator] :as course}
                respond]
 (let [{:keys [affordances course-url]} (meta course)
       {:keys [browsable? forkable? trackable? editable?]} affordances]
  [:.container
   [:.card {:on-click #()}
    [:.card--section
     [:a.card--title {:href (-> course meta :course-url)} goal]]
    [:.card--dropdown
     [:.card--section (card-meta course)]
     [:.card--section (item-list :todo checkpoints trackable? respond)]
     (when forkable?
      [:.card--section
       [:ul.card--actions
        (when browsable? (button {:button-text "Browse"} course-url))
        (when forkable? (button {:button-text "Fork"} #(respond [:fork course])))]])
     (when-let [socialable? false]
      [:.card--section (card-social)])]]]))

(rum/defc cards [{:keys [courses]} respond]
  [:.cards (map #(rum/with-key (card % respond) (:course-id %)) courses)])
