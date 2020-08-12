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

`Mapping` is a array of `paths` pairs. First `path` of pair used as setter rule and second as a getter rule.

``` clj
(def mapping
  [[[:username] [:login]]])

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

In this sample we take value from `user` by path `[:login]` and set this value into result with path `[:username]`.

Path can be deep

``` clj
(def window-mapping
  [[[:hash] [:window :location :hash]]
   [[:path] [:window :location :pathname]]])

(def window
  {:window {:location {:hash     "#about"
                       :pathname "/index.html"}}})

(bdm/import window window-mapping)
;; Will return
;; {:hash "#about"
;;  :path "/index.html"}

(def location
 {:hash "#contact"
  :path "/spa.html"})

(bdm/export location window-mapping)
;; Will return
;; {:window {:location {:hash     "#contact"
;;                      :pathname "/spa.html"}}})
```



### Path
...

### Short alias
...

### Predicates and arrays
...

### Setters
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

## Extra

For more details and usage details see tests.



MIT License Copyright (c) 2020 Marat Surmashev
