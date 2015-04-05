(ns refactor-nrepl.ns.resolve-missing
  "Resolve a missing symbol on the classpath."
  (:require [cider.nrepl.middleware.info :refer [info-clj]]
            [refactor-nrepl.ns.slam.hound.regrow :as slamhound]))

(defn- candidates [sym]
  (seq (concat (slamhound/candidates :import sym [] {})
               (slamhound/candidates :refer sym [] {}))))

(defn- get-type [sym]
  (let [info (info-clj 'user sym)]
    (if-let [clazz (:class info)]
      (cond
        ((set (:interfaces info)) 'clojure.lang.IType) :type
        ((set (:interfaces info)) 'clojure.lang.IRecord) :type
        :else :class)                   ; interfaces are included here
      :ns)))

(defn- collate-type-info
  [candidates]
  (map #(list % (get-type %)) candidates))

(defn resolve-missing [{sym :symbol}]
  (when-not (and sym (string? sym) (seq sym))
    (throw (IllegalArgumentException.
            (str "Invalid input to resolve missing: '" sym "'"))))
  (some->> sym symbol candidates collate-type-info pr-str))
