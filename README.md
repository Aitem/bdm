# BiDirectionalMapper

[![Clojars Project](https://img.shields.io/clojars/v/bdm.svg)](https://clojars.org/bdm)
![ci](https://github.com/aitem/bdm/workflows/CI/badge.svg)


BDM is super simple, fully declarative clj/cljc bi directional mapper that allow convert your data from `A` to `B` and back.

## Usage

``` clj
(ns user
  (:require [bdm.core as bdm]))

(def person
  {:name "John"
   :lastname "Smith"
   :contacts [{:type  "email"
               :value "john@smith.com"}
              {:type  "phone"
			   :use   "home"
               :value "12223334455"}
			  {:type  "phone"
			   :use   "work"
			   :value "15556667788"}]})

(def mapping
  [[[:name]]
   [[:lastname]]
   [[:emails] [:contacts {:get [:= :type "email"]} :#]]
   [[:phones] [:contacts {:get [:= :type "phone"]} :#]]])

(bdm/import person mapping)

;; Will return
;; {:name "John",
;;  :lastname "Smith",
;;  :emails [{:type "email", :value "john@smith.com"}],
;;  :phones [{:type "phone", :use "home", :value "12223334455"}
;;           {:type "phone", :use "vork", :value "15556667788"}]}


;; We can do import/export roundtrip

(= person (bdm/export (bdm/import person mapping) mapping)) ;; => true

```

## Concepts

### Mapping

`Mapping` is a array of `paths` pairs. First `path` of pair used as getter rule and second as setter.

``` clj
(def mapping
  [[[:login]  [:username]]])

(def user
  {:login "super-user"})

(bdm/import user mapping)
;; will return
;; {:username "super-user"}

(def imported-user
  {:username "super-user"})

(bdm/export imported-user mapping)
;; will return
;; {:login "super-user"}
```

In this sample we take value from user by path `[:login]` and set this value into resutl by path `[:username]`



### Path
...

### Short alias
...

### Getters and setters
...

### Predicates
...

### Submapping
...



## Develop

```
# run repl
$ make repl

# run test
$ make test
```



MIT License Copyright (c) 2020 Marat Surmashev
